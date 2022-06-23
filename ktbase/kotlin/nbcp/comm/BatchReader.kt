package nbcp.comm

import java.util.function.Supplier

/**
 * 流式批量读取，适用于分批遍历数据库的场景
 * @sample 如下：
 *
 *         var reader = BatchReader.init(20, { skip, take ->
 *             mor.base.sysCity.query().limit(skip, take).toList()
 *         });
 *
 *         while (reader.hasNext()) {
 *             var current= reader.next();
 *
 *         }
 */
class BatchReader<T> private constructor(
    private val startIndex: Int = 0,
    private val batchSize: Int = 50,
    private val producer: (Int, Int) -> List<T>
) : Iterator<T> {
    companion object {
        @JvmStatic
        fun <T> init(batchSize: Int = 20, producer: (Int, Int) -> List<T>): BatchReader<T> {
            return init(0, batchSize, producer);
        }

        @JvmStatic
        fun <T> init(producer: (Int, Int) -> List<T>): BatchReader<T> {
            return init(0, 20, producer);
        }

        /*
        以下操作会产生Bug！ 查询条件 被破坏！应该把查询条件去除！
BatchReader.init{ skip,take -> mor.table1.limit(skip,take).where{ it.status match 0 }.toList() }
            .forEach{  row ->
                mor.table1.updateById(row.id).set{ it.status to  1}.exec();
            }
         */
        /** 流式批量读取，适用于分批遍历数据库的场景
         * @param startIndex:开始位置
         * @param batchSize:批量数
         * @param producer:生产者，参数是 startIndex + 偏移,batchSize，如果生产者返回空列表，则遍历完成。
         *
         */
        @JvmStatic
        fun <T> init(startIndex: Int = 0, batchSize: Int = 20, producer: (Int, Int) -> List<T>): BatchReader<T> {
            return BatchReader(startIndex, batchSize, producer)
        }
    }

    private var nextEntity: T? = null

    var skip = 0
        get
        private set;

    private var doFetch = true;
    private var over = false;

    private var currentData = listOf<T>();

    var fetchCount = 0
        get
        private set;

    var total = 0
        get
        private set;

    var current = 0
        get
        private set;

    private fun doNextFunc(): T? {
        if (doFetch && !over) {
            current = 0;
            skip += batchSize;

            currentData = producer(skip, batchSize);
            if (currentData.any() == false) {
                over = true;
                return null;
            }

            doFetch = false;
            fetchCount++;
            total += currentData.size;
        }

        if (current in currentData.indices) {
            var ret = currentData[current];
            current++;
            return ret;
        }

        return null;
    }

    private fun nextFunc(): T? {
        if (over) {
            return null;
        }
        
        var ret = doNextFunc()
        if (ret != null) {
            return ret;
        }

        if (over) {
            return null;
        }

        doFetch = true;
        return doNextFunc()
    }

    init {
        skip = startIndex - batchSize;
        nextEntity = nextFunc();
    }

    override fun hasNext(): Boolean {
        return nextEntity != null
    }

    override fun next(): T {
        var nextValue = nextEntity
        if (nextValue == null) {
            throw RuntimeException("null")
        }

        this.nextEntity = nextFunc();
        return nextValue;
    }

    fun firstOrNull(predicate: (T) -> Boolean):T?{
        for (element in this) if (predicate(element)) return element
        return null
    }
}