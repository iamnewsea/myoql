package nbcp.myoql.db.flyway

import nbcp.base.comm.config
import nbcp.base.extend.AsBoolean
import nbcp.base.extend.Important
import nbcp.base.utils.SpringUtil
import nbcp.myoql.db.db
import nbcp.myoql.db.mongo.entity.SysFlywayVersion
import nbcp.myoql.db.mongo.query
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime


@Component
@ConditionalOnClass(MongoTemplate::class)
class FlywayMongoBeanProcessor : FlywayBaseComponent() {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    /**
     * 同步版本
     */
    fun playFlyVersion(version: Int? = null) {
        if (isAllow() == false) {
            return;
        }

        if (config.getConfig("app.flyway.mongo.enable").AsBoolean(true) == false) {
            logger.Important("""
~~-~~-~~-~~  app.flyway.mongo.enable 配置为禁用,跳过执行 MongoFlyway ! ~~-~~-~~-~~
""")
            return;
        }

        if (config.getConfig("spring.data.mongodb.uri").isNullOrEmpty()) {
            logger.Important("找不到数据库连接字符串配置:spring.data.mongodb.uri,跳过执行 MongoFlyway !")
            return;
        }

        var flyways = SpringUtil.context.getBeansOfType(FlywayMongoBaseService::class.java).values

        if (version != null) {
            flyways.firstOrNull { it.version == version }
                    .apply {
                        if (this == null) {
                            throw RuntimeException("找不到Flyway版本号:${version}")
                        }

                        playFlyway(this);
                        return;
                    }
        }


        var dbMaxVersion = db.morBase.sysFlywayVersion.query()
                .where { it.isSuccess mongoEquals true }
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

    private fun playFlyway(it: FlywayMongoBaseService) {
        var err_msg = "";
        val ent = SysFlywayVersion();
        ent.version = it.version
        ent.execClass = it::class.java.name
        ent.startAt = LocalDateTime.now()

        try {
            it.exec();
            ent.isSuccess = true;
            ent.finishAt = LocalDateTime.now();
            db.morBase.sysFlywayVersion.doInsert(ent);
        } catch (e: Exception) {
            ent.isSuccess = false;
            db.morBase.sysFlywayVersion.doInsert(ent);
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