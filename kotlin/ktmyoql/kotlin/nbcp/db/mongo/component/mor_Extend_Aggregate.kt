package nbcp.db.mongo

import com.mongodb.BasicDBObject
import com.mongodb.DBCursor
import com.mongodb.DBObject
import com.mongodb.client.model.DBCollectionFindOptions
import nbcp.base.comm.JsonMap
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import nbcp.base.comm.ListResult
//import nbcp.base.comm.MyCache
import nbcp.base.extend.*
import nbcp.base.line_break
import nbcp.base.utils.Md5Util
import nbcp.db.db
import nbcp.db.mongo.*
import org.slf4j.LoggerFactory

/**
 * Created by Cy on 17-4-7.
 */
/** https://docs.mongodb.com/manual/reference/operator/aggregation-pipeline/ */

/**
 * MongoAggregate
 */
class MongoAggregateClip<M : MongoBaseEntity<E>, E : IMongoDocument>(var moerEntity: M) : MongoClipBase(moerEntity.tableName) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass);
    }

    private var pipeLines = mutableListOf<Pair<String, Any>>();
    private var whereData = mutableListOf<Criteria>()
    private var selectColumns = mutableSetOf<String>();
    private var skip: Int = 0;
    private var take: Int = -1;

    fun addPipeLine(key: PipeLineJsonOperatorEnum, json: JsonMap): MongoAggregateClip<M, E> {
        this.pipeLines.add("\$${key}" to json);
        return this;
    }

    fun limit(take: Int): MongoAggregateClip<M, E> {
        this.take = take;
        this.pipeLines.add("\$limit" to take);
        return this;
    }

    fun skip(skip: Int): MongoAggregateClip<M, E> {
        this.skip = skip;
        this.pipeLines.add("\$skip" to skip);
        return this;
    }

    fun limit(skip: Int, take: Int): MongoAggregateClip<M, E> {
        this.skip = skip;
        this.take = take;
        return this.skip(skip).limit(take)
    }

    fun where(whereData: Criteria): MongoAggregateClip<M, E> {
        this.whereData.add(whereData);
        return this;
    }

    fun where(where: (M) -> Criteria): MongoAggregateClip<M, E> {
        this.whereData.add(where(moerEntity));
        return this;
    }


    fun select(vararg columns: String): MongoAggregateClip<M, E> {
        selectColumns.addAll(columns)
        return this;
    }

    fun select(column: (M) -> MongoColumnName): MongoAggregateClip<M, E> {
        this.selectColumns.add(column(moerEntity).toString());
        return this;
    }


    fun toExpression(): String {
        var pipeLines = mutableListOf<Pair<String, Any>>();
        if (this.whereData.any()) {
            var criteria = db.getMongoCriteria(*whereData.toTypedArray());
            pipeLines.add("\$match" to criteria)
        }
        pipeLines.addAll(this.pipeLines);

        if (this.selectColumns.any()) {
            pipeLines.add("\$project" to this.selectColumns.map { it to "\$${it}" }.toMap());
        }

        var pipeLineExpression = "[" + pipeLines.map {
            var key = it.first;
            var value = it.second;
            if (value is Criteria) {
                return@map """{$key:${value.criteriaObject.toJson()}}"""
            } else if (value is Number) {
                return@map "{$key:$value}";
            } else if (value is String) {
                return@map """{$key:"${value}"}"""
            } else if (value is Map<*, *>) {
                return@map "{$key:${value.ToJson()}}"
            }

            println("不识别的类型：${value::class.java.name}")
            return@map "{$key:${value.ToJson()}}"
        }.joinToString(",") + "]"

        var exp = """{
aggregate: "${this.moerEntity.tableName}",
pipeline: ${pipeLineExpression} ,
cursor: {} } """

        logger.info(exp);
        return exp;
    }

    /**
     * 返回该对象的 Md5。
     */
    private fun getCacheKey(): String {
        var exp = toExpression();
        return Md5Util.getBase64Md5(exp);
    }

    fun toList(itemFunc: ((JsonMap) -> Unit)? = null): MutableList<E> {
        return toList(this.moerEntity.entityClass, itemFunc);
    }

    fun <R : Any> toList(clazz: Class<R>, itemFunc: ((JsonMap) -> Unit)? = null): MutableList<R> {
        return toMapList(itemFunc).map { it.ConvertJson(clazz) }.toMutableList()
    }

    fun toMapList(itemFunc: ((JsonMap) -> Unit)? = null): MutableList<Document> {
        var queryJson = toExpression();
        var result = mongoTemplate.executeCommand(queryJson)

        var ret = mutableListOf<Document>()
        if (result.getDouble("ok") != 1.0) {
            return ret
        }

        ((result.get("cursor") as Document).get("firstBatch") as ArrayList<Document>).forEach {
            db.change_id2Id(it);
            ret.add(it)
        }

        return ret
    }


    fun toMap(itemFunc: ((JsonMap) -> Unit)? = null): Document {
        this.limit(1);
        var ret = toMapList(itemFunc);
        if (ret.any() == false) return Document();
        return ret.first();
    }

    fun toScalar(): Any? {
        var doc = toMap();
        if (doc.keys.any() == false) return null
        return doc.get(doc.keys.last())
    }

    /**
     * 将忽略 skip , take
     */
    fun count(): Int {
        this.pipeLines.add("\$count" to "count");
        return toScalar()?.AsInt() ?: 0
    }

    fun exists(): Boolean {
        this.select("_id");
        return toScalar()?.AsString().HasValue;
    }

    fun toList(): MutableList<E> {
        return toList(moerEntity.entityClass)
    }

    fun toListResult(itemFunc: ((JsonMap) -> Unit)? = null): ListResult<E> {
        return toListResult(moerEntity.entityClass, itemFunc);
    }

    fun <R : Any> toListResult(entityClass: Class<R>, itemFunc: ((JsonMap) -> Unit)? = null): ListResult<R> {
        var ret = ListResult<R>()
        var data = toList(entityClass, itemFunc)

        if (this.skip == 0 && this.take > 0) {
            if (data.size < this.take) {
                ret.total = data.size;
            } else {
                ret.total = count()
            }
        }

        ret.data = data;
        return ret
    }

    fun toEntity(): E? {
        this.limit(1)
        return toList(moerEntity.entityClass).firstOrNull();
    }

    inline fun <reified R : Any> toEntity(): R? {
        return toEntity(R::class.java);
    }

    fun <R : Any> toEntity(clazz: Class<R>, itemFunc: ((JsonMap) -> Unit)? = null): R? {
        this.limit(1);
        return toList(clazz, itemFunc).firstOrNull();
    }
}


