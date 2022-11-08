//package nbcp.db.es
//
//
//import nbcp.base.comm.*
//import nbcp.base.utils.*
//import nbcp.db.db
//import org.slf4j.LoggerFactory
//
///**
// * Created by udi on 17-4-7.
// */
//
////根据Id，更新Es的一个键。
//
///**
// * EsUpdate
// */
//class EsSetEntityUpdateClip<M : EsBaseEntity<out Serializable>>(var moerEntity: M, var entity: Serializable)
//    : EsClipBase(moerEntity.tableName) {
//    companion object {
//        private val logger by lazy {
//            return@lazy LoggerFactory.getLogger(this::class.java)
//        }
//    }
//
//
//    fun exec(): Int {
//    }
//
//}
//
