package nbcp.db.mongo

import com.mongodb.BasicDBObject
import nbcp.comm.*
import nbcp.db.BaseEntity
import nbcp.db.db
import nbcp.db.mongo.*
import org.bson.Document
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.time.LocalDateTime

open class MongoBaseInsertClip(tableName: String) : MongoClipBase(tableName), IMongoWhereable {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    var entities = mutableListOf<Any>()

    /**
     * 批量添加中的添加实体。
     * 支持两种类型：IMongoDocument,Map。  Document,DbObject 算是Map
     */
    fun addEntity(entity: Any) {
        if (entity is BaseEntity) {
            if (entity.id.isEmpty()) {
                entity.id = ObjectId().toString()
            }
            entity.createAt = LocalDateTime.now();
        } else if (entity is MutableMap<*, *>) {
            var map = entity as MutableMap<String, Any?>
            if (map.get("_id").AsString().isEmpty()) {
                map.put("_id", ObjectId().toString())
            }

            map.put("createAt", LocalDateTime.now());
        }


        if (entity is IMongoDocument) {
            this.entities.add(entity);
            return;
        }

        if (entity is MutableMap<*, *>) {
            this.entities.add(entity)
            return;
        }
        if (entity is Collection<*>) {
            //不能插入列表，请一条一条的插入
            throw RuntimeException("不能插入列表!")
        }

        throw RuntimeException("不支持插入类型：${entity.javaClass}")
    }

    fun exec(): Int {
        db.affectRowCount = -1;
        var ret = 0;

        var settingResult = db.mongo.mongoEvents.onInserting(this)
        if (settingResult.any { it.second.result == false }) {
            return 0;
        }

        var startAt = LocalDateTime.now()
        try {
            mongoTemplate.insert(entities, this.collectionName)
            db.executeTime = LocalDateTime.now() - startAt

            usingScope(arrayOf(OrmLogScope.IgnoreAffectRow, OrmLogScope.IgnoreExecuteTime)) {
                settingResult.forEach {
                    it.first.insert(this, it.second)
                }
            }

            ret = entities.size;
            db.affectRowCount = entities.size
            return db.affectRowCount
        } catch (e: Exception) {
            ret = -1;
            throw e;
        } finally {
            logger.InfoError(ret < 0) {
                """[insert] ${this.collectionName}
${
                    if (logger.debug) "[entities] ${entities.ToJson()}" else "[enities.ids] ${
                        entities.map {
                            if (it is BaseEntity) {
                                return@map it.id
                            } else if (it is Map<*, *>) {
                                return@map it.get("id").AsString()
                            }
                            //反射 id.
                            var idField = it.javaClass.FindField("id")
                            if (idField != null) {
                                idField.isAccessible = true;
                                return@map idField.get(it);
                            }
                            return@map ""
                        }.joinToString(",")
                    }"
                }
[result] ${ret}
[耗时] ${db.executeTime}
"""
            };
        }

        return ret;
    }
}