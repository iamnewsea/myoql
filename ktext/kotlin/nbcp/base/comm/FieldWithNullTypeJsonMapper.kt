package nbcp.comm

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import nbcp.utils.*

/**
 * 使用 字段值 方式序列化JSON，应用在数据库的场景中。
 */
open class FieldWithNullTypeJsonMapper : JsonBaseObjectMapper() {
    init {
        this.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        this.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        this.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
        this.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        this.registerModule(SpringUtil.getBean<MyJsonModule>());
    }


    companion object {
        /**
         * 创建只输出非Null且非Empty(如List.isEmpty)的属性到Json字符串的Mapper,建议在外部接口中使用.
         */
        val instance: FieldWithNullTypeJsonMapper by lazy { return@lazy FieldWithNullTypeJsonMapper() }
    }
}