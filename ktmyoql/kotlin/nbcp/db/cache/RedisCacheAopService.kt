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
import java.lang.reflect.Method

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


        fun getRequestParamFullUrl(request: Any): String {
            var clazz = Class.forName("javax.servlet.http.HttpServletRequest")
            var requestClass = request::class.java
            if (clazz.isAssignableFrom(requestClass) == false) return "";

            var getRequestURI = requestClass.getMethod("getRequestURI")
            var getQueryString = requestClass.getMethod("getQueryString")
            return getRequestURI.invoke(request).toString() + "?" + getQueryString.invoke(request).AsString()
        }
    }

    //@annotation 表示拦截方法级别上的注解。
    //@within 表示拦截类级别上的注解。

    /**
     * RedisCache
     * 使用 scan 遍历key.
     * insert破坏: 所有 join_tabls，主表
     */
    @Around("@annotation(nbcp.db.cache.FromRedisCache)")
    fun cacheSelect(joinPoint: ProceedingJoinPoint): Any? {
        var signature = joinPoint.signature as MethodSignature;
        var method = signature.method
        var cache = method.getAnnotationsByType(FromRedisCache::class.java).firstOrNull()

        var args = joinPoint.args
        if (cache == null || cache.table.isEmpty() || cache.cacheSeconds <= 0) {
            return joinPoint.proceed(args)
        }


        var variables = LocalVariableTableParameterNameDiscoverer().getParameterNames(method) ?: arrayOf()
        var variableMap = JsonMap();
        for (i in variables.indices) {
            variableMap.put(variables.get(i), args.get(i))
        }

        var ext = "";
        if (cache.sql.isEmpty()) {
            ext = signature.declaringType.name;

            if (config.isInWebEnv) {
                try {
                    var httpContext = Class.forName("nbcp.web.HttpContext")
                    ext += ":" + getRequestParamFullUrl(httpContext.getMethod("getRequest").invoke(null))
                } catch (e: Exception) {
                    logger.warn("在Web环境下找不到 HttpContext.request，忽略缓存中的路径")
                    e.printStackTrace();
                }
            }

            if (variableMap.any()) {
                //排除掉 任一基类的 package 包含 javax.servlet.http 的。

                ext += ":" + variableMap.values.filter {
                    if (it == null) return@filter false;
                    if (it.javaClass.AnySuperClass { it.name.startsWith("javax.servlet.http.") }) {
                        return@filter false;
                    }

                    return@filter true;
                }.ToJson()
            }
        }

        var cacheData = FromRedisCacheData.of(cache, ext, variableMap);

        return cacheData.usingRedisCache(signature.returnType, {
            return@usingRedisCache joinPoint.proceed(args)
        });
    }


    /**
     * MasterAlternateMap
     */
    @Around("@annotation(nbcp.db.cache.BrokeRedisCache)")
    fun cacheBroke(joinPoint: ProceedingJoinPoint): Any? {
        var signature = joinPoint.signature as MethodSignature;
        var method = signature.method
        var cache = method.getAnnotationsByType(BrokeRedisCache::class.java).firstOrNull()

        var args = joinPoint.args

        if (cache != null && cache.table.HasValue) {
            brokeCache(method, args, cache);
        }

        var ret = joinPoint.proceed(args)

        if (cache != null && cache.table.HasValue) {
            brokeCache(method, args, cache);
        }
        return ret;
    }

    private fun brokeCache(method: Method, args: Array<Any>, cache: BrokeRedisCache) {
        var variables = LocalVariableTableParameterNameDiscoverer().getParameterNames(method) ?: arrayOf()
        var variableMap = JsonMap();
        for (i in variables.indices) {
            variableMap.put(variables.get(i), args.get(i))
        }

        BrokeRedisCacheData.of(cache, variableMap).brokeCache();
    }
}
