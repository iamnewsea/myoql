package nbcp.db.mongo

import nbcp.db.IdUrl
import nbcp.comm.*
import nbcp.db.db
import org.bson.Document
import java.io.Serializable

enum class MongoImageActionEnum {
    remove,
    add,
    swap
}

/**
 * 删除数组中的一项。
 * @param fieldName , 数组列名
 * @param id 实体Id
 * @param imageId 数组中的某一项。
 */
private fun <M : MongoBaseMetaCollection<E>, E : Serializable> M.remove(fieldName: String, id: String, imageId: String): Int {
    return this.updateById(id)
            .pull({ MongoColumnName(fieldName) }, "_id" match imageId)
            .exec()
}

/**
 * 向数组中添加一项。
 * @param fieldName 数组列名
 * @param id 实体Id
 * @param image 添加的图片
 */
private fun <M : MongoBaseMetaCollection<E>, E : Serializable> M.add(fieldName: String, id: String, image: IdUrl): Int {
    return this.updateById(id)
            .push { MongoColumnName(fieldName) to image }
            .exec()
}

fun <M : MongoBaseMetaCollection<E>, E : Serializable> M.swap(fieldName: (M) -> MongoColumnName, id: String, index1: Int, index2: Int): JsonResult {
    return this.swap(fieldName(this).toString(), id, index1, index2);
}

/**
 * 交换数组中的两项
 * @param fieldName 数组项
 * @param id 实体Id
 * @param index1 交换项索引
 * @param index2 交换项索引
 */
fun <M : MongoBaseMetaCollection<E>, E : Serializable> M.swap(fieldName: String, id: String, index1: Int, index2: Int): JsonResult {
    if (index1 == index2) {
        return JsonResult.error("非法")
    }
    var info = this.queryById(id)
            .select(fieldName)
            .toEntity(Document::class.java);

    if (info == null) {
        return JsonResult.error("找不到数据")
    }

    var imagesData = info.get(fieldName);

    if (imagesData == null || (imagesData is Collection<*> == false)) {
        return JsonResult.error("找不到数据信息")
    }

    var images = imagesData as MutableList<*>;


    if (index1 >= images.size && index2 >= images.size) {
        return JsonResult.error("索引超出范围")
    }

    images.Swap(index1, index2);

    MongoUpdateClip(this).where("id" match id)
            .set(fieldName, images)
            .exec()

    if (db.affectRowCount == 0) {
        return JsonResult.error("交换图片位置失败")
    }

    return JsonResult()
}

/**
 * 图片操作，添加，删除，交换
 */
fun <M : MongoBaseMetaCollection<E>, E : Serializable> M.imageChange(
        action: MongoImageActionEnum, fieldName: String, id: String, image: IdUrl, index1: Int, index2: Int
): JsonResult {

    if (action == MongoImageActionEnum.add) {
        return this.add(fieldName, id, image).run {
            if (this == 0) return@run JsonResult.error("添加图片失败")
            return@run JsonResult()
        };
    } else if (action == MongoImageActionEnum.remove) {
        return this.remove(fieldName, id, image.id).run {
            if (this == 0) {
                return@run JsonResult.error("删除图片失败")
            }
            return@run JsonResult();
        }
    } else if (action == MongoImageActionEnum.swap) {
        return this.swap(fieldName, id, index1, index2);
    }
    return JsonResult()
}


fun <M : MongoBaseMetaCollection<E>, E : Serializable> M.imageSet(
        fieldName: String, id: String, image: IdUrl
): JsonResult {

    var ret = JsonResult();
    this.updateById(id)
            .set(fieldName, image)
            .exec()

    if (db.affectRowCount == 0) {
        ret.msg = "保存图片信息出错";
    }
    return ret;
}