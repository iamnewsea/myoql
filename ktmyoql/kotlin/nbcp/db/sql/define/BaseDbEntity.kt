package nbcp.db.sql

import nbcp.base.extend.AsString
import nbcp.base.extend.AllFields
import nbcp.db.sql.SqlBaseTable
import java.lang.reflect.Modifier

/**
 * Created by yuxh on 2018/7/18
 */
abstract class BaseDbEntity(val tableName: String) : java.io.Serializable {
    protected var tableAliaValue: String = ""


    fun getAliaTableName(): String {
        return this.tableAliaValue.AsString(this.tableName);
    }

    //请调用 setAlias
    fun oriSetAlias(alias: String) {
        if (alias == this.tableAliaValue) {
            return;
        }
        this.tableAliaValue = alias;
    }
}

fun <T : BaseDbEntity> T.alias(alias: String): T {
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

    if( ret is SqlBaseTable<*>){
        ret.getColumns().forEach {
            it.tableName = alias;
        }
    }
    return ret;
}