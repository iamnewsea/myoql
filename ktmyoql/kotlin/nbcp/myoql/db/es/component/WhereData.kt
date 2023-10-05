package nbcp.myoql.db.es.component

import nbcp.base.comm.JsonMap
import nbcp.myoql.db.db


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
        /**
         * term
         */
        @JvmStatic
        fun esEquals(key: String, value: Any?): WhereData {
            var target = db.es.proc_es_value(value);
            var ret = WhereData(
                "term" to JsonMap(
                    key to target
                )
            );

            return ret;
        }

        @JvmStatic
        fun esMatch(key: String, value: Any?): WhereData {
            var target = db.es.proc_es_value(value);
            var ret = WhereData(
                "match" to JsonMap(
                    key to target
                )
            );

            return ret;
        }

        private fun range(key: String, op: String, value: Any): WhereData {
            var target = db.es.proc_es_value(value);
            var ret = WhereData(
                "range" to JsonMap(
                    key to JsonMap(op to target)
                )
            );

            return ret;
        }


        /**
         *
         * @param value  ?和*分别代替一个和多个字符
         */
        fun wildcard(key: String, value: Any): WhereData {
            var target = db.es.proc_es_value(value);
            var ret = WhereData(
                "wildcard" to JsonMap(
                    key to JsonMap("value" to target)
                )
            );

            return ret;
        }


        fun like(key: String, value: String): WhereData {
            return wildcard(key, "*" + value + "*")
        }

        @JvmStatic
        fun greaterThenEquals(key: String, value: Any): WhereData {
            return range(key, "gte", value);
        }


        @JvmStatic
        fun greaterThen(key: String, value: Any): WhereData {
            return range(key, "gt", value);
        }


        @JvmStatic
        fun lessThenEquals(key: String, value: Any): WhereData {
            return range(key, "lte", value);
        }

        @JvmStatic
        fun lessThen(key: String, value: Any): WhereData {
            return range(key, "lt", value);
        }
    }
}
