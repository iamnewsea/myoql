package nbcp.comm

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import nbcp.base.utils.SpringUtil

/**
 * 使用 GET，SET 方式序列化JSON，应用在Web返回值的场景中。
 */
open class GetSetWithNullTypeJsonMapper : JsonBaseObjectMapper() {

    init {
        this.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.DEFAULT);
        this.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.DEFAULT);
        this.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.DEFAULT);
        this.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.NONE);

        this.registerModule(SpringUtil.getBean<JavascriptDateModule>());
    }

    companion object {
        /**
         * 创建只输出非Null且非Empty(如List.isEmpty)的属性到Json字符串的Mapper,建议在外部接口中使用.
         */
        val instance: GetSetWithNullTypeJsonMapper by lazy { return@lazy GetSetWithNullTypeJsonMapper() }
    }
}