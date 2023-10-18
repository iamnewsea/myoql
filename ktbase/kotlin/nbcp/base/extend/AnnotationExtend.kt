@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.base.extend

import java.lang.reflect.Method
import java.lang.reflect.Modifier


/**
 * 通过反射获取 memberValues。 避免Jdk17 添加 --add-opens java.base/sun.reflect.annotation=ALL-UNNAMED
 */
fun Annotation.getMemberValues(): Map<Method,Any?> {
    return this.annotationClass.java.declaredMethods
        .filter { it.parameters.size == 0 }
        .filter { Modifier.isAbstract(it.modifiers) }
        .map {
            it to it.invoke(this)
        }
        .toMap()
}