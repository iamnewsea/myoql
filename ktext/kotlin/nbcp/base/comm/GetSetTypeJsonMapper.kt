package nbcp.comm

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * 使用 GET，SET 方式序列化JSON，应用在Web返回值的场景中。
 */
class GetSetTypeJsonMapper : GetSetWithNullTypeJsonMapper() {

    init {
        //在某些时候，如 mongo.aggregate.group._id 时， null 。
        this.setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }

    companion object {
        /**
         * 创建只输出非Null且非Empty(如List.isEmpty)的属性到Json字符串的Mapper,建议在外部接口中使用.
         */
        val instance: GetSetTypeJsonMapper by lazy { return@lazy GetSetTypeJsonMapper() }
    }
}