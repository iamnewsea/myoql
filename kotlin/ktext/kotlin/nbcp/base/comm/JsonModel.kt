package nbcp.base.comm

/**
 * Created by udi on 17-3-30.
 */

/**
 * 把客户Post的Json,整体映射到Model上.如:
 * 客户端Post : { id: 1 , name: "ok" }  -> 服务器接收:  info: IdName
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class JsonModel




//表示该字段必须有值。可以通过反射，遍历字段，进行验证。
//参数 Group 表示分组，用途等。可以重复使用该注解
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
//@Repeatable
annotation class Require(val group: String = "")

//@Repeatable
//@Target(AnnotationTarget.FIELD, AnnotationTarget.TYPE, AnnotationTarget.CLASS )
//@Retention(AnnotationRetention.RUNTIME)
//annotation class Setted(val settedFunc: String = "")
//
//
//@Repeatable
//@Target(AnnotationTarget.FIELD,AnnotationTarget.TYPE, AnnotationTarget.CLASS)
//@Retention(AnnotationRetention.RUNTIME)
//annotation class Setting(val settingFunc: String = "")
