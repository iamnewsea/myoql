package nbcp.base

import nbcp.base.annotation.*
import nbcp.base.config.TaskConfig
import nbcp.base.utils.SpringUtil
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor

@GroupLog("main")
class TestSome : TestBase() {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    @Test
    fun Test_Duration() {
        var d = SpringUtil.containsBean<ScheduledAnnotationBeanPostProcessor>();
        logger.error("III")
        logger.abcv("OK")
    }

}

inline fun Logger.abcv(msg: String) {
    this.error(msg)
}