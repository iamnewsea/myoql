package nbcp.bean

import nbcp.comm.ForEachExt
import nbcp.comm.Important
import nbcp.comm.usingScope
import nbcp.db.*
import nbcp.db.mongo.*
import nbcp.db.mongo.event.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component
import java.io.Serializable

@Component
@ConditionalOnClass(MongoTemplate::class)
class FlywayInitCollector : BeanPostProcessor {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)

        //需要删 除后放入垃圾箱的实体
        @JvmStatic
        val flyways = mutableListOf<IFlywayInit>()  //mongo entity class
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        if (bean is IFlywayInit) {
            flyways.add(bean)
        }

        return super.postProcessAfterInitialization(bean, beanName)
    }

    /**
     * 同步版本
     */
    fun syncVersionWork() {
        var dbMaxVersion = db.mor_base.flywayVersion.query()
            .orderByDesc { it.version }
            .toEntity()
            .run {
                if (this == null) {
                    return@run 0;
                }

                return@run this.version;
            }

        flyways.filter { it.version > dbMaxVersion }
            .sortedBy { it.version }
            .forEach {
                var err_msg = "";

                try {
                    it.init();
                } catch (e: Exception) {
                    err_msg = e.message ?: "异常!";
                    throw e;
                } finally {
                    if (err_msg.isEmpty()) {
                        logger.Important("执行FlywayInit成功! version: ${it.version}, ${it::class.java.name}")
                    } else {
                        logger.Important("执行FlywayInit失败！version: ${it.version}, ${it::class.java.name}, ${err_msg}")
                    }
                }
            }
    }
}