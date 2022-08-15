package nbcp.db.mongo.service

import nbcp.db.db
import nbcp.db.mongo.entity.*
import nbcp.db.mongo.*
import nbcp.model.IUploadFileDbService
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Primary
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component


@Primary
@ConditionalOnClass(MongoTemplate::class)
@Component
class UploadFileMongoService : IUploadFileDbService {
    companion object {
    }

    override fun insert(annex: SysAnnex): Int {
        db.morBase.sysAnnex.doInsert(annex);
        return db.affectRowCount;
    }

    override fun queryById(id: String): SysAnnex? {
        return db.morBase.sysAnnex.queryById(id).toEntity()
    }
}