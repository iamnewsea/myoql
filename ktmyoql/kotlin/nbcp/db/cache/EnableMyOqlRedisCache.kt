package nbcp.db.cache

import nbcp.myOqlBeanProcessor.MyOqlBeanProcessorRedis
import nbcp.utils.SpringUtil
import org.springframework.context.annotation.Import
import java.lang.annotation.Inherited

@Inherited
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Import(value = [SpringUtil::class, MyOqlBeanProcessorRedis::class, RedisCacheAopService::class])
annotation class EnableMyOqlRedisCache {
}