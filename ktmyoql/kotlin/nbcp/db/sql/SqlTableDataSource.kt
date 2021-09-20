package nbcp.db.sql

import nbcp.comm.*
import nbcp.db.AbstractMyOqlMultipleDataSourceProperties
import nbcp.db.SqlCrudEnum
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * 定义Sql不同的数据源
 */
@ConfigurationProperties(prefix = "app.sql.ds")
@Component
class SqlTableDataSource : AbstractMyOqlMultipleDataSourceProperties() {
    /*
app:
    sql:
        ds-yapi: mongodb://dev:123@mongo:27017/yapi
        ds:
            db:
                yapi:
                - group
                - project
                - api
                - interface_cat
            read:
                yapi-read:
                - group
                - project
        log:
            select:
            - group
            insert:
            - project
            update:
            - abc
     */
}


@ConfigurationProperties(prefix = "app.sql.log")
@Component
class SqlTableLogProperties :  InitializingBean{
    var select: List<String> = listOf()
    var insert: List<String> = listOf()
    var update: List<String> = listOf()
    var delete: List<String> = listOf()



    fun getSelectLog(tableDbName: String): Boolean {
        if (select.contains(tableDbName.toLowerCase())) return true;
        if (logDefault.contains(SqlCrudEnum.select)) return true;
        return false;
    }

    fun getInsertLog(tableDbName: String): Boolean {
        if (insert.contains(tableDbName.toLowerCase())) return true;
        if (logDefault.contains(SqlCrudEnum.insert)) return true;
        return false
    }

    fun getUpdateLog(tableDbName: String): Boolean {
        if (update.contains(tableDbName.toLowerCase())) return true;
        if (logDefault.contains(SqlCrudEnum.update)) return true;
        return false;
    }

    fun getDeleteLog(tableDbName: String): Boolean {
        if (delete.contains(tableDbName.toLowerCase())) return true;
        if (logDefault.contains(SqlCrudEnum.delete)) return true;
        return false;
    }


    val logDefault by lazy {
        var value = config.getConfig("app.sql.log-default").AsString().trim();
        if (value.HasValue) {
            if (value == "*") {
                return@lazy SqlCrudEnum::class.java.GetEnumList()
            }
            return@lazy SqlCrudEnum::class.java.GetEnumList(value)
        }
        return@lazy listOf<SqlCrudEnum>()
    }


    override fun afterPropertiesSet() {
        select = select.map { it.toLowerCase() }
        insert = insert.map { it.toLowerCase() }
        update = update.map { it.toLowerCase() }
        delete = delete.map { it.toLowerCase() }
    }
}