package nbcp.component

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.*
import nbcp.extend.initObjectMapper

import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component


@Primary
class AppJsonMapper : ObjectMapper(), InitializingBean {

    companion object{
        var extendMappers = listOf<ObjectMapper>()
            private set
    }


    override fun afterPropertiesSet() {
        this.initObjectMapper();

        this.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        this.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        this.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
        this.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);


        extendMappers = listOf(DbJsonMapper.INSTANCE, WebJsonMapper.INSTANCE,YamlObjectMapper.INSTANCE)
    }
}