package nbcp.myoql.db.sql.entity

import nbcp.base.db.annotation.Cn
import nbcp.base.db.annotation.DbEntityGroup
import nbcp.base.db.annotation.DbEntityIndex
import java.io.Serializable

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