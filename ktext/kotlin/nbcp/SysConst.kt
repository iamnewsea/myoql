package nbcp.comm

import nbcp.utils.MyUtil
import nbcp.utils.SpringUtil
import org.springframework.beans.factory.Aware
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.ImportSelector
import org.springframework.core.type.AnnotationMetadata
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.lang.annotation.Inherited
import java.nio.charset.Charset


/**
 * Created by yuxh on 2018/11/13
 */

val utf8: Charset = Charset.forName("utf-8")


//换行符符
val line_break: String = System.getProperty("line.separator")


