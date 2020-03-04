package nbcp.comm


/**
 * 仅允许指定角色的用户访问。
 * 注解式权限控制到 Controller！足矣。
 * 使用该注解表示该接口的权限是固定的，是一定不可配置的。如涉及到管理员的操作。
 * 原则：用户的角色只要满足注解的任意一种角色即可允许访问，例：
 *
 * user 的角色是 op_user
 * 如果Controller注解角色是 admin,op_user,则所有的Action都能访问.
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RoleAction(vararg val roleNames: String)