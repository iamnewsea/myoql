package nbcp.db.sql.entity

import nbcp.db.DbEntityGroup
import nbcp.db.Key
import java.time.LocalDateTime
import nbcp.db.sql.*

@DbEntityGroup("SqlBase")
@SqlUks("id")
@SqlRks("corpId")
data class s_annex(
        var id: String = "",
        var name: String = "",          //显示的名字,友好的名称
        var tags: String = "",
        var ext: String = "",           //后缀名。
        var size: Int = 0,              //大小
        var checkCode: String = "",     //Md5,Sha1
        var imgWidth: Int = 0,          // 图像宽度值。
        var imgHeight: Int = 0,         // 图像宽度值。
        var url: String = "",           //下载的路径。没有 host

//        @SqlFk("s_user", "id")
        var creator_id: String = "", //创建者
        var creator_name: String = "", //创建者
        var corpId: String = "", //企业Id
        var errorMsg: String = "",      //文件处理时的错误消息
        var createAt: LocalDateTime = LocalDateTime.now()
) : ISqlDbEntity {
}


@DbEntityGroup("SqlBase")
data class s_log(
        @Key
//        @SqlAutoIncrementKey
        var id: String = "",
        var module: String = "", //模块
        var type: String = "",  //类型
        var key: String = "",   //实体标志, 查询用： module + key
        var msg: String = "",   //消息
        var data: String = "",
        var remark: String = "",
        var clientIp: String = "",
        var creatAt: LocalDateTime = LocalDateTime.now(),
        var creatorId: String = ""
) : ISqlDbEntity

@DbEntityGroup("SqlBase")
data class s_dustbin(
        @Key
        var id: String = "",
        var table: String = "",
        var remark: String = "",
        var creator_id: String = "",
        var creator_name: String = "",
        var data: String = "",  //保存 JSON 数据
        var createAt: LocalDateTime = LocalDateTime.now()
) : ISqlDbEntity