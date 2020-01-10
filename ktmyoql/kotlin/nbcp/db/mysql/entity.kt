package nbcp.db.mysql

import nbcp.db.sql.*
import java.time.LocalDateTime

@SqlEntityGroup("base")
@SqlUks("id")
@SqlRks("corp_id")
data class s_annex(
        var id: String = "",
        var name: String = "",          //显示的名字,友好的名称
//        var localPath: String = "",     //本地文件路径以及文件名。 用来删除。
        var ext: String = "",           //后缀名。
        var size: Int = 0,              //大小
        var checkCode: String = "",     //Md5,Sha1
        var imgWidth: Int = 0,          // 图像宽度值。
        var imgHeight: Int = 0,         // 图像宽度值。
        var url: String = "",           //下载的路径。没有 host

//        @SqlFk("s_user", "id")
        var createby_id: Int = 0, //创建者
        var createby_name: String = "", //创建者
        var corp_id: String = "", //企业Id
        var errorMsg: String = "",      //文件处理时的错误消息
        var createAt: LocalDateTime = LocalDateTime.now()
) : IBaseDbEntity() {
}


@SqlEntityGroup("base")
@SqlUks("id")
data class s_log(
//        @SqlAutoIncrementKey
        var id: String = "",
        var msg: String = "",
        var creatAt: LocalDateTime = LocalDateTime.now(),
        var createBy_id: String = "",
        var createBy_name: String = "",
        var corp_id: String = "",
        var type: String = "",
        var clientIp: String = "",
        var module: String = "",
        var remark: String = ""
) : IBaseDbEntity()