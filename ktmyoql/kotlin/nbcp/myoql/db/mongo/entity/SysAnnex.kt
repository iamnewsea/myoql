package nbcp.myoql.db.mongo.entity

import nbcp.base.db.IdName
import nbcp.base.db.annotation.Cn
import nbcp.base.db.annotation.DbEntityGroup
import nbcp.myoql.db.BaseEntity
import org.springframework.data.mongodb.core.mapping.Document

//系统附件表
@Document
@DbEntityGroup("MongoBase")
@Cn("系统附件")
open class SysAnnex @JvmOverloads constructor(
        @Cn("文件名")
    var name: String = "",          //显示的名字,友好的名称
        @Cn("标签")
    var tags: List<String> = listOf(),
        @Cn("扩展名")
    var ext: String = "",           //后缀名。
        @Cn("大小")
    var size: Int = 0,              //大小

        @Cn("图像宽度")
    var imgWidth: Int = 0,          // 图像宽度值。
        @Cn("图像高度")
    var imgHeight: Int = 0,         // 图像高度值。

        @Cn("时长")
    var videoTime: Int = 0,          //视频时长,秒

        @Cn("视频封面地址")
    var videoLogoUrl: String = "",      //视频封面地址

        @Cn("存储类型")
    var storageType: String = "",

        /**
     * 下载地址
     */
    @Cn("下载地址")
    var url: String = "",           //下载的路径。没有 host

        @Cn("创建者")
    var creator: IdName = IdName(), //创建者

        @Cn("组")
    var group: String = "",          //所属的大分组，可能是不同的系统。

        @Cn("所属企业")
    var corpId: String = "",        //创建所属企业,如果是 admin,则 id = "admin" , name = "admin 即可

        @Cn("错误消息")
    var errorMsg: String = ""      //文件处理时的错误消息
) : BaseEntity() {
}