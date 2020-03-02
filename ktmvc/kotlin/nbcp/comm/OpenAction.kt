package nbcp.comm

/**
 * Created by udi on 17-3-30.
 */

/**
 * 不需要权限的Action
 * 定义配置类，继承HandlerInterceptorAdapter，判断是否有注解。
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class OpenAction
