package nbcp.db.mongo.service

import nbcp.base.extend.HasValue
import nbcp.db.db
import nbcp.db.mongo.entity.SysAnnex
import nbcp.db.mongo.match
import nbcp.db.mongo.query
import nbcp.db.mongo.table.BaseGroup
import nbcp.db.mongo.updateById
import nbcp.model.IUploadFileDbService
import org.springframework.stereotype.Service

@Service
class UploadFileMongoService :IUploadFileDbService{
    companion object{
        private val mor_base by lazy {
            return@lazy BaseGroup()
        }
    }

    override fun getByMd5(corpId: String, md5: String): SysAnnex? {
        return  mor_base.sysAnnex.query()
                .where { it.checkCode match md5 }
                .whereIf( corpId.HasValue,  { it.corpId match corpId })
                .orderByDesc { it.id }
                .toEntity();
    }

    override fun insert(annex: SysAnnex): Int {
         mor_base.sysAnnex.doInsert(annex);
        return db.affectRowCount;
    }

    override fun clearMd5ById(id: String) {

        mor_base.sysAnnex.updateById( id)
                .set { it.checkCode to "" }
                .exec();
    }

}