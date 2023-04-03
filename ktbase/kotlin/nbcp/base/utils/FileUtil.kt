package nbcp.base.utils

import nbcp.base.extend.HasValue
import nbcp.base.extend.ToHexLowerString
import nbcp.base.extend.Unwind
import java.io.File
import java.io.FileWriter

object FileUtil {
    /**
     * 把文件的各个部分组织在一起， 处理 . 和 .. 部分
     */
    @JvmStatic
    fun resolvePath(vararg path: String): String {
        if (path.any() == false) return "";
        var isRoot = path.first().let { it.startsWith('/') || it.startsWith('\\') }

        var list = mutableListOf<String>()

        path.map {
            it.split('/', '\\')
        }
                .Unwind()
                .filter { it.HasValue }
                .filter { it != "." }
                .forEach {
                    if (it == "..") {
                        if (list.removeLastOrNull() == null) {
                            throw RuntimeException("路径层级溢出")
                        }
                        return@forEach
                    }

                    list.add(it);
                }


        return (if (isRoot) File.separator else "") + list.joinToString(File.separator)
    }


    /**
     * 最好传前8个字节，判断文件类型。
     */
    @JvmStatic
    fun getFileTypeWithBom(byteArray8: ByteArray): String {
        //https://blog.csdn.net/hch15112345824/article/details/86640092
        //https://blog.csdn.net/gagapencil/article/details/40392363

        //小于4个字节，返回空。
        if (byteArray8.size < 4) {
            return "";
        }

        var value = byteArray8.ToHexLowerString()
        var map = mapOf(
                "4D546864" to "mid",
                "FFD8FF" to "jpg",
                "89504E47" to "png",
                "47494638" to "gif",
                "49492A00" to "tif",
                "424D" to "bmp",
                "41433130" to "dwg",
                "38425053" to "psd",
                "7B5C727466" to "rtf",
                "3C3F786D6C" to "xml",
                "68746D6C3E" to "html",
//                "44656C69766572792D646174653A" to "eml",
//                "CFAD12FEC5FD746F" to "dbx",
//                "2142444E" to "pst",
                "D0CF11E0" to "doc",
//                "5374616E64617264204A" to "mdb",
                "FF575043" to "wpd",
//                "252150532D41646F6265" to "ps",
                "255044462D312E" to "pdf",
                "AC9EBD8F" to "qdf",
                "E3828596" to "pwl",
                "504B0304" to "zip",
                "52617221" to "rar",
                "57415645" to "wav",
                "41564920" to "avi",
                "2E7261FD" to "ram",
                "2E524D46" to "rm",
                "000001BA" to "mpg",
                "000001B3" to "mpg",
                "6D6F6F76" to "mov",
                "3026B2758E66CF11" to "asf"
        );

        return map.filterKeys { value.startsWith(it) }.values.firstOrNull() ?: ""
    }

    fun writeContent(fileName: String, txt: String) {
        FileWriter(fileName).use { f ->
            f.write(txt);
        }
    }
}