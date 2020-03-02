package nbcp.db.mongo

import nbcp.db.IdUrl
import java.time.LocalDate

enum class UserSexEnum(var remark: String) {
    Male("男"),
    Female("女")
}

data class UserIdCardData(
        var photo: IdUrl = IdUrl(),
        var name: String = "",
        var number: String ="",
        var sex: UserSexEnum? = null,
        var birthday: LocalDate? = null,
        var location: String = ""   //身份证地址
)