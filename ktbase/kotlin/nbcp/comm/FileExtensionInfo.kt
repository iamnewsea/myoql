package nbcp.comm


enum class FileExtensionTypeEnum {
    Image,
    Video,
    Html,
    Office,
    Other
}

open class FileExtensionInfo() {
    var name: String = "";  //不带扩展名
    var extName: String = ""; //不带.的扩展名
    var extType: FileExtensionTypeEnum = FileExtensionTypeEnum.Other;

    companion object {

        @JvmStatic
        fun ofUrl(fileUrl: String): FileExtensionInfo {
            var fileName = fileUrl;
            var tailIndex = fileName.indexOfAny("?#".toCharArray());
            if (tailIndex > -1) {
                fileName = fileName.substring(0..tailIndex);
            }

            return ofFileName(fileName)
        }

        @JvmStatic
        fun ofFileName(fileFullName: String): FileExtensionInfo {
            var ret = FileExtensionInfo();

            var fileName = fileFullName.split("/", "\\").last();

            var tailIndex = fileName.lastIndexOf('.');
            if (tailIndex >= 0) {
                ret.extName = fileName.substring(tailIndex + 1);
                ret.name = fileName.substring(0, tailIndex);

            } else {
                ret.name = fileName;
            }

            ret.name = ret.name.remove('>', '<', '*', '|', ':', '?', '"', '\'');

            if (ret.extName.lowercase().IsIn("ico", "icon", "png", "jpg", "jpeg", "gif", "bmp", "ttf", "otf", "tiff")) {
                ret.extType = FileExtensionTypeEnum.Image;
            } else if (ret.extName.lowercase().IsIn(
                    "mp4",
                    "mp3",
                    "avi",
                    "rm",
                    "rmvb",
                    "flv",
                    "flash",
                    "swf",
                    "3gp",
                    "wma",
                    "m3u8",
                    "ts",
                    "hls",
                    "mov"
                )
            ) {
                ret.extType = FileExtensionTypeEnum.Video
            } else if (ret.extName.lowercase().IsIn("js", "css", "txt", "html", "htm", "xml", "xhtml", "json")) {
                ret.extType = FileExtensionTypeEnum.Html
            } else if (ret.extName.lowercase().IsIn("doc", "docx", "pdf", "xls", "xlsx", "ppt", "pptx", "rtf")) {
                ret.extType = FileExtensionTypeEnum.Office;
            }

            return ret;
        }
    }

    override fun toString(): String {
        return this.getFileName();
    }

    fun getFileName(): String {
        var name2 = this.name.replace("[<>/\\\\:*?\"\\|]".toRegex(), "");


        if (this.extName.HasValue) {
            return name2 + "." + this.extName
        }
        return name2;
    }
//    val isStaticURI: Boolean
//        get() {
//            return extType != FileExtentionTypeEnum.Other;
//        }
}