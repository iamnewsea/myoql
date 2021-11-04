package nbcp

import nbcp.base.service.*
import nbcp.config.MySwaggerConfig
import nbcp.base.filter.MyAllFilter
import nbcp.base.filter.MyOqlCrossFilter
import org.springframework.boot.context.event.ApplicationPreparedEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.event.EventListener

@Configuration
@Import(
    value = [
        MySwaggerConfig::class,
        MyOqlCrossFilter::class,
        MyAllFilter::class,
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