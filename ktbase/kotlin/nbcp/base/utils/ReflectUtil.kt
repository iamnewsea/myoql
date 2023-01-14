package nbcp.base.utils

import nbcp.base.comm.JsonMap
import nbcp.base.extend.*
import java.lang.reflect.Field

object ReflectUtil {
    /**
     * 多层级设置值
     */
    @JvmStatic
    @JvmOverloads
    fun setValueByWbsPath(
            data: Any,
            vararg keys: String,
            value: Any?,
            ignoreCase: Boolean = false
    ): Boolean {
        if (keys.any() == false) return false;

        var unwindKeys = keys
                .map { it.split('.') }
                .Unwind()
                .map {
                    var index = it.indexOf('[')
                    if (index <= 0) {
                        return@map listOf(it)
                    }
                    return@map listOf(it.Slice(0, index), it.Slice(index))
                }
                .Unwind()
                .filter { it.HasValue }
                .toTypedArray();

        if (unwindKeys.size != keys.size) {
            return setValueByWbsPath(data, *unwindKeys, ignoreCase = ignoreCase, value = value);
        }

        var beforeKeys = keys.ArraySlice(0, -1);
        var lastKey = keys.last()

        var objValue: Any? = data;

        if (beforeKeys.any()) {
            var fillLastArray = lastKey.startsWith("[") && lastKey.endsWith("]")
            objValue = getValueByWbsPath(
                    data,
                    *beforeKeys.toTypedArray(),
                    ignoreCase = ignoreCase,
                    fillMap = true,
                    fillLastArray = fillLastArray
            );
        }

        if (objValue == null) {
            return false;
        }

        if (objValue is Map<*, *>) {
            if (objValue is MutableMap<*, *> == false) {
                throw RuntimeException("不是可修改的map")
            }

            var vbKeys = objValue.keys.filter { it.toString().compareTo(lastKey, ignoreCase) == 0 }

            if (vbKeys.size > 1) {
                throw RuntimeException("找到多个 key: ${lastKey}")
            } else if (vbKeys.any()) {
                lastKey = vbKeys.first().toString();
            }

            if (value == null) {
                (objValue as MutableMap<String, Any?>).remove(lastKey);
            } else {
                (objValue as MutableMap<String, Any?>).put(lastKey, value);
            }
            return true;
        } else if (objValue is Array<*>) {
            if (lastKey.startsWith("[") && lastKey.endsWith("]")) {
                var index = lastKey.substring(1, lastKey.length - 1).AsInt(-1)
                if (index < 0) {
                    throw RuntimeException("索引值错误:${lastKey},${index}")
                }

                (data as Array<Any?>).set(index, value);
                return true;
            }

            return false;
        } else if (objValue is Collection<*>) {
            if (lastKey.startsWith("[") && lastKey.endsWith("]")) {
                var index = lastKey.substring(1, lastKey.length - 1).AsInt(-1)
                if (index < 0) {
                    throw RuntimeException("索引值错误:${lastKey},${index}")
                }

                for (i in objValue.size..index) {
                    (objValue as MutableList<Any?>).add(JsonMap())
                }

                (objValue as MutableList<Any?>).set(index, value);
                return true;
            }

            return false;
        }

        //如果是对象
        return setPrivatePropertyValue(objValue, lastKey, ignoreCase = ignoreCase, value = value)
    }



    @JvmStatic
    fun getPrivatePropertyValue(entity: Any, type: Field): Any? {
        type.isAccessible = true;
        return type.get(entity);
    }

    @JvmStatic
    fun setPrivatePropertyValue(entity: Any, type: Field, value: Any?) {
        type.isAccessible = true;
        type.set(entity, value);
    }





    /**
     * @param properties 多级属性 , 请使用 ReflectUtil.getValueByWbsPath
     */
    private fun getPrivatePropertyValue(entity: Any?, vararg properties: String, ignoreCase: Boolean = false): Any? {
        if (entity == null) return null;
        if (properties.any() == false) return null;

        var type = entity::class.java.FindField(properties.first(), ignoreCase);
        if (type == null) {
            return null;
        }

        var ret = type.get(entity);
        if (properties.size == 1) {
            return ret
        }
        var leftProperties = properties.slice(1 until properties.size).toTypedArray();
        return getPrivatePropertyValue(ret, *leftProperties)
    }


    /**
     * 支持多层级设置属性值
     */
    private fun setPrivatePropertyValue(
            entity: Any?,
            vararg properties: String,
            value: Any?,
            ignoreCase: Boolean = false
    ): Boolean {
        if (entity == null) return false;
        if (properties.any() == false) return false;

        var type = entity::class.java.FindField(properties.first(), ignoreCase);
        if (type != null) {
            var ret = type.get(entity);
            if (properties.size == 1) {
                type.set(entity, value);
                return true
            } else {
                var leftProperties = properties.slice(1 until properties.size).toTypedArray();
                return setPrivatePropertyValue(ret, *leftProperties, ignoreCase = ignoreCase, value = value)
            }
        }
        return false;
    }

    @JvmStatic
    fun setPrivatePropertyValue(entity: Any, property: String, value: Any?): Boolean {
        return setPrivatePropertyValue(entity, *arrayOf(property), ignoreCase = false, value = value);
    }

    /**
     * 通过 path 获取 value,每级返回的值必须是 Map<String,V> 否则返回 null
     * @param keys: 可以传多个key，也可以使用 . 分隔；如果查询数组，使用 products[],products[0], products.[] 或 products.[0] 或 "products","[]"
     */
    @JvmStatic
    @JvmOverloads
    fun getValueByWbsPath(
            data: Any,
            vararg keys: String,
            ignoreCase: Boolean = false,
            fillMap: Boolean = false,
            fillLastArray: Boolean = false
    ): Any? {
        if (keys.any() == false) return null;

        var unwindKeys = keys
                .map { it.split('.') }
                .Unwind()
                .map { it.trim() }
                .filter { it.HasValue }
                .toTypedArray();

        if (unwindKeys.size != keys.size) {
            return getValueByWbsPath(
                    data,
                    *unwindKeys,
                    ignoreCase = ignoreCase,
                    fillMap = fillMap,
                    fillLastArray = fillLastArray
            );
        }

        var key = keys.first();
        var left_keys = keys.ArraySlice(1);

        if (key.isEmpty()) {
            return null;
        }

        var isLastKey = left_keys.any() == false;

        if (key.endsWith("]")) {
            if (key != "[]" && key.endsWith("[]")) {
                var keys2 = mutableListOf<String>()
                keys2.add(key.Slice(0, -2))
                keys2.add("[]")
                keys2.addAll(left_keys);

                return getValueByWbsPath(
                        data,
                        keys = *keys2.toTypedArray(),
                        ignoreCase = ignoreCase,
                        fillMap = fillMap,
                        fillLastArray = fillLastArray
                );
            }
            var start_index = key.lastIndexOf('[');
            if (start_index > 0) {
                var keys2 = mutableListOf<String>()
                keys2.add(key.slice(0 until start_index))
                keys2.add(key.Slice(start_index))
                keys2.addAll(left_keys);
                return getValueByWbsPath(
                        data,
                        keys = *keys2.toTypedArray(),
                        ignoreCase = ignoreCase,
                        fillMap = fillMap,
                        fillLastArray = fillLastArray
                )
            }
        }

        if (data is Map<*, *>) {
            var vbKeys = data.keys.filter { it.toString().compareTo(key, ignoreCase) == 0 }

            if (vbKeys.size > 1) {
                throw RuntimeException("找到多个 key: ${key}")
            } else if (vbKeys.size == 0) {
                if (isLastKey && fillLastArray) {
                    (data as MutableMap<String, Any?>).put(key, mutableListOf<Any?>());
                } else if (fillMap) {
                    if (left_keys.any() && left_keys.first().startsWith("[")) {
                        (data as MutableMap<String, Any?>).put(key, mutableListOf<Any?>());
                    } else {
                        (data as MutableMap<String, Any?>).put(key, JsonMap());
                    }
                } else {
                    return null;
                }
            } else {
                key = vbKeys.first().toString();
            }

            var v = data.get(key)

            if (v == null) {
                return null;
            }

            if (left_keys.any() == false) return v;

            return getValueByWbsPath(
                    v,
                    *left_keys.toTypedArray(),
                    ignoreCase = ignoreCase,
                    fillMap = fillMap,
                    fillLastArray = fillLastArray
            )
        } else if (key == "[]") {
            var data2: List<*>
            if (data is Array<*>) {
                data2 = data.filter { it != null }
            } else if (data is Collection<*>) {
                data2 = data.filter { it != null }
            } else {
                throw RuntimeException("数据类型不匹配,${keys} 中 ${key} 需要是数组类型")
            }

            if (left_keys.any() == false) return data2;

            return data2
                    .map {
                        getValueByWbsPath(
                                it!!,
                                *left_keys.toTypedArray(),
                                ignoreCase = ignoreCase,
                                fillMap = fillMap,
                                fillLastArray = fillLastArray
                        )
                    }
                    .filter { it != null }

        } else if (key.startsWith("[") && key.endsWith("]")) {
            var index = key.substring(1, key.length - 1).AsInt(-1)
            if (index < 0) {
                throw RuntimeException("索引值错误:${key}")
            }

            var data2: Any?
            if (data is Array<*>) {
                data2 = data.get(index)
            } else if (data is Collection<*>) {
                //数组的数组很麻烦

                if (fillMap) {
                    for (i in data.size..index) {
                        (data as MutableList<Any?>).add(JsonMap())
                    }
                }

                data2 = data.elementAt(index)
            } else {
                throw RuntimeException("需要数组类型,但是实际类型是${data::class.java.name}, keys:${keys.joinToString(",")},key: ${key}")
            }

            if (data2 == null) {
                return null;
            }

            if (left_keys.any() == false) return data2;

            return getValueByWbsPath(
                    data2,
                    *left_keys.toTypedArray(),
                    ignoreCase = ignoreCase,
                    fillMap = fillMap,
                    fillLastArray = fillLastArray
            )
        }

        //如果是对象

        var v = getPrivatePropertyValue(data, key)
        if (v == null) return null;
        if (left_keys.any() == false) {
            return v;
        }

        return getValueByWbsPath(
                v,
                *left_keys.toTypedArray(),
                ignoreCase = ignoreCase,
                fillMap = fillMap,
                fillLastArray = fillLastArray
        )
    }
}