package nbcp.tool

import nbcp.comm.*
import nbcp.db.BaseMetaData
import nbcp.utils.MyUtil
import java.time.LocalDateTime

object UserCodeGenerator {
    fun genMvc(group: String, entity: BaseMetaData): String {
        var stream = this::class.java.getResourceAsStream("/kotlin_mvc_template.txt")
        var text = stream.readBytes().toString(utf8)

        return gen(group, entity, text);
    }

    fun genVueList(group:String,entity:BaseMetaData):String{
        var stream = this::class.java.getResourceAsStream("/vue_list_template.txt")
        var text = stream.readBytes().toString(utf8)

        return gen(group, entity, text);
    }
    fun genVueCard(group:String,entity:BaseMetaData):String{
        var stream = this::class.java.getResourceAsStream("/vue_card_template.txt")
        var text = stream.readBytes().toString(utf8)

        return gen(group, entity, text);
    }
    fun genVueRef(group:String,entity:BaseMetaData):String{
        var stream = this::class.java.getResourceAsStream("/vue_ref_template.txt")
        var text = stream.readBytes().toString(utf8)

        return gen(group, entity, text);
    }

    private fun gen(group: String, entity: BaseMetaData, text: String): String {
        var text = text;
        var entityFields = entity::class.java.AllFields;
        //先处理${for:fields}
        var startIndex = 0;

        while (true) {
            var start = Regex("\\$\\{if:(\\w+)}").find(text  , startIndex);
            if( start == null) break;

            if( start.groups.size != 2){
                break;
            }

            var tagName = start.groups[1]!!.value;

            var start_index = start.groups[1]!!.range.start;
            var endIndex = start.groups[1]!!.range.last;

            var end_index = text.indexOf("\${endif}", endIndex);
            if (end_index < 0) break;

            //-----
            var p1 = text.Slice(0, start_index);
            var t = text.Slice(start_index + "\${for:fields}".length, end_index);
            var p2 = text.Slice(end_index + "\${endfor}".length);

            var t2 = entityFields.map {
                t.formatWithJson(StringMap(
                        "name" to it.name,
                        "remark" to it.name,
                        "type" to it.type.name,
                        "isSimpleType" to it.type.IsSimpleType().toString().toLowerCase()
                ), "\${}")
            }.joinToString("\n");

            text = p1 + t2 + p2;

            startIndex = end_index;
        }


        startIndex = 0;

        while (true) {
            var start_index = text.indexOf("\${for:fields}", startIndex);
            if (start_index < 0) break;
            var end_index = text.indexOf("\${endfor}", start_index);
            if (end_index < 0) break;

            var p1 = text.Slice(0, start_index);
            var t = text.Slice(start_index + "\${for:fields}".length, end_index);
            var p2 = text.Slice(end_index + "\${endfor}".length);

            var t2 = entityFields.map {
                t.formatWithJson(StringMap(
                        "name" to it.name,
                        "remark" to it.name,
                        "type" to it.type.name,
                        "isSimpleType" to it.type.IsSimpleType().toString().toLowerCase()
                ), "\${}")
            }.joinToString("\n");

            text = p1 + t2 + p2;

            startIndex = end_index;
        }

        var entity_class = MyUtil.getBigCamelCase(entity.tableName);
        var entity_url = MyUtil.getHyphen(entity.tableName);

        return text.formatWithJson(
                StringMap(
                        "group" to group,
                        "entity" to entity.tableName,
                        "title" to entity_class,
                        "entity_class" to entity_class,
                        "entity_url" to entity_url,
                        "now" to LocalDateTime.now().toString()
                ), "\${}")

    }
}