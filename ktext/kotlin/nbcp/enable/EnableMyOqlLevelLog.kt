package nbcp.enable

import nbcp.comm.LogLevelIntercepter
import nbcp.utils.SpringUtil
import org.springframework.context.annotation.Import
import java.lang.annotation.Inherited

@Inherited
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Import(value = [SpringUtil::class, LogLevelIntercepter::class])
annotation class EnableMyOqlLevelLog {
}