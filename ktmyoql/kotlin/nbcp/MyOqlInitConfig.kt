package nbcp

import nbcp.bean.*
import nbcp.db.mongo.MongoEntityCollector
import nbcp.db.mongo.event.*
import nbcp.db.mongo.service.UploadFileMongoService
import nbcp.db.mybatis.MyBatisRedisCachePointcutAdvisor
import nbcp.db.mybatis.MybatisDbConfig
import nbcp.db.mysql.MySqlDataSourceConfig
import nbcp.db.mysql.service.UploadFileSqlService
import nbcp.db.MyOqlBaseActionLogDefine
import nbcp.db.MyOqlMultipleDataSourceDefine
import nbcp.db.cache.RedisCacheAopService
import nbcp.db.es.*
import nbcp.db.sql.event.*
import nbcp.model.IUploadFileDbService
import nbcp.utils.SpringUtil
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.beans.factory.config.BeanDefinitionHolder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.boot.context.event.ApplicationPreparedEvent
import org.springframework.context.ResourceLoaderAware
import org.springframework.context.annotation.*
import org.springframework.context.event.EventListener
import org.springframework.core.io.ResourceLoader
import org.springframework.core.type.AnnotationMetadata
import org.springframework.core.type.filter.AssignableTypeFilter
import org.springframework.stereotype.Component

@Configuration
@Import(
    value = [
        SqlEntityCollector::class,
        MongoEntityCollector::class,
        EsEntityCollector::class,


        MyOqlMongoJsonSerializerConfig::class,
        MyOqlMongoConfig::class,
        MyOqlRabbitMqConfig::class,
        MyOqlRedisConfig::class,

        //下面是数据库配置
        MySqlDataSourceConfig::class,
        MybatisDbConfig::class,

        MyBatisRedisCachePointcutAdvisor::class,
        RedisCacheAopService::class,

        MyOqlBeanImporter::class
    ]
)
//@ComponentScan("nbcp.db.mongo.event")
class MyOqlInitConfig {

    @EventListener
    fun prepared(ev: ApplicationPreparedEvent) {
        val msg = "myoql"

        val flyways =  SpringUtil.getBeanWithNull(FlywayInitCollector::class.java)
        if( flyways != null ){
            flyways.syncVersionWork();
        }
    }
}

@Component
class MyOqlBeanImporter : ImportBeanDefinitionRegistrar, ResourceLoaderAware, BeanFactoryAware {
    private lateinit var resourceLoader: ResourceLoader
    private lateinit var beanFactory: BeanFactory;


    /**
     * 动态置顶扫描包路径下特殊的类加载到Bean中
     * @param importingClassMetadata
     * @param registry
     */
    override fun registerBeanDefinitions(
        importingClassMetadata: AnnotationMetadata?,
        registry: BeanDefinitionRegistry?
    ) {
        // 当前MyClassPathBeanDefinitionScanner已被修改为扫描带有指定注解的类
        val scanner = MyClassPathBeanDefinitionScanner(registry, false)
        scanner.resourceLoader = resourceLoader
        scanner.registerFilters()
        scanner.doScan("nbcp")
    }

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory;
    }

    override fun setResourceLoader(resourceLoader: ResourceLoader) {
        this.resourceLoader = resourceLoader
    }
}


/**
 * 类 名: MyClassPathBeanDefinitionScanner
 * 描 述: 定义一个扫描器，指定需要扫描的标识
 *
 * @author: jiaYao
 */
class MyClassPathBeanDefinitionScanner(registry: BeanDefinitionRegistry?, useDefaultFilters: Boolean) :
    ClassPathBeanDefinitionScanner(registry, useDefaultFilters) {
    /**
     * @addIncludeFilter 将自定义的注解添加到扫描任务中
     * @addExcludeFilter 将带有自定义注解的类 ，不加载到容器中
     */
    fun registerFilters() {
        /**
         * TODO addIncludeFilter  满足任意includeFilters会被加载
         */

        addIncludeFilter(AssignableTypeFilter(IMongoEntityQuery::class.java))
        addIncludeFilter(AssignableTypeFilter(IMongoEntityInsert::class.java))
        addIncludeFilter(AssignableTypeFilter(IMongoEntityUpdate::class.java))
        addIncludeFilter(AssignableTypeFilter(IMongoEntityDelete::class.java))
        addIncludeFilter(AssignableTypeFilter(IMongoDataSource::class.java))

        addIncludeFilter(AssignableTypeFilter(ISqlEntitySelect::class.java))
        addIncludeFilter(AssignableTypeFilter(ISqlEntityInsert::class.java))
        addIncludeFilter(AssignableTypeFilter(ISqlEntityUpdate::class.java))
        addIncludeFilter(AssignableTypeFilter(ISqlEntityDelete::class.java))
        addIncludeFilter(AssignableTypeFilter(ISqlDataSource::class.java))


        addIncludeFilter(AssignableTypeFilter(IEsEntityQuery::class.java))
        addIncludeFilter(AssignableTypeFilter(IEsEntityInsert::class.java))
        addIncludeFilter(AssignableTypeFilter(IEsEntityUpdate::class.java))
        addIncludeFilter(AssignableTypeFilter(IEsEntityDelete::class.java))
        addIncludeFilter(AssignableTypeFilter(IEsDataSource::class.java))


        addIncludeFilter(AssignableTypeFilter(IUploadFileDbService::class.java))


        addIncludeFilter(AssignableTypeFilter(MyOqlMultipleDataSourceDefine::class.java))
        addIncludeFilter(AssignableTypeFilter(MyOqlBaseActionLogDefine::class.java))


        /**
         * TODO addExcludeFilter 同样的满足任意excludeFilters不会被加载
         */
        // addExcludeFilter(new AnnotationTypeFilter(MyService.class));
    }

    /**
     * 重写类扫描包路径加载器，调用父类受保护的扫描方法 doScan
     * @param basePackages
     * @return
     */
    public override fun doScan(vararg basePackages: String): Set<BeanDefinitionHolder> {
        return super.doScan(*basePackages)
    }
}