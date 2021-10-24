package nbcp.myOqlBeanProcessor



import nbcp.comm.clazzesIsSimpleDefine
import nbcp.component.BaseJsonMapper
import nbcp.component.DbJsonMapper
import nbcp.db.mongo.*
import nbcp.utils.SpringUtil
import org.bson.Document
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Component

@Component
class MyOqlJsonConfig : BeanPostProcessor {
    companion object {
        private var inited = false;
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        if (inited == false) {
            inited = true;

            init_app();
        }

        return super.postProcessBeforeInitialization(bean, beanName)
    }

    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
        var ret = super.postProcessAfterInitialization(bean, beanName)

        return ret;
    }


    /**
     * 在所有Bean初始化之前执行
     */
    private fun init_app() {
        clazzesIsSimpleDefine.add(ObjectId::class.java)

        BaseJsonMapper.addSerializer(ObjectId::class.java, ObjectIdJsonSerializer(), ObjectIdJsonDeserializer())
        DbJsonMapper.addSerializer(Document::class.java, DocumentJsonSerializer(), DocumentJsonDeserializer())
    }


}