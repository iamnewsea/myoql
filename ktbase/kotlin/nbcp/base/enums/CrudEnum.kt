package nbcp.base.enums

enum class CrudEnum(val remark: String) {
    create("创建"),
    read("读取"),
    update("更新"),
    delete("删除"),
}


enum class DbOperationTypeEnum(val remark: String) {
    CREATE("创建"),
    LIST("列表"),
    DETAIL("详情"),
    UPDATE("更新"),
    DELETE("删除");
}