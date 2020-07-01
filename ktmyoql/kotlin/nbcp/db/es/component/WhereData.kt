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
//    var expression = ""
//        private set;
//
//    var value :Any? = null
//        private set;
//
//    constructor(expression: String = "", value: Any? = null) {
//        this.expression = expression
//        this.value = value;
//    }
//
//
//    //与 expression 互斥
//    var child: WhereData? = null
//
//    // and or
//    var linker: String = "";
//
//    var next: WhereData? = null;
//
//
//    fun getLast(): WhereData {
//        if (this.linker.isEmpty() || this.next == null) return this;
//        return this.next!!.getLast();
//    }
//
//    val hasValue: Boolean
//        get() {
//            if (this.child == null && this.expression.isEmpty()) return false
//            return true;
//        }
//
//
//    infix fun and(next: WhereData): WhereData {
//        if (this.hasValue == false) {
//            this.value = next.value;
//
//            this.expression = next.expression
//            this.child = next.child
//            this.linker = next.linker
//            this.next = next.next
//            return this
//        }
//
//        if (next.hasValue == false) {
//            return this
//        }
//
//        var last = this.getLast()
//        last.linker = "and"
//        last.next = next
//        return this;
//    }
//
//    infix fun or(next: WhereData): WhereData {
//        if (this.hasValue == false) {
//            this.expression = next.expression
//            this.child = next.child
//
//            this.value = next.value;
//
//            return this
//        }
//
//        var ret = WhereData()
//        ret.child = this.CloneObject()
//        ret.linker = "or"
//
//        var next2 = WhereData()
//        next2.child = next
//
//        ret.next = next2
//
//        var wrap = WhereData();
//        wrap.child = ret;
//
//
//        //更改 this
//        this.child = wrap.child
//        this.expression = wrap.expression
//        this.linker = wrap.linker
//        this.next = wrap.next;
//        this.value = next.value;
//
//        return wrap;
//    }
}
