package nbcp.db.mongo

import com.mongodb.DBCursor
import com.mongodb.client.model.DBCollectionFindOptions
import nbcp.comm.*
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import nbcp.comm.*
//import nbcp.comm.*
import nbcp.base.extend.*
import nbcp.base.line_break
import nbcp.base.utils.Md5Util
import nbcp.base.utils.MyUtil
import nbcp.db.db
import nbcp.db.mongo.*
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.query.BasicQuery
import java.lang.Exception
import java.lang.RuntimeException

/**
 * Created by Cy on 17-4-7.
 */

/**
 * MongoQuery
 */
class MongoQueryClip<M : MongoBaseEntity<E>, E : IMongoDocument>(var moerEntity: M) : MongoClipBase(moerEntity.tableName), IMongoWhereable {
    var whereData = mutableListOf<Criteria>()
    private var skip: Int = 0;
    private var take: Int = -1;
    private var sort: Document = Document()
    //    private var whereJs: String = "";
    private var selectColumns = mutableSetOf<String>();
    //    private var selectDbObjects = mutableSetOf<String>();
    private var unSelectColumns = mutableSetOf<String>()

    fun limit(skip: Int, take: Int): MongoQueryClip<M, E> {
        this.skip = skip;
        this.take = take;
        return this;
    }

    fun orderBy(sortFunc: (M) -> MongoOrderBy): MongoQueryClip<M, E> {
        var sort = sortFunc(this.moerEntity)
        var sortName = sort.orderBy.toString()
        if (sortName == "id") {
            sortName = "_id"
        } else if (sortName.endsWith(".id")) {
            sortName = sortName.slice(0..sortName.length - 3) + "._id";
        }

        this.sort.put(sortName, if (sort.Asc) 1 else -1)
        return this;
    }

    fun where(whereData: Criteria): MongoQueryClip<M, E> {
        this.whereData.add(whereData);
        return this;
    }

    fun where(whereData: List<Criteria>): MongoQueryClip<M, E> {
        this.whereData.addAll(whereData);
        return this;
    }

    fun where(where: (M) -> Criteria): MongoQueryClip<M, E> {
        this.whereData.add(where(moerEntity));
        return this;
    }

    fun whereOr(vararg wheres: (M) -> Criteria): MongoQueryClip<M, E> {
        return whereOr(*wheres.map { it(moerEntity) }.toTypedArray())
    }

    fun whereOr(vararg wheres: Criteria): MongoQueryClip<M, E> {
        if (wheres.any() == false) return this;
        var where = Criteria();
        where.orOperator(*wheres)
        this.whereData.add(where);
        return this;
    }

    fun whereIf(whereIf: Boolean, whereData: ((M) -> Criteria)): MongoQueryClip<M, E> {
        if (whereIf == false) return this;

        this.whereData.add(whereData(moerEntity));
        return this;
    }

    fun select(vararg columns: MongoColumnName): MongoQueryClip<M, E> {
        selectColumns.addAll(columns.map { it.toString() })
        return this;
    }


    fun select(vararg columns: String): MongoQueryClip<M, E> {
        selectColumns.addAll(columns)
        return this;
    }

    fun select(column: (M) -> MongoColumnName): MongoQueryClip<M, E> {
        this.selectColumns.add(column(moerEntity).toString());
        return this;
    }

    fun unSelect(column: (M) -> MongoColumnName): MongoQueryClip<M, E> {
        unSelectColumns.add(column(this.moerEntity).toString());
        return this;
    }

    companion object {
        private val logger by lazy {
            return@lazy LoggerFactory.getLogger(this::class.java)
        }
    }


    /**
     * 返回该对象的 Md5。
     */
    private fun getCacheKey(): String {
        var unKeys = mutableListOf<String>()

        unKeys.add(whereData.map { it.criteriaObject.toJson() }.joinToString("&"))
        unKeys.add(skip.toString())
        unKeys.add(take.toString())
        unKeys.add(sort.toJson())
        unKeys.add(selectColumns.joinToString(","))
        unKeys.add(unSelectColumns.joinToString(","))


        return Md5Util.getBase64Md5(unKeys.joinToString("\n"));
    }

    fun <R> toList(clazz: Class<R>, mapFunc: ((Document) -> Unit)? = null): MutableList<R> {
        db.affectRowCount = -1;
        var isString = false;
        if (clazz.IsSimpleType()) {
            isString = clazz.name == "java.lang.String";
        }
//        else if (selectColumns.any() == false) {
//            if (Map::class.java.isAssignableFrom(clazz) == false) {
//                select(*clazz.fields.map { it.name }.toTypedArray())
//            }
//        }

        var criteria = this.moerEntity.getMongoCriteria(*whereData.toTypedArray());
        var projection = Document();
        selectColumns.forEach {
            projection.put(it, 1)
        }

//        selectDbObjects.forEach {
////            it.keys.forEach { key ->
////                projection.put(key, it.get(key))
////            }
////        }

        unSelectColumns.forEach {
            projection.put(it, 0)
        }

        var query = BasicQuery(criteria.toDocument(), projection);

        if (this.skip > 0) {
            query.skip(this.skip.AsLong())
        }

        if (this.take > 0) {
            query.limit(this.take)
        }

        if (sort.any()) {
            query.sortObject = sort
        }

        var cursor = mongoTemplate.find(query, Document::class.java, this.collectionName)

//        var cacheValue = MyCache.find(this.collectionName, this.getCacheKey())
//        if (cacheValue.HasValue) {
//            return cacheValue.FromJson<MutableList<R>>()
//        }


//        var mapper = mor.util.getMongoMapper(collectionName, clazz);

        var ret = mutableListOf<R>();
        var lastKey = selectColumns.lastOrNull() ?: ""
        var error = false;
        try {
            db.change_id2Id(cursor);
            cursor.forEach {
                //            if( it.containsField("_id")){
//                it.put("id",it.get("_id").toString())
//            }

                if (isString) {
                    if (lastKey.isEmpty()) {
                        lastKey = it.keys.last()
                    }

                    ret.add(it.GetComplexPropertyValue(lastKey) as R)
                } else if (clazz.IsSimpleType()) {
                    if (lastKey.isEmpty()) {
                        lastKey = it.keys.last()
                    }

                    ret.add(it.GetComplexPropertyValue(*lastKey.split(".").toTypedArray()) as R);
                } else {
                    if (Document::class.java.isAssignableFrom(clazz)) {
                        ret.add(it as R);
                    } else {
//                    var ent = mapper.toEntity(it)
                        var ent = it.ConvertJson(clazz)
                        ret.add(ent);
                    }
                }
            }

            db.affectRowCount = cursor.size;
        } catch (e: Exception) {
            error = true;
            throw e;
        } finally {
            fun getMsgs(): String {
                var msgs = mutableListOf<String>()
                msgs.add("query:[" + this.collectionName + "] ");
                msgs.add(" where:" + criteria.criteriaObject.toJson())
                if (selectColumns.any()) {
                    msgs.add(" select:" + selectColumns.joinToString(","))
                }
                if (unSelectColumns.any()) {
                    msgs.add(" unselect:" + unSelectColumns.joinToString(","))
                }
                if (sort.any()) {
                    msgs.add(" sort:" + sort.ToJson())
                }
                if (skip > 0 || take > 0) {
                    msgs.add(" limit:${skip},${take}")
                }

                msgs.add(" result_count:" + cursor.size.toString())
                return msgs.joinToString(line_break);
            }

            logger.InfoError(error) { getMsgs() }
        }


//        if (total != null && skip == 0) {
//            if (ret.size < take) {
//                total(ret.size);
//            } else {
//                total(this.count())
//            }
//        }

        return ret
    }

    /**
     * 将忽略 skip , take
     */
    fun count(): Int {
        return mongoTemplate.count(Query.query(this.moerEntity.getMongoCriteria(*whereData.toTypedArray())), collectionName).toInt()
    }

    fun exists(): Boolean {
        return mongoTemplate.exists(Query.query(this.moerEntity.getMongoCriteria(*whereData.toTypedArray())), collectionName);
    }

    fun toList(mapFunc: ((Document) -> Unit)? = null): MutableList<E> {
        return toList(moerEntity.entityClass, mapFunc)
    }

    fun toEntity(mapFunc: ((Document) -> Unit)? = null): E? {
        this.take = 1;
        return toList(moerEntity.entityClass, mapFunc).firstOrNull();
    }

//    inline fun <reified R> toEntity(): R? {
//        return toEntity(R::class.java);
//    }

    fun <R> toEntity(clazz: Class<R>, mapFunc: ((Document) -> Unit)? = null): R? {
        this.take = 1;
        return toList(clazz, mapFunc).firstOrNull();
    }


    fun <R> toListResult(clazz: Class<R>, mapFunc: ((Document) -> Unit)? = null): ListResult<R> {
        var ret = ListResult<R>();
        ret.data = toList(clazz, mapFunc);

        if (this.skip == 0 && this.take > 0) {
            if (ret.data.size < this.take) {
                ret.total = ret.data.size;
            } else {
                ret.total = count()
            }
        }
        return ret;
    }

    fun toListResult(mapFunc: ((Document) -> Unit)? = null): ListResult<E> {
        return toListResult(this.moerEntity.entityClass, mapFunc);
    }

    fun toMapListResult(): ListResult<JsonMap> {
        return toListResult(JsonMap::class.java);
    }


    // ------------------------------
    fun unSelect(column: MongoColumnName): MongoQueryClip<M, E> {
        unSelectColumns.add(column.toString());
        return this;
    }


    fun ForEach(batchSize: Int, initSkip: Int = 0, func: (E?, Int) -> Int?) {
        var skip = initSkip;
        while (true) {
            var ents = this.limit(skip, batchSize).toList()
            var ret: Int? = null


            var len = ents.size;
            if (len == 0) {
                ret = func(null, skip)
                if (ret == null) {
                    break
                } else {
                    skip += ret
                    continue
                }
            } else {
                if (ents.ForEachExt { item, index ->
                            ret = func(item, skip + index)
                            if (ret == null) {
                                return
                            }

                            if (ret == 0) {
                                return@ForEachExt true
                            } else {
                                skip += (1 + index + ret!!)
                                return@ForEachExt false
                            }
                        }) {
                    skip += len;
                } else {
                    continue
                }
            }
        }
    }

    fun readStreamEntity(initSkip: Int = 0, batchSize: Int = 20): DbReader<E> {
        return readStreamEntity(this.moerEntity.entityClass, null, initSkip, batchSize);
    }

    /**
     * 不是一次查询出来，而是分批查。适用于大数据遍历,导出
     * 该方法会忽略 query.limit。
     * @param initSkip: 先跳过多少条。
     * @param batchSize: 每次取的条数。
     * @param func : 每条数据的回调，返回Int,表示在下一条数据的基础上跳过多少行，默认为0，可以是负数。
     * @return 执行回调的总条数
     */
    fun <R> readStreamEntity(clazz: Class<R>, mapFunc: ((Document) -> Unit)? = null, initSkip: Int = 0, batchSize: Int = 20): DbReader<R> {

        var skip = initSkip;

        var currentData = this.limit(skip, batchSize).toList(clazz, mapFunc);

        var current = 0;

        var nextFunc = abc@{
            if (currentData.any() == false) {
                return@abc null;
            }

            if (current in currentData.indices) {
                var ret = currentData[current];
                current++;
                return@abc ret;
            }

            if (currentData.size < batchSize) {
                return@abc null;
            }

            current = 0;

            skip += batchSize;

            currentData = this.limit(skip, batchSize).toList(clazz, mapFunc);
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

        return DbReader(nextFunc)
    }
}


class DbReader<T>(private val nextFunc: () -> T?) : Iterator<T> {
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


