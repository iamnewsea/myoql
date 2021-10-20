package nbcp.db.mybatis

import nbcp.comm.*
import nbcp.db.cache.BrokeRedisCacheData
import nbcp.db.cache.FromRedisCacheData
import nbcp.utils.MyUtil
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.aop.Advisor
import org.springframework.aop.support.DefaultPointcutAdvisor
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.lang.reflect.Proxy

@Component
class MyBatisCache {
    @Bean
    fun get(): Advisor {
        var pointcut = AnnotationMatchingPointcut(CacheForMyBatisPlusBaseMapper::class.java, true);
        return DefaultPointcutAdvisor(pointcut, MyBatisPlusAopForCache())
    }
}

class MyBatisPlusAopForCache : MethodInterceptor {
    override fun invoke(invocation: MethodInvocation): Any? {

        var target = Proxy.getInvocationHandler(invocation.`this`!!)

        var type = MyUtil.getPrivatePropertyValue(target, "mapperInterface") as Class<*>

        val cache = type.getAnnotationsByType(CacheForMyBatisPlusBaseMapper::class.java).firstOrNull()
        if (cache == null) {
            return invocation.proceed();
        }

        val method = invocation.method
        //执行前。
        val tableName = cache.value.simpleName!!;

        if (method.name == "selectById") {
            val cacheValue = selectById(invocation, cache);
            if (cacheValue != null) {
                return cacheValue.usingRedisCache(cache.value.java) {
                    return@usingRedisCache invocation.proceed()
                }
            }
        }


        if (method.name.IsIn("selectBatchIds", "selectByMap", "selectList")
        ) {
            return FromRedisCacheData(
                tableName,
                arrayOf(),
                "",
                "",
                getMethodFullName(invocation),
                cacheSeconds = cache.cacheSeconds
            )
                .usingRedisCacheForList(cache.value.java) {
                    return@usingRedisCacheForList invocation.proceed() as List<*>
                }
        }

        if (method.name == "selectCount") {
            return FromRedisCacheData(
                tableName,
                arrayOf(),
                "",
                "",
                getMethodFullName(invocation),
                cacheSeconds = cache.cacheSeconds
            )
                .usingRedisCache(Long::class.java) {
                    return@usingRedisCache invocation.proceed()
                }
        }

        if (method.name == "selectOne") {
            return FromRedisCacheData(
                tableName,
                arrayOf(),
                "",
                "",
                getMethodFullName(invocation),
                cacheSeconds = cache.cacheSeconds
            )
                .usingRedisCache(cache.value.java) {
                    return@usingRedisCache invocation.proceed()
                }
        }


        val ret = invocation.proceed()


        //int insert(T entity);
        if (method.name == "insert" || method.name == "delete" || method.name == "update") {
            BrokeRedisCacheData(tableName, "", "").brokeCache()
            return ret;
        }

        if (method.name == "deleteById") {
            deleteById(invocation, cache)
            return ret;
        }

        if (method.name == "deleteByMap") {
            deleteByMap(invocation, cache)
            return ret;
        }

        if (method.name == "deleteBatchIds") {
            deleteBatchIds(invocation, cache)
            return ret;
        }

        if (method.name == "updateById") {
            deleteById(invocation, cache)
            return ret;
        }


        return ret;
    }

    private fun selectById(invocation: MethodInvocation, cache: CacheForMyBatisPlusBaseMapper): FromRedisCacheData? {
        val tableName = cache.value.simpleName!!;
        val idValue = invocation.arguments[0].AsString();
        if (idValue.isEmpty()) return null;

        return FromRedisCacheData(
            tableName,
            arrayOf(),
            "id",
            idValue,
            getMethodFullName(invocation),
            cacheSeconds = cache.cacheSeconds,
        )
    }

    private fun getMethodFullName(invocation: MethodInvocation): String {
        val method = invocation.method
        return method.declaringClass.name + "." + method.name + invocation.arguments.ToJson()
    }

    private fun deleteBatchIds(invocation: MethodInvocation, cache: CacheForMyBatisPlusBaseMapper) {
        val tableName = cache.value.simpleName!!;

        val deleteValue = invocation.arguments[0] as Collection<*>?;
        if (deleteValue == null) return;


        deleteValue.forEach { idValue ->
            BrokeRedisCacheData(tableName, "id", idValue.toString()).brokeCache()
        }
    }

    private fun deleteByMap(invocation: MethodInvocation, cache: CacheForMyBatisPlusBaseMapper) {
        val tableName = cache.value.simpleName!!;

        val deleteValue = invocation.arguments[0] as Map<*, *>?;
        if (deleteValue == null) return;

        if (cache.groupKey.HasValue) {
            val cacheKeyValue = deleteValue.get(cache.groupKey);
            if (cacheKeyValue != null) {
                BrokeRedisCacheData(tableName, cache.groupKey, cacheKeyValue.toString()).brokeCache()
                return;
            }
        }

        val idValue = (deleteValue).get("id");
        if (idValue != null) {
            BrokeRedisCacheData(tableName, "id", idValue.toString()).brokeCache()
        }
    }

    private fun deleteById(invocation: MethodInvocation, cache: CacheForMyBatisPlusBaseMapper) {
        val tableName = cache.value.simpleName!!;

        val deleteValue = invocation.arguments[0];
        if (deleteValue == null) return;

        val deleteValueType = deleteValue::class.java
        var idValue = ""
        if (deleteValueType.IsSimpleType()) {
            idValue = deleteValue.toString()
        } else {
            idValue = MyUtil.getPrivatePropertyValue(deleteValue, "id").AsString()
        }


        if (idValue.HasValue) {
            BrokeRedisCacheData(tableName, "id", idValue).brokeCache()
        }
    }
}