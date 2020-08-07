package nbcp.db.sql

import nbcp.comm.AllFields
import nbcp.comm.AsString
import nbcp.db.*
import java.lang.reflect.Modifier


abstract class SqlBaseMetaTable<T : ISqlDbEntity>(val tableClass: Class<T>, tableName: String)
    : BaseMetaData(tableName) {
    abstract fun getUks(): Array<Array<String>>
    abstract fun getFks(): Array<FkDefine>
    abstract fun getRks(): Array<Array<String>>
    abstract fun getAutoIncrementKey(): String
    abstract fun getColumns(): SqlColumnNames
    abstract fun getSpreadColumns(): Array<String>
    abstract fun getConvertValueColumns(): Array<String>

//    fun getColumns(): SqlColumnNames {
//        var ret = SqlColumnNames()
//        ret.addAll(this::class.java.AllFields
//                .filter { it.type == SqlColumnName::class.java }
//                .map { it.isAccessible = true; it.get(this) as SqlColumnName }
//        )
//
//        return ret;
//    }


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
    fun oriSetAlias(alias: String) {
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
    var type = this::class.java;
    var ret = type.newInstance()

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
