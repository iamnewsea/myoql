@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.comm

import nbcp.utils.MyUtil
import nbcp.utils.SpringUtil
import org.slf4j.Logger
import org.springframework.beans.factory.Aware
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.ImportSelector
import org.springframework.core.type.AnnotationMetadata
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.util.unit.DataSize
import java.lang.annotation.Inherited
import java.nio.charset.Charset

/**
 * 配置项
 */
object config {
    private var _debug: Boolean? = null;
    val debug: Boolean
        get() {
            if (_debug != null) {
                return _debug!!;
            }

            if (SpringUtil.isInited == false) return false;
            _debug = SpringUtil.context.environment.getProperty("debug").AsBoolean();
            return _debug ?: false;
        }

    val uploadHost: String by lazy {
        return@lazy SpringUtil.context.environment.getProperty("app.upload.host") ?: "";
    }

    val mybatisPackage: String by lazy {
        return@lazy SpringUtil.context.environment.getProperty("app.mybatis.package") ?: ""
    }

    /**
     * 映射到 DatabaseEnum 枚举上
     */
    val databaseType: String by lazy {
        var type = SpringUtil.context.environment.getProperty("app.database-type")
        if (type.HasValue) {
            return@lazy type;
        }

        var mongo = SpringUtil.context.containsBean("mongoAutoConfiguration");
        if (mongo) {
            return@lazy "Mongo"
        }

        var sql = SpringUtil.context.containsBean("dataSourceAutoConfiguration");
        if (sql) {

            var conn = SpringUtil.context.environment.getProperty("spring.datasource.url");
            if (conn.isNullOrEmpty()) {
                conn = SpringUtil.context.environment.getProperty("spring.datasource.hikari.url")
            }

            if (conn.isNullOrEmpty()) {
                conn = SpringUtil.context.environment.getProperty("spring.datasource.hikari.jdbc-url")
            }

            if (conn.isNullOrEmpty()) {
                return@lazy "Mysql"
            }

            if (conn.startsWith("jdbc:mysql://")) {
                return@lazy "Mysql"
            }

            if (conn.startsWith("jdbc:sqlserver://")) {
                return@lazy "Mssql"
            }
            if (conn.startsWith("jdbc:oracle:")) {
                return@lazy "Oracle"
            }
            if (conn.startsWith("jdbc:postgresql://")) {
                return@lazy "Postgre"
            }
            return@lazy "Mysql"
        }
        return@lazy ""
    }

    val maxHttpPostSize: DataSize by lazy {
        return@lazy DataSize.parse(
                SpringUtil.context.environment.getProperty("server.servlet.max-http-post-size")
                        ?: SpringUtil.context.environment.getProperty("server.tomcat.max-http-post-size") ?: "2MB")
    }

    val redisHost: String by lazy {
        return@lazy SpringUtil.context.environment.getProperty("spring.redis.host");
    }

    val redisTaskSize: Int by lazy {
        return@lazy SpringUtil.context.environment.getProperty("app.redis.task.size").AsInt(1024)
    }

    val redisTaskDelay: Int by lazy {
        return@lazy SpringUtil.context.environment.getProperty("app.redis.task.delay").AsInt(15)
    }

    val wxAppId: String by lazy {
        return@lazy SpringUtil.context.environment.getProperty("app.wx.appId") ?: ""
    }

    val wxMchId: String by lazy {
        return@lazy SpringUtil.context.environment.getProperty("app.wx.mchId") ?: ""
    }
}