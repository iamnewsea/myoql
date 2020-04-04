package nbcp.comm

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.PropertyAccessor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import nbcp.base.utils.SpringUtil
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Primary

/**
 * 使用 字段值 方式序列化JSON，应用在数据库的场景中。
 */
@Primary
@Component
@DependsOn(value = arrayOf("javascriptDateModule","springUtil"))
open class FieldTypeJsonMapper : JsonBaseObjectMapper(), InitializingBean {
    init {
        this.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        this.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        this.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
        this.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        //在某些时候，如 mongo.aggregate.group._id 时， null 。
        //默认只序列化 not null 的。
        this.setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }

    companion object {
        /**
         * 创建只输出非Null且非Empty(如List.isEmpty)的属性到Json字符串的Mapper,建议在外部接口中使用.
         */
        val instance: FieldTypeJsonMapper by lazy { SpringUtil.getBean<FieldTypeJsonMapper>() }

        internal val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }


    override fun afterPropertiesSet() {
        var dateModule = SpringUtil.getBean<JavascriptDateModule>();
        this.registerModule(dateModule);
    }
}