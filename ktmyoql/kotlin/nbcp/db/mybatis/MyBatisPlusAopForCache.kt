package nbcp.db.mybatis

import nbcp.comm.*
import nbcp.db.cache.BrokeRedisCacheData
import nbcp.db.cache.FromRedisCacheData
import nbcp.utils.MyUtil
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.stereotype.Component


@Aspect
@Component
class MyBatisPlusAopForCache {
    @Around("@within(nbcp.db.mybatis.CacheForMyBatisPlusBaseMapper)")
    fun logPoint(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature as MethodSignature;
        val method = signature.method
        val cache = method.getAnnotationsByType(CacheForMyBatisPlusBaseMapper::class.java).firstOrNull()
        if (cache == null) {
            return joinPoint.proceed(joinPoint.args);
        }

        //执行前。
        val tableName = cache.value.simpleName!!;

        if (method.name == "selectById") {
            val cacheValue = selectById(joinPoint, cache);
            if (cacheValue != null) {
                return cacheValue.usingRedisCache(cache.value.java) {
                    return@usingRedisCache joinPoint.proceed(joinPoint.args)
                }
            }
        }

        if (method.name.IsIn(
                "selectBatchIds",
                "selectByMap",
                "selectOne",
                "selectCount",
                "selectList",
                "selectMaps",
                "selectObjs",
                "selectPage",
                "selectMapsPage"
            )
        ) {
            return FromRedisCacheData(cache.cacheSeconds, tableName, arrayOf(), "", "", getMethodFullName(joinPoint))
                .usingRedisCache(cache.value.java) {
                    return@usingRedisCache joinPoint.proceed(joinPoint.args)
                }
        }


        val ret = joinPoint.proceed(joinPoint.args)


        //int insert(T entity);
        if (method.name == "insert" || method.name == "delete" || method.name == "update") {
            BrokeRedisCacheData(tableName, "", "").brokeCache()
            return ret;
        }

        if (method.name == "deleteById") {
            deleteById(joinPoint, cache)
            return ret;
        }

        if (method.name == "deleteByMap") {
            deleteByMap(joinPoint, cache)
            return ret;
        }

        if (method.name == "deleteBatchIds") {
            deleteBatchIds(joinPoint, cache)
            return ret;
        }

        if (method.name == "updateById") {
            deleteById(joinPoint, cache)
            return ret;
        }


        return ret;
    }

    private fun selectById(joinPoint: ProceedingJoinPoint, cache: CacheForMyBatisPlusBaseMapper): FromRedisCacheData? {
        val tableName = cache.value.simpleName!!;
        val idValue = joinPoint.args[0].AsString();
        if (idValue.isEmpty()) return null;

        return FromRedisCacheData(cache.cacheSeconds, tableName, arrayOf(), "id", idValue, getMethodFullName(joinPoint))
    }

    private fun getMethodFullName(joinPoint: ProceedingJoinPoint): String {
        val signature = joinPoint.signature as MethodSignature;
        val method = signature.method
        return method.declaringClass.name + "." + method.name + joinPoint.args.ToJson()
    }

    private fun deleteBatchIds(joinPoint: ProceedingJoinPoint, cache: CacheForMyBatisPlusBaseMapper) {
        val tableName = cache.value.simpleName!!;

        val deleteValue = joinPoint.args[0] as Collection<*>?;
        if (deleteValue == null) return;


        deleteValue.forEach { idValue ->
            BrokeRedisCacheData(tableName, "id", idValue.toString()).brokeCache()
        }
    }

    private fun deleteByMap(joinPoint: ProceedingJoinPoint, cache: CacheForMyBatisPlusBaseMapper) {
        val tableName = cache.value.simpleName!!;

        val deleteValue = joinPoint.args[0] as Map<*, *>?;
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

    private fun deleteById(joinPoint: ProceedingJoinPoint, cache: CacheForMyBatisPlusBaseMapper) {
        val tableName = cache.value.simpleName!!;

        val deleteValue = joinPoint.args[0];
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