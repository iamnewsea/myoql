package nbcp.tool

import nbcp.db.Cn
import java.lang.reflect.Field

object CodeGeneratorHelper {
    /**
     * 获取表的中文注释及Cn注解
     */
    fun getEntityComment(entType: Class<*>): String {
        var cn = entType.getAnnotation(Cn::class.java)?.value ?: "";
        if (cn.isNullOrEmpty()) return "";

        return """/**
* ${cn}
*/
@Cn("${cn}")
"""
    }

    fun getFieldComment(field: Field): String {
        var cn = field.getAnnotation(Cn::class.java)?.value ?: "";
        if (cn.isNullOrEmpty()) return "";
        return """/**
* ${cn}
*/
@Cn("${cn}")
"""
    }
}