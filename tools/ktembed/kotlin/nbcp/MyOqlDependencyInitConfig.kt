package nbcp

import nbcp.comm.AsString
import nbcp.comm.HasValue
import nbcp.comm.IsSimpleType
import nbcp.comm.ifTrue
import nbcp.utils.ClassUtil
import nbcp.utils.MyUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.*
import org.springframework.context.event.EventListener
import java.lang.reflect.Proxy

@Configuration
class MyOqlDependencyInitConfig : BeanPostProcessor {
    companion object {
        private var inited = false;
        private var dependency = DependencyData();
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    class DependencyData(
        var components: MutableList<BaseComponentEnum> = mutableListOf(),
        var services: MutableList<String> = mutableListOf()
    )

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        if (inited == false) {
            inited = true;

            init_app();
        }
        return super.postProcessBeforeInitialization(bean, beanName)
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        //收集Feign
        if (dependency.components.contains(BaseComponentEnum.Feign)) {
            var beanType = bean::class.java;
            var feign =
                beanType.annotations.firstOrNull { it.annotationClass.qualifiedName == "org.springframework.cloud.openfeign.FeignClient" }

            if (feign != null) {
                var url = MyUtil.getValueByWbsPath(feign, "url").AsString();
                if (url.HasValue) {
                    dependency.services.add(url)
                } else {
                    var value = MyUtil.getValueByWbsPath(feign, "value").AsString();
                    if (value.HasValue) {
                        dependency.services.add(value)
                    }
                }
            }
        }

        return super.postProcessAfterInitialization(bean, beanName)
    }

    private fun init_app() {
        dependency = getDependencys();
    }


    @EventListener
    fun prepared(ev: ApplicationReadyEvent) {

    }

    /**
     * 保存
     */
    private fun push(dependency: DependencyData) {

    }

    /**
     * 收集依赖
     */
    private fun getDependencys(): DependencyData {
        var dependency = DependencyData();

        ClassUtil.exists("org.springframework.data.redis.core.RedisTemplate").ifTrue {
            dependency.components.add(BaseComponentEnum.Redis)
        }

        ClassUtil.exists("org.springframework.data.mongodb.core.MongoTemplate").ifTrue {
            dependency.components.add(BaseComponentEnum.Mongo)
        }

        ClassUtil.exists("org.springframework.jdbc.core.JdbcTemplate").ifTrue {
            dependency.components.add(BaseComponentEnum.Jdbc)


        }
        ClassUtil.exists("org.springframework.amqp.rabbit.core.RabbitTemplate").ifTrue {
            dependency.components.add(BaseComponentEnum.Rabbitmq)
        }

        ClassUtil.exists("org.elasticsearch.client.RestClientBuilder").ifTrue {
            dependency.components.add(BaseComponentEnum.Es)
        }

        ClassUtil.exists("org.springframework.cloud.openfeign.FeignClient").ifTrue {
            dependency.components.add(BaseComponentEnum.Feign)
        }

        ClassUtil.exists("io.minio.MinioClient").ifTrue {
            dependency.components.add(BaseComponentEnum.Minio)
        }


        return dependency;
    }
}
