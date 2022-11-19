package nbcp.base.enums

enum class DbOperationTypeEnum(val remark: String) {
    CREATE("创建"),
    LIST("列表"),
    DETAIL("详情"),
    UPDATE("更新"),
    DELETE("删除");
}