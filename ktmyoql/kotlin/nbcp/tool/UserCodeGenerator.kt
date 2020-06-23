package nbcp.tool

import nbcp.comm.*
import nbcp.db.BaseMetaData
import nbcp.db.IdName
import nbcp.db.IdUrl
import nbcp.db.mongo.MongoBaseMetaCollection
import nbcp.utils.MyUtil
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl
import java.lang.RuntimeException
import java.lang.reflect.Field
import java.time.LocalDateTime

object UserCodeGenerator {
    fun genMvcAuto(group: String, entity: BaseMetaData): String {
        var stream = this::class.java.getResourceAsStream("/kotlin_mvc_template_auto.txt")
        var text = stream.readBytes().toString(utf8)

        return gen(group, entity, text);
    }

    fun genMvc(group: String, entity: BaseMetaData): String {
        var stream = this::class.java.getResourceAsStream("/kotlin_mvc_template.txt")
        var text = stream.readBytes().toString(utf8)

        return gen(group, entity, text);
    }

    fun genVueList(group: String, entity: BaseMetaData): String {
        var stream = this::class.java.getResourceAsStream("/vue_list_template.txt")
        var text = stream.readBytes().toString(utf8)

        return gen(group, entity, text);
    }

    fun genVueCard(group: String, entity: BaseMetaData): String {
        var stream = this::class.java.getResourceAsStream("/vue_card_template.txt")
        var text = stream.readBytes().toString(utf8)

        return gen(group, entity, text);
    }

    fun genVueRef(group: String, entity: BaseMetaData): String {
        var stream = this::class.java.getResourceAsStream("/vue_ref_template.txt")
        var text = stream.readBytes().toString(utf8)

        return gen(group, entity, text);
    }

    private fun gen(group: String, entity: BaseMetaData, text: String): String {
        var text = text;
        var entityFields = (entity as MongoBaseMetaCollection<*>).entityClass.AllFields;
        //先处理${for:fields}


        var status_enum_class = ""
        var statusField = entityFields.firstOrNull { it.name == "status" }
        if (statusField != null) {
            status_enum_class = statusField.type.simpleName;
        }

        text = procIf(entityFields, text);

        text = procFor(entityFields, text);

        var entity_class = MyUtil.getBigCamelCase(entity.tableName);
        var entity_url = MyUtil.getHyphen(entity.tableName);

        if (entity_url.startsWith(group + "-")) {
            entity_url = entity_url.substring((group + "-").length);
        }


        return text.formatWithJson(
                StringMap(
                        "group" to group,
                        "entity" to entity.tableName,
                        "title" to entity_class,
                        "entity_class" to entity_class,
                        "entity_url" to entity_url,
                        "now" to LocalDateTime.now().toString(),
                        "status_enum_class" to status_enum_class
                ), "\${}")

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

                return@map procForIf(entityFields, it, forExp).formatWithJson(StringMap(
                        "name" to it.name,
                        "remark" to it.name,
                        "type" to it.type.simpleName,
                        "isSimpleType" to it.type.IsSimpleType().toString().toLowerCase()
                ), "\${}")
            }.filter { it.HasValue }.joinToString("\n");

            text = beforeExp + t2 + afterExp;
        }

        return text;
    }

    private fun procForIf(entityFields: List<Field>, field: Field, content: String): String {
        var times = 0;
        var text = content;
        while (true) {
            times++
            if (times > 999) {
                throw RuntimeException("查找fif执行了999次！")
            }
            var start = Regex("\\$\\{fif:([^}]+)}").find(text, 0);
            if (start == null) break;

            if (start.groups.size != 2) {
                break;
            }

            var ifKey = start.groups[1]!!.value;

            var begin_tag_start_index = start.groups[0]!!.range.start;
            var begin_tag_end_index = start.groups[0]!!.range.last;

            var end_tag_start_index = text.indexOf("\${endif}", begin_tag_end_index);
            if (end_tag_start_index < 0) break;

            //-----
            var beforeExp = text.substring(0, begin_tag_start_index);
            var afterExp = text.substring(end_tag_start_index + "\${endif}".length);

            var ifExp = "";
            if (decideIfExp(entityFields, field, ifKey)) {
                var t = text.substring(begin_tag_end_index + 1, end_tag_start_index);

                t = removeNewLine(t);

                ifExp = t;
            }

            text = beforeExp + ifExp + afterExp;
        }

        return text;
    }

    private fun procIf(entityFields: List<Field>, content: String): String {
        var times = 0;
        var text = content;
        while (true) {
            times++;
            if (times > 999) {
                throw RuntimeException("查找if执行了999次！")
            }

            var start = Regex("\\$\\{if:([^}]+)}").find(text, 0);
            if (start == null) break;

            if (start.groups.size != 2) {
                break;
            }

            var ifKey = start.groups[1]!!.value;

            var begin_tag_start_index = start.groups[0]!!.range.start;
            var begin_tag_end_index = start.groups[0]!!.range.last;

            var end_tag_start_index = text.indexOf("\${endif}", begin_tag_end_index);
            if (end_tag_start_index < 0) break;

            //-----
            var beforeExp = text.substring(0, begin_tag_start_index);
            var afterExp = text.substring(end_tag_start_index + "\${endif}".length);

            var ifExp = "";
            if (decideIfExp(entityFields, null, ifKey)) {
                var t = text.substring(begin_tag_end_index + 1, end_tag_start_index);

                t = removeNewLine(t);

                ifExp = t;
            }

            text = beforeExp + ifExp + afterExp;
        }

        return text;
    }

    private fun decideIfExp(entityFields: List<Field>, field: Field?, ifKey: String): Boolean {
        if (ifKey.startsWith("@")) {
            return field!!.type.simpleName VbSame ifKey.substring(1);
        } else if (ifKey.startsWith("#")) {
            var name = ifKey.substring(1);
            if (name == "enum") {
                return field!!.type.isEnum;
            } else if (name == "normal") {
                //非：enum ,IdUrl,IdName,boolean,Int
                return !field!!.type.isEnum &&
                        !field.type.IsBooleanType() &&
                        (field.type != Int::class.java) &&
                        (field.type != IdUrl::class.java) &&
                        (field.type != IdName::class.java)

            }
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