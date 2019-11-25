package nbcp.model

import nbcp.base.comm.DiffData
import nbcp.base.comm.JsonMap
import nbcp.base.comm.StringMap
import nbcp.base.extend.AsString
import nbcp.base.extend.IsIn
import java.io.Serializable
import java.lang.reflect.ParameterizedType

@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ExcelSheetName(val name: String)

@Target(AnnotationTarget.TYPE, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ExcelColumnName(val name: String)

/**
 * ExcelComponent 返回的数据类
 */
class DataTable<T> : Serializable {
    var columns = arrayOf<String>()
    var sheetName = ""
    var columnDefines = StringMap()  // key = Excel 的列， value是实体属性。

    constructor() {
        var type = (this::class.java as ParameterizedType).actualTypeArguments.first() as Class<T>;
        this.sheetName = (type.getAnnotationsByType(ExcelSheetName::class.java).firstOrNull() as ExcelSheetName?)?.name
                ?: ""

        type.declaredFields.forEach {
            this.columnDefines.set((it.getAnnotationsByType(ExcelColumnName::class.java).firstOrNull() as ExcelColumnName?)?.name
                    ?: it.name, it.name)
        }

        this.columns = this.columnDefines.values.toTypedArray();
    }

    val rows = mutableListOf<T>()

    fun <R> resetToOtherTable(translate:((T)->R)): DataTable<R> {
        var ret = DataTable<R>()
        this.rows.forEach { row ->
            ret.rows.add(translate(row))
        }

        return ret;
    }
}