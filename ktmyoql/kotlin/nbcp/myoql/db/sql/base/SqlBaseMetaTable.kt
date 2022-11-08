package nbcp.myoql.db.sql.base


import nbcp.myoql.db.sql.define.FkDefine
import nbcp.base.extend.*;
import nbcp.myoql.db.comm.*
import java.lang.reflect.Modifier
import java.io.Serializable

abstract class SqlBaseMetaTable<T : Serializable>(
    entityClass: Class<T>,
    tableName: String = "",
    databaseId: String = ""
) : BaseMetaData<T>(entityClass, tableName, databaseId) {
    companion object {
        @Transient
        private var table_columns = mutableMapOf<String, SqlColumnNames>()

        @Transient
        private var table_json_columns = mutableMapOf<String, SqlColumnNames>()


//        @Transient
//        private var table_spread_columns = mutableMapOf<String, SqlColumnNames>()
    }

    abstract fun getUks(): Array<Array<String>>
    abstract fun getFks(): Array<FkDefine>

    //    abstract fun getRks(): Array<Array<String>>
    abstract fun getAutoIncrementKey(): String

    //    abstract fun getColumns(): SqlColumnNames
    abstract fun getSpreadColumns(): Array<SqlSpreadColumnData>
//    abstract fun getConvertValueColumns(): Array<String>

    fun getColumns(): SqlColumnNames {
        var tableMetaName = this::class.java.simpleName
        var _columns = table_columns.get(tableMetaName)
        if (_columns != null) {
            return _columns;
        }

        _columns = SqlColumnNames(*this::class.java.AllFields
            .filter { it.type == SqlColumnName::class.java }
            .map { it.get(this) as SqlColumnName }
            .toTypedArray()
        )

        table_columns.put(tableMetaName, _columns)
        return _columns;
    }

    fun getJsonColumns(): SqlColumnNames {
        var tableMetaName = this::class.java.simpleName
        var _columns = table_json_columns.get(tableMetaName)
        if (_columns != null) {
            return _columns;
        }

        var all_columns = getColumns();
        _columns = SqlColumnNames(*this.entityClass.AllFields
            .filter { it.type.IsCollectionType || it.type.IsMapType || !it.type.IsSimpleType() }
            .filter {
                return@filter it.getAnnotation(SqlSpreadColumn::class.java) == null
            }
            .map { field -> all_columns.firstOrNull { it.name == field.name } }
            .filter { it != null }
            .map { it!! }
            .toTypedArray()
        )

        table_json_columns.put(tableMetaName, _columns)
        return _columns;
    }

//    fun getSpreadColumns(): SqlColumnNames {
//        var tableMetaName = this::class.java.simpleName
//        var _columns = table_spread_columns.get(tableMetaName)
//        if (_columns != null) {
//            return _columns;
//        }
//
//        _columns = SqlColumnNames(*this::class.java.AllFields
//            .filter {
//                return@filter it.getAnnotation(SqlSpreadColumn::class.java) != null
//            }
//            .map { it.get(this) as SqlColumnName }
//            .toTypedArray()
//        )
//
//        table_spread_columns.put(tableMetaName, _columns)
//        return _columns;
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
