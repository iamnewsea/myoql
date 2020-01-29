package nbcp.db.mongo

import com.mongodb.DBCursor
import com.mongodb.DBObject
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
import nbcp.db.db
import nbcp.db.mongo.*
import org.slf4j.LoggerFactory
import java.lang.RuntimeException

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
    private var skip: Int = 0;
    private var take: Int = -1;

    /**
     * 通用函数
     */
    fun addPipeLine(key: PipeLineEnum, json: JsonMap): MongoAggregateClip<M, E> {
        this.pipeLines.add("\$${key}" to json);
        return this;
    }

    fun addPipeLine(key: PipeLineEnum, json: String): MongoAggregateClip<M, E> {
        this.pipeLines.add("\$${key}" to json);
        return this;
    }

    fun skip(skip: Int): MongoAggregateClip<M, E> {
        this.skip = skip;
        this.pipeLines.add("\$skip" to skip);
        return this;
    }

    fun take(take: Int): MongoAggregateClip<M, E> {
        this.take = take;
        this.pipeLines.add("\$limit" to take);
        return this;
    }

    fun limit(skip: Int, take: Int): MongoAggregateClip<M, E> {
        return this.skip(skip).take(take)
    }

    fun wheres(vararg whereDatas: Criteria): MongoAggregateClip<M, E> {
        if (whereDatas.any() == false) return this;
        pipeLines.add("\$match" to this.moerEntity.getMongoCriteria(*whereDatas))
        return this;
    }

    fun wheres(vararg wheres: (M) -> Criteria): MongoAggregateClip<M, E> {
        return wheres(*wheres.map { it(moerEntity) }.toTypedArray());
    }

    /**开始收集where条件
     */
    fun beginMatch(): BeginMatchClip<M, E> {
        return BeginMatchClip<M, E>(this)
    }

    fun select(vararg columns: String): MongoAggregateClip<M, E> {
        pipeLines.add("\$project" to columns.map { it to "\$${it}" }.toMap());
        return this;
    }

    fun select(column: (M) -> MongoColumnName): MongoAggregateClip<M, E> {
        return select(column(moerEntity).toString());
    }

    fun count(columnName: String): MongoAggregateClip<M, E> {
        pipeLines.add("\$count" to columnName)
        return this;
    }

    fun unset(vararg columns: String): MongoAggregateClip<M, E> {
        pipeLines.add("\$unset" to columns)
        return this;
    }

    /**
     * @param _id: 如果要设置列，前面加$.
     * @param eachItems: 每一个聚合的表达式。
     * @see MongoExpression
     * .group("$level",db.mongo..)
     */
    fun group(_id: String, eachItems: JsonMap): MongoAggregateClip<M, E> {
        var raw = JsonMap();
        raw.put("_id", _id)
        raw.putAll(eachItems)


        pipeLines.add("\$group" to raw)
        return this;
    }

    fun group(_id: JsonMap?, eachItems: JsonMap? = null): MongoAggregateClip<M, E> {
        var raw = JsonMap();
        raw.put("_id", _id)

        if (eachItems != null) {
            raw.putAll(eachItems)
        }

        pipeLines.add("\$group" to raw)
        return this;
    }

    /**
     * @param sortFuncs: true:正序， false,逆序。
     */
    fun orderBy(vararg sortFuncs: Pair<String, Boolean>): MongoAggregateClip<M, E> {
        var sorts = sortFuncs.map {
            var sortName = it.first
            if (sortName == "id") {
                sortName = "_id"
            } else if (sortName.endsWith(".id")) {
                sortName = sortName.slice(0..sortName.length - 3) + "._id";
            }

            return@map sortName to (if (it.second) 1 else -1)
        }

        pipeLines.add("\$sort" to JsonMap(sorts.toList()))
        return this;
    }

//    fun orderBy(vararg sortFuncs: (M) -> MongoOrderBy): MongoAggregateClip<M, E> {
//        return orderBy(*sortFuncs.map {
//            var order = it(moerEntity);
//            return@map order.orderBy.toString() to order.Asc
//        }.toTypedArray());
//    }

    fun toExpression(): String {
        var pipeLines = mutableListOf<Pair<String, Any>>();
        pipeLines.addAll(this.pipeLines);

        var pipeLineExpression = "[" + pipeLines.map {
            var key = it.first;
            var value = it.second;
            if (value is Criteria) {
                return@map """{$key:${value.criteriaObject.ToJsonWithNull()}}"""
            } else if (value is Number) {
                return@map "{$key:$value}";
            } else if (value is String) {
//                if( key == "_id" || key.endsWith("._id")){
//                    return@map """{$key:{##oid:"${value}"}}""".replace("##","$")
//                }
                return@map """{$key:"${value}"}"""
            } else if (value is Map<*, *>) {
                return@map "{$key:${value.ToJsonWithNull()}}"
            }

            logger.warn("不识别的类型：${value::class.java.name}")
            return@map "{$key:${value.ToJsonWithNull()}}"
        }.joinToString(",") + "]"

        var exp = """{
aggregate: "${this.moerEntity.tableName}",
pipeline: ${pipeLineExpression} ,
cursor: {} } """
        return exp;
    }

    /**
     * 返回该对象的 Md5。
     */
    private fun getCacheKey(): String {
        var exp = toExpression();
        return Md5Util.getBase64Md5(exp);
    }

    fun toList(itemFunc: ((Document) -> Unit)? = null): MutableList<E> {
        return toList(this.moerEntity.entityClass, itemFunc);
    }

    fun <R : Any> toList(clazz: Class<R>, itemFunc: ((Document) -> Unit)? = null): MutableList<R> {
        return toMapList(itemFunc).map { it.ConvertJson(clazz) }.toMutableList()
    }

    /**
     * 核心函数
     */
    fun toMapList(itemFunc: ((Document) -> Unit)? = null): MutableList<Document> {
        db.affectRowCount = -1;
        var queryJson = toExpression();
        var result: Document? = null
        try {
            result = mongoTemplate.executeCommand(queryJson)
        } catch (e: Exception) {
            throw e;
        } finally {
            logger.InfoError(result == null) { queryJson + " result:" + (result?.ToJson() ?: "") }
        }

        if (result == null) {
            throw RuntimeException("mongo aggregate执行错误!")
        }
        if (result.containsKey("ok") == false) {
            throw RuntimeException("mongo aggregate执行错误!" + result.ToJson())
        }

        var ret = mutableListOf<Document>()
        if (result.getDouble("ok") != 1.0) {
            db.affectRowCount = result.getDouble("ok").AsInt()
            return ret
        }

        var list = ((result.get("cursor") as Document).get("firstBatch") as ArrayList<Document>);

        db.affectRowCount = list.size;

        list.forEach {
            //            db.change_id2Id(it);
            //value 可能会是： Document{{answerRole=Patriarch}}
            db.procResultDocumentJsonData(it);
            if (itemFunc != null) {
                itemFunc(it);
            }
            ret.add(it)
        }

        return ret
    }


    fun toMap(itemFunc: ((Document) -> Unit)? = null): Document {
        this.take(1);
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

    fun toListResult(itemFunc: ((Document) -> Unit)? = null): ListResult<E> {
        return toListResult(moerEntity.entityClass, itemFunc);
    }

    fun <R : Any> toListResult(entityClass: Class<R>, itemFunc: ((Document) -> Unit)? = null): ListResult<R> {
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
        this.take(1)
        return toList(moerEntity.entityClass).firstOrNull();
    }

    inline fun <reified R : Any> toEntity(): R? {
        return toEntity(R::class.java);
    }

    fun <R : Any> toEntity(clazz: Class<R>, itemFunc: ((Document) -> Unit)? = null): R? {
        this.take(1);
        return toList(clazz, itemFunc).firstOrNull();
    }
}


class BeginMatchClip<M : MongoBaseEntity<E>, E : IMongoDocument>(var aggregate: MongoAggregateClip<M, E>) {
    private var wheres = mutableListOf<Criteria>()
    fun where(where: (M) -> Criteria): BeginMatchClip<M, E> {
        wheres.add(where(this.aggregate.moerEntity))
        return this;
    }

    fun endMatch(): MongoAggregateClip<M, E> {
        return this.aggregate.wheres(*this.wheres.toTypedArray())
    }
}