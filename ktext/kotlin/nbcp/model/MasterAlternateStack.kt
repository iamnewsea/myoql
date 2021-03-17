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
open class MasterAlternateStack<T>(var consumer: Consumer<T>) {
    private var masterOpen = false;
    private val masterStack = Stack<T>()
    private val alternateStack = Stack<T>()
    private var sleep: Long = 1000;

    fun isEmpty(): Boolean {
        return this.masterStack.isEmpty() && this.alternateStack.isEmpty()
    }

    var thread = thread {
        var consumerStack: (Stack<T>) -> Unit = {
            var len = it.count();
            for (i in 1..len) {
                consumer.accept(it.pop())
            }
        }

        while (true) {
            Thread.sleep(sleep)
            if (isEmpty()) {
                continue;
            }

            masterOpen = !masterOpen

            if (masterOpen) {
                consumerStack(alternateStack);
                consumerStack(masterStack);
            } else {
                consumerStack(masterStack);
                consumerStack(alternateStack);
            }
        }
    }


    /**
     * 写入蓄水池
     */
    fun push(value: T) {
        if (masterOpen) {
            alternateStack.push(value);
        } else {
            masterStack.push(value);
        }
    }

    /**
     * 清除所有存储空间的指定key
     */
    fun removeAll(key: String) {
        masterStack.remove(key);
        alternateStack.remove(key);
    }

    /**
     * 清空流出池
     */
    fun clearWorking() {
        if (masterOpen) {
            masterStack.clear();
        } else {
            alternateStack.clear()
        }
    }
}