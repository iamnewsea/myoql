package nbcp.comm

import org.slf4j.LoggerFactory
import nbcp.comm.*
import nbcp.utils.*
import java.io.Serializable
import java.lang.RuntimeException

open class JsonList<T> : ArrayList<T>, Serializable {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)

    }

    constructor() : super() {
    }

    constructor(array: ArrayList<T>) : super(array) {
    }

    constructor(vararg items: T) : this() {
        this.addAll(items)
    }
}
