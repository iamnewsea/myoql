package nbcp.web.mvc.handler

import nbcp.base.annotation.Require
import nbcp.base.comm.*
import nbcp.base.db.*
import nbcp.base.enums.*
import nbcp.base.exception.ParameterInvalidException
import nbcp.base.extend.*
import nbcp.base.utils.*
import nbcp.mvc.mvc.findParameterValue
import nbcp.mvc.annotation.*
import nbcp.myoql.db.db
import nbcp.myoql.db.mongo.MongoBaseQueryClip
import nbcp.myoql.db.mongo.MongoTemplateScope
import nbcp.myoql.db.mongo.base.MongoColumnName
import nbcp.myoql.db.mongo.component.MongoBaseUpdateClip
import nbcp.myoql.db.mongo.extend.match_or
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@OpenAction
@RestController
@RequestMapping("/dev/yapi")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
class DevYapiDataTypeServlet {
    /**
     * 修正 yapi 的数据类型,在 title 字段设置如下格式： :IdName,IdUrl,会对其下属性添加 id,name,url 字段。
     * @param typeMap , 形如： {"IdName": {id: {type:"string",description:"id",mock:"1"} ,name:{} }
     */
    @RequestMapping("/user-types", method = [RequestMethod.POST, RequestMethod.GET])
    fun dbTypes(@Require connString: String, request: HttpServletRequest): nbcp.base.comm.ApiResult<JsonMap> {
        var typeMapObject = request.findParameterValue("typeMap")

        if (typeMapObject == null) {
            throw ParameterInvalidException("需要 typeMap 参数")
        }

        var typeMap = mutableMapOf<String, Any?>()
        var typeMapClass = typeMapObject::class.java;
        if (typeMapClass.IsStringType) {
            typeMap = typeMapObject.AsString().FromJson<MutableMap<String, Any?>>()!!
        } else {
            typeMap = typeMapObject as MutableMap<String, Any?>
        }

        var ret = JsonMap()
        usingScope(MongoTemplateScope(db.mongo.getMongoTemplateByUri(connString)!!)) {
            var query = MongoBaseQueryClip("interface")

            var where = (MongoColumnName("req_body_other") match_like "title\":\":") match_or
                    (MongoColumnName("res_body") match_like "title\":\":")

            query.whereData.putAll(where.criteriaObject)
            var list = query.toMapList();

            list.forEach {
                var id = it.getIntValue("id");


                var item_msgs = mutableListOf<String>()
                var req_body_other = it.getStringValue("req_body_other") ?: "{}"
                var json = req_body_other.FromJson<Map<String, Any?>>();
                if (json != null) {
                    item_msgs.addAll(proc(json, typeMap));

                    req_body_other = json.ToJson()
                }

                var res_body = it.getStringValue("res_body") ?: "{}";

                var json2 = res_body.FromJson<Map<String, Any?>>()
                if (json2 != null) {
                    item_msgs.addAll(proc(json2, typeMap));

                    res_body = json2.ToJson()
                }


                var update = MongoBaseUpdateClip("interface")
                update.whereData.putAll((MongoColumnName("_id") match id).criteriaObject)
                update.setValue("req_body_other", req_body_other)
                update.setValue("res_body", res_body)
                update.exec();

                if (db.affectRowCount == 0) {

                } else {
                    ret.put(it.getStringValue("path") ?: it.getStringValue("title") ?: it.toString(), item_msgs)
                }
            }
        }

        return nbcp.base.comm.ApiResult.of(ret)
    }


    /**
     * 遍历，并判断 title 是否以 ： 开头
     */
    private fun proc(json: Map<String, Any?>, typeMap: MutableMap<String, Any?>): List<String> {

        var items_msg = mutableListOf<String>();

        RecursionUtil.recursionJson(json, { jsonValue ->
            if (jsonValue.containsKey("title") == false) {
                return@recursionJson true
            }
            var title = jsonValue.get("title").AsString();
            if (title.startsWith(":") == false) {
                return@recursionJson true;
            }

            title = title.substring(1);
            var json_item = jsonValue as MutableMap<String, Any?>;
            var msg_ok = mutableListOf<String>()
            var msg_no = mutableListOf<String>()

            title.split(",").forEach {
                if (proc_item(it, json_item, typeMap)) {
                    msg_ok.add(it)
                } else {
                    msg_no.add(it);
                }
            }

            items_msg.add("成功处理" + msg_ok.joinToString(",") + (if (msg_no.any()) ",未处理" + msg_no.joinToString(",") + "!" else ""))
            json_item.set("title", if (msg_no.any()) "-" + msg_no.joinToString(";") else "");
            return@recursionJson true;
        })
        return items_msg;
    }

    /**
     * @param typeMap 形如： {"IdName": {id: {type:"string",description:"id"} ,name:{} }
     */
    private fun proc_item(type: String, json: MutableMap<String, Any?>, typeMap: MutableMap<String, Any?>): Boolean {
        if (typeMap.containsKey(type) == false) return false;
        var userTypeDefine: MutableMap<String, Any> = mutableMapOf()

        var v = typeMap.get(type)!!
        var v_class = v::class.java;

        if (v_class.IsStringType) {
            v.AsString().split(",").forEach {
                userTypeDefine.put(it, JsonMap());
            }
        } else {
            userTypeDefine = v as MutableMap<String, Any>;
        }


        if (json.containsKey("type") == false) {
            throw RuntimeException("title,type必须同级!")
        }
        if (json.getStringValue("type") != "object") {
            json.set("type", "object")
            json.set("properties", JsonMap())
        }

        proc_object(json, userTypeDefine);
        return true;
    }

    private fun proc_object(json: MutableMap<String, Any?>, userTypeDefine: Map<String, Any>) {
        if (json.containsKey("properties") == false) {
            json.put("properties", JsonMap())
        }

        var properties = json.get("properties") as MutableMap<String, Any?>

        userTypeDefine.forEach {
            if (properties.containsKey(it.key) == false) {
                var mapValue = it.value as Map<String, Any>
                var itemJson = JsonMap();
                itemJson.put("type", mapValue.getStringValue("type") ?: "string");

                var description = mapValue.getStringValue("description") ?: ""
                if (description.HasValue) {
                    itemJson.put("description", description)
                }

                var mock = mapValue.get("mock")
                if (mock != null) {
                    if (itemJson.containsKey("mock") == false) {
                        itemJson.put("mock", JsonMap());
                    }
                    (itemJson.get("mock") as MutableMap<String, Any?>).put("mock", mock);
                }

                properties.put(it.key, itemJson);
            }
        }
    }
}
