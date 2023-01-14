package nbcp.base.enums

import nbcp.base.db.annotation.Cn
import nbcp.base.utils.MyUtil
import nbcp.base.utils.StringUtil

/**
 * 名称映射类型
 */
enum class NameMappingTypeEnum {
    @Cn("原始值")
    ORIGIN,

    @Cn("小驼峰")
    SMALL_CAMEL,

    @Cn("大驼峰")
    BIG_CAMEL,

    @Cn("连字符")
    KEBAB,

    @Cn("下划线")
    UNDER_LINE;

    fun getResult(input: String): String {
        if (this == SMALL_CAMEL) return StringUtil.getSmallCamelCase(input);
        if (this == BIG_CAMEL) return StringUtil.getBigCamelCase(input);
        if (this == KEBAB) return StringUtil.getKebabCase(input);
        if (this == UNDER_LINE) return StringUtil.getUnderlineCase(input);

        return input;
    }
}