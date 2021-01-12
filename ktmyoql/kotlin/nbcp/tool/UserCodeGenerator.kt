package nbcp.tool

import nbcp.comm.*
import nbcp.db.BaseMetaData
import nbcp.db.IdUrl
import nbcp.db.mongo.MongoBaseMetaCollection
import nbcp.utils.MyUtil
import java.lang.RuntimeException
import java.lang.reflect.Field
import java.lang.reflect.ParameterizedType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*

object UserCodeGenerator {
    /**
     * 生成基础的CRUD接口
     */
    fun genMvcCrud(group: String, entity: BaseMetaData): String {
        var stream = this::class.java.getResourceAsStream("/kotlin_mvc_template_crud.txt")
        var text = stream.readBytes().toString(utf8)

        return gen(group, entity, text);
    }

    /**
     * 生成空的Mvc类
     */
    fun genMvc(group: String, entity: BaseMetaData): String {
        var stream = this::class.java.getResourceAsStream("/kotlin_mvc_template.txt")
        var text = stream.readBytes().toString(utf8)

        return gen(group, entity, text);
    }

    /**
     * 生成Vue列表页面
     */
    fun genVueList(group: String, entity: BaseMetaData): String {
        var stream = this::class.java.getResourceAsStream("/vue_list_template.txt")
        var text = stream.readBytes().toString(utf8)

        return gen(group, entity, text);
    }

    /**
     * 生成Vue卡片页面
     */
    fun genVueCard(group: String, entity: BaseMetaData): String {
        var stream = this::class.java.getResourceAsStream("/vue_card_template.txt")
        var text = stream.readBytes().toString(utf8)

        return gen(group, entity, text);
    }

    /**
     * 生成Vue引用
     */
    fun genVueRef(group: String, entity: BaseMetaData): String {
        var stream = this::class.java.getResourceAsStream("/vue_ref_template.txt")
        var text = stream.readBytes().toString(utf8)

        return gen(group, entity, text);
    }

    private fun gen(group: String, metaEntity: BaseMetaData, text: String): String {
        var text = text;
        var entityClass = (metaEntity as MongoBaseMetaCollection<*>).entityClass
        var entityFields = entityClass.AllFields.MoveToFirst { it.name == "name" }.MoveToFirst { it.name == "id" }
        //先处理${for:fields}


        var status_enum_class = ""
        var statusField = entityFields.firstOrNull { it.name == "status" }
        if (statusField != null) {
            status_enum_class = statusField.type.simpleName;
        }

        text = procIf("if", entityFields, null, text);

        text = procFor(entityFields, text);

        var title = CodeGeneratorHelper.getEntityCommentValue(entityClass).AsString(metaEntity.tableName);

//        var entity_url = MyUtil.getKebabCase(metaEntity.tableName);
//
//        if (entity_url.startsWith(group + "-")) {
//            entity_url = entity_url.substring((group + "-").length);
//        }

        var map: StringKeyMap<((String) -> String)> = StringKeyMap()
        map.put("-", { MyUtil.getKebabCase(it) })
        map.put("W", { MyUtil.getBigCamelCase(it) })
        map.put("w", { MyUtil.getSmallCamelCase(it) })
        map.put("U", { it.toUpperCase() })
        map.put("u", { it.toLowerCase() })

        var map2: StringKeyMap<((String).(String) -> String)> = StringKeyMap()
        map2.put("trim", {
            var v = this;
            if (v.startsWith(it)) {
                v = v.substring(it.length)
            }
            if (v.endsWith(it)) {
                v = v.substring(0, v.length - it.length)
            }
            return@put v;
        })

        var mapDefine = StringMap(
            "group" to group,
            "entity" to entityClass.simpleName,
            "entityField" to MyUtil.getSmallCamelCase(entityClass.simpleName),
            "title" to title,
            "now" to LocalDateTime.now().toString(),
            "status_enum_class" to status_enum_class
        )
        return text.formatWithJson(
            mapDefine, "\${}",
            { key ->
                key.split("|").first()
            },
            { fullKey, value ->
                var sects = fullKey.split("|")
                if (sects.size <= 1 || value == null) {
                    return@formatWithJson value;
                }

                var key = sects.first();
                var result = value!!;
                sects.Skip(1).forEach { funcString ->
                    //如：   substring:2,3
                    var sects2 = funcString.split(":")
                    var funcName = sects2.first();
                    var params = listOf<String>()
                    if (sects2.size > 2) {
                        throw RuntimeException("表达式中多个冒号非法：${funcString}")
                    } else if (sects2.size == 2) {
                        // 如：   substring:2,3 中的 2,2 参数部分
                        params = sects2[1].split(",")
                    }


                    if (params.size == 1) {
                        var funcBody = map2.get(funcName)
                        if (funcBody == null) {
                            throw RuntimeException("找不到 ${funcName}")
                        }
                        var param = params[0];
                        var paramValue = param;
                        if  (param.startsWith("'") && param.endsWith("'")) {
                            paramValue = param.substring(1,param.length-1);
                        }
                        else if( param.IsNumberic()){
                            paramValue = param;
                        }
                        else {
                            paramValue = mapDefine.get(param).AsString()
                        }
                        result = funcBody.invoke(result, paramValue)
                    } else if (params.size == 0) {
                        var funcBody = map.get(funcName)
                        if (funcBody == null) {
                            throw RuntimeException("找不到 ${funcName}")
                        }
                        result = funcBody.invoke(result)
                    }

                    return@forEach
                }


                return@formatWithJson result
            }
        )

    }

    private fun procFor(entityFields: List<Field>, content: String): String {
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

                var forExp2 = procIf("fif", entityFields, it, forExp);
                return@map forExp2.formatWithJson(
                    StringMap(
                        "name" to it.name,
                        "remark" to CodeGeneratorHelper.getFieldCommentValue(it).AsString(it.name),
                        "type" to it.type.simpleName,
                        "NAME" to MyUtil.getBigCamelCase(it.name),
                        "name-" to MyUtil.getKebabCase(it.name),
                        "isSimpleType" to it.type.IsSimpleType().toString().toLowerCase()
                    ), "\${}"
                )
            }.filter { it.HasValue }.joinToString(line_break);

            text = beforeExp + t2 + afterExp;
        }

        return text;
    }

    private fun procIf(startTag: String, entityFields: List<Field>, field: Field?, content: String): String {
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

    private fun decideIfExp(entityFields: List<Field>, field: Field?, ifKey: String): Boolean {
        if (ifKey.startsWith("@")) {
            return field!!.type.simpleName VbSame ifKey.substring(1);
        } else if (ifKey.startsWith("#")) {
            var name = ifKey.substring(1);
            if (name VbSame "Res") {
                return field!!.type.isEnum ||
//                        field.type == LocalDateTime::class.java ||
//                        field.type == LocalDate::class.java ||
//                        field.type == LocalTime::class.java ||
//                        field.type == Date::class.java ||
                        field.type == Boolean::class.java
            }

            if (name VbSame "enum1") {
                return field!!.type.isEnum;
            }
            if (name VbSame "enumList") {
                return CodeGeneratorHelper.IsListEnum(field!!);
            }

            if (name VbSame "enum") {
                return field!!.type.isEnum || CodeGeneratorHelper.IsListEnum(field!!);
            }

            if (name VbSame "IdUrlList") {
                return CodeGeneratorHelper.IsListType<IdUrl>(field!!);
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