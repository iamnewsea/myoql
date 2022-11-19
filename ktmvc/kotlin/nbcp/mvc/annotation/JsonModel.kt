package nbcp.mvc.annotation

import java.lang.annotation.Inherited

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