package nbcp.db.cache

import nbcp.comm.*
import nbcp.utils.SpringUtil
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.core.LocalVariableTableParameterNameDiscoverer
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

/**
 * #数字表示参数名，如: #0 == userName
 * #参数值 如：#userName
 * ##methodName, ##returnType ##result
 */
@Aspect
@Component
open class RedisCacheAopService {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    //@annotation 表示拦截方法级别上的注解。
    //@within 表示拦截类级别上的注解。

    /**
     * RedisCache
     * 使用 scan 遍历key.
     * insert破坏: 所有 join_tabls，主表
     */
    @Around("@annotation(nbcp.db.cache.CacheForSelect)")
    fun cacheSelect(joinPoint: ProceedingJoinPoint): Any? {
        var signature = joinPoint.signature as MethodSignature;
        var method = signature.method
        var cache = method.getAnnotationsByType(FromRedisCache::class.java).firstOrNull()

        var args = joinPoint.args
        if (cache == null || cache.table.isEmpty() || cache.cacheSeconds <= 0) {
            return joinPoint.proceed(args)
        }


        var variables = LocalVariableTableParameterNameDiscoverer().getParameterNames(method)
        var variableMap = JsonMap();
        for (i in variables.indices) {
            variableMap.put(variables.get(i), args.get(i))
        }

        var ext = signature.declaringType.name;
        if (signature.parameterNames.any()) {
            ext += "&" + args.joinToString(",")
        }

        var cacheData = FromRedisCacheData.of(cache, ext, variableMap);

        return cacheData.usingRedisCache(signature.returnType, {
            return@usingRedisCache joinPoint.proceed(args)
        });
    }


    /**
     * MasterAlternateMap
     */
    @Around("@annotation(nbcp.db.cache.CacheForBroke)")
    fun cacheBroke(joinPoint: ProceedingJoinPoint): Any? {
        var signature = joinPoint.signature as MethodSignature;
        var method = signature.method
        var cache = method.getAnnotationsByType(BrokeRedisCache::class.java).firstOrNull()

        var args = joinPoint.args
        var ret = joinPoint.proceed(args)
        if (cache == null || cache.table.isEmpty()) {
            return ret;
        }

        var variables = LocalVariableTableParameterNameDiscoverer().getParameterNames(method)
        var variableMap = JsonMap();
        for (i in variables.indices) {
            variableMap.put(variables.get(i), args.get(i))
        }

        BrokeRedisCacheData.of(cache, variableMap).brokeCache();

        return ret;
    }
}
