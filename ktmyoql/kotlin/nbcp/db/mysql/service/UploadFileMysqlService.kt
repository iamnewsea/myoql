package nbcp.db.mysql.service

import nbcp.db.mongo.entity.SysAnnex
import nbcp.model.IUploadFileDbService
import org.springframework.stereotype.Service

@Service
class UploadFileMysqlService :IUploadFileDbService{
    override fun getByMd5(md5: String, corpId: String): SysAnnex? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun insert(annex: SysAnnex): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun clearMd5ById(id: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}