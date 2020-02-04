package nbcp.base.extend

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import nbcp.base.utils.SpringUtil
import java.text.SimpleDateFormat
import java.util.*

abstract class JsonBaseObjectMapper : ObjectMapper(){
    fun setDefaultConfig(){
        // 设置输出时包含属性的风格
        this.findAndRegisterModules();
        //在某些时候，如 mongo.aggregate.group._id 时， null 。
        //序列化 null的。
//        this.setSerializationInclusion(JsonInclude.Include.NON_NULL)

        // 允许单引号、允许不带引号的字段名称
        this.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
        this.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
        this.configure(MapperFeature.USE_STD_BEAN_NAMING, true)


        this.setDateFormat(SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        // 设置输入时忽略在JSON字符串中存在但Java对象实际没有的属性
        this.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
//        })
        // 设置时区
        this.setTimeZone(TimeZone.getTimeZone("GMT+:08:00"))

        this.registerKotlinModule()
    }

}