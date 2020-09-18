@file:JvmName("MyWebHelper")
@file:JvmMultifileClass

package nbcp.web

import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext
import org.springframework.context.ApplicationContext

/**
 * Created by yuxh on 2018/7/5
 */

val ApplicationContext.debugServerInfo: String
    get() {
        var list = mutableListOf<String>()
        if (this is ServletWebServerApplicationContext) {
            var port = this.environment.getProperty("server.port")
            list.add("${this.servletContext!!.serverInfo}:${port}")
        }
        var applicationName = this.environment.getProperty("spring.application.name")
        var version = this.environment.activeProfiles.joinToString(",")
        var cloudVersion = this.environment.getProperty("spring.cloud.config.profile") ?: ""


        if (cloudVersion.isNotEmpty()) {
            version += "-cloud-" + cloudVersion
        }

        list.add("${applicationName}:${version}")

        return list.joinToString(" -- ");
    }
