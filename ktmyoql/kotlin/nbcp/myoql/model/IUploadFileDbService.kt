package nbcp.myoql.model

import nbcp.myoql.db.mongo.entity.SysAnnex

/**
 * 上传服务接口， 以Mongo为准
 */
interface IUploadFileDbService {
    fun insert(annex: SysAnnex): Int
    fun queryById(id: String): SysAnnex?
}