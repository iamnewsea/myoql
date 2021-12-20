package nbcp.db.sql

import nbcp.comm.*
import nbcp.db.MyOqlBaseActionLogDefine
import nbcp.db.MyOqlMultipleDataSourceDefine
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * 定义Sql不同的数据源
 */
@ConfigurationProperties(prefix = "app.sql")
@Component
class SqlTableDataSource : MyOqlMultipleDataSourceDefine() {
    /*
app:
    sql:
        yapi:
            ds:
                uri: mongodb://dev:123@mongo:27017/yapi
                username:
                password:
            tables:
                yapi:
                - group
                - project
                - api
                - interface_cat
            read-tables:
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
class SqlTableLogProperties :  MyOqlBaseActionLogDefine("app.sql.log-default"){
}