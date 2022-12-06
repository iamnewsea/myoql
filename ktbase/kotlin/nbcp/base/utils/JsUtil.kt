package nbcp.base.utils

import nbcp.base.comm.JsonMap
import nbcp.base.data.UrlQueryJsonData
import nbcp.base.extend.AsString
import nbcp.base.extend.HasValue
import nbcp.base.extend.Slice
import nbcp.base.extend.toUrlQuery
import java.net.URLDecoder
import java.net.URLEncoder
import javax.script.Compilable
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

/**
 * Created by udi on 17-4-21.
 */


object JsUtil {

    @JvmStatic
    fun getSoloKeysFromUrl(value: String): Array<String> {
        return value.trim()
            .split("?")
            .last()
            .split("&")
            .filter { it.HasValue }
            .map { it.split("=") }
            .filter { it.size == 1 || it.last().isEmpty() }
            .map { it.first() }
            .toTypedArray()
    }

    private fun set_key_value(ret: MutableMap<String, Any?>, keys: List<String>, value: String) {
        if (keys.any() == false) return;

        var key = keys.first();
        if (keys.size == 1) {
            //如果指明是数组 []
//                var isArray = false;
            if (key.endsWith("[]")) {
                key = key.Slice(0, -2);
//                    isArray = true;

                if (ret.containsKey(key) == false) {
                    ret.put(key, mutableListOf<String>())
                }
            }

            if (ret.containsKey(key) == false) {
                ret.put(key, value)
                return;
            }

            var v_list = mutableListOf<String>()
            var dbValue = ret[key];
            if (dbValue is ArrayList<*>) {
                v_list = dbValue as ArrayList<String>;
            } else {
                if (dbValue != null) {
                    v_list.add(dbValue.AsString())
                }
            }

            v_list.add(value)
            ret.set(key, v_list);
            return;
        }

        if (ret.containsKey(key) == false) {
            ret.put(key, JsonMap());
        }

        var subObj = ret.get(key)!!
        if (subObj is JsonMap == false) {
            throw RuntimeException("${keys.joinToString(".")},已有类型:${subObj::class.java.name}")
        }

        set_key_value(subObj, keys.Slice(1), value);
    }


    /**
     */
    @JvmStatic
    @JvmOverloads
    fun urlQueryToJson(urlQueryString: String, soloIsTrue: Boolean = false): JsonMap {
        val ret = JsonMap()
        var urlQuery = urlQueryString.trim().split("?").last()
        if (urlQuery.isEmpty()) return ret;

        val list = urlQuery.split("&").filter { it.HasValue }.toTypedArray()
        for (item in list) {
            val kv = item.split("=").dropLastWhile { it.isEmpty() }.toTypedArray()
            if (kv.size < 1) {
                continue;
            }

            var key = kv[0];
            var value: String? = null;

            if (kv.size == 1) {
                if (soloIsTrue) {
                    value = "true"
                }
            } else {
                value = JsUtil.decodeURIComponent(kv[1]);
            }

            if (value == null) {
                continue;
            }
            //如果 key 是多级对象。
            var key_parts = key.split(".");
            set_key_value(ret, key_parts, value);
        }

        return ret
    }


    @JvmStatic
    fun encodeURIComponent(value: String): String {
        return URLEncoder.encode(value, "utf-8");
    }

    @JvmStatic
    fun decodeURIComponent(value: String): String {
        return URLDecoder.decode(value, "utf-8");
    }

    /**
     * url 需要提前 decodeURIComponent
     */
    @JvmStatic
    fun parseUrlQueryJson(url: String): UrlQueryJsonData {
        return UrlQueryJsonData.of(url);
    }

    @JvmStatic
    fun toUrlQuery(map: Map<String, *>): String {
        return map.toUrlQuery()
    }
}


