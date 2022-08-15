package nbcp.db.mysql.service


import nbcp.comm.ConvertJson
import nbcp.db.db
import nbcp.db.mongo.entity.*
import nbcp.db.mysql.ExistsSqlSourceConfigCondition
import nbcp.db.sql.doInsert
import nbcp.db.sql.entity.s_annex
import nbcp.model.IUploadFileDbService
import org.springframework.context.annotation.Conditional
import org.springframework.stereotype.Component

@Component
@Conditional(ExistsSqlSourceConfigCondition::class)
//@ConditionalOnProperty("spring.datasource.url")
class UploadFileSqlService : IUploadFileDbService {
    override fun insert(annex: SysAnnex): Int {
        var ent = annex.ConvertJson(s_annex::class.java);
        ent.creator = annex.creator;
        return db.sqlBase.s_annex.doInsert(ent)
    }

    override fun queryById(id: String): SysAnnex? {
        return db.sqlBase.s_annex.queryById(id).toEntity(SysAnnex::class.java)
    }
}