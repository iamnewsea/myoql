package nbcp.tool

import nbcp.comm.*
import nbcp.db.IdUrl
import nbcp.utils.MyUtil
import java.lang.RuntimeException
import java.lang.reflect.Field

object MyTemplateProc {

    /**
    //        var text = CodeGeneratorHelper.procIf(text, "if", entityFields, null);
    //
    //        text = CodeGeneratorHelper.procFor(text, entityFields, idKey);

    var title = CodeGeneratorHelper.getEntityCommentValue(entityClass).AsString(tableName);

    var url = "/${MyUtil.getKebabCase(group)}/${MyUtil.getKebabCase(entityClass.simpleName)}"
    var mapDefine = StringMap(
    "url" to url,
    "group" to group,
    "entity" to entityClass.simpleName,
    "entityField" to MyUtil.getSmallCamelCase(entityClass.simpleName),
    "title" to title,
    "now" to LocalDateTime.now().AsString(),
    "status_enum_class" to status_enum_class,
    "idKey" to idKey
    )
    return MyUtil.formatTemplateJson(text, mapDefine, { key, value, func, funcParam ->
    if (key == "idKey" && func == "type") {
    return@formatTemplateJson entityFields.first { it.name == idKey }.type.kotlinTypeName
    }
    return@formatTemplateJson null
    })
     */
    fun proc(){

    }

    fun procFor(content: String, entityFields: List<Field>, idKey: String): String {
        var text = content;
        var times = 0;
        while (true) {
            times++;
            if (times > 999) {
                throw RuntimeException("查找for执行了999次！")
            }
            var start_index = text.indexOf("\${for:fields}", 0);
            if (start_index < 0) break;
            var end_index = text.indexOf("\${endfor}", start_index);
            if (end_index < 0) break;

            var beforeExp = text.substring(0, start_index);
            var forExp = text.substring(start_index + "\${for:fields}".length, end_index);
            var afterExp = text.substring(end_index + "\${endfor}".length);

            forExp = removeNewLine(forExp);

            var t2 = entityFields.map {
                var forExp2 = procIf(forExp, "fif", entityFields, it);
                return@map MyUtil.formatTemplateJson(
                    forExp2,
                    StringMap(
                        "name" to it.name,
                        "remark" to CodeGeneratorHelper.getFieldCommentValue(it).AsString(it.name),
                        "type" to it.type.simpleName,
                        "isSimpleType" to it.type.IsSimpleType().toString().toLowerCase(),
                        "idKey" to idKey
                    ),
                    { key, value, func, funcParam ->
                        if (key == "idKey" && func == "type") {
                            return@formatTemplateJson entityFields.first { it.name == idKey }.type.kotlinTypeName
                        }
                        return@formatTemplateJson null
                    }, "\${}"
                )
            }.filter { it.HasValue }.joinToString(const.line_break);

            text = beforeExp + t2 + afterExp;
        }

        return text;
    }

    fun procIf(content: String, startTag: String, entityFields: List<Field>, field: Field?): String {
        var times = 0;
        var text = content;
        while (true) {
            times++;
            if (times > 999) {
                throw RuntimeException("查找if执行了999次！")
            }

            var start = Regex("\\$\\{" + startTag + ":([^}]+)}").find(text, 0);
            if (start == null) break;

            if (start.groups.size != 2) {
                break;
            }

//            var ifKey = start.groups[1]!!.value;

            var begin_tag_start_index = start.groups[0]!!.range.start;
            var begin_tag_end_index = start.groups[0]!!.range.last;

            var end_tag_start_index = text.indexOf("\${endif}", begin_tag_end_index);
            if (end_tag_start_index < 0) break;

            //-----
            var beforeExp = text.substring(0, begin_tag_start_index);
            var afterExp = text.substring(end_tag_start_index + "\${endif}".length);

            var ifAllExp = text.substring(begin_tag_start_index, end_tag_start_index + "\${endif}".length);

            var ifExp = getIfExpression(ifAllExp, startTag, entityFields, field)


            text = beforeExp + ifExp + afterExp;
        }

        return text;
    }

    fun getIfExpression(context: String, startTag: String, entityFields: List<Field>, field: Field?): String {
        var text = context;
        var start = Regex("\\$\\{" + startTag + ":([^}]+)}").find(text, 0);
        if (start == null) return "";

        if (start.groups.size != 2) {
            return "";
        }

        var ifKey = start.groups[1]!!.value;
        var mapIf = StringMap();
        var valueElse = "";

        var prevIndex = start.range.last + 1;
        var startIndex = 0;
        while (true) {
            var elseIfSect = Regex("\\$\\{((elseif:([^}]+))|(else)|(endif))}").find(text, startIndex);
            if (elseIfSect == null) {
                break;
            }

            var beforeExp = text.substring(prevIndex, elseIfSect.range.first)

            beforeExp = removeNewLine(beforeExp);

            if (ifKey.HasValue) {
                mapIf.put(ifKey, beforeExp);

                prevIndex = elseIfSect.range.last + 1;
            } else {
                valueElse = beforeExp;
            }

            if (elseIfSect.groups[3] != null) {
                ifKey = elseIfSect.groups[3]!!.value;
            } else {
                ifKey = "";
            }

            startIndex = elseIfSect.range.last;
        }

        var retKey = mapIf.keys.firstOrNull { ifKey -> decideIfExp(entityFields, field, ifKey) };
        if (retKey != null) {
            return mapIf.getStringValue(retKey)!!;
        }

        return valueElse;
    }

    /**
     * 处理 If
     * ifKey:
     * @ 开头表示字段类型;
     *      == #Res 表示是否是可资源化;
     *      #enum1表示数据库字段是否是单个枚举,
     *      #enumList表示数据库字段是否是枚举列表.
     *      #enum表示是否是枚举(单枚举或多枚举)
     *      #IdUrlList 表示是否是文件
     * 字符串:表示是否是该字段,比如: if:name 表示 field.name =?= name
     */
    private fun decideIfExp(entityFields: List<Field>, field: Field?, ifKey: String): Boolean {
        if (ifKey.startsWith("@")) {
            return field!!.type.simpleName VbSame ifKey.substring(1);
        } else if (ifKey.startsWith("#")) {
            var name = ifKey.substring(1);
            if (name VbSame "Res") {
                return field!!.type.isEnum ||
                        field.type == Boolean::class.java
            }

            if (name VbSame "enum1") {
                return field!!.type.isEnum;
            }
            if (name VbSame "enumList") {
                return CodeGeneratorHelper.IsListEnum(field!!);
            }

            if (name VbSame "enum") {
                return field!!.type.isEnum || CodeGeneratorHelper.IsListEnum(field);
            }

            if (name VbSame "IdUrlList") {
                return CodeGeneratorHelper.IsListType(field!!,"IdUrl");
            }
        }

        if (field != null) {
            return field.name == ifKey;
        }
        return entityFields.any { it.name == ifKey }
    }


    private fun removeNewLine(value: String): String {
        var t = value;
        //去除首尾的回车。
        if (t.startsWith("\r\n")) {
            t = t.substring(2);
        } else if (t.startsWith("\n")) {
            t = t.substring(1);
        }

        if (t.endsWith("\r\n")) {
            t = t.Slice(0, -2);
        } else if (t.endsWith("\n")) {
            t = t.Slice(0, -1);
        }
        return t;
    }
}