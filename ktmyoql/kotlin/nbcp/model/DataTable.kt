package nbcp.model

import nbcp.comm.*
import nbcp.comm.*
import nbcp.comm.*
import nbcp.base.extend.AsString
import nbcp.base.extend.IsIn
import java.io.Serializable
import java.lang.reflect.ParameterizedType

/**
 * ExcelComponent 返回的数据类
 */

class DataTable<T>(clazz: Class<T>) : Serializable {
    var columns = arrayOf<String>()
    var sheetName = ""
//    var columnDefines = StringMap()  // key = Excel 的列， value是实体属性。

    init  {
        var type = clazz;
        this.sheetName = type.simpleName;

        this.columns = type.declaredFields.map { it.name }.toTypedArray()
    }

    val rows = mutableListOf<T>()

    inline fun <reified R> resetToOtherTable(translate:((T)->R)): DataTable<R> {
        var ret = DataTable<R>(R::class.java)
        this.rows.forEach { row ->
            ret.rows.add(translate(row))
        }

        return ret;
    }
}