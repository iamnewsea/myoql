package nbcp.myoql.db.es.component

import nbcp.base.comm.JsonMap


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

//    companion object {
//        @JvmStatic
//        fun eq(key: String, value: Any?): WhereData {
//            var ret = WhereData("term" to JsonMap(
//                    key to value
//            ));
//
//            return ret;
//        }
//    }
}
