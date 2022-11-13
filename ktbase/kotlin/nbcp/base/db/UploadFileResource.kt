package nbcp.base.db

import java.io.InputStream


data class UploadFileResource(var fileName: String, val stream: InputStream)
