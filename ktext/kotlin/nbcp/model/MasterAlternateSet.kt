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
open class MasterAlternateSet<T>(private var consumer: Consumer<T>) {
    private var masterOpen = false;
    private val masterSet = mutableSetOf<T>()
    private val alternateSet = mutableSetOf<T>()

    fun isEmpty(): Boolean {
        return this.masterSet.isEmpty() && this.alternateSet.isEmpty()
    }

    fun consumeTask() {
        var consumerSet: (MutableSet<T>) -> Unit = {
            var len = it.count();
            for (i in 1..len) {
                var item = it.elementAt(0);
                consumer.accept(item);
                it.remove(item);
            }
        }

        consumerSet(getIdleSet());
        masterOpen = !masterOpen;
        consumerSet(getIdleSet());
    }

    fun getWorkingSet(): MutableSet<T> {
        if (masterOpen) return masterSet;
        else return alternateSet;
    }

    fun getIdleSet(): MutableSet<T> {
        if (masterOpen) return alternateSet;
        else return masterSet;
    }

    /**
     * 写入蓄水池
     */
    fun push(value: T) {
        if (masterOpen) {
            alternateSet.add(value);
        } else {
            masterSet.add(value);
        }
    }

    /**
     * 清除所有存储空间的指定key
     */
    fun removeAll(key: String) {
        masterSet.remove(key);
        alternateSet.remove(key);
    }

    /**
     * 清空流出池
     */
    fun clearWorking() {
        if (masterOpen) {
            masterSet.clear();
        } else {
            alternateSet.clear()
        }
    }
}