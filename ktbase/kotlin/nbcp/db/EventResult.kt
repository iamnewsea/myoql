package nbcp.db

/**
 * 更新或删除事件执行的结果
 */
data class EventResult @JvmOverloads constructor(
    /**
     * 执行结果 ，返回 false 将停止后面的执行。
     */
    var result: Boolean = true,
    /**
     * 传递给后续操作的额外数据。
     */
    var extData: Any? = null
)

/**
 * 一般业务含义的Crud
 */
enum class CrudEnum(val remark: String) {
    create("创建"),
    read("读取"),
    update("更新"),
    delete("删除"),
}

enum class HttpCrudEnum(val remark: String) {
    post("创建"),
    get("读取"),
    put("更新"),
    delete("删除"),
}

enum class SqlCrudEnum(val remark: String) {
    insert("创建"),
    select("读取"),
    update("更新"),
    delete("删除"),
}

enum class MongoCrudEnum(val remark: String) {
    insert("创建"),
    find("读取"),
    update("更新"),
    remove("删除"),
}