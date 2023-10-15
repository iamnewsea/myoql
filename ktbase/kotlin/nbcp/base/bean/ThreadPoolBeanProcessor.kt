package nbcp.base.bean

import nbcp.base.comm.config
import nbcp.base.extend.AsInt
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Component

@Component
class ThreadPoolBeanProcessor : BeanPostProcessor {
    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        if (bean is ThreadPoolTaskExecutor) {
            config.getConfig("app.executor.core-pool-size").AsInt().apply {
                if (this > 0) {
                    bean.corePoolSize = this;
                }
            }
            config.getConfig("app.executor.max-pool-size").AsInt().apply {
                if (this > 0) {
                    bean.maxPoolSize = this;
                }
            }
            config.getConfig("app.executor.queue-capacity").AsInt().apply {
                if (this > 0) {
                    bean.setQueueCapacity(this);
                }
            }
        }
        return super.postProcessBeforeInitialization(bean, beanName)
    }
}