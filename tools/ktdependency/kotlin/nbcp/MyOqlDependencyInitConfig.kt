package nbcp

import nbcp.utils.ClassUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.*
import org.springframework.context.event.EventListener
import org.springframework.data.mongodb.core.MongoTemplate

@Configuration
class MyOqlDependencyInitConfig : BeanPostProcessor {
    companion object {
        private var inited = false;
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)

        private var existsRedis = false;
        private var existsMysql = false;
        private var existsRabbitMq = false;
        private var existsMongo = false;
        private var existsEs = false;
        private var existsFeign = false;
        private var existsMinio = false;
    }

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        if (inited == false) {
            inited = true;

            init_app();
        }

        return super.postProcessBeforeInitialization(bean, beanName)
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {

        if (existsMongo) {
            if (bean is MongoTemplate) {
                println("MongoTemplate")
            }
        }

        return super.postProcessAfterInitialization(bean, beanName)
    }

    private fun init_app() {

        existsRedis = ClassUtil.exists("org.springframework.data.redis.core.RedisTemplate")
        existsMongo = ClassUtil.exists("org.springframework.data.mongodb.core.MongoTemplate")
        existsMysql = ClassUtil.exists("org.springframework.jdbc.core.JdbcTemplate")
        existsRabbitMq = ClassUtil.exists("org.springframework.amqp.rabbit.core.RabbitTemplate")
        existsEs = ClassUtil.exists("org.elasticsearch.client.RestClientBuilder")
        existsFeign = ClassUtil.exists("org.springframework.cloud.openfeign.FeignClient")
        existsMinio = ClassUtil.exists("io.minio.MinioClient")

    }

    @EventListener
    fun prepared(ev: ApplicationReadyEvent) {

    }
}
