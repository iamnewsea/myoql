package nbcp.db.mysql.service

import com.mysql.cj.jdbc.MysqlDataSource
import nbcp.comm.AsString
import nbcp.comm.ConvertJson
import nbcp.comm.ConvertType
import nbcp.comm.HasValue
import nbcp.db.IdName
import nbcp.db.db
import nbcp.db.mongo.entity.*
import nbcp.db.sql.doInsert
import nbcp.db.sql.entity.s_annex
import nbcp.db.sql.match
import nbcp.db.sql.query
import nbcp.model.IUploadFileDbService
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

@Component
@ConditionalOnClass(MysqlDataSource::class)
class UploadFileSqlService : IUploadFileDbService {
    override fun insert(annex: SysAnnex): Int {
        var ent = annex.ConvertJson(s_annex::class.java);
        ent.creator = annex.creator;
        return db.sql_base.s_annex.doInsert(ent)
    }

    override fun queryById(id: String): SysAnnex? {
        return db.sql_base.s_annex.queryById(id).toEntity(SysAnnex::class.java)
    }
}