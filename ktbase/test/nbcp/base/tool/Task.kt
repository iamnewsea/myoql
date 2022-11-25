package nbcp.base.tool

import nbcp.base.TestBase
import nbcp.base.comm.StringMap
import nbcp.base.extend.AsString
import nbcp.base.extend.FromListJson
import nbcp.base.extend.formatWithJson
import nbcp.base.utils.HttpUtil
import org.junit.jupiter.api.Test
import org.springframework.scheduling.annotation.Scheduled


class Task : TestBase() {
    var url = "https://registry.hub.docker.com/v1/repositories/{image}/tags"


    @Scheduled(fixedDelay = 1000)
    fun abc() {
        println("=----------------------")
    }

    @Test
    fun getDockerTags() {
        Thread.sleep(1000 * 5)
    }
}