package nbcp.base.enums

import nbcp.db.Cn
import nbcp.utils.MyUtil

/**
 * 名称映射类型
 */
enum class NameMappingTypeEnum {
    @Cn("原始值")
    Origin,
    @Cn("小驼峰")
    SmallCamel,
    @Cn("大驼峰")
    BigCamel,
    @Cn("连字符")
    Kebab,
    @Cn("下划线")
    Underline;

    fun getResult(input:String):String{
        if( this == SmallCamel) return MyUtil.getSmallCamelCase(input);
        if( this == BigCamel) return MyUtil.getBigCamelCase(input);
        if( this == Kebab) return MyUtil.getKebabCase(input);
        if( this == Underline) return MyUtil.getUnderlineCase(input);

        return input;
    }
}