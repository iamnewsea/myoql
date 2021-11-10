package nbcp.bean

import nbcp.comm.ForEachExt
import nbcp.comm.Important
import nbcp.comm.minus
import nbcp.comm.usingScope
import nbcp.db.*
import nbcp.db.mongo.*
import nbcp.db.mongo.entity.FlywayVersion
import nbcp.db.mongo.event.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component
import java.io.Serializable
import java.time.LocalDateTime

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
            .where { it.isSuccess match true }
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
            .all {
                var err_msg = "";
                val ent = FlywayVersion();
                ent.version = it.version
                ent.execClass = it::class.java.name
                ent.startAt = LocalDateTime.now()

                try {
                    it.init();
                    ent.isSuccess = true;
                    ent.finishAt = LocalDateTime.now();
                    db.mor_base.flywayVersion.doInsert(ent);
                } catch (e: Exception) {
                    ent.isSuccess = false;
                    db.mor_base.flywayVersion.doInsert(ent);
                    err_msg = e.message ?: "异常!";
                    throw e;
                } finally {
                    if (err_msg.isEmpty()) {
                        logger.Important("执行FlywayInit成功! version: ${it.version}, ${it::class.java.name}")
                    } else {
                        logger.Important("执行FlywayInit失败！version: ${it.version}, ${it::class.java.name}, ${err_msg}")
                    }
                }

                return@all true;
            }
    }
}