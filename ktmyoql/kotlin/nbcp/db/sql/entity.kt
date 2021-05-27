package nbcp.db.sql.entity

import nbcp.db.*
import java.time.LocalDateTime
import nbcp.db.sql.*

@DbEntityGroup("SqlBase")
@DbUks("id")
@SqlRks("corpId")
open class s_annex(
    var name: String = "",          //显示的名字,友好的名称
    var tags: String = "",
    var ext: String = "",           //后缀名。
    var size: Int = 0,              //大小
    var checkCode: String = "",     //Md5,Sha1
    var imgWidth: Int = 0,          // 图像宽度值。
    var imgHeight: Int = 0,         // 图像宽度值。
    var url: String = "",           //下载的路径。没有 host

    @SqlSpreadColumn()
    var creator: IdName = IdName(), //创建者
    var corpId: String = "", //企业Id
    var errorMsg: String = ""       //文件处理时的错误消息

) : AutoIdSqlDbEntity() {
}


@DbEntityGroup("SqlBase")
open class s_log(
    var module: String = "", //模块
    var type: String = "",  //类型
    var key: String = "",   //实体标志, 查询用： module + key
    var msg: String = "",   //消息
    var data: String = "",
    var remark: String = "",
    var clientIp: String = "",
    var creatorId: String = ""
) : AutoNumberSqlDbEntity()


@DbEntityGroup("SqlBase")
open class s_city(
    @DbKey
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
) : ISqlDbEntity

@DbEntityGroup("SqlBase")
open class s_dustbin(
    var table: String = "",
    var remark: String = "",
    @SqlSpreadColumn()
    var creator: IdName = IdName(),
    var data: String = "",  //保存 JSON 数据
) : AutoNumberSqlDbEntity()