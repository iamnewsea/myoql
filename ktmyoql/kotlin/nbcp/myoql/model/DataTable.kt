package nbcp.myoql.model

import nbcp.base.comm.JsonMap
import nbcp.base.comm.const
import nbcp.base.extend.*
import java.io.Serializable

/**
 * ExcelComponent 返回的数据类
 */

class DataTable<T : Any>(type: Class<T>) : Serializable {
    var columns = arrayOf<String>()
    var sheetName = ""
//    var columnDefines = StringMap()  // key = Excel 的列， value是实体属性。

    init {
        this.sheetName = type.simpleName;

        if (type.isMemberClass == false) {
            this.columns = type.AllFields.map { it.name }.toTypedArray()
        }
    }

    val rows = mutableListOf<T>()

    inline fun <reified R : Any> resetToOtherTable(translate: ((T) -> R)): DataTable<R> {
        var ret = DataTable<R>(R::class.java)
        this.rows.forEach { row ->
            ret.rows.add(translate(row))
        }

        return ret;
    }

    /**
     * 第一行是标题
     */
    fun toCsvContent(): String {
        var list = mutableListOf<String>()

        list.add(this.columns.map { toCsvItemText(it) }.joinToString(","))

        list.addAll(this.rows.map {
            var map = it.ConvertJson(JsonMap::class.java)
            return@map this.columns.map { toCsvItemText(map.getStringValue(it).AsString()) }.joinToString(",")
        })

        return list.joinToString(const.line_break)
    }

    companion object {
        /**
         * 第一行是标题
         */
        @JvmStatic
        fun <T : Any> loadFromCsv(content: String, type: Class<T>): DataTable<T> {
            var ret = DataTable(type)
            var words = content.Tokenizer(
                { it == ',' || it == '\n' }, arrayOf(
                    TokenQuoteDefine('"', '"', '"')
                )
            );


            var findTitle = false;

            words.SplitGroup { it == "\n" }
                .map { rowData ->
                    //去除 ，,注意连续两个逗号的情况，奇数位置必须是 逗号
                    var list = mutableListOf<String>()
                    list.addAll(rowData);
                    for (i in 0 until list.size) {
                        if (i % 2 == 0) {
                            if (list[i] == ",") {
                                list.add(i, "");
                            }
                        }
                    }

                    for (i in list.size - 1 downTo 0) {
                        if (i % 2 == 1) {
                            list.removeAt(i);
                        }
                    }

                    return@map list.map { getCsvItemText(it) }
                }
                .filter { rowData ->
                    if (rowData.size == 0) return@filter false;
                    if (rowData.size == 1 && rowData.first().trim().isEmpty()) return@filter false;
                    return@filter true;
                }
                .forEach { rowData ->
                    if (findTitle == false) {
                        ret.columns = rowData.toTypedArray();
                        findTitle = true;

                        return@forEach
                    }

                    var jsonMap = JsonMap();

                    ret.columns.forEachIndexed { index, k ->
                        if (index < rowData.size) {
                            jsonMap.put(k, rowData.get(index))
                        } else {
                            jsonMap.put(k, "");
                        }
                    }

                    ret.rows.add(jsonMap.ConvertJson(type));
                }

            return ret;
        }

        private fun getCsvItemText(value: String): String {
            if (!value.startsWith('"') || !value.endsWith('"')) {
                return value;
            }

            return value.Slice(1, -1).replace("\"\"", "\"");
        }

        private fun toCsvItemText(value: String): String {
            if (value.any { it.isLetterOrDigit() == false }) {
                return "\"" + value.replace("\"", "\"\"") + "\""
            }

            return value;
        }
    }
}