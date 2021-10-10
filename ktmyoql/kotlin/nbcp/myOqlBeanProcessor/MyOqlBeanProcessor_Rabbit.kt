package nbcp.myOqlBeanProcessor

import nbcp.utils.SpringUtil
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.AcknowledgeMode
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.amqp.RabbitProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Component

@Component
@Import(SpringUtil::class)
@ConditionalOnClass(RabbitTemplate::class)
class MyOqlBeanProcessor_Rabbit : BeanPostProcessor {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        var ret = super.postProcessAfterInitialization(bean, beanName)

        if (bean is RabbitTemplate) {

        } else if (bean is RabbitProperties) {
            //ConfirmCallback 确认消息是否到达 Broker 服务器
            if (bean.publisherConfirmType == null) {
                bean.publisherConfirmType =
                    org.springframework.amqp.rabbit.connection.CachingConnectionFactory.ConfirmType.CORRELATED;
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