package nbcp.enable

import nbcp.app.GroupLogIntercepter
import nbcp.utils.SpringUtil
import org.springframework.context.annotation.Import
import java.lang.annotation.Inherited

@Inherited
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Import(value = [GroupLogIntercepter::class])
annotation class EnableMyOqlGroupLog {
}