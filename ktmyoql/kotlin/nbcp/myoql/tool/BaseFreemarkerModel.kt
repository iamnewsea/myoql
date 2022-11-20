package nbcp.myoql.tool

import nbcp.base.extend.AsString
import nbcp.base.utils.SpringUtil.Companion.context
import nbcp.myoql.tool.freemarker.*
import java.time.LocalDateTime

open class BaseFreemarkerModel {
    val user = context.environment.getProperty("user.name")
    val now = LocalDateTime.now().AsString()
    val hasValue = FreemarkerHasValue()
    val kb = FreemarkerKebabCase()
    val bc = FreemarkerBigCamelCase()
    val sc = FreemarkerSmallCamelCase()
    fun getIsObject() = FreemarkerIsObject()
    fun getIsRes() = FreemarkerIsType()
    val allFields = FreemarkerAllField()
    val fieldIsEnumList = FreemarkerFieldIsEnumList()
    val fieldCn = FreemarkerFieldCn()
    val fieldValue = FreemarkerFieldValue()
    val fieldIsList = FreemarkerIsList()
    val fieldListType = FreemarkerFieldListType()
}