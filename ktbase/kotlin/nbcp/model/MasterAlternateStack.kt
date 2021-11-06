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
open class MasterAlternateStack<T>(private var consumer: Consumer<T>) {
    private var masterOpen = false;
    private val masterStack = Stack<T>()
    private val alternateStack = Stack<T>()

    fun isEmpty(): Boolean {
        return this.masterStack.isEmpty() && this.alternateStack.isEmpty()
    }

    fun consumeTask() {
        val consumerStack: (Stack<T>) -> Unit = {
            val len = it.count();
            for (i in 1..len) {
                consumer.accept(it.pop())
            }
        }


        consumerStack(getIdleStack());
        masterOpen = !masterOpen;
        consumerStack(getIdleStack());
    }


    fun getWorkingStack(): Stack<T> {
        if (masterOpen) return masterStack;
        else return alternateStack;
    }


    fun getIdleStack(): Stack<T> {
        if (masterOpen) return alternateStack;
        else return masterStack;
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
    fun removeAll(key: T) {
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