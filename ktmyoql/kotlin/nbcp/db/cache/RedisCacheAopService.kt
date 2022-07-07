package nbcp.db.cache

import nbcp.comm.*
import nbcp.db.db
import nbcp.utils.CodeUtil
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
        val signature = joinPoint.signature as MethodSignature;
        var method = signature.method
        val key = listOf(
            config.applicationName,
            signature.declaringType.name + "." + method.name
        )
            .filter { it.HasValue }
            .joinToString(":")

        val now = LocalDateTime.now();
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
        var setted = false;

        try {
            //如果存在,则查看时间
            var v = db.rer_base.taskLock(key).get();
            if (v.HasValue && v.AsLocalDateTime()!!.plusSeconds(cacheTime.AsLong()) > now) {

                logger.Important("Redis锁 ${key} 被占用,跳过任务")
                return null;
            }

            db.rer_base.taskLock(key).deleteKey()

            setted = db.rer_base.taskLock(key).setIfAbsent(now.toNumberString(), cacheTime);
        } catch (e: Exception) {
            logger.error(e.message, e);
            return null;
        }

        if (setted == false) {
            logger.Important("未能获取到锁 ${key}")
            return null;
        }


        try {
            return joinPoint.proceed(joinPoint.args)
        } catch (e: Exception) {
            logger.error(e.message, e);
            return null;
        }
    }
}
