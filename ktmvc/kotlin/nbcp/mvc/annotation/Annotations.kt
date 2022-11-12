package nbcp.mvc.annotation

import java.lang.annotation.Inherited

/**
 * Created by udi on 17-3-30.
 */

/**
 * 不需要权限的Action
 * 定义配置类，继承 HandlerInterceptorAdapter，判断是否有注解。
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class OpenAction

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class AdminSysOpsAction
/**
 * 把客户Post的Json,整体映射到Model上.如:
 * 客户端Post : { id: 1 , name: "ok" }  -> 服务器接收:  info: IdName
 * value 表示必填值,支持 [] 以及 . 表示式。
 */
//AnnotationTarget.TYPE 表示返回值，但是无法被反射到。
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class JsonModel()


/**
 * 仅允许指定角色的用户访问。对应 LoginUserModel.roles
 * 注解式权限控制到 Controller！足矣。
 * 使用该注解表示该接口的权限是固定的，是一定不可配置的。如涉及到管理员的操作。
 * 原则：用户的角色只要满足注解的任意一种角色即可允许访问，例：
 *
 * user 的角色是 op_user
 * 如果 Controller注解角色是 admin,op_user,表示 admin 或者 op_user 可以访问
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class RoleAction(vararg val roleNames: String)