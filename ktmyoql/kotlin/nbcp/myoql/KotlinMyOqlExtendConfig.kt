package nbcp.myoql

import nbcp.base.component.BaseImportBeanDefinitionRegistrar
import org.springframework.context.annotation.Configuration
import org.springframework.core.type.filter.AnnotationTypeFilter
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
){


}