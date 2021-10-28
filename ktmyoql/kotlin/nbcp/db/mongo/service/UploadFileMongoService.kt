package nbcp.db.mongo.service

import nbcp.comm.HasValue
import nbcp.db.db
import nbcp.db.mongo.entity.*
import nbcp.db.mongo.*
import nbcp.db.mongo.table.MongoBaseGroup
import nbcp.model.IUploadFileDbService
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Primary
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Service


@Primary
@ConditionalOnClass(MongoTemplate::class)
class UploadFileMongoService : IUploadFileDbService {
    companion object {
    }

    override fun insert(annex: SysAnnex): Int {
        db.mor_base.sysAnnex.doInsert(annex);
        return db.affectRowCount;
    }

    override fun queryById(id: String): SysAnnex? {
        return db.mor_base.sysAnnex.queryById(id).toEntity()
    }
}