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


class BatchReader<T> private constructor(
    private val startIndex: Int = 0,
    private val batchSize: Int = 50,
    private val producer: (Int, Int) -> List<T>
) : Iterable<T> {
    override fun iterator(): Iterator<T> {
        return BatchReaderIterator(startIndex, batchSize, producer);
    }

    companion object {
        @JvmStatic
        fun <T> init(batchSize: Int = 20, producer: (Int, Int) -> List<T>): BatchReader<T> {
            return BatchReader.init(0, batchSize, producer);
        }

        @JvmStatic
        fun <T> init(producer: (Int, Int) -> List<T>): BatchReader<T> {
            return BatchReader.init(0, 20, producer);
        }

        /** 流式批量读取，适用于分批遍历数据库的场景
         * @param startIndex:开始位置
         * @param batchSize:批量数
         * @param producer:生产者，参数是 startIndex + 偏移,batchSize，如果生产者返回空列表，则遍历完成。
         *
         */
        @JvmName("init1")
        @JvmStatic
        fun <T> init(
            startIndex: Int = 0,
            batchSize: Int = 20,
            producer: (Int, Int) -> List<T>
        ): BatchReader<T> {
            return BatchReader(startIndex, batchSize, producer)
        }
    }


    class BatchReaderIterator<T> constructor(
        private val startIndex: Int = 0,
        private val batchSize: Int = 50,
        private val producer: (Int, Int) -> List<T>
    ) : Iterator<T> {
        companion object {
            /*
        以下操作会产生Bug！ 查询条件 被破坏！应该把查询条件去除！
BatchReader.init{ skip,take -> mor.table1.limit(skip,take).where{ it.status match 0 }.toList() }
            .forEach{  row ->
                mor.table1.updateById(row.id).set{ it.status to  1}.exec();
            }
         */
        }

        private var nextEntity: T? = null

        var skip = 0
            get
            private set;

        private var doFetch = true;
        private var over = false;

        private var currentBatch = listOf<T>();

        var fetchCount = 0
            get
            private set;

        var total = 0
            get
            private set;

        var currentIndex = 0
            get
            private set;

        private fun doNextFunc(): T? {
            if (currentIndex in currentBatch.indices) {
                var ret = currentBatch[currentIndex];
                currentIndex++;
                return ret;
            }


            if (!over) {
                currentIndex = 0;
                skip += batchSize;

                currentBatch = producer(skip, batchSize);
                fetchCount++;
                total += currentBatch.size;

                if (currentBatch.size < batchSize) {
                    over = true;
                }


                if (currentIndex in currentBatch.indices) {
                    var ret = currentBatch[currentIndex];
                    currentIndex++;
                    return ret;
                }
            }

            return null;
        }


        init {
            skip = startIndex - batchSize;
            nextEntity = doNextFunc();
        }

        override fun hasNext(): Boolean {
            return nextEntity != null
        }

        override fun next(): T {
            var nextValue = nextEntity
            if (nextValue == null) {
                throw RuntimeException("null")
            }

            this.nextEntity = doNextFunc();
            return nextValue;
        }

        fun firstOrNull(predicate: (T) -> Boolean): T? {
            for (element in this) if (predicate(element)) return element
            return null
        }
    }
}