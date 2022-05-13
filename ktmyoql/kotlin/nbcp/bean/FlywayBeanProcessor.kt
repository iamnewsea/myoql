package nbcp.bean

import nbcp.comm.AsInt
import nbcp.comm.Important
import nbcp.db.*
import nbcp.db.mongo.*
import nbcp.db.mongo.entity.SysFlywayVersion
import nbcp.utils.SpringUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationEvent
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime


@Component
@ConditionalOnClass(MongoTemplate::class)
@ConditionalOnProperty("spring.data.mongodb.uri")
class FlywayBeanProcessor {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    /**
     * 同步版本
     */
    fun playFlyVersion(version: Int? = null) {
        if (SpringUtil.runningInTest) {
            logger.Important("单元测试环境下,跳过Flyway处理!")
            return;
        }
        var flyways = SpringUtil.context.getBeansOfType(FlywayVersionBaseService::class.java).values

        if (version != null) {
            flyways.firstOrNull { it.version == version }
                .apply {
                    if (this == null) {
                        throw  RuntimeException("找不到Flyway版本号:${version}")
                    }

                    playFlyway(this);
                }
        }


        var dbMaxVersion = db.mor_base.sysFlywayVersion.query()
            .where { it.isSuccess match true }
            .orderByDesc { it.version }
            .toEntity()
            ?.version


        //对负数版本，倒序执行，且总是执行！
        flyways.filter { it.version < 0 }
            .sortedByDescending { it.version }
            .all {
                playFlyway(it)

                return@all true;
            }


        //对正数版本，正序执行！
        flyways
            .filter { it.version >= 0 }
            .filter { it.version > (dbMaxVersion ?: -1) }
            .sortedBy { it.version }
            .all {
                playFlyway(it)

                return@all true;
            }
    }

    private fun playFlyway(it: FlywayVersionBaseService) {
        var err_msg = "";
        val ent = SysFlywayVersion();
        ent.version = it.version
        ent.execClass = it::class.java.name
        ent.startAt = LocalDateTime.now()

        try {
            it.exec();
            ent.isSuccess = true;
            ent.finishAt = LocalDateTime.now();
            db.mor_base.sysFlywayVersion.doInsert(ent);
        } catch (e: Exception) {
            ent.isSuccess = false;
            db.mor_base.sysFlywayVersion.doInsert(ent);
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
