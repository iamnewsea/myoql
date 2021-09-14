//package nbcp.db.cache
//
//import nbcp.db.*
//import org.slf4j.LoggerFactory
//import org.springframework.boot.context.properties.ConfigurationProperties
//import org.springframework.stereotype.Service
//import nbcp.comm.*
//import nbcp.utils.*
//import nbcp.db.CacheKey
//import nbcp.db.CacheKeyTypeEnum
//import javax.annotation.PostConstruct
//
///**
// * Created by yuxh on 2018/7/18
// */
//
///**
// * Redis处理缓存组件
// */
//@Service("redisCache")
//@ConfigurationProperties(prefix = "sql-orm")
//open class DataCache4SqlServiceImpl : DataCache4SqlService {
//    companion object {
//        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
//        private val rer_base = db.rer_base
//    }
//
//    var cacheDefine: LinkedHashMap<String, String> = linkedMapOf()
//
//    //这个字段从 spring.redis.host 中判断
//    val enable: Boolean by lazy {
//        return@lazy config.redisHost.HasValue
//    }
//
//    override fun isEnable(): Boolean {
//        return enable;
//    }
//
//    @PostConstruct
//    fun init() {
//        cacheDefine.keys.forEach { key ->
//            var value = cacheDefine[key]
//            if (value == null || value!!.isEmpty()) {
//                cacheDefine.remove(key)
//                return@forEach
//            }
//
//            if (value.endsWith("s")) {
//                cacheDefine.set(key, value.Slice(0, -1))
//            } else if (value.endsWith("m")) {
//                cacheDefine.set(key, (value.Slice(0, -1).AsInt() * 60).toString())
//            } else if (value.endsWith("h")) {
//                cacheDefine.set(key, (value.Slice(0, -1).AsInt() * 3600).toString())
//            } else if (value.endsWith("d")) {
//                cacheDefine.set(key, (value.Slice(0, -1).AsInt() * 86400).toString())
//            }
//
//            value = cacheDefine[key]
//            if (value!!.isEmpty()) {
//                cacheDefine.remove(key)
//                return@forEach
//            }
//        }
//    }
//
//    override fun getCacheSeconds(tableName: String?): Int {
//        if (tableName == null || tableName.isEmpty()) return 0;
//        return this.cacheDefine.getOrDefault(tableName, "").AsInt()
//    }
//
//    override fun getCacheJson(cacheKey: CacheKey): String {
//        if (isEnable() == false) return ""
//        if (cacheKey.key == CacheKeyTypeEnum.None) return ""
//
//        if (getCacheSeconds(cacheKey.tableName) <= 0) return ""
//
//        //如果正在删除依赖表中的任何一个,都不要再返回了.
//        if (cacheKey.dependencies.any { rer_base.sqlCache.brokeKeys(it).size("") > 0 }) {
//            return "";
//        }
//        var ret = rer_base.sqlCache.cacheSqlData.get(cacheKey.getExpression());
//
//        if (ret.isNotEmpty()) {
//            logger.info("命中缓存数据: ${cacheKey}")
//        }
//
//        return ret;
//    }
//
//    override fun setCacheJson(cacheKey: CacheKey, cacheJson: String) {
//        if (isEnable() == false) return;
//        if (cacheKey.key == CacheKeyTypeEnum.None) return
//        var cacheSeconds = getCacheSeconds(cacheKey.tableName)
//        if (cacheSeconds <= 0) return
//
//        //如果正在删除依赖表中的任何一个,都不要再添加了.
//        var brokingTable = rer_base.sqlCache.brokingTable.get()
//
//        if (cacheKey.dependencies.any { brokingTable == it }) {
//            return;
//        }
//
//        rer_base.sqlCache.cacheSqlData.set(cacheKey.getExpression(), cacheJson, cacheSeconds)
//    }
//
//    override fun brokeCache(tableName: String, keys: Set<String>) {
//        rer_base.sqlCache.brokeKeys(tableName).add("", *keys.toTypedArray())
//        rer_base.sqlCache.borkeKeysChangedVersion.increment("")
//    }
//
//    override fun clear(tableName: String) {
//        if (tableName.isEmpty()) {
//            return;
//        }
//
//        var set = hashSetOf<String>()
//        set.add("uk*-${tableName}-*")
//        set.add("rk*-${tableName}-*")
//        set.add("urk*-${tableName}-*")
//        set.add("sql*-${tableName}-*")
//
//        brokeCache(tableName, set);
//    }
//}