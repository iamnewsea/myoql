//@file:JvmName("MyOqlMongo")
//@file:JvmMultifileClass
//
//package nbcp.db.mongo
//
//import nbcp.comm.ApiResult
//import java.io.Serializable
//import nbcp.comm.BatchReader
//import nbcp.comm.HasValue
//import nbcp.myoql.db.ITreeData
//import nbcp.utils.RecursionReturnEnum
//import nbcp.base.utils.RecursionUtil
//import org.bson.types.ObjectId
//import java.lang.RuntimeException
//
//
///**
// * wbs,从根开始，到当前节点
// */
//class TreeResultData() : LinkedHashSet<ITreeData<*>>() {
//    constructor(value: Set<ITreeData<*>>) : this() {
//        this.addAll(value);
//    }
//
//    val root: ITreeData<*>
//        get() = this.first();
//
//    val parent: ITreeData<*>?
//        get() =
//            if (this.size < 2) null else this.elementAt(this.size - 2)
//
//    val current: ITreeData<*>
//        get() = this.last()
//}
//
//
///**
// * 在数据库中遍历查找树节点。返回所在树中的 根，父，本身。
// */
//fun <M : MongoBaseMetaCollection<T>, T:Any> M.findTreeById(id: String): TreeResultData
//    where T : ITreeData<*> {
//    return this.findTree { it.id == id };
//}
//
///**
// * 在数据库中遍历查找树节点。返回所在树中的 根，父，本身。
// */
//fun <M : MongoBaseMetaCollection<T>, T:Any> M.findTree(callback: ((ITreeData<*>) -> Boolean)): TreeResultData
//    where T : ITreeData<*> {
//    var reader = BatchReader.init(5, { skip, take ->
//        this.query().limit(skip, take).toList()
//    });
//    var ret: TreeResultData = TreeResultData();
//    while (reader.hasNext()) {
//        var current = reader.next();
//
//        RecursionUtil.execute<ITreeData<*>>(
//            mutableListOf(current),
//            { it.children() as MutableList<ITreeData<*>> },
//            { wbs, _ ->
//                var item = wbs.last()
//                if (callback(item)) {
//                    ret = TreeResultData(wbs);
//                    return@execute RecursionReturnEnum.Abord;
//                }
//                return@execute RecursionReturnEnum.Go
//            });
//    }
//    return ret;
//}
//
///**
// * 删除树节点，可能删除的是根节点，也可能删除的是子级节点
// */
//fun <M : MongoBaseMetaCollection<T>, T:Any> M.deleteTreeNodeById(id: String): ApiResult<String>
//    where    T : ITreeData<*> {
//    var result = this.findTreeById(id)
////    if (result == null) return ApiResult.error("找不到数据")
//    var root = result.root;
//    var parent = result.parent
//
//    if (root.id == id) {
//        if (this.deleteById(id).exec() == 0) {
//            return ApiResult.error("删除失败")
//        }
//        return ApiResult();
//    }
//
//    (parent!!.children() as MutableList<ITreeData<*>>).removeIf { it.id == id }
//
//    this.updateWithEntity(root as T).execUpdate();
//    return ApiResult();
//}
//
//
///**
// * 树型数据保存，可能改变所在的 parent
// *
// * 先找 entity的父节点，判断 pid 是否相同。
// * @return 父实体id
// */
//fun <M : MongoBaseMetaCollection<T>, T:Any> M.saveTreeToParent(
//    pid: String,
//    saveEntity: T
//): ApiResult<String>
//    where T : Serializable,
//          T : ITreeData<*> {
//
//    if (saveEntity.id.HasValue) {
//        this.deleteTreeNodeById(saveEntity.id).apply {
//            if (this.msg.HasValue) return this;
//        }
//    }
//
//    return this.addTreeToTree(pid, saveEntity);
//}
//
//
//private fun <M : MongoBaseMetaCollection<T>, T> M.addTreeToTree(
//    pid: String,
//    saveEntity: T
//): ApiResult<String>
//    where T : Serializable,
//          T : ITreeData<*> {
//
//    if (saveEntity.id.isEmpty()) {
//        saveEntity.id = ObjectId().toString();
//    }
//
//    //添加新实体
//    if (pid.isEmpty()) {
//        if (this.doInsert(saveEntity).isEmpty()) {
//            return ApiResult.error("添加失败")
//        }
//        return ApiResult.of(saveEntity.id);
//    }
//
//
//    //添加到已存在的节点
//    var targetTreeResult = this.findTreeById(pid);
////    if (targetTreeResult == null) {
////        throw RuntimeException("找不到数据")
////    }
//
//    var root = targetTreeResult.root;
//    var targetNode = targetTreeResult.current;
//
//    (targetNode.children() as MutableList<ITreeData<*>>).add(saveEntity);
//
//    this.updateWithEntity(root as T).execUpdate();
//    return ApiResult.of(root.id)
//}
//
//
