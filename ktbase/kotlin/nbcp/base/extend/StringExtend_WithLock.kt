@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.base.extend

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock


class NumberCountLock : ReentrantLock() {
    var count: AtomicInteger = AtomicInteger();
}

val lockMaps: HashMap<String, NumberCountLock> = hashMapOf()

/**
 * 使用方法：
 * return "file:c:\a.txt".withLock(300) {
 *      doSomething();
 *      returnValue;
 * };
 */
inline fun <R> String.withLock(seconds: Long, body: () -> R): R {
    var key = this;
    var lock = lockMaps.getOrPut(key, { NumberCountLock() });
    lock.count.incrementAndGet();
    lock.tryLock(seconds, TimeUnit.SECONDS);
    try {
        return body();
    } finally {
        if (lock.count.decrementAndGet() <= 0) {
            lockMaps.remove(key);
        }
        lock.unlock();
    }
}

