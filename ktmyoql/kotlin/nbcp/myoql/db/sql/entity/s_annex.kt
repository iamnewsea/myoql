package nbcp.myoql.db.sql.entity

import nbcp.base.db.IdName
import nbcp.base.db.annotation.Cn
import nbcp.base.db.annotation.DbEntityGroup
import nbcp.myoql.db.sql.annotation.SqlColumnType
import nbcp.myoql.db.sql.annotation.SqlSpreadColumn
import nbcp.myoql.db.sql.base.AutoIdSqlBaseEntity

@DbEntityGroup("SqlBase")
@Cn("附件")
open class s_annex @JvmOverloads constructor(
        @Cn("附件名称")
    var name: String = "",          //显示的名字,友好的名称
        @Cn("附件标签")
    var tags: List<String> = listOf(),
        @Cn("后缀名")
    @SqlColumnType("varchar(8)")
    var ext: String = "",           //后缀名。

        @Cn("大小")
    var size: Int = 0,              //大小

        @Cn("图像宽度值")
    var imgWidth: Int = 0,          // 图像宽度值。
        @Cn("图像高度值")
    var imgHeight: Int = 0,         // 图像高度值。

        /**
     * 尽量的形式是  //host/路径,  兼容 http,https
     */
    @SqlColumnType("varchar(255)")
    @Cn("下载地址")
    var url: String = "",           //下载的路径。有 host

        @Cn("创建者")
    @SqlSpreadColumn()
    var creator: IdName = IdName(), //创建者

        @Cn("文件组")
    var group: String = "",
        @Cn("所属企业Id")
    var corpId: String = "", //企业Id

        @Cn("错误消息")
    @SqlColumnType("varchar(255)")
    var errorMsg: String = ""       //文件处理时的错误消息

) : AutoIdSqlBaseEntity() {
}