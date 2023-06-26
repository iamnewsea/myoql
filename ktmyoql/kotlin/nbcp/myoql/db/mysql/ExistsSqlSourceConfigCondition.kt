package nbcp.myoql.db.mysql

import nbcp.base.comm.config
import nbcp.base.utils.ClassUtil
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

class ExistsSqlSourceConfigCondition : Condition {
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        if( config.getConfig("spring.datasource.url").isNullOrEmpty()){
            return false;
        }

        return ClassUtil.existsClass("org.mariadb.jdbc.Driver") ||
                ClassUtil.existsClass("com.mysql.cj.jdbc.Driver") ||
                ClassUtil.existsClass("com.microsoft.sqlserver.jdbc.SQLServerDriver") ||
                ClassUtil.existsClass("org.sqlite.JDBC") ||
                ClassUtil.existsClass("org.postgresql.Driver") ||
                ClassUtil.existsClass("oracle.jdbc.driver.OracleDriver")
    }
}