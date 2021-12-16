package nbcp.db.mongo

import nbcp.comm.IsStringType
import nbcp.comm.Slice
import nbcp.comm.StringMap
import nbcp.utils.RecursionUtil
import org.bson.Document
import org.bson.types.ObjectId

object MongoDocument2EntityUtil {

    /**
     * 把 _id 转换为 id
     */
    @JvmOverloads
    fun procResultData_id2Id(value: Collection<*>, remove_id: Boolean = true) {
        value.forEach { v ->
            if (v == null) {
                return@forEach
            }

            if (v is MutableMap<*, *>) {
                procResultData_id2Id(v, remove_id);
            } else if (v is Collection<*>) {
                procResultData_id2Id(v, remove_id);
            } else if (v is Array<*>) {
                procResultData_id2Id(v, remove_id);
            }
        }
    }


    /**
     * 把 _id 转换为 id
     */
    @JvmOverloads
    fun procResultData_id2Id(value: Array<*>, remove_id: Boolean = true) {
        value.forEach { v ->
            if (v == null) {
                return@forEach
            }

            if (v is MutableMap<*, *>) {
                procResultData_id2Id(v, remove_id);
            } else if (v is Collection<*>) {
                procResultData_id2Id(v, remove_id);
            } else if (v is Array<*>) {
                procResultData_id2Id(v, remove_id);
            }
        }
    }

    @JvmOverloads
    fun procResultData_id2Id(value: MutableMap<*, *>, remove_id: Boolean = true) {
        var keys = value.keys.toTypedArray();
        var needReplace = keys.contains("_id") && !keys.contains("id")

        for (k in keys) {
            var v = value.get(k);
            if (needReplace && (k == "_id")) {
                if (v == null) {
                    v = "";
                } else if (v is ObjectId) {
                    v = v.toString()
                }

                (value as MutableMap<Any, Any?>).set("id", v);
                if (remove_id) {
                    value.remove("_id")
                }
                needReplace = false;
                continue;
            }
            if (v == null) {
                continue;
            }
            if (v is MutableMap<*, *>) {
                procResultData_id2Id(v, remove_id);
            } else if (v is Collection<*>) {
                procResultData_id2Id(v, remove_id);
            }
        }
    }


    /**
     *value 可能会是： Document{{answerRole=Patriarch}}
     */
    fun procDocumentJson(value: Document) {
        fun testDocumentString(item: Any?): Boolean {
            if (item == null) return false;
            var type = item::class.java;
            if (type.IsStringType == false) return false;
            var v_string_value = item.toString()
            return v_string_value.contains("{{") && v_string_value.endsWith("}}")
        }

        fun procDocumentString(v_string_value: String): Any {
            //Document{{answerRole=Patriarch}}
            //目前只发现一个键值对形式的。
            val startIndex = v_string_value.indexOf("{{");

            val json = StringMap();
            v_string_value.Slice(startIndex + 2, -2).split(",").forEach { item ->
                val sect = item.split("=");
                json.put(sect[0], sect[1]);
            }
            return json;
        }

        MongoDocument2EntityUtil.procResultData_id2Id(value);

        RecursionUtil.recursionAny(value, { json ->
            json.keys.toTypedArray().forEachIndexed { _, key ->
                if (key == null) {
                    return@forEachIndexed
                }
                var documentStringValue = json.get(key);
                if (!testDocumentString(documentStringValue)) {
                    return@forEachIndexed
                }
                (json as MutableMap<Any, Any>).set(key, procDocumentString(documentStringValue.toString()));

                return@forEachIndexed
            }
            return@recursionAny true
        }, { list ->
            var arrayList = list as MutableList<Any?>
            arrayList.forEachIndexed { index, it ->
                if (it == null || !testDocumentString(it)) {
                    return@forEachIndexed
                }
                arrayList[index] = procDocumentString(it.toString());
            }

            return@recursionAny true
        })
    }
}