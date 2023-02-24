package nbcp.myoql.db.flyway

import nbcp.base.comm.config
import nbcp.base.extend.AsBoolean
import nbcp.base.extend.Important
import nbcp.base.utils.SpringUtil
import org.slf4j.LoggerFactory

abstract class FlywayBaseComponent {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    fun isAllow(): Boolean {
        if (SpringUtil.runningInTest) {
            logger.Important("""
~~-~~-~~-~~  单元测试环境下,跳过 Flyway 执行! ~~-~~-~~-~~ 
""")
            return false;
        }

        if (config.getConfig("app.flyway.enable").AsBoolean(true) == false) {
            logger.Important("""
~~-~~-~~-~~  app.flyway.enable 配置为禁用,跳过 Flyway 执行! ~~-~~-~~-~~
""")
            return false;
        }

        return true;
    }
}