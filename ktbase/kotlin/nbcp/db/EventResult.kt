package nbcp.db


/**
 * 一般业务含义的Crud
 */
enum class CrudEnum(val remark: String) {
    create("创建"),
    read("读取"),
    update("更新"),
    delete("删除"),
}

//enum class HttpCrudEnum(val remark: String) {
//    post("创建"),
//    get("读取"),
//    put("更新"),
//    delete("删除"),
//}
//
//enum class SqlCrudEnum(val remark: String) {
//    insert("创建"),
//    select("读取"),
//    update("更新"),
//    delete("删除"),
//}
//
//enum class MongoCrudEnum(val remark: String) {
//    insert("创建"),
//    find("读取"),
//    update("更新"),
//    remove("删除"),
//}