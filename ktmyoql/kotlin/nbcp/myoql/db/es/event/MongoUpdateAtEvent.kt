//package nbcp.db.es
//
//import nbcp.db.DbEntityEventResult
//import nbcp.db.es.*
//import org.springframework.stereotype.Component
//import java.time.LocalDateTime
//
///**
// * 同步处理，更新的实体，添加 updateAt 字段。
// */
//@Component
//class EsUpdateAtEvent : IEsEntityUpdate {
//    override fun beforeUpdate(update: EsBaseUpdateClip): DbEntityEventResult {
//        update.setValue("updateAt", LocalDateTime.now())
//        return DbEntityEventResult(true, null)
//    }
//
//    override fun update(update: EsBaseUpdateClip, eventData: DbEntityEventResult) {
//    }
//}