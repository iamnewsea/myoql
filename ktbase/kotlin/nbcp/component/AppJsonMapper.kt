package nbcp.component

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.*
import nbcp.extend.initObjectMapper

import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

class AppJsonMapper : ObjectMapper(), InitializingBean {

    companion object {
        var allObjectMappers = listOf<ObjectMapper>()
            private set

        var extendObjectMappers = listOf<ObjectMapper>()
    }


    override fun afterPropertiesSet() {
        this.initObjectMapper();

        this.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        this.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        this.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
        this.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);


        allObjectMappers = listOf(this, DbJsonMapper.INSTANCE, WebJsonMapper.INSTANCE, YamlObjectMapper.INSTANCE)
        extendObjectMappers = listOf(DbJsonMapper.INSTANCE, WebJsonMapper.INSTANCE, YamlObjectMapper.INSTANCE)
    }
}