package nbcp.base.tool

import nbcp.base.TestBase
import nbcp.base.comm.StringMap
import nbcp.base.extend.AsString
import nbcp.base.extend.FromListJson
import nbcp.base.extend.formatWithJson
import nbcp.base.utils.HttpUtil
import org.junit.jupiter.api.Test


class Docker : TestBase() {
    var url = "https://registry.hub.docker.com/v1/repositories/{image}/tags"

    @Test
    fun getDockerTags() {
        var image = "openjdk"

        var http = HttpUtil(url.formatWithJson(mapOf("image" to image)))
        var list = http.doGet().FromListJson(StringMap::class.java).map { it.get("name").AsString() }

        var ret = list
            .filter { !it.contains("windows") }
            .filter { !it.contains("alpine") }
            .filter { !it.contains("buster") && !it.contains("stretch") && !it.contains("jessie") }
            .filter { it.contains("jre") }

        println("结果:")
        println("==================================")
        println(ret.sorted().sortedBy { it.length }.joinToString("\n"))
        println("==================================")
    }
}