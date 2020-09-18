@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.comm

import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext
import org.springframework.context.ApplicationContext
import java.util.HashMap
import java.util.concurrent.locks.ReentrantLock


val lockMaps: HashMap<String, ReentrantLock> = hashMapOf()

/**
 * 使用方法：请在方法的类型上使用Lock
 * return this::function::class.java.Lock {
 *      doSomething();
 *      returnValue;
 * };
 * 在	Kotlin	中有一个约定,如果函数的最后一个参数是一个函数,并且你传递一个	lambda	表达式作为相应的参数,你可以在圆括号之外指定它:
 */
inline fun <R> Class<out Any>.Lock(body: () -> R): R {
    var key = this.name;
    if (lockMaps.containsKey(key) == false) {
        lockMaps.put(key, ReentrantLock())
    }

    var lock = lockMaps.get(key)!!;
    lock.lock();
    try {
        return body();
    } finally {
        lock.unlock();
    }
}
