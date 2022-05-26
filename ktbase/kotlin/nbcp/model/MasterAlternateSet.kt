package nbcp.model

import java.util.*
import java.util.function.Consumer
import kotlin.concurrent.thread

/**
 * 主备两个存储对象。可以想像是两个蓄水池。流出池，蓄水池。
 * 当 masterOpen 时，masterMap 可读,可清空，但不允许写入. 只允许写入alternateMap
 *
 *
 */
open class DualPoolSet<T>() : DualPoolData<MutableSet<T>>(mutableSetOf<T>(), mutableSetOf<T>()) {


    fun isEmpty(): Boolean {
        return this.masterPool.isEmpty() && this.alternatePool.isEmpty()
    }

    override fun consumePool(pool: MutableSet<T>) {
        if (consumer == null) return;

        while (true) {
            if (pool.any() == false) {
                break;
            }

            var item = pool.elementAt(0);
            consumer!!.invoke(item);
            pool.remove(item);
        }
    }


    private var consumer: ((T) -> Unit)? = null
    fun consumer(consumer: (T) -> Unit): DualPoolSet<T> {
        this.consumer = consumer;
        return this;
    }

    /**
     * 清除所有存储空间的指定key
     */
    fun removeAllItem(key: T) {
        masterPool.remove(key);
        alternatePool.remove(key);
    }

}