package nbcp.base.enums


/**
 * Api 参数类型
 */
enum class ApiParameterTypeEnum(var remark: String) {
    Boolean("布尔"),
    Int("整数"),
    Float("小数"),
    Number("数字"),
    Text("文本"),
    TextArea("多行文本"),
    Radio("单选"),
    Check("多选"),

    Url("网址"),
    File("文件"),
    Color("颜色"),
    Array("数组"),
    Object("对象"),
    Date("日期"),
    Time("时间"),
    DateTime("日期时间")
}