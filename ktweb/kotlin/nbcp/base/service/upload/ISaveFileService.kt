package nbcp.base.service.upload

import nbcp.comm.JsonResult
import java.io.InputStream

interface ISaveFileService {
    fun save(fileStream: InputStream, group: String, fileData: UploadFileNameData): String
    fun delete(url: String): JsonResult
}