package nbcp.db.mongo

import com.mongodb.BasicDBObject
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.repository.MongoRepository
import nbcp.base.extend.HasValue
import nbcp.base.extend.IsSimpleType
import nbcp.base.extend.ToJson
import nbcp.base.extend.*
import nbcp.base.utils.MyUtil
import nbcp.db.db
import nbcp.db.mongo.*
import org.slf4j.LoggerFactory
import java.lang.RuntimeException
import java.time.LocalDateTime

/**
 * Created by udi on 17-4-7.
 */

//根据Id，更新Mongo的一个键。

/**
 * MongoUpdate
 */
class MongoSetEntityUpdateClip<M : MongoBaseEntity<out IMongoDocument>>(var moerEntity: M, var entity: IMongoDocument) : MongoClipBase(moerEntity.tableName), IMongoWhereable {
    companion object {
        private val logger by lazy {
            return@lazy LoggerFactory.getLogger(this::class.java)
        }
    }

    private var whereColumns: MutableSet<String>? = null
    private var setColumns: MutableSet<String>? = null
    private var unsetColumns = mutableSetOf<String>()
    /**
     * @param entity 要更新的实体
     * @param whereColumnsFunc where 列。
     * @param unsetColumnsFunc 排除要更新的列。
     */
//    fun setEntity(entity: IMongoDocument): MongoSetEntityUpdateClip<M> {
//        this.entity = entity;
//        return this;
//    }

    fun withColumns(setFunc: (M) -> MongoColumns): MongoSetEntityUpdateClip<M> {
        if (this.setColumns == null) {
            this.setColumns = mutableSetOf<String>()
        }
        this.setColumns!!.addAll(setFunc(this.moerEntity).map { it.toString() })
        return this;
    }

    fun withoutColumns(unsetFunc: (M) -> MongoColumns): MongoSetEntityUpdateClip<M> {
        this.unsetColumns.addAll(unsetFunc(this.moerEntity).map { it.toString() })
        return this;
    }

    fun whereColumns(whereFunc: (M) -> MongoColumns): MongoSetEntityUpdateClip<M> {
        if (whereColumns == null) {
            this.whereColumns = mutableSetOf<String>()
        }
        this.whereColumns!!.addAll(whereFunc(this.moerEntity).map { it.toString() })
        return this;
    }

    fun withRequestParams(keys: Set<String>): MongoSetEntityUpdateClip<M> {
        var cols = MongoColumns();
        keys.forEach {
            cols.add(MongoColumnName(it));
        }
        return withColumns { cols };
    }

    fun exec(): Int {
//        if (this.entity == null) {
//            throw RuntimeException("entity不能为空")
//        }

        if (whereColumns == null) {
            whereColumns = mutableSetOf("_id");
        }

        var update = MongoUpdateClip(this.moerEntity)
        this.entity::class.java.AllFields.forEach {
            var findKey = it.name;
            if (it.name == "id") {
                findKey = "_id"
            }

            if (whereColumns!!.contains(findKey)) {
                var value = MyUtil.getPrivatePropertyValue(this.entity, it.name);
                if (value != null) {
                    if (value is String) {
                        if (value.HasValue) {
                            update.where(it.name match value);
                        }
                    } else {
                        update.where(it.name match value);
                    }
                }

                return@forEach
            }

            if (it.name == "id") {
                return@forEach
            }

            if (setColumns != null && !setColumns!!.contains(it.name)) {
                return@forEach;
            }

            if (unsetColumns.contains(it.name)) {
                return@forEach
            }

            var value = MyUtil.getPrivatePropertyValue(this.entity, it.name);
//            if (value == null) {
//                return@forEach
//            }
            update.set(it.name, value);
        }

        return update.exec();
    }

    /**
     * 先更新，如果不存在，则插入。
     * @return: 返回插入的Id，如果是更新则返回空字串
     */
    fun updateOrAdd(): String {
        var ret = "";
        if (this.exec() == 0) {
            if (entity.id.isEmpty()) {
                entity.id = ObjectId().toString();
            }

            mongoTemplate.insert(entity, this.moerEntity.tableName);
            db.affectRowCount = 1;
            ret = entity.id;
        }
        return ret;
    }
}

