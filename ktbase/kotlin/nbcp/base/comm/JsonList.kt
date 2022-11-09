package nbcp.base.comm

import org.slf4j.LoggerFactory
import java.io.Serializable

open class JsonList<T> : ArrayList<T>, Serializable {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)

        fun <T> of(item: T, vararg items: T): JsonList<T> {
            var ret = JsonList<T>();
            ret.add(item)
            ret.addAll(items)
            return ret;
        }
    }

    constructor() : super() {
    }

    constructor(array: ArrayList<T>) : super(array) {
    }
}
