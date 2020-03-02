package nbcp.comm


/**
 * 仅允许指定角色的用户访问。
 * 注解式权限控制到 Controller！足矣。
 * 精细化到Action的权限控制，应该动态配置。
 * 原则：用户的角色只要满足注解的角色或数据库角色就允许，例：
 *
 * user 的角色是 op_user
 * 如果Controller注解角色是 op_user,则所有的Action都能访问.
 * 如果Controller注解角色不是 op_user，则根据数据库动态化配置判断Action是否可访问
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RoleAction(vararg val roleNames: String)