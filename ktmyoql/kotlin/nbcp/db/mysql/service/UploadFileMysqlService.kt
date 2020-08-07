package nbcp.db.mysql.service

import nbcp.comm.AsString
import nbcp.comm.ConvertJson
import nbcp.comm.ConvertType
import nbcp.db.IdName
import nbcp.db.db
import nbcp.db.mongo.entity.*
import nbcp.db.sql.doInsert
import nbcp.db.sql.entity.s_annex
import nbcp.db.sql.match
import nbcp.db.sql.query
import nbcp.model.IUploadFileDbService
import org.springframework.stereotype.Service

@Service
class UploadFileMysqlService : IUploadFileDbService {
    override fun getByMd5(md5: String, corpId: String): SysAnnex? {
        return db.sql_base.s_annex.query()
                .where { it.checkCode match md5 }
                .where { it.corpId match corpId }
                .toEntity(SysAnnex::class.java) {
                    it["creator"] = IdName(it["creator_id"].AsString(), it["creator_name"].AsString())
                }
    }

    override fun insert(annex: SysAnnex): Int {
        var ent = annex.ConvertJson(s_annex::class.java);
        ent.creator = annex.creator;
//        ent.creator_id = annex.creator.id;
//        ent.creator_name = annex.creator.name;
        return db.sql_base.s_annex.doInsert(ent)
    }

    override fun clearMd5ById(id: String) {
        db.sql_base.s_annex.updateById(id).set { it.checkCode to "" }.exec()
    }

    override fun queryById(id: String): SysAnnex? {
        return db.sql_base.s_annex.queryById(id).toEntity(SysAnnex::class.java)
//        {
//            it["creator"] = IdName(it["creator_id"].AsString(), it["creator_name"].AsString())
//        }
    }
}