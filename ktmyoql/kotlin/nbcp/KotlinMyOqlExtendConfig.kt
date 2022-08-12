package nbcp

import nbcp.component.BaseImportBeanDefinitionRegistrar
import nbcp.db.mongo.event.*
import nbcp.db.MyOqlBaseActionLogDefine
import nbcp.db.MyOqlMultipleDataSourceDefine
import nbcp.db.es.*
import nbcp.db.sql.event.*
import nbcp.model.IUploadFileDbService
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.beans.factory.config.BeanDefinitionHolder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.context.ResourceLoaderAware
import org.springframework.context.annotation.*
import org.springframework.core.io.ResourceLoader
import org.springframework.core.type.AnnotationMetadata
import org.springframework.core.type.filter.AnnotationTypeFilter
import org.springframework.core.type.filter.AssignableTypeFilter
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service


//@Import(MyOqlInitConfig::class)
//@Component
@Configuration
class KotlinMyOqlExtendConfig : BaseImportBeanDefinitionRegistrar(
    "nbcp",
    listOf(
//        AssignableTypeFilter(IMongoEntityQuery::class.java),
//        AssignableTypeFilter(IMongoEntityInsert::class.java),
//        AssignableTypeFilter(IMongoEntityUpdate::class.java),
//        AssignableTypeFilter(IMongoEntityDelete::class.java),
//        AssignableTypeFilter(IMongoEntityAggregate::class.java),
//        AssignableTypeFilter(IMongoCollectionVarName::class.java),
//        AssignableTypeFilter(ISqlEntitySelect::class.java),
//        AssignableTypeFilter(ISqlEntityInsert::class.java),
//        AssignableTypeFilter(ISqlEntityUpdate::class.java),
//        AssignableTypeFilter(ISqlEntityDelete::class.java),
//        AssignableTypeFilter(ISqlDataSource::class.java),
//        AssignableTypeFilter(IEsEntityQuery::class.java),
//        AssignableTypeFilter(IEsEntityInsert::class.java),
//        AssignableTypeFilter(IEsEntityUpdate::class.java),
//        AssignableTypeFilter(IEsEntityDelete::class.java),
//        AssignableTypeFilter(IEsDataSource::class.java),
//        AssignableTypeFilter(IUploadFileDbService::class.java),
//        AssignableTypeFilter(MyOqlMultipleDataSourceDefine::class.java),
//        AssignableTypeFilter(MyOqlBaseActionLogDefine::class.java),
        AnnotationTypeFilter(Service::class.java),
        AnnotationTypeFilter(Component::class.java),
        AnnotationTypeFilter(Configuration::class.java)
    )
)

