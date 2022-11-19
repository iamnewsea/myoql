package nbcp.mvc.annotation

import java.lang.annotation.Inherited

/**
 * 不需要权限的Action
 * 定义配置类，继承 HandlerInterceptorAdapter，判断是否有注解。
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class OpenAction