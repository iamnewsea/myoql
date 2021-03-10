@file:JvmName("MyMvcHelper")
@file:JvmMultifileClass

package nbcp.web

import nbcp.utils.SpringUtil
import org.springframework.boot.web.context.WebServerApplicationContext
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext
import org.springframework.context.ApplicationContext

/**
 * Created by yuxh on 2018/7/5
 */

//val ApplicationContext.debugServerInfo: String
//    get() {
//        var list = mutableListOf<String>()
//        if (this is WebServerApplicationContext) {
//            var port = this.environment.getProperty("server.port")
//            list.add("${this.webServer.javaClass.simpleName}:${port}")
//        }
//        var applicationName = this.environment.getProperty("spring.application.name")
//        var version = this.environment.activeProfiles.joinToString(",")
//        var server = SpringUtil.containsBean(NacosDiscoveryAutoConfiguration::class.java)
//        var config = SpringUtil.containsBean(NacosConfigAutoConfiguration::class.java)
//
//
//        if (server) {
//            list.add(
//                "nacos: ${this.environment.getProperty("spring.cloud.nacos.discovery.server-addr")}(${
//                    this.environment.getProperty(
//                        "spring.cloud.nacos.discovery.namespace"
//                    )
//                })"
//            )
//        }
//        else{
//            list.add("nacos:none")
//        }
//
//        if (config) {
//            list.add("nacos-config:enabled")
//        }
//        else{
//            list.add("nacos-config:none")
//        }
//
//        list.add("${applicationName}:${version}")
//
//        return list.joinToString(" -- ");
//    }
