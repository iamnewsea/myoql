package nbcp.model

/**
 * 主备两个存储对象。可以想像是两个蓄水池。流出池，蓄水池。
 * 当 masterOpen 时，masterMap 可读,可清空，但不允许写入. 只允许写入alternateMap
 *
 *
 */

abstract class DualPoolData<T>(protected val masterPool: T, protected val alternatePool: T) {
    protected var masterOpen = false;
    val openPool: T
        get() {
            if (masterOpen) return masterPool;
            else return alternatePool;
        }


    fun shift() {
        masterOpen = !masterOpen;
    }

    protected fun getCloseMap(): T {
        if (masterOpen) return alternatePool;
        else return masterPool;
    }

    abstract fun consumePool(pool: T);


    fun consumeTask() {
        consumePool(getCloseMap());
        this.shift()
        consumePool(getCloseMap());
    }
}


open class DualPoolMap<K, V>() : DualPoolData<MutableMap<K, V>>(mutableMapOf<K, V>(), mutableMapOf<K, V>()) {
    fun isEmpty(): Boolean {
        return this.masterPool.isEmpty() && this.alternatePool.isEmpty()
    }

    override fun consumePool(pool: MutableMap<K, V>) {
        if (consumer == null) return;

        var len = pool.count();
        for (i in 1..len) {
            var key = pool.entries.elementAt(0) as K;
            var value = pool.get(key);
            if (value != null) {
                consumer!!.invoke(key, pool.get(key)!!)
            }
            pool.remove(key);
        }
    }


    private var consumer: ((K, V) -> Unit)? = null
    fun consumer(consumer: (K, V) -> Unit): DualPoolMap<K, V> {
        this.consumer = consumer;
        return this;
    }

    /**
     * 清除所有存储空间的指定key
     */
    fun removeAllItem(key: K) {
        masterPool.remove(key);
        alternatePool.remove(key);
    }
}