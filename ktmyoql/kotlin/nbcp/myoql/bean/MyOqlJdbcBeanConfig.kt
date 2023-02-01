//package nbcp.myoql.bean
//
//import org.slf4j.LoggerFactory
//import org.springframework.beans.factory.config.BeanPostProcessor
//import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
//import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
//import org.springframework.stereotype.Component
//
//@Component
//@ConditionalOnClass(org.mariadb.jdbc.Driver::class)
//class MyOqlJdbcBeanConfig : BeanPostProcessor {
//    companion object {
//        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
//    }
//
//    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any? {
//        var ret = super.postProcessAfterInitialization(bean, beanName)
//
//        if (bean is DataSourceProperties) {
//            if (bean.driverClassName.isNullOrEmpty()) {
//                bean.driverClassName = "org.mariadb.jdbc.Driver"
//            }
//        }
//
//        return ret;
//    }
//}