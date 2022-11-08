package nbcp.myoql.db.sql

import nbcp.base.comm.*
import nbcp.base.comm.*;
import nbcp.base.db.*;
import nbcp.base.enums.*;
import nbcp.base.extend.*;
import nbcp.base.utils.*;
import nbcp.myoql.db.enums.*
import nbcp.myoql.db.*;
import nbcp.myoql.db.comm.*
import nbcp.myoql.db.comm.MyOqlMultipleDataSourceDefine
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * 定义Sql不同的数据源
 */
@Component
class SqlTableDataSource : MyOqlMultipleDataSourceDefine("app.sql") {
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