package nbcp.db.mysql

import nbcp.comm.*
import nbcp.base.extend.CloneObject
import nbcp.db.sql.*
import nbcp.db.*
import nbcp.base.extend.HasValue
import java.io.Serializable
import java.util.LinkedHashSet


/**
 * Created by yuxh on 2018/6/29
 */


class WhereData : Serializable {
    var expression: String = ""
        get() = field
        private set(value) {
            field = value
        }
    val values: JsonMap = JsonMap()

    constructor(expression: String = "", values: JsonMap = JsonMap()) {
        this.expression = expression
        this.values.putAll(values)
    }


    //与 expression 互斥
    var child: WhereData? = null

    // and or
    var linker: String = "";

    var next: WhereData? = null;

    //生成单一个 WhereData
    fun toSingleData(): SingleSqlData {
        if (this.hasValue == false) return SingleSqlData();

        var ret = SingleSqlData();

        if (this.child != null) {
            var c = this.child!!.toSingleData();
            ret.expression += "("
            ret += c;
            ret.expression += ")"
        } else {
            ret += SingleSqlData(this.expression, this.values);
        }

        if (this.next != null && this.linker.HasValue) {
            var n = this.next!!.toSingleData()

            ret.expression += " ${this.linker} "
            ret += n;
        }

        return ret
    }


    fun getLast(): WhereData {
        if (this.linker.isEmpty() || this.next == null) return this;
        return this.next!!.getLast();
    }

    val hasValue: Boolean
        get() {
            if (this.child == null && this.expression.isEmpty()) return false
            return true;
        }


    infix fun and(next: WhereData): WhereData {
        if (this.hasValue == false) {
            this.values.clear()
            this.values.putAll(next.values)

            this.expression = next.expression
            this.child = next.child
            this.linker = next.linker
            this.next = next.next
            return this
        }

        if (next.hasValue == false) {
            return this
        }

        var last = this.getLast()
        last.linker = "and"
        last.next = next
        return this;
    }

    infix fun or(next: WhereData): WhereData {
        if (this.hasValue == false) {
            this.expression = next.expression
            this.child = next.child

            this.values.clear()
            this.values.putAll(next.values)

            return this
        }

        var ret = WhereData()
        ret.child = this.CloneObject()
        ret.linker = "or"

        var next2 = WhereData()
        next2.child = next

        ret.next = next2

        var wrap = WhereData();
        wrap.child = ret;


        //更改 this
        this.child = wrap.child
        this.expression = wrap.expression
        this.linker = wrap.linker
        this.next = wrap.next;
        this.values.clear()
        this.values.putAll(wrap.values)

        return wrap;
    }
}
