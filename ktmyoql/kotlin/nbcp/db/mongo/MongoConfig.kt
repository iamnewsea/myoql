package nbcp.db.mongo

import nbcp.comm.*
import nbcp.db.AbstractMyOqlMultipleDataSourceProperties
import nbcp.db.MongoCrudEnum
import nbcp.utils.SpringUtil
import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.autoconfigure.AutoConfigureAfter

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.*
import org.springframework.core.convert.support.GenericConversionService
import org.springframework.core.type.AnnotatedTypeMetadata
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.MongoTransactionManager
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import org.springframework.stereotype.Component


/**
 * https://docs.spring.io/spring-data/data-mongodb/docs/current/reference/html/index.html
 */
//@Configuration
//@Conditional(ExistsMongoDataSourceConfigCondition::class)
//@AutoConfigureAfter(MongoAutoConfiguration::class)
//class MyOqlMongoDbConfig {
//
//    @Bean
//    fun mongoContext(): MongoMappingContext {
//        var ret = MongoMappingContext()
////        ret.setInitialEntitySet(setOf(Document::class.java))
//        return ret;
//    }
//
//    @Primary
//    @Bean
//    @Throws(Exception::class)
//    fun mongoPrimaryTemplate(dbFactory: MongoDatabaseFactory): MongoTemplate {
//        //remove _class
//        val converter =
//            MappingMongoConverter(DefaultDbRefResolver(dbFactory), SpringUtil.getBean<MongoMappingContext>())
//            converter.setTypeMapper(DefaultMongoTypeMapper(null));
//
//        (converter.conversionService as GenericConversionService).addConverter(Date2LocalDateTimeConverter())
//
//        return MongoTemplate(dbFactory, converter)
//    }
//
//
//    /**
//     * 事务支持。在需要事务的方法上添加注解:@Transactional,使用 mongotemplate 即可。
//     */
//    @Bean
//    fun transactionManager(dbFactory: MongoDatabaseFactory): MongoTransactionManager {
//        return MongoTransactionManager(dbFactory)
//    }
//}

//
//class ExistsMongoDataSourceConfigCondition : Condition {
//    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
//        return (context.environment.getProperty("spring.data.mongodb.uri") != null ||
//            context.environment.getProperty("spring.data.mongodb.database") != null)
//    }
//
//}

/**
 * 定义Mongo不同的数据源
 *
 *app:
 *  mongo:
 *    yapi-ds: mongodb://dev:123@mongo:27017/yapi
 *    db:
 *      yapi:
 *          - group
 *          - project
 *          - api
 *          - interface_cat
 *    read:
 *      yapi-read:
 *          - group
 *          - project
 */
@ConfigurationProperties(prefix = "app.mongo.ds")
@Component
class MongoCollectionDataSource : AbstractMyOqlMultipleDataSourceProperties() {
}



@ConfigurationProperties(prefix = "app.mongo.log")
@Component
class MongoCollectionLogProperties :  InitializingBean{
    var find: List<String> = listOf()
    var insert: List<String> = listOf()
    var update: List<String> = listOf()
    var remove: List<String> = listOf()



    fun getFindLog(tableDbName: String): Boolean {
        if (find.contains(tableDbName.toLowerCase())) return true;
        if (logDefault.contains(MongoCrudEnum.find)) return true;
        return false;
    }

    fun getInsertLog(tableDbName: String): Boolean {
        if (insert.contains(tableDbName.toLowerCase())) return true;
        if (logDefault.contains(MongoCrudEnum.insert)) return true;
        return false
    }

    fun getUpdateLog(tableDbName: String): Boolean {
        if (update.contains(tableDbName.toLowerCase())) return true;
        if (logDefault.contains(MongoCrudEnum.update)) return true;
        return false;
    }

    fun getRemoveLog(tableDbName: String): Boolean {
        if (remove.contains(tableDbName.toLowerCase())) return true;
        if (logDefault.contains(MongoCrudEnum.remove)) return true;
        return false;
    }


    val logDefault by lazy {
        var value = config.getConfig("app.mongo.log-default").AsString().trim();
        if (value.HasValue) {
            if (value == "*") {
                return@lazy MongoCrudEnum::class.java.GetEnumList()
            }
            return@lazy MongoCrudEnum::class.java.GetEnumList(value)
        }
        return@lazy listOf<MongoCrudEnum>()
    }


    override fun afterPropertiesSet() {
        find = find.map { it.toLowerCase() }
        insert = insert.map { it.toLowerCase() }
        update = update.map { it.toLowerCase() }
        remove = remove.map { it.toLowerCase() }
    }
}