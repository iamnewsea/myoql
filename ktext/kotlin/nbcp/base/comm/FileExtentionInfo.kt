package nbcp.comm

import nbcp.comm.IsIn
import nbcp.comm.Remove


enum class FileExtentionTypeEnum {
    Image,
    Video,
    Html,
    Office,
    Other
}

data class FileExtentionInfo(private var url: String) {
    var name: String = "";  //不带扩展名
    var extName: String = ""; //不带.的扩展名
    var extType: FileExtentionTypeEnum = FileExtentionTypeEnum.Other;

    init {
        var tailIndex = url.indexOfAny("?#".toCharArray());
        if (tailIndex > -1) {
            url = url.substring(0..tailIndex);
        }

        tailIndex = url.lastIndexOfAny("./\\".toCharArray());
        if (tailIndex > -1) {
            if (url[tailIndex] == '.') {
                extName = url.substring(tailIndex + 1);
                name = url.substring(url.lastIndexOfAny("/\\".toCharArray()) + 1, tailIndex);
            } else {
                name = url.substring(tailIndex + 1);
            }
        } else {
            name = url;
        }

        name = name.Remove('>', '<', '*', '|', ':', '?', '"', '\'');

        if (extName.toLowerCase().IsIn("ico", "icon", "png", "jpg", "jpeg", "gif", "bmp", "ttf", "otf", "tiff")) {
            extType = FileExtentionTypeEnum.Image;
        }
        else if (extName.toLowerCase().IsIn("mp4", "mp3", "avi", "rm", "rmvb", "flv", "flash", "swf", "3gp", "wma", "m3u8", "ts", "hls", "mov")) {
            extType = FileExtentionTypeEnum.Video
        }
        else if (extName.toLowerCase().IsIn("js", "css", "txt", "html", "htm", "xml", "xhtml", "json")) {
            extType = FileExtentionTypeEnum.Html
        }
        else if (extName.toLowerCase().IsIn("doc", "docx", "pdf", "xls", "xlsx", "ppt", "pptx", "rtf")) {
            extType = FileExtentionTypeEnum.Office;
        }
    }

    override fun toString(): String {
        return this.extName;
    }

//    val isStaticURI: Boolean
//        get() {
//            return extType != FileExtentionTypeEnum.Other;
//        }
}