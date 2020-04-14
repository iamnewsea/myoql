package nbcp.comm

import org.springframework.stereotype.Component
import java.lang.annotation.ElementType
import java.lang.annotation.Inherited
import java.security.KeyPair

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


/**
 * 必传字段，用于Mvc请求参数的注解，标记了该注解，表示该参数不能为空字符串，不能为空值。
 */
@java.lang.annotation.Target(ElementType.FIELD, ElementType.PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
//@Repeatable
annotation class Require(val value: String = "")

/**
 * 忽略字段
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Ignore(val value: String = "")


@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Defines(val values: Array<Define>)

/**
 * 字段定义，用于 Es实体 生成 Mapping
 */
@java.lang.annotation.Repeatable(Defines::class)
@Repeatable
@Component
@Inherited
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Define(val value: String, val key: String = "")

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
