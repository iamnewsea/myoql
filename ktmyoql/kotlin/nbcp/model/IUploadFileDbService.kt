package nbcp.model

import nbcp.db.mongo.entity.SysAnnex

/**
 * 上传服务接口， 以Mongo为准
 */
interface IUploadFileDbService {
    fun getByMd5(md5: String, corpId: String = ""): SysAnnex?
    fun insert(annex: SysAnnex): Int
    fun clearMd5ById(id: String)
    fun queryById(id:String):SysAnnex?
}