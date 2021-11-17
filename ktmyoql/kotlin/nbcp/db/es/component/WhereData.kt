package nbcp.db.es

import nbcp.comm.*
import nbcp.db.*
import java.io.Serializable


/**
 */
class WhereData : JsonMap {

    constructor() : super() {
    }

    constructor(data: Map<String, Any?>) : super(data) {
    }

    constructor(vararg pairs: Pair<String, Any?>) : super(*pairs) {
    }

    constructor(data: Collection<Pair<String, Any?>>) : super(data) {
    }

    companion object {
        @JvmStatic
        fun eq(key: String, value: Any?): WhereData {
            var ret = WhereData("term" to JsonMap(
                    key to value
            ));

            return ret;
        }
    }
}
