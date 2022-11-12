package nbcp.myoql.db.sql.entity

import nbcp.base.db.Cn
import nbcp.base.db.DbEntityGroup
import nbcp.base.db.DbEntityIndex
import nbcp.base.db.IdName
import nbcp.myoql.db.sql.base.AutoIdSqlBaseEntity
import nbcp.myoql.db.sql.annotation.*
import java.io.Serializable

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


@DbEntityGroup("SqlBase")
@Cn("日志")
open class s_log @JvmOverloads constructor(
    @Cn("模块名称")
    var module: String = "", //模块

    @Cn("类型")
    var type: String = "",  //类型

    @Cn("标签")
    var tags: List<String> = listOf(),   //实体标志, 查询用： module + key

    @Cn("消息")
    @SqlColumnType("varchar(255)")
    var msg: String = "",   //消息

    @Cn("请求")
    @SqlColumnType("varchar(512)")
    var request: String = "",

    @Cn("数据")
    @SqlColumnType("varchar(512)")
    var data: String = "",

    @Cn("响应")
    @SqlColumnType("varchar(512)")
    var response: String = "",

    @Cn("创建者")
    @SqlSpreadColumn()
    var creator: IdName = IdName()
) : AutoIdSqlBaseEntity()


@DbEntityGroup("SqlBase")
@DbEntityIndex("code", unique = true)
@Cn("城市")
open class s_city @JvmOverloads constructor(
    @Cn("城市编码")
    var code: Int = 0,
    @Cn("城市短名称")
    var shortName: String = "",    // 城市控件使用 simpleName
    @Cn("城市全称")
    var name: String = "",          //界面常用全称
    @Cn("级别")
    var level: Int = 0,
    @Cn("经度")
    var lng: Float = 0F, //经度
    @Cn("纬度")
    var lat: Float = 0F, //纬度
    @Cn("拼音")
    var pinyin: String = "",
    @Cn("电话码")
    var telCode: String = "",
    @Cn("邮编")
    var postCode: String = "",
    @Cn("父级码")
    var pcode: Int = 0
) : Serializable


/**
 * 删除数据 前，先把数据放在这里，再删除原数据！
 *
 */
@DbEntityGroup("SqlBase")
@Cn("数据垃圾箱")
open class s_dustbin @JvmOverloads constructor(
    @Cn("表名")
    var table: String = "",

    @Cn("备注")
    var remark: String = "",

    @Cn("操作者")
    @SqlSpreadColumn()
    var creator: IdName = IdName(),

    @Cn("数据")
    @SqlColumnType("varchar(255)")
    var data: String = "",  //保存 JSON 数据
) : AutoIdSqlBaseEntity()