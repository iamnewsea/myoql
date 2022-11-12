package nbcp.web.annotation

import nbcp.web.mvc.WebMvcInterceptorConfig
import org.springframework.context.annotation.Import

@Import(WebMvcInterceptorConfig::class)
annotation class EnableTokenMvcInterceptor