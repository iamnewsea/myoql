package nbcp.comm

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component
import nbcp.utils.*
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.Primary
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.LinkedHashMap

/**
 * 使用 字段值 方式序列化JSON，应用在数据库的场景中。
 */
@Primary
@Component
@DependsOn(value = arrayOf("springUtil"))
open class DefaultMyJsonMapper : ObjectMapper(), InitializingBean {
    override fun afterPropertiesSet() {
        this.setStyle()
    }

    companion object {
        val sers: MutableList<SimpleModule> = mutableListOf()
        val desers: MutableList<SimpleModule> = mutableListOf()

        @JvmStatic
        fun get(): ObjectMapper {
            var styles = scopes.getScopeTypes<JsonStyleEnumScope>()
            return get(*styles.toTypedArray());
        }

        @JvmStatic
        fun get(vararg styles: JsonStyleEnumScope): ObjectMapper {
            if (styles.isEmpty()) return SpringUtil.getBean<DefaultMyJsonMapper>()
            return ObjectMapper().setStyle(*styles)
        }

        @JvmStatic
        fun <T> addSerializer(type: Class< T>, ser: JsonSerializer<T>,deser: JsonDeserializer< T>) {
            if (this.sers.any { it.moduleName == type.name } == false) {
                var item = SimpleModule(type.name)
                item.addSerializer(type, ser)
                this.sers.add(item);
            }
            if (this.desers.any { it.moduleName == type.name } == false) {
                var item = SimpleModule(type.name)
                item.addDeserializer(type, deser)
                this.desers.add(item)
            }
        }
    }

}


