package nbcp.base.json

import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.DeserializationConfig
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleDeserializers

class BaseEnumDeserializers : SimpleDeserializers() {
    override fun findEnumDeserializer(
        type: Class<*>,
        config: DeserializationConfig,
        beanDesc: BeanDescription
    ): JsonDeserializer<*>? {
          if (!type.isEnum) {
           return  null
        }

        return BaseEnumJsonDeserializer(type)
    } //省略部分接口
}