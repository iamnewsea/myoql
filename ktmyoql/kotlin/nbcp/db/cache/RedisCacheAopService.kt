package nbcp.db.cache

import nbcp.comm.*
import nbcp.db.db
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.scheduling.support.CronExpression
import org.springframework.stereotype.Component
import java.lang.reflect.Method
import java.time.LocalDateTime

/**
 * #数字表示参数名，如: #0 == userName
 * #参数值 如：#userName
 * ##methodName, ##returnType ##result
 */
@Aspect
@Component
@ConditionalOnClass(RedisTemplate::class)
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
        if (cache == null || cache.getTableName().isEmpty() || cache.cacheSeconds <= 0) {
            return joinPoint.proceed(args)
        }

        //如果有 HttpRequest,则添加Url
        var hasHttpRequest = false;
        val variableMap = JsonMap(
            method.parameters
                .filter {
                    if (it.type.AnySuperClass { it.name.startsWith("javax.servlet.") }) {
                        hasHttpRequest = true;
                        return@filter false;
                    }

                    return@filter true;
                }
                .mapIndexed { index, it -> it.name to args.get(index) }
        );

        var ext = "";

        if (cache.sql.isEmpty()) {
            ext = signature.declaringType.name + "." + method.name;

            try {
                if (hasHttpRequest) {
                    val httpContext = Class.forName("nbcp.base.mvc.HttpContext")
                    ext += getRequestParamFullUrl(httpContext.getMethod("getRequest").invoke(null));
                }
            } catch (e: Exception) {
                logger.error("在Web环境下找不到 HttpContext.request，忽略缓存中的路径", e)
            }


            if (variableMap.any()) {
                ext += ":" + variableMap.values.ToJson()
            }
        }

        return cache
            .resolveWithVariable(variableMap, ext)
            .getJson(signature.returnType, {
                return@getJson joinPoint.proceed(args)
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

//        if (cache != null && cache.table.HasValue) {
//            brokeCache(method, args, cache);
//        }

        val ret = joinPoint.proceed(args)

        if (cache != null && cache.getTableName().HasValue) {
            brokeCache(method, args, cache);
        }
        return ret;
    }

    private fun brokeCache(method: Method, args: Array<Any>, cache: BrokeRedisCache) {
        val variableMap = JsonMap(
            method.parameters
                .filter {
                    if (it.type.AnySuperClass { it.name == "javax.servlet.ServletRequest" }) {
                        return@filter false;
                    }

                    return@filter true;
                }
                .mapIndexed { index, it -> it.name to args.get(index) }
        );

        cache.resolveWithVariable(variableMap).brokeCache();
    }

    @Around("@annotation(org.springframework.scheduling.annotation.Scheduled)")
    fun oneTask(joinPoint: ProceedingJoinPoint): Any? {
        val now = LocalDateTime.now();
        val signature = joinPoint.signature as MethodSignature;
        var method = signature.method
        val key = signature.declaringType.name + "." + method.name

        var cacheTime = 0;
        var scheduled = method.getAnnotationsByType(Scheduled::class.java).first()
        if (scheduled.cron.HasValue) {
            var cornExp = CronExpression.parse(scheduled.cron)
            var timeSpan = cornExp.next(now)!! - now;
            cacheTime = timeSpan.seconds.AsInt();
        } else if (scheduled.fixedDelay > 0) {
            cacheTime = (scheduled.fixedDelay / 1000).AsInt();
        } else if (scheduled.fixedRate > 0) {
            cacheTime = (scheduled.fixedRate / 1000).AsInt();
        }

        if (cacheTime > 3) {
            cacheTime--;
        }

        var setted =
            db.rer_base.taskLock.setIfAbsent(key, LocalDateTime.now().toNumberString(), cacheTime);

        if (setted == false) {
            return null;
        }

        val ret = joinPoint.proceed(joinPoint.args)
        return ret;
    }

}
