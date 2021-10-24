package nbcp.enable

import nbcp.filter.MyAllFilter
import org.springframework.context.annotation.Import
import java.lang.annotation.Inherited


@Inherited
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Import(value = [MyAllFilter::class])
annotation class EnableMyOqlAllFilter {
}