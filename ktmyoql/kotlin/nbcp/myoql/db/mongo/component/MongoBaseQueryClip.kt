package nbcp.myoql.db.mongo

import nbcp.base.comm.*
import nbcp.base.enums.JsonStyleScopeEnum
import nbcp.base.extend.*
import nbcp.base.extend.*
import nbcp.base.extend.*
import nbcp.base.extend.*
import nbcp.base.utils.Md5Util
import nbcp.base.utils.ReflectUtil
import nbcp.myoql.annotation.FromRedisCache
import nbcp.myoql.annotation.onlyGetFromCache
import nbcp.myoql.annotation.onlySetToCache
import nbcp.myoql.db.db
import nbcp.myoql.db.enums.MyOqlDbScopeEnum
import nbcp.myoql.db.mongo.component.IMongoWhere
import nbcp.myoql.db.mongo.component.MongoClipBase
import nbcp.myoql.db.mongo.extend.toDocument
import nbcp.myoql.db.mongo.logger.logFind
import org.bson.Document
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.query.BasicQuery
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import java.time.LocalDateTime

open class MongoBaseQueryClip(tableName: String) : MongoClipBase(tableName), IMongoWhere {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }


    override val whereData = MongoWhereClip()

    var skip: Int = 0;
    var take: Int = -1;
    var sort: Document = Document()


    //    private var whereJs: String = "";
    protected var selectColumns = mutableSetOf<String>();

    /**
     * 更复杂的查询表达式
     */
    protected var selectProjections = JsonMap();

    //    private var selectDbObjects = mutableSetOf<String>();
    protected var unSelectColumns = mutableSetOf<String>()

    fun selectField(column: String) {
        this.selectColumns.add(column);
    }

    /**
     * TODO 需要将 jsCallback 转化为Mongo的函数对象！
     */
    fun where(jsCallback: String) {
        this.whereData.add(JsonMap("${'$'}where" to jsCallback));
    }


    fun whereOr(vararg wheres: Criteria) {
        if (wheres.any() == false) return;
        val where = Criteria();
        where.orOperator(*wheres)
        this.whereData.putAll(where.criteriaObject);
    }

    /**
     * 对同一个字段多个条件时使用。
     */
    fun whereAnd(vararg wheres: Criteria) {
        if (wheres.any() == false) return;
        var where = Criteria();
        where.andOperator(*wheres)
        this.whereData.putAll(where.criteriaObject);
    }

    /**
     * 返回该对象的 Md5。
     */
    private fun getCacheKey(): String {
        var unKeys = mutableListOf<String>()

        unKeys.add(whereData.map { it.ToJson() }.joinToString("&"))
        unKeys.add(skip.toString())
        unKeys.add(take.toString())
        unKeys.add(sort.ToJson())
        unKeys.add(selectColumns.joinToString(","))
        unKeys.add(selectProjections.ToJson())
        unKeys.add(unSelectColumns.joinToString(","))


        return Md5Util.getBase64Md5(unKeys.joinToString("\n"));
    }


    /**
     * 核心功能，查询列表，原始数据对象是 Document
     */
    @JvmOverloads
    fun <R> toList(type: Class<R>, mapFunc: ((Document) -> Unit)? = null): MutableList<R> {
        db.affectRowCount = -1;

        val settingResult = db.mongo.mongoEvents.onQuering(this)
        if (settingResult.any { it.result.result == false }) {
            return mutableListOf();
        }

        val criteria = db.mongo.getMergedMongoCriteria(whereData);
        val projection = Document();
        selectColumns.forEach {
            projection.put(it, 1)
        }

        selectProjections.forEach {
            projection.put(it.key, it.value)
        }

        unSelectColumns.forEach {
            projection.put(it, 0)
        }

        val query = BasicQuery(criteria.toDocument(), projection);

        if (this.skip > 0) {
            query.skip(this.skip.AsLong())
        }

        if (this.take > 0) {
            query.limit(this.take)
        }

        if (sort.any()) {
            query.sortObject = sort
        }

        val startAt = LocalDateTime.now();
        this.script = this.getQueryScript(criteria);

        var cursor: List<Document>? = null;
        val kvs = getMatchDefaultCacheIdValue();
        for (kv in kvs) {
            cursor = getFromDefaultCache(kv);
            if (cursor != null) break;
        }

        if (cursor == null) {
            cursor =
                    getMongoTemplate(settingResult.lastOrNull { it.result.dataSource.HasValue }?.result?.dataSource)
                            .find(
                                    query,
                                    Document::class.java,
                                    this.actualTableName
                            )
        }

        this.executeTime = LocalDateTime.now() - startAt

        val ret = mutableListOf<R>();
        var lastKey = selectColumns.lastOrNull() ?: selectProjections.map { it.key }.lastOrNull() ?: ""

        lastKey = db.mongo.getEntityColumnName(lastKey)


        var error: Exception? = null;
        var skipNullCount = 0;
        try {

            cursor!!.forEach { row ->
                MongoDocument2EntityUtil.procDocumentJson(row);

                if (mapFunc != null) {
                    mapFunc(row);
                }
                if (type.IsSimpleType()) {
                    if (lastKey.isEmpty()) {
                        lastKey = row.keys.last()
                    }

                    val value = ReflectUtil.getValueByWbsPath(row, *lastKey.split(".").toTypedArray());
                    if (value != null) {
                        ret.add(value.ConvertType(type) as R);
                    } else {
                        skipNullCount++;
                    }
                } else {
                    if (Document::class.java.isAssignableFrom(type)) {
                        ret.add(row as R);
                    } else {
                        val ent = row.ConvertJson(type)
                        ret.add(ent);
                    }
                }
            }


            kvs.forEach { kv ->
                FromRedisCache(
                        table = this.actualTableName,
                        groupKey = kv.keys.joinToString(","),
                        groupValue = kv.values.joinToString(","),
                        sql = "def"
                ).onlySetToCache(cursor)
            }

            this.affectRowCount = cursor.size;

            usingScope(arrayOf(MyOqlDbScopeEnum.IGNORE_AFFECT_ROW, MyOqlDbScopeEnum.IGNORE_EXECUTE_TIME)) {
                settingResult.forEach {
                    it.event.query(this, it.result)
                }
            }

        } catch (e: Exception) {
            error = e;
            throw e;
        } finally {
            fun getMsgs(): String {
                val msgs = mutableListOf<String>()
                msgs.add(this.script)

                if (config.debug) {
                    msgs.add("[result] ${cursor.ToJson()}")
                } else {
                    msgs.add("[result.size] " + cursor!!.size.toString())
                }
                if (skipNullCount > 0) {
                    msgs.add("[skipNullRows] ${skipNullCount}")
                }

                msgs.add("[耗时] ${this.executeTime}")
                return msgs.joinToString(const.line_break);
            }

            logger.logFind(error, actualTableName, ::getMsgs);
        }

        return ret
    }

    private fun getMatchDefaultCacheIdValue(): List<StringMap> {
        var def = MongoEntityCollector.sysRedisCacheDefines.get(this.defEntityName)
        if (def == null) {
            return listOf();
        }

        if (this.selectProjections.any()) return listOf();
        if (this.skip > 0) return listOf();
        if (this.take > 1) return listOf();
        if (db.mongo.hasOrClip(this.whereData)) return listOf();

        var list = mutableListOf<StringMap>();

        def.forEach { cacheColumnGroup ->
            var kv = StringMap();
            cacheColumnGroup.forEach {
                var v = this.whereData.findValueFromRootLevel(it).AsString()
                if (v.isNullOrEmpty()) return@forEach
                kv.put(it, v)
            }
            if (kv.keys.size != def.size) return@forEach;
            list.add(kv);
        }

        return list;
    }

    /**
     * 从缓存中获取数据
     */
    private fun getFromDefaultCache(kv: StringMap): List<Document>? {
        return FromRedisCache(
                table = this.actualTableName,
                groupKey = kv.keys.joinToString(","),
                groupValue = kv.values.joinToString(","),
                sql = "def"
        )
                .onlyGetFromCache({ it.FromListJson(Document::class.java) })
    }

    private fun getQueryScript(criteria: Criteria): String {
        var msgs = mutableListOf<String>()
        usingScope(JsonStyleScopeEnum.WITH_NULL) {
            msgs.add("[query] " + this.actualTableName);
            msgs.add("[where] " + criteria.criteriaObject.ToJson())
            if (selectColumns.any() || selectProjections.any()) {
                msgs.add(
                        "[select] " + arrayOf(
                                selectColumns.joinToString(","),
                                selectProjections.ToJson()
                        ).joinToString(",")
                )
            }

            if (unSelectColumns.any()) {
                msgs.add("[unselect] " + unSelectColumns.joinToString(","))
            }
            if (sort.any()) {
                msgs.add("[sort] " + sort.ToJson())
            }
            if (skip > 0 || take > 0) {
                msgs.add("[limit] ${skip},${take}")
            }
        }
        return msgs.joinToString(const.line_break)
    }


    /**
     * 将忽略 skip , take
     */
    fun count(): Int {
        var startAt = LocalDateTime.now();

        var ret = -1;
        val settingResult = db.mongo.mongoEvents.onQuering(this)
        if (settingResult.any { it.result.result == false }) {
            return -1;
        }

        var error: Exception? = null
        val criteria = db.mongo.getMergedMongoCriteria(whereData)
        var query = Query.query(criteria);
        try {
            this.script = getQueryScript(criteria)
            ret =
                    getMongoTemplate(settingResult.lastOrNull { it.result.dataSource.HasValue }?.result?.dataSource)
                            .count(
                                    query,
                                    actualTableName
                            ).toInt()
            this.executeTime = LocalDateTime.now() - startAt

            usingScope(arrayOf(MyOqlDbScopeEnum.IGNORE_AFFECT_ROW, MyOqlDbScopeEnum.IGNORE_EXECUTE_TIME)) {
                settingResult.forEach {
                    it.event.query(this, it.result)
                }
            }
        } catch (e: Exception) {
            error = e;
            throw e;
        } finally {
            logger.logFind(error, actualTableName, query, JsonMap("result" to ret))
//            logger.InfoError(ret < 0) {
//                return@InfoError """[count] ${this.collectionName}
//[query] ${query.queryObject.ToJson()}
//[result] ${ret}
//[耗时] ${db.executeTime}"""
//            }
        }
        return ret;
    }

    fun exists(): Boolean {
        var startAt = LocalDateTime.now();
        var ret: Boolean? = null;
        val settingResult = db.mongo.mongoEvents.onQuering(this)
        if (settingResult.any { it.result.result == false }) {
            return false;
        }

        val criteria = db.mongo.getMergedMongoCriteria(whereData);
        var query = Query.query(criteria);
        var error: Exception? = null;
        try {
            this.script = getQueryScript(criteria)
            ret =
                    getMongoTemplate(settingResult.lastOrNull { it.result.dataSource.HasValue }?.result?.dataSource)
                            .exists(
                                    query,
                                    actualTableName
                            );
            this.executeTime = LocalDateTime.now() - startAt;

            usingScope(arrayOf(MyOqlDbScopeEnum.IGNORE_AFFECT_ROW, MyOqlDbScopeEnum.IGNORE_EXECUTE_TIME)) {
                settingResult.forEach {
                    it.event.query(this, it.result)
                }
            }

            this.affectRowCount = 1;
        } catch (e: Exception) {
            error = e
            throw e;
        } finally {
            logger.logFind(error, actualTableName, query, JsonMap("result" to ret))
//            logger.InfoError(ret == null) {
//                return@InfoError """[exists] ${this.collectionName}
//[query] ${query.queryObject.ToJson()}
//[result] ${ret}
//[耗时] ${db.executeTime}"""
//            }
        }
        return ret ?: false;
    }


    @JvmOverloads
    fun <R> toListResult(type: Class<R>, mapFunc: ((Document) -> Unit)? = null): ListResult<R> {
        var ret = ListResult<R>();
        ret.data = toList(type, mapFunc);

        if (config.listResultWithCount) {
            ret.total = count()
        } else if (this.skip == 0 && this.take > 0) {
            if (ret.data.size < this.take) {
                ret.total = ret.data.size;
            } else {
                usingScope(MyOqlDbScopeEnum.IGNORE_EXECUTE_TIME) {
                    usingScope(MyOqlDbScopeEnum.IGNORE_AFFECT_ROW) {
                        ret.total = count()
                    }
                }
            }
        }
        return ret;
    }


    fun toMapList(): List<Document> {
        return toList(Document::class.java);
    }

    fun toMapListResult(): ListResult<Document> {
        return toListResult(Document::class.java);
    }

//    override fun writeExternal(out: ObjectOutput) {
//        out.writeObject(this.whereData.map { it.toDocument() }.ToJson())
//    }
//
//    override fun readExternal(inStream: ObjectInput) {
//        this.whereData = inStream.readObject().AsString().FromListJson(JsonMap::class.java)
//            .map { db.mongo.getCriteriaFromDocument(it) }.toMutableList()
//    }
}