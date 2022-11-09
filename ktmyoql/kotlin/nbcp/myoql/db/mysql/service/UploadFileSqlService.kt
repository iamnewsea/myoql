package nbcp.myoql.db.mysql.service


import nbcp.base.extend.ConvertJson
import nbcp.myoql.db.db
import nbcp.myoql.db.mongo.entity.SysAnnex
import nbcp.myoql.db.mysql.ExistsSqlSourceConfigCondition
import nbcp.myoql.db.sql.component.doInsert
import nbcp.myoql.db.sql.entity.s_annex
import nbcp.myoql.model.IUploadFileDbService
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