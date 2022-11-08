//package nbcp.db.mongo;
//
//import nbcp.comm.AsString
//import nbcp.comm.scopes
//import nbcp.myoql.db.*
//import nbcp.db.mongo.entity.*
//import nbcp.db.mongo.event.*
//import org.bson.Document
//import org.slf4j.LoggerFactory
//import org.springframework.stereotype.Component
//import java.io.Serializable
//
//
///**
// * 逻辑删除
// */
//@Component
//class MongoDeleteWithLogicalDeleteEvent : IMongoEntityDelete {
//    companion object {
//        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
//    }
//
//    override fun beforeDelete(delete: MongoDeleteClip<*>, chain: EventChain): EventResult {
//        if (scopes.getLatest(MyOqlDbScopeEnum.IgnoreLogicalDelete) != null) {
//            return EventResult(true, null)
//        }
//
//        var logicalDelete = delete.moerEntity.entityClass.getAnnotation(LogicalDelete::class.java)
//        if (logicalDelete == null) {
//            return EventResult(true);
//        }
//
//        throw RuntimeException("请使用更新(逻辑删除)!")
//    }
//
//    override fun delete(delete: MongoDeleteClip<*>, chain: EventChain, eventData: EventResult) {
//    }
//}