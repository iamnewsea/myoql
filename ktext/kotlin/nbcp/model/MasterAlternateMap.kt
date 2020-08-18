package nbcp.model

import nbcp.comm.StringKeyMap
import java.time.LocalDateTime

/**
 * 主备两个存储对象。可以想像是两个蓄水池。流出池，蓄水池。
 * 当 masterOpen 时，masterMap 可读,可清空，但不允许写入. 只允许写入alternateMap
 */
open class MasterAlternateMap<T> {
    private var masterOpen = false;
    private val masterMap = StringKeyMap<T>()
    private val alternateMap = StringKeyMap<T>()
    var switchAt = LocalDateTime.now();

    /**
     * 写入蓄水池
     */
    fun put(key: String, value: T) {
        if (masterOpen) {
            alternateMap.put(key, value);
        } else {
            masterMap.put(key, value);
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
     * 切换工作空间，返回是否是master
     */
    fun switch(): Boolean {
        //为了保险，把流出池的数据放到蓄水池
        if (masterOpen) {
            this.alternateMap.putAll(this.masterMap)
            this.masterMap.clear();
        } else {
            this.masterMap.putAll(this.alternateMap)
            this.alternateMap.clear()
        }

        switchAt = LocalDateTime.now();
        masterOpen = !masterOpen
        return masterOpen
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


    /**
     * 从流出池中取出一项，并删除之
     */
    fun pop(): Pair<String, T?>? {
        var map: StringKeyMap<T>;
        if (masterOpen) {
            map = masterMap;
        } else {
            map = alternateMap
        }

        var key = map.keys.firstOrNull();
        if (key == null) {
            return null;
        }

        var ret = map.get(key);
        map.remove(key);
        return key to ret;
    }


    /**
     * 返回 蓄水池大小
     */
    val reservoirSize: Int
        get() {
            if (masterOpen) {
                return alternateMap.size
            } else return masterMap.size
        }

}