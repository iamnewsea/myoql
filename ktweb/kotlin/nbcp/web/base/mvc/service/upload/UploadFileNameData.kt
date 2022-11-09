package nbcp.web.base.mvc.service.upload

import nbcp.base.comm.*;
import nbcp.base.db.*;
import nbcp.base.enums.*;
import nbcp.base.extend.*;
import nbcp.base.utils.*;
import java.time.LocalDate

class UploadFileNameData @JvmOverloads constructor(var msg: String = "") {
    var fileName: String = ""
    var extName: String = ""
    var extType: FileExtensionTypeEnum = FileExtensionTypeEnum.Other

    //        var imgWidth: Int = 0
//        var imgHeight: Int = 0
    var corpId: String = ""

    /*
 * 把文件转移到相应的文件夹下.
 * 1. 第一级目录,按 年-月 归档.
 * 2. 第二级目录,按 企业Id 归档.
 *      2.1.如果是后台, 企业Id = _admin_
 *      2.2.如果是商城用户,企业Id = _shop_
 * 3. 第三级目录,如果是非图片是 extType , 如果是图片是 宽-高
 * 4. 第四级是,如果是图片,是256宽度像素大小的缩略图.
     */
    private fun getTargetPaths(): Array<String> {
        var list = mutableListOf<String>()
        list.add(LocalDate.now().Format("yyyy-MM"));
//            var pixelTotal = imgWidth * imgHeight;
//
//
//            if (pixelTotal > 0) {
//                var pixel = (pixelTotal / 10000.0).toInt();
//                //按图片像素文件夹命名。不足1万 = 0
//                list.add(pixel.toString())
//            } else {
//                list.add(this.extType.toString())
//            }
        return list.toTypedArray();
    }

    /**
     * 保存全路径
     */
    fun getTargetFileName(): List<String> {
        val targetFileName = mutableListOf<String>()

        if (corpId.HasValue) {
            targetFileName.add(corpId);
        }
        targetFileName.addAll(getTargetPaths())
        targetFileName.add(CodeUtil.getCode())

        if (fileName.HasValue) {
            targetFileName.add(fileName)
        }

        return targetFileName
    }
}