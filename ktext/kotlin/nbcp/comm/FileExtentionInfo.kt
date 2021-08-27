package nbcp.comm


enum class FileExtentionTypeEnum {
    Image,
    Video,
    Html,
    Office,
    Other
}

data class FileExtentionInfo(private var fileName: String) {
    var name: String = "";  //不带扩展名
    var extName: String = ""; //不带.的扩展名
    var extType: FileExtentionTypeEnum = FileExtentionTypeEnum.Other;

    init {
        var tailIndex = fileName.indexOfAny("?#".toCharArray());
        if (tailIndex > -1) {
            fileName = fileName.substring(0..tailIndex);
        }

        tailIndex = fileName.lastIndexOfAny("./\\".toCharArray());
        if (tailIndex > -1) {
            if (fileName[tailIndex] == '.') {
                extName = fileName.substring(tailIndex + 1);
                name = fileName.substring(fileName.lastIndexOfAny("/\\".toCharArray()) + 1, tailIndex);
            } else {
                name = fileName.substring(tailIndex + 1);
            }
        } else {
            name = fileName;
        }

        name = name.Remove('>', '<', '*', '|', ':', '?', '"', '\'');

        if (extName.toLowerCase().IsIn("ico", "icon", "png", "jpg", "jpeg", "gif", "bmp", "ttf", "otf", "tiff")) {
            extType = FileExtentionTypeEnum.Image;
        } else if (extName.toLowerCase().IsIn(
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
            extType = FileExtentionTypeEnum.Video
        } else if (extName.toLowerCase().IsIn("js", "css", "txt", "html", "htm", "xml", "xhtml", "json")) {
            extType = FileExtentionTypeEnum.Html
        } else if (extName.toLowerCase().IsIn("doc", "docx", "pdf", "xls", "xlsx", "ppt", "pptx", "rtf")) {
            extType = FileExtentionTypeEnum.Office;
        }
    }

    override fun toString(): String {
        return this.getFileName();
    }

    fun getFileName(): String {
        var name2 = this.name.replace("[<>/\\\\:*?\"\\|]".toRegex(),"");


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