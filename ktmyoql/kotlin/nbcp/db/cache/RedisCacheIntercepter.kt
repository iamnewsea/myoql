package nbcp.db.cache

import nbcp.comm.*
import nbcp.db.redis.AnyTypeRedisTemplate
import nbcp.model.MasterAlternateStack
import nbcp.utils.Md5Util
import nbcp.utils.SpringUtil
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

/**
 * #数字表示参数名，如: #0 == userName
 * #参数值 如：#userName
 * ##methodName, ##returnType ##result
 */
@Aspect
@Component
open class RedisCacheIntercepter {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)

        private val redisTemplate: AnyTypeRedisTemplate by lazy {
            return@lazy SpringUtil.getBean<AnyTypeRedisTemplate>()
        }
        private var cache = MasterAlternateStack<CacheForBroke>() {
            var pattern = "";
            if (it.key.HasValue && it.value.HasValue) {
                pattern = "sc:${it.table}:*(${it.key}-${it.value})*";
            } else {
                pattern = "sc:${it.table}:*";
            }

            for (i in 0..99) {
                var list = redisTemplate.scan(pattern);
                if (list.any() == false) {
                    break;
                }

                redisTemplate.delete(list);
            }

            pattern = "sc:*[${it.table}]*"

            for (i in 0..99) {
                var list = redisTemplate.scan(pattern);
                if (list.any() == false) {
                    break;
                }

                redisTemplate.delete(list);
            }
        }

        fun pushBrokeKey(key: CacheForBroke) {
            cache.push(key)
        }
    }

    @Autowired
    @Lazy
    lateinit var redisTemplate: StringRedisTemplate

    fun getCacheKey(cache: CacheForSelect, ext: String): String {

        var list = mutableListOf<String>();
        list.add(cache.key)
        list.add(cache.value)

        list.add(ext)
        var md5 = Md5Util.getBase64Md5(list.joinToString(line_break));

        var ret = "sc:${cache.table}:${
            cache.joinTables.toSortedSet().map { "[${it}]" }.joinToString("")
        }";

        if (cache.key.HasValue && cache.value.HasValue) {
            ret += "(${cache.key}-${cache.value})"
        }
        return "${ret}${md5}"
    }
    //@annotation 表示拦截方法级别上的注解。
    //@within 表示拦截类级别上的注解。

    /**
     * RedisCache , sc=sqlcache
     * key规则：6部分： sc:{主表}:{join_tables.sort().map("[]")}:{主表key}-{key_value}-{sql md5}
     * 使用 scan 遍历key.
     * insert破坏: 所有 join_tabls，主表
     */
    @Around("@annotation(nbcp.db.cache.CacheForSelect)")
    fun cacheSelect(joinPoint: ProceedingJoinPoint): Any? {
        var signature = joinPoint.signature as MethodSignature;
        var method = signature.method
        var cache = method.getAnnotationsByType(CacheForSelect::class.java).firstOrNull()

        var args = joinPoint.args
        if (cache == null || cache.table.isEmpty()) {
            return joinPoint.proceed(args)
        }

        var cacheKey =
            getCacheKey(cache, signature.declaringType.name + ":" + signature.parameterNames.joinToString(","))

        var cacheValue = redisTemplate.opsForValue().get(cacheKey)

        if (cacheValue.HasValue) {
            return cacheValue.FromJson(signature.returnType)
        }
        return joinPoint.proceed(args)
    }


    /**
     * MasterAlternateMap
     */
    @Around("@annotation(nbcp.db.cache.CacheForBroke)")
    fun cacheBroke(joinPoint: ProceedingJoinPoint): Any? {
        var signature = joinPoint.signature as MethodSignature;
        var method = signature.method
        var cache = method.getAnnotationsByType(CacheForBroke::class.java).firstOrNull()

        var args = joinPoint.args
        if (cache == null || cache.table.isEmpty()) {
            return joinPoint.proceed(args)
        }
        pushBrokeKey(cache)
        return joinPoint.proceed(args)
    }
}
