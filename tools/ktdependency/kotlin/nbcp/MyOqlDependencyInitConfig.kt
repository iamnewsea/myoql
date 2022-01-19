package nbcp

import nbcp.comm.ifTrue
import nbcp.utils.ClassUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.*
import org.springframework.context.event.EventListener
import org.springframework.data.mongodb.core.MongoTemplate

@Configuration
class MyOqlDependencyInitConfig {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    @EventListener
    fun prepared(ev: ApplicationReadyEvent) {
        var list = mutableListOf<BaseComponentEnum>()

        ClassUtil.exists("org.springframework.data.redis.core.RedisTemplate").ifTrue {
            list.add(BaseComponentEnum.Redis)
        }

        ClassUtil.exists("org.springframework.data.mongodb.core.MongoTemplate").ifTrue {
            list.add(BaseComponentEnum.Mongo)
        }

        ClassUtil.exists("org.springframework.jdbc.core.JdbcTemplate").ifTrue {
            list.add(BaseComponentEnum.Jdbc)


        }
        ClassUtil.exists("org.springframework.amqp.rabbit.core.RabbitTemplate").ifTrue {
            list.add(BaseComponentEnum.Rabbitmq)
        }

        ClassUtil.exists("org.elasticsearch.client.RestClientBuilder").ifTrue {
            list.add(BaseComponentEnum.Es)
        }

        ClassUtil.exists("org.springframework.cloud.openfeign.FeignClient").ifTrue {
            list.add(BaseComponentEnum.Feign)
        }

        ClassUtil.exists("io.minio.MinioClient").ifTrue {
            list.add(BaseComponentEnum.Minio)
        }
    }
}
