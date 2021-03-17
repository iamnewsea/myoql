package nbcp.db.cache

import nbcp.comm.*
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.stereotype.Component

/**
 * #数字表示参数名，如: #0 == userName
 * #参数值 如：#userName
 * ##methodName, ##returnType ##result
 */
@Aspect
@Component
open class RedisCacheIntercepter {

    //@annotation 表示拦截方法级别上的注解。
    //@within 表示拦截类级别上的注解。

    /**
     * RedisCache , sc=sqlcache
     * key规则：6部分： sc:{主表}:{join_tables.sort().join("-")}:{主表key}-{key_value}:{sql md5}
     * 使用 scan 遍历key.
     * insert破坏: 所有 join_tabls，主表
     */
    @Around("@annotation(nbcp.db.cache.CacheForSelect)")
    fun cacheSelect(joinPoint: ProceedingJoinPoint): Any? {
        var signature = joinPoint.signature as MethodSignature;
        var method = signature.method
        var cache = method.getAnnotationsByType(CacheForSelect::class.java).firstOrNull()

        var args = joinPoint.args
        if (cache == null) {
            return joinPoint.proceed(args)
        }


        var values = signature.parameterNames;
        var argMap = JsonMap();
        args.forEachIndexed { index, key ->
            argMap.put(key.toString(), values[index])
        }

        return joinPoint.proceed(args)
    }
}
