package nbcp.enable

import nbcp.db.cache.RedisCacheAopService
import nbcp.myOqlBeanProcessor.MyOqlBeanProcessorRedis
import nbcp.utils.SpringUtil
import org.springframework.context.annotation.Import
import java.lang.annotation.Inherited

@Inherited
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Import(value = [MyOqlBeanProcessorRedis::class, RedisCacheAopService::class])
annotation class EnableMyOqlRedisCache {
}