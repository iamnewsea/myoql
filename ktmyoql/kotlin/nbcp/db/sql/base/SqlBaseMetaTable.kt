package nbcp.db.sql

import nbcp.comm.AllFields
import nbcp.comm.AsString
import nbcp.db.*
import java.lang.reflect.Modifier
import java.io.Serializable

abstract class SqlBaseMetaTable<T : Serializable>(
        val tableClass: Class<T>,
        defEntityName: String,
        tableName: String = "",
        databaseId: String = ""
) : BaseMetaData(defEntityName, tableName, databaseId) {
    abstract fun getUks(): Array<Array<String>>
    abstract fun getFks(): Array<FkDefine>

    //    abstract fun getRks(): Array<Array<String>>
    abstract fun getAutoIncrementKey(): String

    //    abstract fun getColumns(): SqlColumnNames
    abstract fun getSpreadColumns(): Array<String>
//    abstract fun getConvertValueColumns(): Array<String>

    @Transient
    private var _columns = SqlColumnNames()
    fun getColumns(): SqlColumnNames {
        if (_columns.isNotEmpty()) {
            return _columns;
        }

        _columns = SqlColumnNames(*this::class.java.AllFields
                .filter { it.type == SqlColumnName::class.java }
                .map { it.get(this) as SqlColumnName }
                .toTypedArray()
        )

        return _columns;
    }


    protected var tableAliaValue: String = ""

    /**
     * this.tableAliaValue.AsString(this.tableName)
     */
    fun getAliaTableName(): String {
        return this.tableAliaValue.AsString(this.tableName);
    }

    /**
     * 调用扩展方法 alias
     */
    internal fun oriSetAlias(alias: String) {
        if (alias == this.tableAliaValue) {
            return;
        }
        this.tableAliaValue = alias;
    }
}


/**
 * 设置别名。
 */
fun <T : SqlBaseMetaTable<M>, M> T.alias(alias: String): T {
    val type = this::class.java;
    val ret = type.newInstance()

    type.AllFields.forEach {
        if (Modifier.isFinal(it.modifiers)) {
            return@forEach
        }

        it.isAccessible = true

        it.set(ret, it.get(this))
    }

    ret.oriSetAlias(alias);

    if (ret is SqlBaseMetaTable<*>) {
        ret.getColumns().forEach {
            it.tableName = alias;
        }
    }
    return ret;
}
