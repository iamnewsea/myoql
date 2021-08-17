@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.comm

import nbcp.data.TokenStorageTypeEnum
import nbcp.utils.CodeUtil
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
    @JvmStatic
    val debug: Boolean
        get() {
            if (_debug != null) {
                return _debug!!;
            }

            if (SpringUtil.isInited == false) return false;
            _debug = getConfig("debug").AsBoolean();
            return _debug ?: false;
        }

    @JvmStatic
    fun getConfig(key: String, defaultValue: String): String {
        return SpringUtil.context.environment.getProperty(key) ?: defaultValue
    }
    @JvmStatic
    fun getConfig(key: String): String? {
        return SpringUtil.context.environment.getProperty(key)
    }

    /**
     * 指定 ${app}.log 是否包含全部GroupLog日志。
     */
    @JvmStatic
    val defAllScopeLog: Boolean by lazy {
        return@lazy getConfig("app.def-all-scope-log").AsBoolean()
    }

    /**
     * 上传到本地时使用该配置,最后不带 "/"
     */
//    val uploadHost: String by lazy {
//        return@lazy getConfig("app.upload.host")
//            .must { it.HasValue }
//            .elseThrow { "必须指定 app.upload.host" }
//    }

    /**
     * 上传到本地时使用该配置,最后不带 "/"
     */
//    val uploadPath: String by lazy {
//        return@lazy getConfig("app.upload.path")
//            .must { it.HasValue }
//            .elseThrow { "必须指定 app.upload.path" }
//    }
    @JvmStatic
    val mybatisPackage: String by lazy {
        return@lazy getConfig("app.mybatis.package", "")
    }
    @JvmStatic
    val listResultWithCount: Boolean by lazy {
        return@lazy getConfig("app.list-result-with-count", "").AsBoolean()
    }

    /**
     * 由于 SameSite 限制，避免使用 Cookie，定义一个额外值来保持会话。
     * 如果设置为空，则使用 set-cookie方式， 用于以下两个地方：
     * header["token"]
     * redis: admin:token
     */
    @JvmStatic
    val tokenKey: String by lazy {
        return@lazy getConfig("app.token-key") ?: "token"
    }

    /**
     * 配置 redis 存储方式 。
     * 如果没有指定，且没有配置 RedisAutoConfiguration，使用 Nemory。
     * 否则使用 Redis
     */
    @JvmStatic
    val tokenStorage: TokenStorageTypeEnum by lazy {
        var configValue = getConfig("app.token-storage").AsStringWithNull()?.ToEnum<TokenStorageTypeEnum>();
        if (configValue != null) {
            return@lazy configValue
        }

        if (redisHost.HasValue &&
            SpringUtil.context.containsBean("org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration")
        ) {
            return@lazy TokenStorageTypeEnum.Redis
        }

        return@lazy TokenStorageTypeEnum.Memory
    }

    /**
     * token 缓存时间，默认四个小时
     */
    @JvmStatic
    val tokenCacheSeconds: Int by lazy {
        return@lazy Duration.parse(
            getConfig("app.token-cache-seconds").AsString("PT4H")
        ).seconds.toInt()
    }

    /**
     * 强制 token 过期时间。单位是秒,默认是7天，从第一次登录，连续使用7天后，强制过期。
     */
    @JvmStatic
    val tokenKeyExpireSeconds: Int by lazy {
        var ret = Duration.parse(getConfig("app.token-key-expire").AsString("P7D"));
        if (ret.seconds < tokenCacheSeconds * 3) {
            return@lazy tokenCacheSeconds * 3
        }
        return@lazy ret.seconds.toInt()
    }

    /**
     * 验证码缓存时间，默认5分钟
     */
    @JvmStatic
    val validateCodeCacheSeconds: Int by lazy {
        return@lazy Duration.parse(
            getConfig("app.validate-code-cache-seconds").AsString("PT5M")
        ).seconds.toInt()
    }

    /**
     * 映射到 DatabaseEnum 枚举上
     */
    @JvmStatic
    val databaseType: String by lazy {
        var type = getConfig("app.database-type", "")
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

            var conn = getConfig("spring.datasource.url");

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

    @JvmStatic
    val maxHttpPostSize: DataSize by lazy {
        return@lazy DataSize.parse(
            getConfig("server.servlet.max-http-post-size")
                .AsString(getConfig("server.tomcat.max-http-post-size", "")).AsString("2MB")
        )
    }

    @JvmStatic
    val redisHost: String by lazy {
        return@lazy getConfig("spring.redis.host", "");
    }

//    val redisTaskSize: Int by lazy {
//        return@lazy SpringUtil.context.environment.getProperty("app.redis.task.size").AsInt(1024)
//    }
//
//    val redisTaskDelay: Int by lazy {
//        return@lazy SpringUtil.context.environment.getProperty("app.redis.task.delay").AsInt(15)
//    }

    @JvmStatic
    val wxAppId: String by lazy {
        return@lazy getConfig("app.wx.appId")
            .must { it.HasValue }
            .elseThrow { "必须指定 app.wx.appId" }
    }

    @JvmStatic
    val wxMchId: String by lazy {
        return@lazy getConfig("app.wx.mchId")
            .must { it.HasValue }
            .elseThrow { "必须指定 app.wx.mchId" }
    }

    @JvmStatic
    val applicationName: String by lazy {
        return@lazy getConfig("spring.application.name")
            .must { it.HasValue }
            .elseThrow { "必须指定 spring.application.name" }
    }
}