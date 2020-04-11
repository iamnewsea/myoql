package nbcp.db.es

import nbcp.base.extend.getLatestScope
import nbcp.base.extend.scopes
import nbcp.base.utils.SpringUtil
import nbcp.db.db
import org.elasticsearch.client.RestClient
import java.io.Serializable

/**
 * Created by udi on 17-4-24.
 */


//collectionClazz 是集合类型。
open class EsClipBase(var collectionName: String) : Serializable {

    val esTemplate: RestClient by lazy {
        return@lazy SpringUtil.getBean<RestClient>()
    }

}

interface IEsWhereable {

}
