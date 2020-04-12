package nbcp.db


import nbcp.comm.*
import nbcp.utils.*
import org.springframework.core.convert.support.GenericConversionService
import nbcp.db.es.*
/**
 * 请使用 db.mongo
 */
object db_es {

    //所有的组。
    val groups = mutableSetOf<IDataGroup>()

    //     val sqlEvents = SpringUtil.getBean<SqlEventConfig>();
    val esEvents by lazy {
        return@lazy SpringUtil.getBean<EsEntityEvent>();
    }
}