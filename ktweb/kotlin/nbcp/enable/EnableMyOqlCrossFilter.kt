package nbcp.enable

import nbcp.filter.MyOqlCrossFilter
import org.springframework.context.annotation.Import
import java.lang.annotation.Inherited


@Inherited
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Import(value = [MyOqlCrossFilter::class])
annotation class EnableMyOqlCrossFilter {
}