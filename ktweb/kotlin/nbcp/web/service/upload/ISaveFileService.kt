package nbcp.web.service.upload

import nbcp.base.comm.JsonResult
import java.io.InputStream

interface ISaveFileService {
    fun save(fileStream: InputStream, group: String, fileData: UploadFileNameData): String
    fun delete(url: String): JsonResult
}