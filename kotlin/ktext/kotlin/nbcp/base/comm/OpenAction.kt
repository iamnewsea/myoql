package nbcp.base.comm

/**
 * Created by udi on 17-3-30.
 */

/**
 * 不需要权限的Action
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class OpenAction
