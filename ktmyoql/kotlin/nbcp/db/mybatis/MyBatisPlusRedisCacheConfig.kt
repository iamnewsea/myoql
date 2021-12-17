package nbcp.db.mybatis

import com.baomidou.mybatisplus.core.conditions.ISqlSegment
import com.baomidou.mybatisplus.core.mapper.BaseMapper
import nbcp.comm.*
import nbcp.db.db
import nbcp.db.cache.*
import nbcp.utils.MyUtil
import org.aopalliance.intercept.MethodInterceptor
import org.aopalliance.intercept.MethodInvocation
import org.springframework.aop.Advisor
import org.springframework.aop.support.DefaultPointcutAdvisor
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.lang.reflect.Proxy
import java.time.Duration


@ConditionalOnClass(BaseMapper::class)
@Configuration
class MyBatisRedisCachePointcutAdvisor {

    private class MyBatisPlusAopForCacheForMyBatisPlusBaseMapper : MethodInterceptor {
        override fun invoke(invocation: MethodInvocation): Any? {

            var target = Proxy.getInvocationHandler(invocation.`this`!!)

            var type = MyUtil.getValueByWbsPath(target, "mapperInterface") as Class<*>

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
                    return cacheValue.getJson(cache.value.java) {
                        return@getJson invocation.proceed()
                    }
                }
            }

            var fromRedisCache = FromRedisCache(
                table = tableName,
                sql = getMethodFullName(invocation),
                cacheSeconds = cache.cacheSeconds
            );


            if (method.name.IsIn("selectBatchIds", "selectByMap", "selectList")
            ) {
                return fromRedisCache.getList(cache.value.java) {
                    return@getList invocation.proceed() as List<Any>
                }
            }

            if (method.name == "selectCount") {
                return fromRedisCache.getJson(Long::class.java) {
                    return@getJson invocation.proceed()
                }
            }

            if (method.name == "selectOne") {
                return fromRedisCache.getJson(cache.value.java) {
                    return@getJson invocation.proceed()
                }
            }


            val ret = invocation.proceed()


            //int insert(T entity);
            if (method.name == "insert" || method.name == "delete" || method.name == "update") {
                BrokeRedisCache(table = tableName).brokeCache()
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

        private fun selectById(
            invocation: MethodInvocation,
            cache: CacheForMyBatisPlusBaseMapper
        ): FromRedisCache? {
            val tableName = cache.value.simpleName!!;
            val idValue = invocation.arguments[0].AsString();
            if (idValue.isEmpty()) return null;

            return FromRedisCache(
                table = tableName,
                groupKey = "id",
                groupValue = idValue,
                sql = getMethodFullName(invocation),
                cacheSeconds = cache.cacheSeconds,
            )
        }

        private fun getMethodFullName(invocation: MethodInvocation): String {
            val method = invocation.method
            return method.declaringClass.name + "." + method.name + invocation.arguments.map {
                if (it is ISqlSegment) {
                    return@map it.getSqlSegment()
                }

                return@map it.ToJson()
            }.joinToString(",")
        }

        private fun deleteBatchIds(invocation: MethodInvocation, cache: CacheForMyBatisPlusBaseMapper) {
            val tableName = cache.value.simpleName!!;

            val deleteValue = invocation.arguments[0] as Collection<*>?;
            if (deleteValue == null) return;


            deleteValue.forEach { idValue ->
                BrokeRedisCache(table = tableName, groupKey = "id", groupValue = idValue.toString()).brokeCache()
            }
        }

        private fun deleteByMap(invocation: MethodInvocation, cache: CacheForMyBatisPlusBaseMapper) {
            val tableName = cache.value.simpleName!!;

            val deleteValue = invocation.arguments[0] as Map<*, *>?;
            if (deleteValue == null) return;

            if (cache.groupKey.HasValue) {
                val cacheKeyValue = deleteValue.get(cache.groupKey);
                if (cacheKeyValue != null) {
                    BrokeRedisCache(
                        table = tableName,
                        groupKey = cache.groupKey,
                        groupValue = cacheKeyValue.toString()
                    ).brokeCache()
                    return;
                }
            }

            val idValue = (deleteValue).get("id");
            if (idValue != null) {
                BrokeRedisCache(table = tableName, groupKey = "id", groupValue = idValue.toString()).brokeCache()
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
                idValue = MyUtil.getValueByWbsPath(deleteValue, "id").AsString()
            }


            if (idValue.HasValue) {
                BrokeRedisCache(table = tableName, groupKey = "id", groupValue = idValue).brokeCache()
            }
        }
    }

    //@ConditionalOnClass用在类上面是绝对可靠的。但只要在具有@Bean的方法上使用@ConditionalOnClass时，要注意返回值类是否与条件类相关。
    @Bean
    fun getMybatisPlusBaseMapperAdvisor(): Advisor {
        val pointcut = AnnotationMatchingPointcut(CacheForMyBatisPlusBaseMapper::class.java, true);
        return DefaultPointcutAdvisor(pointcut, MyBatisPlusAopForCacheForMyBatisPlusBaseMapper())
    }
}

