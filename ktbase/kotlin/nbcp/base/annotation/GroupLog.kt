package nbcp.base.annotation

import java.lang.annotation.Inherited


/**
 * Created by udi on 17-3-30.
 */

//@Target(AnnotationTarget.CLASS)
//@Retention(AnnotationRetention.RUNTIME)
//annotation class AutoLoadBean()


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


/**
 * 定时任务组件注解
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class GroupLog(val value: String = "")