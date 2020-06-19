@file:JvmName("MyWebHelper")
@file:JvmMultifileClass

package nbcp.web

import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext

/**
 * Created by yuxh on 2018/7/5
 */

val ServletWebServerApplicationContext.debugServerInfo: String
    get() {
        var serverName = this.servletContext!!.serverInfo
        var applicationName = this.environment.getProperty("spring.application.name")
        var version = this.environment.activeProfiles.joinToString(",")
        var cloudVersion = this.environment.getProperty("spring.cloud.config.profile") ?: ""
        var port = this.environment.getProperty("server.port")

        if (cloudVersion.isNotEmpty()) {
            version += "-cloud-" + cloudVersion
        }

        return "${serverName}:${port} -- ${applicationName}:${version}"
    }