package nbcp.tool

import nbcp.comm.*
import nbcp.db.BaseMetaData
import nbcp.db.IdUrl
import nbcp.db.es.EsBaseMetaEntity
import nbcp.db.mongo.MongoBaseMetaCollection
import nbcp.db.sql.SqlBaseMetaTable
import nbcp.utils.MyUtil
import java.lang.RuntimeException
import java.lang.reflect.Field
import java.time.LocalDateTime
import java.util.*
import kotlin.reflect.jvm.kotlinProperty

object UserCodeGenerator {
    /**
     * 生成基础的CRUD接口
     */
    fun genMongoMvcCrud(group: String, entity: BaseMetaData): String {
        var stream = this::class.java.getResourceAsStream("/kotlin_mvc_mongo_template_crud.txt")
        var text = stream.readBytes().toString(utf8)

        return gen(group, entity, text);
    }

    fun genMySqlMvcCrud(group: String, entity: BaseMetaData): String {
        var stream = this::class.java.getResourceAsStream("/kotlin_mvc_mysql_template_crud.txt")
        var text = stream.readBytes().toString(utf8)

        return gen(group, entity, text);
    }

    fun genEsMvcCrud(group: String, entity: BaseMetaData): String {
        var stream = this::class.java.getResourceAsStream("/kotlin_mvc_es_template_crud.txt")
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
        var id_name = ""
        lateinit var entityClass: Class<*>

        if (metaEntity is MongoBaseMetaCollection<*>) {
            entityClass = metaEntity.entityClass
            id_name = "id"
        } else if (metaEntity is SqlBaseMetaTable<*>) {
            entityClass = metaEntity.tableClass
            id_name = metaEntity.getAutoIncrementKey().AsString(metaEntity.getUks().first { it.size == 1 }[0])
        } else if (metaEntity is EsBaseMetaEntity<*>) {
            entityClass = metaEntity.entityClass
            id_name = "id"
        }

        var entityFields = entityClass.AllFields.MoveToFirst { it.name == "name" }.MoveToFirst { it.name == "id" }
        //先处理${for:fields}


        var status_enum_class = ""
        var statusField = entityFields.firstOrNull { it.name == "status" }
        if (statusField != null) {
            status_enum_class = statusField.type.kotlinTypeName;
        }

        text = procIf("if", entityFields, null, text);

        text = procFor(entityFields, text, id_name);

        var title = CodeGeneratorHelper.getEntityCommentValue(entityClass).AsString(metaEntity.tableName);

//        var entity_url = MyUtil.getKebabCase(metaEntity.tableName);
//
//        if (entity_url.startsWith(group + "-")) {
//            entity_url = entity_url.substring((group + "-").length);
//        }

        var url = "/${MyUtil.getKebabCase(group)}/${MyUtil.getKebabCase(entityClass.simpleName)}"
        var mapDefine = StringMap(
            "url" to url,
            "group" to group,
            "entity" to entityClass.simpleName,
            "entityField" to MyUtil.getSmallCamelCase(entityClass.simpleName),
            "title" to title,
            "now" to LocalDateTime.now().AsString(),
            "status_enum_class" to status_enum_class,
            "id_name" to id_name
        )
        return MyUtil.formatTemplateJson(text, mapDefine, { key, value, func, funcParam ->
            if (key == "id_name" && func == "type") {
                return@formatTemplateJson entityFields.first { it.name == id_name }.type.kotlinTypeName
            }
            return@formatTemplateJson null
        })
    }

    private fun procFor(entityFields: List<Field>, content: String, id_name: String): String {
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
                return@map MyUtil.formatTemplateJson(
                    forExp2,
                    StringMap(
                        "name" to it.name,
                        "remark" to CodeGeneratorHelper.getFieldCommentValue(it).AsString(it.name),
                        "type" to it.type.simpleName,
                        "isSimpleType" to it.type.IsSimpleType().toString().toLowerCase(),
                        "id_name" to id_name
                    ),
                    { key, value, func, funcParam ->
                        if (key == "id_name" && func == "type") {
                            return@formatTemplateJson entityFields.first { it.name == id_name }.type.kotlinTypeName
                        }
                        return@formatTemplateJson null
                    }, "\${}"
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