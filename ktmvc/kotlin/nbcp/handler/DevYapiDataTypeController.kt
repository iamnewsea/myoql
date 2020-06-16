package nbcp.handler

import nbcp.comm.*
import nbcp.db.db
import nbcp.db.mongo.*
import nbcp.utils.RecursionUtil
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@OpenAction
@RestController
@JsonpMapping("/dev/yapi")
class DevYapiDataTypeController {
    /**
     * 修正 yapi 的数据类型,在 title 字段设置如下格式： :IdName,IdUrl,会对其下属性添加 id,name,url 字段。
     * @param dbTypeJson , 形如： {"IdName": {id: {type:"string",description:"id"} ,name:{} }
     */
    @PostMapping("/user-types")
    fun dbTypes(@Require connString: String, @Require dbTypeJson: JsonMap): ListResult<String> {
        var ret = mutableListOf<String>()
        using(db.mongo.getMongoTemplateByUri(connString)!!) {
            var query = MongoBaseQueryClip("interface")
            query.whereData.add(MongoColumnName("req_body_other") match_like "title\":\":")
            var list = query.toList(JsonMap::class.java);

            list.forEach {
                var id = it.getIntValue("id");
                ret.add(it.getStringValue("path") ?: it.getStringValue("title") ?: it.toString())

                var req_body_other = it.getStringValue("req_body_other") ?: "{}"
                var json = req_body_other.FromJson<Map<String, Any?>>();
                if (json != null) {
                    proc(json, dbTypeJson);

                    req_body_other = json.ToJson()
                }

                var res_body = it.getStringValue("res_body") ?: "{}";

                var json2 = res_body.FromJson<Map<String, Any?>>()
                if (json2 != null) {
                    proc(json2, dbTypeJson);

                    res_body = json2.ToJson()
                }


                var update = MongoBaseUpdateClip("interface")
                update.whereData.add(MongoColumnName("_id") match id)
                update.setValue("req_body_other", req_body_other)
                update.setValue("res_body", res_body)
                update.exec();
            }
        }

        return ListResult.of(ret)
    }


    /**
     * 遍历，并判断 title 是否以 ： 开头
     */
    private fun proc(json: Map<String, Any?>, dbTypeJson: JsonMap) {

        RecursionUtil.recursionJson(json, { json ->
            if (json.containsKey("title") == false) {
                return@recursionJson true
            }
            var title = json.get("title").AsString();
            if (title.startsWith(":") == false) {
                return@recursionJson true;
            }

            title = title.Slice(1);
            var json2 = json as MutableMap<String, Any?>;
            var msgs1 = mutableListOf<String>()
            var msgs2 = mutableListOf<String>()

            title.split(",").forEach {
                if (proc_item(it, json2, dbTypeJson)) {
                    msgs1.add(it)
                } else {
                    msgs2.add(it);
                }
            }
            json2.set("title", arrayOf("+" + msgs1.joinToString(","), "-" + msgs2.joinToString(",")).joinToString(";"));
            return@recursionJson true;
        })
    }

    /**
     * @param dbTypeJson 形如： {"IdName": {id: {type:"string",description:"id"} ,name:{} }
     */
    private fun proc_item(type: String, json: MutableMap<String, Any?>, dbTypeJson: JsonMap): Boolean {
        if (dbTypeJson.containsKey(type) == false) return false;
        var dataTypes = dbTypeJson.get(type) as Map<String, JsonMap>;


        if (json.getStringValue("type") == "array") {
            var items = json.getValue("items") as MutableMap<String, Any?>

            proc_object(items, dataTypes);
        } else if (json.getStringValue("type") == "object") {
            proc_object(json, dataTypes);
        }
        return true;
    }

    private fun proc_object(json: MutableMap<String, Any?>, dataTypes: Map<String, JsonMap>) {

        if (json.get("type") != "object") {
            json.set("type", "object");
        }

        if (json.containsKey("properties") == false) {
            json.put("properties", JsonMap())
        }

        var properties = json.get("properties") as MutableMap<String, Any?>

        dataTypes.forEach {
            if (properties.containsKey(it.key) == false) {
                var idJson = JsonMap();
                idJson.put("type", it.value.getStringValue("type") ?: "string");
                idJson.put("description", it.value.getStringValue("description") ?: "")
                properties.put(it.key, idJson);
            }
        }
    }
}
