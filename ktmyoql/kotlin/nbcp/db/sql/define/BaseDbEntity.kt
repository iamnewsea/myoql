package nbcp.db.sql

import nbcp.comm.AsString
import nbcp.comm.AllFields
import nbcp.db.sql.SqlBaseTable
import java.lang.reflect.Modifier

/**
 * Created by yuxh on 2018/7/18
 */
abstract class BaseDbEntity(val tableName: String) : java.io.Serializable {
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

    if (ret is SqlBaseTable<*>) {
        ret.getColumns().forEach {
            it.tableName = alias;
        }
    }
    return ret;
}