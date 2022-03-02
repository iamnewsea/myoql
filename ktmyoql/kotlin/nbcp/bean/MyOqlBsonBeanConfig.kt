package nbcp.bean

import com.mongodb.MongoClientSettings
import nbcp.MyOqlInitConfig
import nbcp.comm.HasValue
import nbcp.comm.clazzesIsSimpleDefine
import nbcp.comm.getStringValue
import nbcp.component.BaseJsonMapper
import nbcp.component.DbJsonMapper
import nbcp.db.mongo.*
import nbcp.utils.JsUtil
import nbcp.utils.SpringUtil
import org.bson.Document
import org.bson.UuidRepresentation
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.mongo.MongoProperties
import org.springframework.core.convert.support.GenericConversionService
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.stereotype.Component

@Component
@ConditionalOnClass(ObjectId::class)
class MyOqlBsonBeanConfig : BeanPostProcessor {
    companion object {
        private var inited = false;
    }

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        if (inited == false) {
            inited = true;

            init_app();
        }

        return super.postProcessBeforeInitialization(bean, beanName)
    }

    private fun init_app() {
        clazzesIsSimpleDefine.add(ObjectId::class.java)

        SpringUtil.context.getBeansOfType(BaseJsonMapper::class.java).values.forEach { mapper ->
            mapper.addTypeModule(ObjectId::class.java, ObjectIdJsonSerializer(), ObjectIdJsonDeserializer())
            if (mapper is DbJsonMapper) {
                mapper.addTypeModule(Document::class.java, DocumentJsonSerializer(), DocumentJsonDeserializer())
            }
        }
    }
}