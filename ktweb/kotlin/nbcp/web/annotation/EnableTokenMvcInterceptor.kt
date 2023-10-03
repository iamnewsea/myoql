package nbcp.web.annotation

import nbcp.web.sys.WebMvcInterceptorConfig
import org.springframework.context.annotation.Import

@Import(WebMvcInterceptorConfig::class)
annotation class EnableTokenMvcInterceptor