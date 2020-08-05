@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.comm

import nbcp.utils.MyUtil
import nbcp.utils.SpringUtil
import org.slf4j.Logger
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


object config {
    val uploadHost get() = SpringUtil.context.environment.getProperty("app.upload.host") ?: "";

    private var _debug: Boolean? = null;
    val debug: Boolean
        get() {
            if (_debug != null) {
                return _debug!!;
            }

            if (SpringUtil.isInited == false) return false;
            _debug = SpringUtil.context.environment.getProperty("debug").AsBoolean();
            return _debug ?: false;
        }

    val mybatisPackage get() = SpringUtil.context.environment.getProperty("app.mybatis.package") ?: ""

}

/**
 * 获取参数 debug，如果是调试模式，那么查询日志显示结果集，会显示插入数据
 */
val Logger.debug get() = config.debug;