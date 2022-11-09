package nbcp.myoql.db.comm

import nbcp.base.comm.config
import nbcp.base.enums.CrudEnum
import nbcp.base.extend.AsString
import nbcp.base.extend.GetEnumList
import nbcp.base.extend.HasValue
import org.springframework.beans.factory.InitializingBean


abstract class MyOqlBaseActionLogDefine(val logDefaultKey: String) : InitializingBean {
    var query: List<String> = listOf()
    var insert: List<String> = listOf()
    var update: List<String> = listOf()
    var delete: List<String> = listOf()


    fun getQueryLog(tableDbName: String): Boolean {
        if (query.contains(tableDbName.lowercase())) return true;
        if (logDefault.contains(CrudEnum.read)) return true;
        return false;
    }

    fun getInsertLog(tableDbName: String): Boolean {
        if (insert.contains(tableDbName.lowercase())) return true;
        if (logDefault.contains(CrudEnum.create)) return true;
        return false
    }

    fun getUpdateLog(tableDbName: String): Boolean {
        if (update.contains(tableDbName.lowercase())) return true;
        if (logDefault.contains(CrudEnum.update)) return true;
        return false;
    }

    fun getDeleteLog(tableDbName: String): Boolean {
        if (delete.contains(tableDbName.lowercase())) return true;
        if (logDefault.contains(CrudEnum.delete)) return true;
        return false;
    }


    val logDefault by lazy {
        var value = config.getConfig(this.logDefaultKey).AsString().trim();
        if (value.HasValue) {
            if (value == "*") {
                return@lazy CrudEnum::class.java.GetEnumList()
            }
            return@lazy CrudEnum::class.java.GetEnumList(value)
        }
        return@lazy listOf<CrudEnum>()
    }

    override fun afterPropertiesSet() {
        query = query.map { it.lowercase() }
        insert = insert.map { it.lowercase() }
        update = update.map { it.lowercase() }
        delete = delete.map { it.lowercase() }
    }
}
