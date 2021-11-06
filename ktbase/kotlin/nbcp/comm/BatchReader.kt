package nbcp.comm

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
class BatchReader<T>(private val nextFunc: () -> T?) : Iterator<T> {
    companion object {
        @JvmStatic
        fun <T> init(batchSize: Int = 20, producer: (Int, Int) -> List<T>): BatchReader<T> {
            return init(0, batchSize, producer);
        }

        @JvmStatic
        fun <T> init(producer: (Int, Int) -> List<T>): BatchReader<T> {
            return init(0, 20, producer);
        }

        /** 流式批量读取，适用于分批遍历数据库的场景
         * @param startIndex:开始位置
         * @param batchSize:批量数
         * @param producer:生产者，参数是 startIndex + 偏移,batchSize，如果生产者返回空列表，则遍历完成。
         */
        @JvmStatic
        fun <T> init(startIndex: Int = 0, batchSize: Int = 20, producer: (Int, Int) -> List<T>): BatchReader<T> {
            var skip = startIndex - batchSize;

            var currentData = listOf<T>();
            var current = 0;

            var nextFunc = abc@{
                if (current % batchSize == 0) {
                    current = 0;
                    skip += batchSize;

                    currentData = producer(skip, batchSize);
                }

                if (currentData.any() == false) {
                    return@abc null;
                }
                if (current in currentData.indices) {
                    var ret = currentData[current];
                    current++;
                    return@abc ret;
                }

                return@abc null;
            }

            return BatchReader(nextFunc)
        }
    }

    private var nextEntity: T? = null

    init {
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
}