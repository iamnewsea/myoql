package nbcp.myoql.db.mongo


import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.query.Criteria

import nbcp.myoql.db.mongo.component.MongoAggregateBeginMatch
import org.slf4j.LoggerFactory
import java.lang.RuntimeException
import java.time.LocalDateTime
import nbcp.base.comm.*;
import nbcp.base.db.*;
import nbcp.base.enums.*;
import nbcp.base.extend.*;
import nbcp.base.utils.*;
import nbcp.myoql.db.enums.*
import nbcp.myoql.db.*;
import nbcp.myoql.db.mongo.base.MongoColumnName
import nbcp.myoql.db.mongo.component.MongoBaseMetaCollection
import nbcp.myoql.db.mongo.component.MongoClipBase
import nbcp.myoql.db.mongo.enums.PipeLineEnum
import nbcp.myoql.db.mongo.extend.procWithMongoScript
import nbcp.myoql.db.mongo.extend.toOIdJson
import nbcp.myoql.db.mongo.logger.logFind

/** https://docs.mongodb.com/manual/reference/operator/aggregation-pipeline/ */

/**
 * MongoAggregate
 */
class MongoAggregateClip<M : MongoBaseMetaCollection<E>, E : Any>(var moerEntity: M) :
    MongoClipBase(moerEntity.tableName) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass);
    }

    val pipeLines = mutableListOf<Pair<String, Any>>();
    private var skip: Int = 0;
    private var take: Int = -1;

    /**
     * 通用函数
     */
    fun addPipeLine(key: PipeLineEnum, jsonCallback: (M) -> Map<String, Any?>): MongoAggregateClip<M, E> {
        return addPipeLine(key, jsonCallback.invoke(this.moerEntity));
    }

    /**
     * 通用函数
     */
    fun addPipeLine(key: PipeLineEnum, json: Map<String, Any?>): MongoAggregateClip<M, E> {
        this.pipeLines.add("\$${key}" to json);
        return this;
    }

    /**
     * @param rawString 原始的mongo字符串，不解析
     */
    fun addPipeLineRawString(key: PipeLineEnum, rawString: String): MongoAggregateClip<M, E> {
        this.pipeLines.add("\$${key}" to MyRawString(rawString));
        return this;
    }


    /**
     * 递归返回 wbs
     */
    fun addGraphLookup(
        connectFromField: (M) -> MongoColumnName,
        connectToField: (M) -> MongoColumnName,
        alias: String = "wbs"
    ): MongoAggregateClip<M, E> {
        var jsonMap = JsonMap();

        var connectFromField2 = db.mongo.getMongoColumnName(connectFromField(this.moerEntity).toString());

        jsonMap.put("from", this.moerEntity.tableName)
        jsonMap.put("startWith", "$" + connectFromField2)
        jsonMap.put("connectFromField", connectFromField2)
        jsonMap.put("connectToField", db.mongo.getMongoColumnName(connectToField(this.moerEntity).toString()))
        jsonMap.put("as", alias)

        this.pipeLines.add("\$${PipeLineEnum.graphLookup}" to jsonMap);
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
        pipeLines.add("\$match" to db.mongo.getMergedMongoCriteria(*whereDatas))
        return this;
    }

    fun wheres(vararg wheres: (M) -> Criteria): MongoAggregateClip<M, E> {
        return wheres(*wheres.map { it(moerEntity) }.toTypedArray());
    }

    /**开始收集where条件
     */
    fun beginMatch(): MongoAggregateBeginMatch<M, E> {
        return MongoAggregateBeginMatch<M, E>(this)
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
     * @param group: 必须包含 _id
     *
     * {
     *  "列名1": { "聚合函数": "列名"} ,
     *  "列名2": { "聚合函数": "列名"} ,
     *  "列名3": { "聚合函数": "列名"}
     * }
     *
     * 其中, 列名1,列名2,列名3,必须有一个是 _id
     */
    fun group(group: (M) -> Map<String,*>): MongoAggregateClip<M, E> {
        var raw = group.invoke(this.moerEntity)
        if (raw.containsKey("_id") == false) {
            throw RuntimeException("group必须包含_id列")
        }
        pipeLines.add("\$group" to raw)
        return this;
    }

    /**
     * @param _id: 如果要设置列，前面加$.
     * @param eachItems: 每一个聚合的表达式。
     * @see MongoExpression
     */
    fun group(_id: String, eachItems: Map<String,*>): MongoAggregateClip<M, E> {
        var raw = JsonMap();
        raw.put("_id", _id)
        raw.putAll(eachItems)


        pipeLines.add("\$group" to raw)
        return this;
    }

    @JvmOverloads
    fun group(_id: Map<String,Any?>?, vararg eachItems: Map<String,*>): MongoAggregateClip<M, E> {
        var raw = JsonMap();
        raw.put("_id", _id)

        eachItems.forEach {
            raw.putAll(it)
        }

        pipeLines.add("\$group" to raw)
        return this;
    }

    /**
     * @param sortFuncs: true:正序， false,逆序。
     */
    fun orderBy(vararg sortFuncs: Pair<String, Boolean>): MongoAggregateClip<M, E> {
        var sorts = sortFuncs.map {
            var sortName = db.mongo.getMongoColumnName(it.first)

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
            if (value is ObjectId) {
                return@map """{$key:${value.toString().toOIdJson().ToJson(JsonSceneScopeEnum.Db).AsString("null")}}"""
            } else if (value is Criteria) {

                var c_value = value.criteriaObject //.procWithMongoScript();

                return@map """{$key:${c_value.toJson().AsString("null")}}"""
            } else if (value is Document) {
                return@map """{$key:${value.toJson().AsString("null")}}"""
            } else if (value is Number || value is MyRawString) {
                return@map "{$key:$value}";
            } else if (value is String) {
//                if( key == "_id" || key.endsWith("._id")){
//                    return@map """{$key:{##oid:"${value}"}}""".replace("##","$")
//                }
                return@map """{$key:"${value}"}"""
            } else if (value is Map<*, *>) {
                return@map "{$key:${value.procWithMongoScript().ToJson(JsonSceneScopeEnum.Db).AsString("null")}}"
            }

            logger.warn("不识别的类型：${value::class.java.name}")
            return@map "{$key:${value.ToJson(JsonSceneScopeEnum.Db).AsString("null")}}"
        }.joinToString(",") + "]"

        var exp = """{
aggregate: "${actualTableName}",
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


    @JvmOverloads
    fun <R : Any> toList(clazz: Class<R>, itemFunc: ((Document) -> Unit)? = null): MutableList<R> {
        return toMapList(itemFunc).map { it.ConvertJson(clazz) }.toMutableList()
    }

    /**
     * 核心函数
     */
    @JvmOverloads
    fun toMapList(itemFunc: ((Document) -> Unit)? = null): MutableList<Document> {
        db.affectRowCount = -1;
        var queryJson = toExpression();

        val settingResult = db.mongo.mongoEvents.onAggregate(this)
        if (settingResult.any { it.result.result == false }) {
            return mutableListOf();
        }

        var result: Document? = null
        var startAt = LocalDateTime.now();
        var error: Exception? = null;
        try {
            this.script = queryJson;
            result =
                getMongoTemplate(settingResult.lastOrNull { it.result.dataSource.HasValue }?.result?.dataSource).executeCommand(
                    queryJson
                )
            this.executeTime = LocalDateTime.now() - startAt

            usingScope(arrayOf(MyOqlDbScopeEnum.IgnoreAffectRow, MyOqlDbScopeEnum.IgnoreExecuteTime)) {
                settingResult.forEach {
                    it.event.aggregate(this, it.result)
                }
            }
        } catch (e: Exception) {
            error = e;
            throw e;
        } finally {
            logger.logFind(error, actualTableName, queryJson, result);
//            logger.InfoError(result == null) {
//                """[aggregate] ${this.moerEntity.tableName}
//[语句] ${queryJson}
//${if (config.debug) "[result] ${result?.ToJson()}" else "[result.size] ${result?.size}"}
//[耗时] ${db.executeTime}"""
//            }
        }

        if (result == null) {
            throw RuntimeException("mongo aggregate执行错误!")
        }
        if (result.containsKey("ok") == false) {
            throw RuntimeException("mongo aggregate执行错误!" + result.ToJson())
        }

        var ret = mutableListOf<Document>()
        if (result.getDouble("ok") != 1.0) {
            this.affectRowCount = result.getDouble("ok").AsInt()
            return ret
        }

        var list = ((result.get("cursor") as Document).get("firstBatch") as ArrayList<Document>);

        this.affectRowCount = list.size;

        list.forEach {
            //            db.change_id2Id(it);
            //value 可能会是： Document{{answerRole=Patriarch}}
            MongoDocument2EntityUtil.procDocumentJson(it);
            if (itemFunc != null) {
                itemFunc(it);
            }
            ret.add(it)
        }

        return ret
    }


    @JvmOverloads
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


    fun toListResult(itemFunc: ((Document) -> Unit)? = null): ListResult<out E> {
        return toListResult(moerEntity.entityClass, itemFunc);
    }

    @JvmOverloads
    fun <R : Any> toListResult(entityClass: Class<R>, itemFunc: ((Document) -> Unit)? = null): ListResult<R> {
        var ret = ListResult<R>()
        var data = toList(entityClass, itemFunc)

        if (config.listResultWithCount) {
            ret.total = count()
        } else if (this.skip == 0 && this.take > 0) {
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


    @JvmOverloads
    fun <R : Any> toEntity(clazz: Class<R>, itemFunc: ((Document) -> Unit)? = null): R? {
        this.take(1);
        return toList(clazz, itemFunc).firstOrNull();
    }
}


