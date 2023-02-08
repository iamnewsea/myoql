package nbcp.base.comm


import nbcp.base.enums.AlignDirectionEnum
import nbcp.base.extend.*
import nbcp.base.utils.MyUtil
import nbcp.base.utils.StringUtil
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ApplicationListener
import org.springframework.core.env.ConfigurableEnvironment
import java.io.File

/**
 * 配置项, 不能用Component 或Bean，因为它的时机，比Spring容器还要早。
 */
class config : ApplicationListener<ApplicationEnvironmentPreparedEvent>, ApplicationContextAware {
    override fun onApplicationEvent(event: ApplicationEnvironmentPreparedEvent) {
        env = event.environment

        if (logoLoaded == false) {
            logoLoaded = true;

            val env = env!!
            val list = mutableListOf<String>()
            list.add(env.getProperty("spring.application.name").AsString())
            (env.activeProfiles?.toList() ?: listOf()).also {
                if (it.HasValue) {
                    list.add("profiles:" + env.activeProfiles?.joinToString(",").AsString())
                }
            }

            var title = list.filter { it.HasValue }
                .let {
                    if (it.any()) {
                        return@let """${list.joinToString("  ")}
""";
                    }
                    return@let "";
                }

            logger.Important(
                """
    ╔╦╗┬ ┬┌─┐┌─┐ ┬    ╔╗ ┌─┐┌─┐┌─┐    
    ║║║└┬┘│ ││─┼┐│    ╠╩╗├─┤└─┐├┤     
    ╩ ╩ ┴ └─┘└─┘└┴─┘  ╚═╝┴ ┴└─┘└─┘    
${title}
""".Slice(1, -2).WrapByRectangle(AlignDirectionEnum.CENTER)
            )
        }

        init_callbacks.forEach {
            it.invoke(env!!);
        }
    }

    companion object {
        var logoLoaded = false;
        private var contextField: ApplicationContext? = null
        private var env: ConfigurableEnvironment? = null;

        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
        private var _debug: Boolean? = null;

        @JvmStatic
        val debug: Boolean
            @JvmName("debug") get() {
                if (_debug != null) {
                    return _debug!!;
                }

                if (env == null) return false;
                _debug = getConfig("debug").AsBoolean();
                return _debug ?: false;
            }

        val init_callbacks = mutableListOf<(ConfigurableEnvironment) -> Unit>()

        @JvmStatic
        fun onInit(callback: (ConfigurableEnvironment) -> Unit) {
            init_callbacks.add(callback);
        }

        //        /**
//         * 是否在Web环境
//         */
//        @JvmStatic
//        val isInWebEnv by lazy {
//            try {
//                arrayOf(
//                    "javax.servlet.Servlet",
//                    "org.springframework.web.context.ConfigurableWebApplicationContext"
//                ).forEach {
//                    Class.forName(it)
//                }
//                return@lazy true;
//            } catch (e: Exception) {
//                return@lazy false;
//            }
//        }

        /**
         * 当前K8s名称空间
         */
        @JvmStatic
        val currentK8sNamespace by lazy {
            var file = File("/var/run/secrets/kubernetes.io/serviceaccount/namespace")
            if (file.exists() == false) {
                return@lazy "";
            }
            return@lazy file.readText();
        }

        /**
         * 当前集群，一个集群可能有多个产品线。  app.group or currentK8sNamespace
         */
        @JvmStatic
        val appGroup
            get() = getConfig("app.group").AsString { currentK8sNamespace }


        /**
         * 产品线编码
         */
        @JvmStatic
        val productLineCode
            get() = getConfig("app.product-line.code").AsString();
        //setOf(appGroup, getConfig("app.product-line.code")).filter { it.HasValue }.joinToString(".")

        /**
         * redis 前缀，隔离key, = productLineCode/appGroup/currentK8sNamespace
         */
        val appPrefix: String
            get() = productLineCode.AsString(appGroup.AsString())


        @JvmStatic
        val redisProductLineCodePrefixEnable: Boolean
            get() {
                return getConfig("app.product-line.redis-prefix-enable").AsBoolean(true)
            }


        @JvmStatic
        val videoLogo
            get() = getConfig("app.video-logo").AsBoolean();


        @JvmStatic
        private val cache: JsonMap = JsonMap();

        @JvmStatic
        fun getConfig(key: String, defaultValue: String): String {
            if (contextField == null) {
                return env?.getProperty(key) ?: defaultValue
            }


            var cacheValue = cache.get(key) as String?;
            if (cacheValue != null) return cacheValue.AsString()

            cacheValue = contextField!!.environment.getProperty(key).AsStringWithNull()
            if (cacheValue != null) {
                cache.set(key, cacheValue);
            }
            return cacheValue.AsString()
        }

        private fun doGetConfig(key: String): String? {
            if (contextField == null) {
                return env?.getProperty(key)
            }

            var cacheValue = cache.get(key) as String?;
            if (cacheValue != null) return cacheValue

            cacheValue = contextField!!.environment.getProperty(key).AsStringWithNull()
            if (cacheValue != null) {
                cache.set(key, cacheValue);
            }
            return cacheValue
        }

        @JvmStatic
        fun getConfig(key: String): String? {
            var ret = doGetConfig(key);
            if (ret != null) {
                return ret;
            }

            var key2 = key.split(".").map { StringUtil.getKebabCase(it) }.joinToString(".")
            if (key != key2) {
                ret = doGetConfig(key2);
                if (ret != null) {
                    return ret;
                }
            }

            key2 = key.split(".").map { StringUtil.getSmallCamelCase(it) }.joinToString(".")
            if (key != key2) {
                ret = doGetConfig(key2);
                if (ret != null) {
                    return ret;
                }
            }

            return ret;
        }


        /**
         * 优先获取 app.admin-token，否则获取 app.docker-image-version
         */
        @JvmStatic
        val adminToken: String
            get() = getConfig("app.admin-token").AsString { getConfig("app.docker-image-version").AsString() }


        /**
         * 指定 ${app}.log 是否包含全部GroupLog日志。
         */
        @JvmStatic
        val defAllScopeLog: Boolean
            get() = getConfig("app.def-all-scope-log").AsBoolean()


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
        val mybatisPackage: String
            get() = getConfig("app.mybatis.package", "")

        @JvmStatic
        val myoqlKeepDbName: Boolean
            get() = getConfig("app.myoql.sql.keep-db-name").AsBoolean(true)


        @JvmStatic
        val listResultWithCount: Boolean
            get() = getConfig("app.list-result-with-count", "").AsBoolean()


        @JvmStatic
        val jwtSignatureAlgorithm: String
            get() = getConfig("app.jwt-signature-algorithm") ?: "HS256"


        @JvmStatic
        val jwtSecretKey: String
            get() = getConfig("app.jwt-secret-key") ?: ""


        /**
         * 由于 SameSite 限制，避免使用 Cookie，定义一个额外值来保持会话。
         * 如果设置为空，则使用 set-cookie方式， 用于以下两个地方：
         * header["token"]
         * redis: admin:token
         */
        @JvmStatic
        val tokenKey: String
            get() = getConfig("app.token-key").AsString("token")

        /**
         * 配置 redis 存储方式 。
         * 如果没有指定，且没有配置 RedisAutoConfiguration，使用 Nemory。
         * 否则使用 Redis
         */
//    @JvmStatic
//    val tokenStorage: TokenStorageTypeEnum by lazy {
//        val configValue = getConfig("app.token-storage").AsStringWithNull()?.ToEnum<TokenStorageTypeEnum>();
//        if (configValue != null) {
//            return@lazy configValue
//        }
//
//        if (redisHost.HasValue &&
//                SpringUtil.context.containsBean("org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration")
//        ) {
//            return@lazy TokenStorageTypeEnum.REDIS
//        }
//
//        return@lazy TokenStorageTypeEnum.Memory
//    }

        /**
         * token 缓存时间，默认四个小时
         */
        @JvmStatic
        val tokenCacheSeconds: Int
            get() = getConfig("app.token-cache-seconds").AsInt(4 * 3600)


        /**
         * 验证码缓存时间，默认5分钟
         */
        @JvmStatic
        val validateCodeCacheSeconds: Int
            get() = getConfig("app.validate-code-cache-seconds").AsInt(300)


        @JvmStatic
        val redisHost: String
            get() = getConfig("spring.redis.host", "");


        @JvmStatic
        val wxAppId: String
            get() = getConfig("app.wx.appId").must { it.HasValue }.elseThrow { "必须指定 app.wx.appId" }


        @JvmStatic
        val wxMchId: String
            get() = getConfig("app.wx.mchId").must { it.HasValue }.elseThrow { "必须指定 app.wx.mchId" }


        @JvmStatic
        val applicationName: String
            get() = getConfig("spring.application.name").must { it.HasValue }
                .elseThrow { "必须指定 spring.application.name" }
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        contextField = applicationContext
    }
}