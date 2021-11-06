package nbcp.component

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat
import java.util.*

@Primary
@Component()
class AppJsonMapper : BaseJsonMapper(), InitializingBean {
    companion object {
        private val sers: MutableList<SimpleModule> = mutableListOf()
        private val desers: MutableList<SimpleModule> = mutableListOf()

        fun <T> addSerializer(type: Class<T>, ser: JsonSerializer<T>, deser: JsonDeserializer<T>) {
            if (sers.any { it.moduleName == type.name } == false) {
                var item = SimpleModule(type.name)
                item.addSerializer(type, ser)
                sers.add(item);
            }
            if (desers.any { it.moduleName == type.name } == false) {
                var item = SimpleModule(type.name)
                item.addDeserializer(type, deser)
                desers.add(item)
            }
        }
    }

    override fun afterPropertiesSet() {
        this.init();

        this.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        this.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        this.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
        this.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        BaseJsonMapper.sers.forEach {
            this.registerModule(it);
        }

        BaseJsonMapper.desers.forEach {
            this.registerModule(it);
        }

        sers.forEach {
            this.registerModule(it);
        }

        desers.forEach {
            this.registerModule(it);
        }
    }

}