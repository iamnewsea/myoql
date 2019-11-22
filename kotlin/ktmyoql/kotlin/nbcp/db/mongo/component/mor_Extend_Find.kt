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
import nbcp.base.utils.MyUtil
import nbcp.db.db
import nbcp.db.mongo.*

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
    private var sort: BasicDBObject = BasicDBObject()
    //    private var whereJs: String = "";
    private var selectColumns = mutableSetOf<String>();
    private var selectDbObjects = mutableSetOf<DBObject>();
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

    fun select(columns: DBObject): MongoQueryClip<M, E> {
        selectDbObjects.add(columns)
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


    fun toCursor(): DBCursor {
        var criteria = db.getMongoCriteria(*whereData.toTypedArray());

        var msgs = mutableListOf<String>()
        msgs.add("query:[" + this.collectionName + "]" + criteria.criteriaObject.toJson())
        if (selectDbObjects.any()) {
            msgs.add("SelectDbObjects:[" + this.collectionName + "]" + (selectDbObjects.map { (it as BasicDBObject).toJson() }).joinToString(","))
        }
        db.logger.info(msgs.joinToString(line_break))

        var projection = BasicDBObject();
        selectColumns.forEach {
            projection.put(it, 1)
        }

        selectDbObjects.forEach {
            it.keySet().forEach { key ->
                projection.put(key, it.get(key))
            }
        }

        unSelectColumns.forEach {
            projection.put(it, 0)
        }

        var coll = getCollection()


        var option = DBCollectionFindOptions();
        option.projection(projection);

        if (this.skip > 0) {
            option.skip(this.skip)
        }

        if (this.take > 0) {
            option.limit(this.take)
        }

        if (sort.any()) {
            option.sort(sort);
        }

        var cursor = coll.find(criteria.toDBObject(), option)
        return cursor;
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

    fun <R> toList(clazz: Class<R>, mapFunc: ((DBObject) -> Unit)? = null): MutableList<R> {
        var isString = false;
        if (clazz.IsSimpleType()) {
            isString = clazz.name == "java.lang.String";
        }
//        else if (selectColumns.any() == false) {
//            if (Map::class.java.isAssignableFrom(clazz) == false) {
//                select(*clazz.fields.map { it.name }.toTypedArray())
//            }
//        }

        var cursor = toCursor();


//        var cacheValue = MyCache.find(this.collectionName, this.getCacheKey())
//        if (cacheValue.HasValue) {
//            return cacheValue.FromJson<MutableList<R>>()
//        }


//        var mapper = mor.util.getMongoMapper(collectionName, clazz);

        var ret = mutableListOf<R>();
        var lastKey = selectColumns.lastOrNull() ?: ""
        cursor.forEach {
            db.change_id2Id(it);

//            if( it.containsField("_id")){
//                it.put("id",it.get("_id").toString())
//            }

            if (isString) {
                if (lastKey.isEmpty()) {
                    lastKey = it.keySet().last()
                }

                ret.add(it.GetComplexPropertyValue(lastKey) as R)
            } else if (clazz.IsSimpleType()) {
                if (lastKey.isEmpty()) {
                    lastKey = it.keySet().last()
                }

                ret.add(it.GetComplexPropertyValue(*lastKey.split(".").toTypedArray()) as R);
            } else {
                if (DBObject::class.java.isAssignableFrom(clazz)) {
                    ret.add(it as R);
                } else {
//                    var ent = mapper.toEntity(it)
                    var ent = it.ConvertJson(clazz)
                    ret.add(ent);
                }
            }
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
        return mongoTemplate.count(Query.query(db.getMongoCriteria(*whereData.toTypedArray())), collectionName).toInt()
    }

    fun exists(): Boolean {
        return mongoTemplate.exists(Query.query(db.getMongoCriteria(*whereData.toTypedArray())), collectionName);
    }

    fun toList(mapFunc: ((DBObject) -> Unit)? = null): MutableList<E> {
        return toList(moerEntity.entityClass,mapFunc)
    }

    fun toEntity(mapFunc: ((DBObject) -> Unit)? = null): E? {
        this.take = 1;
        return toList(moerEntity.entityClass,mapFunc).firstOrNull();
    }

//    inline fun <reified R> toEntity(): R? {
//        return toEntity(R::class.java);
//    }

    fun <R> toEntity(clazz: Class<R>,mapFunc: ((DBObject) -> Unit)? = null): R? {
        this.take = 1;
        return toList(clazz,mapFunc).firstOrNull();
    }


    fun <R> toListResult(clazz: Class<R>,  mapFunc: ((DBObject) -> Unit)? = null): ListResult<R> {
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

    fun toListResult(mapFunc: ((DBObject) -> Unit)? = null): ListResult<E> {
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
}


