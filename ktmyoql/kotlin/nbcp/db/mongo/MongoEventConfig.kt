package nbcp.db.mongo

import org.springframework.context.ApplicationListener
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.stereotype.Component
import nbcp.base.extend.*
import nbcp.base.utils.JarClassUtil
import nbcp.base.utils.MyUtil
import nbcp.db.db
import nbcp.db.mongo.*
import org.slf4j.LoggerFactory
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.*
import kotlin.jvm.internal.FunctionReference
import kotlin.jvm.internal.MutablePropertyReference1
import kotlin.reflect.KClass
import kotlin.reflect.KFunction2
import kotlin.reflect.KMutableProperty1

//data class MongoUpdateEventObject(var clazz: Class<*>, var idValue: String, var setData: Document) {
//    val newData: LinkedHashMap<String, String> = linkedMapOf()
//    val oldData: LinkedHashMap<String, String> = linkedMapOf()
//}

/**
 * SettingResult
 */
data class SettingResult(
        var result: Boolean = true,
        var extData: Any? = null
)


/**
 * MongoUpdateData
 */
data class MongoUpdateData(
        var entityClass: Class<*>,
        var entityFieldName: String,
        var callback: Method
)

/**
 * MongoEventConfig
 */
@Component
open class MongoEventConfig : ApplicationListener<ContextRefreshedEvent> {
    companion object {
        private val logger by lazy {
            return@lazy LoggerFactory.getLogger(this::class.java)
        }
        //        private val all_entities = mutableListOf<MongoBaseEntity<IMongoDocument>>()
        private val list_entity_settings = mutableListOf<MongoUpdateData>()
        private val list_entity_setteds = mutableListOf<MongoUpdateData>()


        fun addMongoEntityEvent(fieldDefine: KMutableProperty1<*, *>?, callback: KFunction2<MongoUpdateClip<*>, Any, Unit>) {
            list_entity_setteds.addAll(arrayOf(
                    *getEventDatas(fieldDefine, callback)
            ))
        }

        private fun getEventDatas(fieldDefine: KMutableProperty1<*, *>?, callback: KFunction2<MongoUpdateClip<*>, Any, Unit>): Array<MongoUpdateData> {
            var callbackClass = ((callback as FunctionReference).owner as KClass<*>).java

            var callbackMethods = callbackClass.methods.filter {
                it.name == callback.name
            }


            var entityClass = ((fieldDefine as MutablePropertyReference1).owner as KClass<*>).java

            var filedName = fieldDefine?.name ?: ""

            var defineMsg = "${entityClass.name}.${filedName} mongo update事件定义 ${callbackClass.name}.${callback.name}"

            //参数校验。
            if (callbackMethods.size == 0) {
                throw Exception("未找到 ${defineMsg}")
            }

            callbackMethods = callbackMethods.filter {
                if (it.modifiers and Modifier.STATIC != Modifier.STATIC) {
                    throw Exception("${defineMsg} 必须是 static")
                }

                var ps = it.parameters

                //参数个数只能有一个或两个：
                //一个： 拦截对该实体的所有字段的更新
                //两个： 拦截对某一个字段的更新，第二个参数是更新的值。
                if (ps.size != 1 && ps.size != 2) {
                    throw Exception("${defineMsg} 参数必须是2个")
                }

                if (ps[0].type != MongoUpdateClip::class.java) {
                    throw Exception("${defineMsg} 第一个参数必须是 MongoUpdateClip")
                }

                return@filter true
            }

            //参数校验。
            if (callbackMethods.size == 0) {
                throw Exception("${defineMsg} 参数不正确。")
            }


            return callbackMethods.map {
                it.isAccessible = true
                MongoUpdateData(entityClass, filedName, it)
            }.toTypedArray()
        }

    }


//    fun getCollection(collectionName: String): MongoBaseEntity<IMongoDocument>? {
//        return all_entities.find { it.tableName == collectionName }
//    }

    override fun onApplicationEvent(event: ContextRefreshedEvent) {

    }


    fun onUpdating(update: MongoUpdateClip<*>): SettingResult {

        var retResult = SettingResult()

        //先判断是否进行了类拦截.
        var settings = list_entity_settings.filter { it.entityClass == update.moerEntity.entityClass }.sortedBy { it.entityFieldName }
        settings.forEach {
            var ret: Any
            if (it.entityFieldName.isEmpty()) {
                ret = it.callback.invoke(null, update);
            } else {
                var value = update.getSettedData().get(it.entityFieldName)

                if (value == null) {
                    return@forEach
                }

                ret = it.callback.invoke(null, update, value)
            }

            logger.Info { "Mongo entity updatting event: ${it.entityClass.name}.${it.entityFieldName} -> ${it.callback.declaringClass}.${it.callback.name}" }

            if (ret is SettingResult) {
                retResult.result = ret.result
                if (ret.extData != null) {
                    retResult.extData = ret.extData
                }

            } else if (ret is Boolean) {
                retResult.result = ret;
            } else {
                logger.Error { "mongo updating 拦截器返回了不识别的类型:${ret::class.java}" }
            }

            if (retResult.result == false) {
                return retResult;
            }
        }

        return retResult;
    }


    fun onUpdated(update: MongoUpdateClip<*>, extData: Any?) {
        var setted = list_entity_setteds.filter { it.entityClass == update.moerEntity.entityClass }.sortedBy { it.entityFieldName }
        setted.forEach {
            if (it.entityFieldName.isEmpty()) {
                it.callback.invoke(null, update);
            } else {
                var value = update.getSettedData().get(it.entityFieldName)

                if (value == null) {
                    return@forEach
                }

                it.callback.invoke(null, update, value);
            }

            logger.Info { "Mongo entity updatted event: ${it.entityClass.name}.${it.entityFieldName} -> ${it.callback.declaringClass}.${it.callback.name}" }
        }
    }
}