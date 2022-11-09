package nbcp.web.base.mvc.service.upload

import nbcp.base.comm.*;
import nbcp.base.db.*;
import nbcp.base.enums.*;
import nbcp.base.extend.*;
import nbcp.base.utils.*;
import java.io.InputStream

interface ISaveFileService {
    fun save(fileStream: InputStream, group: String, fileData: UploadFileNameData): String
    fun delete(url: String): JsonResult
}