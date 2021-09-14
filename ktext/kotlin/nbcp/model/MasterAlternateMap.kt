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
open class MasterAlternateMap<K, V>(private var v_callback: (V, V) -> V, private var consumer: (K, V) -> Unit) {
    private var masterOpen = false;
    private val masterMap = mutableMapOf<K, V>()
    private val alternateMap = mutableMapOf<K, V>()

    fun isEmpty(): Boolean {
        return this.masterMap.isEmpty() && this.alternateMap.isEmpty()
    }

    fun consumeTask() {
        var consumerMap: (MutableMap<K, V>) -> Unit = {
            var len = it.count();
            for (i in 1..len) {
                var key = it.entries.elementAt(0);
                var value = it.get(key);
                if (value != null) {
                    consumer(key as K, it.get(key)!!)
                }
                it.remove(key);
            }
        }


        consumerMap(getIdleMap());
        masterOpen = !masterOpen;
        consumerMap(getIdleMap());
    }

    fun getWorkingMap(): MutableMap<K, V> {
        if (masterOpen) return masterMap;
        else return alternateMap;
    }

    fun getIdleMap(): MutableMap<K, V> {
        if (masterOpen) return alternateMap;
        else return masterMap;
    }

    /**
     * 写入蓄水池
     */
    fun push(key: K, value: V) {
        var map = getIdleMap();

        var oriValue = map.get(key)
        if (oriValue == null) {
            map.put(key, value);
        } else {
            map.put(key, v_callback(oriValue, value));
        }
    }

    /**
     * 清除所有存储空间的指定key
     */
    fun removeAll(key: String) {
        masterMap.remove(key);
        alternateMap.remove(key);
    }

    /**
     * 清空流出池
     */
    fun clearWorking() {
        if (masterOpen) {
            masterMap.clear();
        } else {
            alternateMap.clear()
        }
    }
}