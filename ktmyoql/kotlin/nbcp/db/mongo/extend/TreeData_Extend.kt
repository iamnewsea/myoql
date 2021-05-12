@file:JvmName("MyOqlMongo")
@file:JvmMultifileClass

package nbcp.db.mongo

import nbcp.comm.ApiResult
import nbcp.comm.BatchReader
import nbcp.comm.HasValue
import nbcp.db.ITreeData
import nbcp.db.db
import nbcp.db.mongo.entity.BasicUserLoginInfo
import nbcp.db.mongo.table.MongoBaseGroup
import nbcp.utils.RecursionReturnEnum
import nbcp.utils.RecursionUtil
import org.bson.types.ObjectId
import java.lang.RuntimeException


data class TreeResultData(var root: ITreeData<*>, var parent: ITreeData<*>?, var current: ITreeData<*>)


/**
 * 返回 root , current
 */
private fun <M : MongoBaseMetaCollection<T>, T> M.findTreeById(id: String): TreeResultData?
        where T : IMongoDocument,
              T : ITreeData<*> {
    var reader = BatchReader.init(5, { skip, take ->
        this.query().limit(skip, take).toList()
    });
    var ret: TreeResultData? = null;
    while (reader.hasNext()) {
        var current = reader.next();

        RecursionUtil.execute<ITreeData<*>>(
            mutableListOf(current),
            { it.getChildren() as MutableList<ITreeData<*>> },
            { item, container, index ->
                if (item.id == id) {
                    ret = TreeResultData(current, container, item);
                    return@execute RecursionReturnEnum.Abord;
                }
                return@execute RecursionReturnEnum.Go
            });
    }
    return ret;
}

/**
 * 返回影响行数
 */
fun <M : MongoBaseMetaCollection<T>, T> M.deleteTreeNodeById(id: String): ApiResult<String>
        where T : IMongoDocument,
              T : ITreeData<*> {
    var result = this.findTreeById(id)
    if (result == null) return ApiResult("找不到数据")
    var root = result.root;
    var parent = result.parent

    if (root.id == id) {
        if (this.deleteById(id).exec() == 0) {
            return ApiResult("删除失败")
        }
        return ApiResult();
    }

    (parent!!.getChildren() as MutableList<ITreeData<*>>).removeIf { it.id == id }

    this.updateWithEntity(root as T).execUpdate();
    return ApiResult();
}


/**
 * 树型数据保存，可能改变所在的 parent
 *
 * 先找 entity的父节点，判断 pid 是否相同。
 * @return 父实体id
 */
fun <M : MongoBaseMetaCollection<T>, T> M.saveTreeToParent(
    pid: String,
    saveEntity: T
): ApiResult<String>
        where T : IMongoDocument,
              T : ITreeData<*> {

    if (saveEntity.id.HasValue) {
        this.deleteTreeNodeById(saveEntity.id).apply {
            if (this.msg.HasValue) return this;
        }
    }

    return this.addTreeToTree(pid, saveEntity);
}


private fun <M : MongoBaseMetaCollection<T>, T> M.addTreeToTree(
    pid: String,
    saveEntity: T
): ApiResult<String>
        where T : IMongoDocument,
              T : ITreeData<*> {

    if (saveEntity.id.isEmpty()) {
        saveEntity.id = ObjectId().toString();
    }

    //添加新实体
    if (pid.isEmpty()) {
        if (this.doInsert(saveEntity).isEmpty()) {
            return ApiResult("添加失败")
        }
        return ApiResult.of(saveEntity.id);
    }


    //添加到已存在的节点
    var targetTreeResult = this.findTreeById(pid);
    if (targetTreeResult == null) {
        throw RuntimeException("找不到数据")
    }

    var root = targetTreeResult.root;
    var targetNode = targetTreeResult.current;

    (targetNode.getChildren() as MutableList<ITreeData<*>>).add(saveEntity);

    this.updateWithEntity(root as T).execUpdate();
    return ApiResult.of(root.id)
}


