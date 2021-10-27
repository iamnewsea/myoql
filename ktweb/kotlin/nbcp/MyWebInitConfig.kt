package nbcp

import nbcp.config.MySwaggerConfig
import nbcp.filter.MyAllFilter
import nbcp.filter.MyOqlCrossFilter
import nbcp.service.*
import org.springframework.boot.context.event.ApplicationPreparedEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.event.EventListener

@Configuration
@Import(
    value = [
        MySwaggerConfig::class,
        MyAllFilter::class,
        MyOqlCrossFilter::class,
        UserAuthenticationService::class,
        UploadService::class,
        UploadFileForLocalService::class,
        UploadFileForMinioService::class,
        UploadFileForAliOssService::class
    ]
)
//@ComponentScan("nbcp.db.mongo.event")
class MyWebInitConfig {

    @EventListener
    fun prepared(ev: ApplicationPreparedEvent) {

    }
}