//package nbcp.web
//
//import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import io.undertow.UndertowOptions
//import org.springframework.boot.web.embedded.undertow.UndertowBuilderCustomizer
//
//
///**
// * Created by yuxh on 2018/6/26
// */
//
//
//@Configuration
//open class UndertowHttp2Config {
//    fun http2(): UndertowServletWebServerFactory {
//        val factory = UndertowServletWebServerFactory()
//        factory.addBuilderCustomizers(UndertowBuilderCustomizer { builder ->
//            builder.setServerOption(UndertowOptions.ENABLE_HTTP2, true)
//                    .setServerOption(UndertowOptions.HTTP2_SETTINGS_ENABLE_PUSH, true)
//        })
//
//        return factory
//    }
//}