package nbcp.db.mongo

import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.*
import org.bson.Document
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.query.BasicQuery
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import java.lang.Exception
import java.time.LocalDateTime

open class MongoBaseQueryClip(tableName: String) : MongoClipBase(tableName), IMongoWhereable {
    var whereData = mutableListOf<Criteria>()
    protected var skip: Int = 0;
    protected var take: Int = -1;
    protected var sort: Document = Document()

    //    private var whereJs: String = "";
    protected var selectColumns = mutableSetOf<String>();
    protected var selectProjections = JsonMap();

    //    private var selectDbObjects = mutableSetOf<String>();
    protected var unSelectColumns = mutableSetOf<String>()

    fun selectField(column: String) {
        this.selectColumns.add(column);
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    /**
     * 返回该对象的 Md5。
     */
    private fun getCacheKey(): String {
        var unKeys = mutableListOf<String>()

        unKeys.add(whereData.map { it.criteriaObject.ToJson() }.joinToString("&"))
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
    fun <R> toList(clazz: Class<R>, mapFunc: ((Document) -> Unit)? = null): MutableList<R> {
        db.affectRowCount = -1;

        var settingResult = db.mongo.mongoEvents.onQuering(this)
        if (settingResult.any { it.second.result == false }) {
            return mutableListOf();
        }

        var criteria = this.getMongoCriteria(*whereData.toTypedArray());
        var projection = Document();
        selectColumns.forEach {
            projection.put(it, 1)
        }

        selectProjections.forEach {
            projection.put(it.key, it.value)
        }

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

        var startAt = LocalDateTime.now();
        this.script = this.getQueryScript(criteria);
        var cursor = mongoTemplate.find(query, Document::class.java, this.collectionName)

        this.executeTime = LocalDateTime.now() - startAt

        var ret = mutableListOf<R>();
        var lastKey = selectColumns.lastOrNull() ?: selectProjections.map { it.key }.lastOrNull() ?: ""

        if (lastKey == "_id") {
            lastKey = "id"
        } else if (lastKey.endsWith("._id")) {
            lastKey = lastKey.Slice(0, -3) + "id";
        }

        var error: Exception? = null;
        var skipNullCount = 0;
        try {
            db.mongo.procResultData_id2Id(cursor);
            cursor.forEach {

                if (mapFunc != null) {
                    mapFunc(it);
                }
                if (clazz.IsSimpleType()) {
                    if (lastKey.isEmpty()) {
                        lastKey = it.keys.last()
                    }

                    var value = it.GetComplexPropertyValue(*lastKey.split(".").toTypedArray());
                    if (value != null) {
                        ret.add(value.ConvertType(clazz) as R);
                    } else {
                        skipNullCount++;
                    }
                } else {
                    if (Document::class.java.isAssignableFrom(clazz)) {
                        ret.add(it as R);
                    } else {
                        var ent = it.ConvertJson(clazz)
                        ret.add(ent);
                    }
                }
            }

            this.affectRowCount = cursor.size;

            usingScope(arrayOf(MyOqlOrmScope.IgnoreAffectRow, MyOqlOrmScope.IgnoreExecuteTime)) {
                settingResult.forEach {
                    it.first.query(this, it.second)
                }
            }

        } catch (e: Exception) {
            error = e;
            throw e;
        } finally {
            fun getMsgs(): String {
                var msgs = mutableListOf<String>()
                msgs.add(this.script)

                if (config.debug) {
                    msgs.add("[result] ${cursor.ToJson()}")
                } else {
                    msgs.add("[result.size] " + cursor.size.toString())
                }
                if (skipNullCount > 0) {
                    msgs.add("[skipNullRows] ${skipNullCount}")
                }

                msgs.add("[耗时] ${this.executeTime}")
                return msgs.joinToString(const.line_break);
            }

            MongoLogger.logFind(error, collectionName, ::getMsgs);
        }

        return ret
    }

    private fun getQueryScript(criteria: Criteria): String {
        var msgs = mutableListOf<String>()
        msgs.add("[query] " + this.collectionName);
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
        return msgs.joinToString(const.line_break)
    }


    /**
     * 将忽略 skip , take
     */
    fun count(): Int {
        var startAt = LocalDateTime.now();
        var ret = -1;
        var error: Exception? = null
        val criteria = this.getMongoCriteria(*whereData.toTypedArray())
        var query = Query.query(criteria);
        try {
            this.script = getQueryScript(criteria)
            ret = mongoTemplate.count(query, collectionName).toInt()
            this.executeTime = LocalDateTime.now() - startAt
        } catch (e: Exception) {
            error = e;
            throw e;
        } finally {
            MongoLogger.logFind(error, collectionName, query, JsonMap("result" to ret))
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
        val criteria = this.getMongoCriteria(*whereData.toTypedArray());
        var query = Query.query(criteria);
        var error: Exception? = null;
        try {
            this.script = getQueryScript(criteria)
            ret = mongoTemplate.exists(query, collectionName);
            this.executeTime = LocalDateTime.now() - startAt;
            this.affectRowCount = 1;
        } catch (e: Exception) {
            error = e
            throw e;
        } finally {
            MongoLogger.logFind(error, collectionName, query, JsonMap("result" to ret))
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
    fun <R> toListResult(clazz: Class<R>, mapFunc: ((Document) -> Unit)? = null): ListResult<R> {
        var ret = ListResult<R>();
        ret.data = toList(clazz, mapFunc);

        if (config.listResultWithCount) {
            ret.total = count()
        } else if (this.skip == 0 && this.take > 0) {
            if (ret.data.size < this.take) {
                ret.total = ret.data.size;
            } else {
                usingScope(MyOqlOrmScope.IgnoreExecuteTime) {
                    usingScope(MyOqlOrmScope.IgnoreAffectRow) {
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
}