package nbcp.bean

import org.elasticsearch.client.RestClientBuilder
import org.elasticsearch.client.RestHighLevelClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientProperties
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
@ConditionalOnClass(RestClientBuilder::class)
class MyOqlEsBeanConfig : BeanPostProcessor {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        var ret = super.postProcessAfterInitialization(bean, beanName)

        //解决 长时间空闲，连接不上的错误
        if (bean is RestClientBuilder) {
            bean.setHttpClientConfigCallback {
                it.setKeepAliveStrategy { response, context ->
                    return@setKeepAliveStrategy TimeUnit.MINUTES.toMillis(3)
                }
            }
        }
//        else if (bean is ElasticsearchRestClientProperties) {
//        }

        return ret;
    }
}