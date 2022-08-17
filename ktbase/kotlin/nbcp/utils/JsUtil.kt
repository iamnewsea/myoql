package nbcp.utils

import nbcp.comm.*
import nbcp.helper.ScriptLanguageEnum
import java.lang.RuntimeException
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset
import java.util.ArrayList
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

    private fun set_key_value(ret: JsonMap, keys: List<String>, value: String) {
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



    fun encodeURIComponent(value: String): String {
        return URLEncoder.encode(value, "utf-8");
    }


    fun decodeURIComponent(value: String): String {
        return URLDecoder.decode(value, "utf-8");
    }

    fun execScript(script: String): Any? {
        return ScriptLanguageEnum.js.execScript(script);
    }

    /**
     * url 需要提前 decodeURIComponent
     */
    fun parseUrlQueryJson(url: String): UrlQueryJsonData {
        return UrlQueryJsonData.of(url);
    }

    fun toUrlQuery(map: Map<String, *>): String {
        return map.toUrlQuery()
    }
}


data class UrlQueryJsonData @JvmOverloads constructor(
    var path: String = "",
    var queryJson: StringMap = StringMap(),
    var hash: String = ""
) {
    fun toUrl(): String {
        var ret = path;
        if (queryJson.any()) {
            ret += ("?" + queryJson.map { it.key + "=" + it.value }.joinToString("&"))
        }

        if (hash.HasValue) {
            ret += ("#" + hash)
        }
        return ret;
    }

    companion object {
        fun of(url: String): UrlQueryJsonData {
            var ret = UrlQueryJsonData();
            if (url.isEmpty()) return ret;

            var sect = url.split("?");
            ret.path = sect[0];
            if (sect.size < 2) return ret;

            if (sect[1].isEmpty()) return ret;

            var queryHash = sect[1].split("#");

            if (queryHash.size > 1) {
                ret.hash = queryHash.last();
            }

            queryHash[0].split("&").forEach {
                var items = it.split("=");
                if (items.size == 1) {
                    ret.queryJson.put(items[0], "")
                } else if (items.size == 2) {
                    ret.queryJson.put(items[0], items[1])
                } else {
                    throw RuntimeException("${items}非法，无法转换为键值对")
                }
            }

            return ret;
        }
    }
}