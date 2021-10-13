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
            val clazz = Class.forName("javax.servlet.http.HttpServletRequest")
            val requestClass = request::class.java
            if (clazz.isAssignableFrom(requestClass) == false) return "";

            val getRequestURI = requestClass.getMethod("getRequestURI")
            val getQueryString = requestClass.getMethod("getQueryString")
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
        val signature = joinPoint.signature as MethodSignature;
        val method = signature.method
        val cache = method.getAnnotationsByType(FromRedisCache::class.java).firstOrNull()

        val args = joinPoint.args
        if (cache == null || cache.table.isEmpty() || cache.cacheSeconds <= 0) {
            return joinPoint.proceed(args)
        }


        val variables = LocalVariableTableParameterNameDiscoverer().getParameterNames(method) ?: arrayOf()
        val variableMap = JsonMap();
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
                    logger.error("在Web环境下找不到 HttpContext.request，忽略缓存中的路径", e)
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

        val cacheData = FromRedisCacheData.of(cache, ext, variableMap);

        return cacheData.usingRedisCache(signature.returnType, {
            return@usingRedisCache joinPoint.proceed(args)
        });
    }


    /**
     * MasterAlternateMap
     */
    @Around("@annotation(nbcp.db.cache.BrokeRedisCache)")
    fun cacheBroke(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature as MethodSignature;
        val method = signature.method
        val cache = method.getAnnotationsByType(BrokeRedisCache::class.java).firstOrNull()

        val args = joinPoint.args

        if (cache != null && cache.table.HasValue) {
            brokeCache(method, args, cache);
        }

        val ret = joinPoint.proceed(args)

        if (cache != null && cache.table.HasValue) {
            brokeCache(method, args, cache);
        }
        return ret;
    }

    private fun brokeCache(method: Method, args: Array<Any>, cache: BrokeRedisCache) {
        val variables = LocalVariableTableParameterNameDiscoverer().getParameterNames(method) ?: arrayOf()
        val variableMap = JsonMap();
        for (i in variables.indices) {
            variableMap.put(variables.get(i), args.get(i))
        }

        BrokeRedisCacheData.of(cache, variableMap).brokeCache();
    }
}
