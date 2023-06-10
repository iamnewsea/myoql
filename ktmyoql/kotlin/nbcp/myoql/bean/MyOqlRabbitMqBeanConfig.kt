package nbcp.myoql.bean

import nbcp.base.utils.SpringUtil
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.AcknowledgeMode
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.amqp.RabbitProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.stereotype.Component

@Component

@ConditionalOnClass(RabbitTemplate::class)
class MyOqlRabbitMqBeanConfig : BeanPostProcessor {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        var ret = super.postProcessAfterInitialization(bean, beanName)

        if (bean is CachingConnectionFactory) {
            var properties = SpringUtil.getBean<RabbitProperties>()
            //修正bug
            bean.port = properties.port ?: 5672;

            if (properties.host != null) {
                bean.host = properties.host;
            }

            if (properties.username != null) {
                bean.username = properties.username;
            }

            if (properties.password != null) {
                bean.setPassword(properties.password)
            }

            if (properties.virtualHost != null) {
                bean.virtualHost = properties.virtualHost;
            }
        } else if (bean is RabbitProperties) {
            //ConfirmCallback 确认消息是否到达 Broker 服务器
            if (bean.publisherConfirmType == null) {
                bean.publisherConfirmType = CachingConnectionFactory.ConfirmType.CORRELATED;
            }

            //需要实现 ReturnCallback 接口
//            if (bean.isPublisherReturns == null) {
//                bean.isPublisherReturns = true;
//            }

            //手动确认
            if (bean.listener.simple.acknowledgeMode == null) {
                bean.listener.simple.acknowledgeMode = AcknowledgeMode.MANUAL;
            }
            if (bean.listener.direct.acknowledgeMode == null) {
                bean.listener.direct.acknowledgeMode = AcknowledgeMode.MANUAL;
            }
        }

        return ret;
    }

}