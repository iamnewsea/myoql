package nbcp.db.mongo

import nbcp.comm.*
import nbcp.db.MyOqlOrmScope
import nbcp.db.db
import nbcp.utils.RecursionUtil
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.time.LocalDateTime

open class MongoBaseInsertClip(tableName: String) : MongoClipBase(tableName) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    var entities = mutableListOf<Any>()

    /**
     * 批量添加中的添加实体。
     * 支持两种类型：Serializable,Map。  Document,DbObject 算是Map
     */
    fun addEntity(entity: Any) {
        //移到拦截器中。
//        if (entity is BaseEntity) {
//            if (entity.id.isEmpty()) {
//                entity.id = ObjectId().toString()
//            }
//            entity.createAt = LocalDateTime.now();
//            this.entities.add(entity);
//            return;
//        } else if (entity is MutableMap<*, *>) {
//            var map = entity as MutableMap<String, Any?>
//            if (map.get("_id").AsString().isEmpty()) {
//                map.put("_id", ObjectId().toString())
//            }
//
//            map.put("createAt", LocalDateTime.now());
//            this.entities.add(entity)
//            return;
//        }
//
//
//
//        if (entity is Serializable) {
//            this.entities.add(entity);
//            return;
//        }

/*        if (entity is Collection<*>) {
            //不能插入列表，请一条一条的插入
            throw RuntimeException("不能插入列表!")
        }*/

        this.entities.add(entity);

//        throw RuntimeException("不支持插入类型：${entity.javaClass}")
    }


    fun addEntities(entities: List<Any>) {
        this.entities.addAll(entities)
    }

    fun exec(): Int {
        db.affectRowCount = -1;

        var settingResult = db.mongo.mongoEvents.onInserting(this)
        if (settingResult.any { it.second.result == false }) {
            return 0;
        }

        var startAt = LocalDateTime.now()
        var error: Exception? = null;
        try {
            mongoTemplate.insert(entities, this.actualTableName)
            this.executeTime = LocalDateTime.now() - startAt

            usingScope(arrayOf(MyOqlOrmScope.IgnoreAffectRow, MyOqlOrmScope.IgnoreExecuteTime)) {
                settingResult.forEach {
                    it.first.insert(this, it.second)
                }
            }

            this.affectRowCount = entities.size
            return this.affectRowCount
        } catch (e: Exception) {
            error = e;
            throw e;
        } finally {
            MongoLogger.logInsert(error, actualTableName, entities)
//            logger.InfoError(ret < 0) {
//                """[insert] ${this.collectionName}
//${
//                    if (config.debug) "[entities] ${entities.ToJson()}" else "[enities.ids] ${
//                        entities.map {
//                            if (it is BaseEntity) {
//                                return@map it.id
//                            } else if (it is Map<*, *>) {
//                                return@map it.get("id").AsString()
//                            }
//                            //反射 id.
//                            var idField = it.javaClass.FindField("id")
//                            if (idField != null) {
//                                idField.isAccessible = true;
//                                return@map idField.get(it);
//                            }
//                            return@map ""
//                        }.joinToString(",")
//                    }"
//                }
//[result] ${ret}
//[耗时] ${db.executeTime}
//"""
//            };
        }

//        return ret;
    }
}