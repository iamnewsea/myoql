package nbcp.base.model

/**
 * 主备两个存储对象。可以想像是两个逻辑蓄水池: 进水池, 流出池
 * 进水时:
 *   masterOpen ===> masterPool
 *              else alternatePool
 *
 * 流出时:
 *   masterOpen ===> alternatePool
 *              else masterPool
 *
 */

abstract class DualPoolData<T>(protected val masterPool: T, protected val alternatePool: T) {

    protected var masterOpen = false;
    val inputPool: T
        get() {
            if (masterOpen) return masterPool;
            else return alternatePool;
        }


    fun shift() {
        masterOpen = !masterOpen;
    }

    val outputPool : T
        get() {
            if (masterOpen) return alternatePool;
            else return masterPool;
        }

    abstract fun consumePool(pool: T);


    fun consumeTask() {
        consumePool(outputPool);
        this.shift()
        consumePool(outputPool);
        this.shift()
    }
}


open class DualPoolMap<K, V>() : DualPoolData<MutableMap<K, V>>(mutableMapOf<K, V>(), mutableMapOf<K, V>()) {
    fun isEmpty(): Boolean {
        return this.masterPool.isEmpty() && this.alternatePool.isEmpty()
    }

    override fun consumePool(pool: MutableMap<K, V>) {
        if (consumer == null) return;

        pool.keys.forEach { key ->
            val value = pool.get(key);
            if (value != null) {
                consumer!!.invoke(key, value)
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