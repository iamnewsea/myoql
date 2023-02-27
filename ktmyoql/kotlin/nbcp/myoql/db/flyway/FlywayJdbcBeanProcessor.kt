package nbcp.myoql.db.flyway

import nbcp.base.comm.config
import nbcp.base.extend.AsBoolean
import nbcp.base.extend.Important
import nbcp.base.utils.SpringUtil
import nbcp.myoql.db.db
import nbcp.myoql.db.sql.component.doInsert
import nbcp.myoql.db.sql.component.query
import nbcp.myoql.db.sql.entity.s_flyway
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime


@Component
@ConditionalOnClass(JdbcTemplate::class)
class FlywayJdbcBeanProcessor : FlywayBaseComponent(){
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    /**
     * 同步版本
     */
    fun playFlyVersion(version: Int? = null) {
        if( isAllow() == false){
            return;
        }

        if (config.getConfig("app.flyway.jdbc.enable").AsBoolean(true) == false) {
            logger.Important("""
~~-~~-~~-~~  app.flyway.jdbc.enable 配置为禁用,跳过执行 JdbcFlyway! ~~-~~-~~-~~
""")
            return;
        }

        if (config.getConfig("spring.datasource.url").isNullOrEmpty()) {
            logger.Important("找不到数据库连接字符串配置:spring.datasource.url,跳过执行 JdbcFlyway!")
            return;
        }

        var flyways = SpringUtil.context.getBeansOfType(FlywayJdbcBaseService::class.java).values

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

        var dbMaxVersion = db.sqlBase.s_flyway.query()
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

    private fun playFlyway(it: FlywayJdbcBaseService) {
        var err_msg = "";
        val ent = s_flyway();
        ent.version = it.version
        ent.execClass = it::class.java.name
        ent.startAt = LocalDateTime.now()

        try {
            it.exec();
            ent.isSuccess = true;
            ent.finishAt = LocalDateTime.now();
            db.sqlBase.s_flyway.doInsert(ent);
        } catch (e: Exception) {
            ent.isSuccess = false;
            db.sqlBase.s_flyway.doInsert(ent);
            err_msg = e.message ?: "异常!";
            throw e;
        } finally {
            if (err_msg.isEmpty()) {
                logger.Important("执行 JdbcFlywayInit 成功! version: ${it.version}, ${it::class.java.name}")
            } else {
                logger.Important("执行 JdbcFlywayInit 失败！version: ${it.version}, ${it::class.java.name}, ${err_msg}")
            }
        }
    }
}
