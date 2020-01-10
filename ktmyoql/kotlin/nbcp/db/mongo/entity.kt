package nbcp.db.mongo

import nbcp.db.IdName
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime


//系统附件表
@Document
@MongoEntityGroup("base")
data class SysAnnex(
        var name: String = "",          //显示的名字,友好的名称
        var tags: List<String> = listOf(),
        var ext: String = "",           //后缀名。
        var size: Int = 0,              //大小
        var checkCode: String = "",     //Md5,Sha1
        var imgWidth: Int = 0,          // 图像宽度值。
        var imgHeight: Int = 0,         // 图像宽度值。
        var url: String = "",           //下载的路径。没有 host

        var createBy: IdName = IdName(), //创建者
        var corpId: String = "",    //创建所属企业,如果是 admin,则 id = "admin" , name = "admin 即可
        var errorMsg: String = "",      //文件处理时的错误消息
        var createAt: LocalDateTime = LocalDateTime.now()
) : IMongoDocument() {
}

@Document
@MongoEntityGroup("base")
data class SysCity(
        var code: Int = 0,
        var pcode: Int = 0,
        var name: String = "",
        var fullName: String = "",
        var level: Int = 0,
        var lng: Float = 0F, //经度
        var lat: Float = 0F, //纬度
        var pinyin: String = "",
        var telCode: String = "",   //010
        var postCode: String = ""   //邮编
) : IMongoDocument()

@Document
@MongoEntityGroup("base")
data class SysLog(
        var msg: String = "",
        var creatAt: LocalDateTime = LocalDateTime.now(),
        var createBy: String = "",
        var type: String = "",
        var clientIp: String = "",
        var module: String = "",
        var remark: String = ""
) : IMongoDocument() {
}

//存放删除的数据。
@Document
@MongoEntityGroup("base")
data class SysDustbin(
        var table: String = "",
        var remark: String = "",
        var creator: IdName = IdName(),
        var data: Any = Object(),
        var createAt: LocalDateTime = LocalDateTime.now()
) : IMongoDocument()

