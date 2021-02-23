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
import java.lang.RuntimeException
import java.lang.annotation.Inherited
import java.nio.charset.Charset
import java.time.Duration

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

    /**
     * 指定 ${app}.log 是否包含全部GroupLog日志。
     */
    val defAllScopeLog: Boolean by lazy {
        return@lazy SpringUtil.context.environment.getProperty("app.def-all-scope-log")
            .AsBoolean()
    }

    /**
     * 上传到本地时使用该配置,最后不带 "/"
     */
    val uploadHost: String by lazy {
        return@lazy SpringUtil.context.environment.getProperty("app.upload.host")
            .must { it.HasValue }
            .elseThrow("必须指定 app.upload.host")
    }

    /**
     * 上传到本地时使用该配置,最后不带 "/"
     */
    val uploadPath: String by lazy {
        return@lazy SpringUtil.context.environment.getProperty("app.upload.path")
            .must { it.HasValue }
            .elseThrow("必须指定 app.upload.path")
    }

    val mybatisPackage: String by lazy {
        return@lazy SpringUtil.context.environment.getProperty("app.mybatis.package") ?: ""
    }

    /**
     * 由于 SameSite 限制，避免使用 Cookie，定义一个额外值来保持会话。
     * 如果设置为空，则使用 set-cookie方式
     */
    val tokenKey: String by lazy {
        return@lazy SpringUtil.context.environment.getProperty("app.token-key") ?: "token"
    }

    /**
     * 强制 token 过期时间。单位是秒,默认是72小时
     */
    val tokenKeyExpireSeconds: Int by lazy {
        var ret = Duration.parse(SpringUtil.context.environment.getProperty("app.token-key-expire").AsString("P3D"));
        if (ret.seconds < tokenKeyRenewalSeconds * 3) {
            return@lazy tokenKeyRenewalSeconds * 3
        }
        return@lazy ret.seconds.toInt()
    }

    /**
     * 到指定时间后(未到过期时间)，返回新的token。默认4小时。会保存到Redis里。单位是秒
     */
    val tokenKeyRenewalSeconds: Int by lazy {
        return@lazy Duration.parse(
            SpringUtil.context.environment.getProperty("app.token-key-renewal").AsString("PT4H")
        ).seconds.toInt()
    }

    /**
     * 映射到 DatabaseEnum 枚举上
     */
    val databaseType: String by lazy {
        var type = SpringUtil.context.environment.getProperty("app.database-type")
        if (type.HasValue) {
            return@lazy type;
        }

        var mongo =
            SpringUtil.context.containsBean("org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration");
        if (mongo) {
            return@lazy "Mongo"
        }

        var sql =
            SpringUtil.context.containsBean("org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration");
        if (sql) {

            var conn = SpringUtil.context.environment.getProperty("spring.datasource.url");

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

        throw RuntimeException("无法识别数据库类型,请指定 app.database-type")
    }

    val maxHttpPostSize: DataSize by lazy {
        return@lazy DataSize.parse(
            SpringUtil.context.environment.getProperty("server.servlet.max-http-post-size")
                ?: SpringUtil.context.environment.getProperty("server.tomcat.max-http-post-size") ?: "2MB"
        )
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
        return@lazy SpringUtil.context.environment.getProperty("app.wx.appId")
            .must { it.HasValue }
            .elseThrow("必须指定 app.wx.appId")
    }

    val wxMchId: String by lazy {
        return@lazy SpringUtil.context.environment.getProperty("app.wx.mchId")
            .must { it.HasValue }
            .elseThrow("必须指定 app.wx.mchId")
    }

    val applicationName: String by lazy {
        return@lazy SpringUtil.context.environment.getProperty("spring.application.name")
            .must { it.HasValue }
            .elseThrow("必须指定 spring.application.name")
    }
}