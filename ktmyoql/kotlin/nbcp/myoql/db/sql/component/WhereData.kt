package nbcp.myoql.db.sql.component

import nbcp.base.comm.JsonMap
import nbcp.base.extend.AsString
import nbcp.base.extend.CloneObject
import nbcp.base.extend.HasValue
import nbcp.base.extend.basicSame
import nbcp.myoql.db.sql.base.SqlParameterData
import java.io.Serializable


/**
 * Created by yuxh on 2018/6/29
 */


class WhereData : Serializable {
    var expression: String = ""
        private set;

    val values: JsonMap = JsonMap()

    @JvmOverloads
    constructor(expression: String = "", values: Map<String,Any?> = mapOf()) {
        this.expression = expression
        this.values.putAll(values)
    }


    //与 expression 互斥
    var child: WhereData? = null

    // and or
    var linker: String = "";

    var next: WhereData? = null;

    //生成单一个 WhereData
    fun toSingleData(): SqlParameterData {
        if (this.hasValue == false) return SqlParameterData();

        var ret = SqlParameterData();

        if (this.child != null) {
            var c = this.child!!.toSingleData();
            ret.expression += "("
            ret += c;
            ret.expression += ")"
        } else {
            ret += SqlParameterData(this.expression, this.values);
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

    fun load(other: WhereData) {
        this.expression = other.expression;
        this.child = other.child;
        this.next = other.next;
        this.linker = other.linker;

        this.values.clear();
        this.values.putAll(other.values)
    }

    infix fun or(next: WhereData): WhereData {
        if (this.hasValue == false) {
            this.load(next)
            return this
        }

        var ret = WhereData()
        ret.child = this.CloneObject()
        ret.linker = "or"

        var next2 = WhereData()
        next2.child = next

        ret.next = next2


        val wrap = WhereData();
        wrap.child = ret;

        //更改 this
        this.load(wrap);
        return this;
    }

    fun hasOrClip(): Boolean {
        if (this.linker basicSame "or") {
            return true;
        }

        if (this.linker.HasValue && this.next != null) {
            return next!!.hasOrClip()
        }

        return false;
    }

    fun findValueFromRootLevel(column: String): String? {
        var index = this.expression.indexOf(column + " = ")
        if (index >= 0) {
            var v = this.expression.substring(column.length + 3).trim();
            if (v.startsWith(":")) {
                return values.get(v.substring(1)).AsString()
            }
        } else if (this.linker.HasValue && this.next != null) {
            return this.next!!.findValueFromRootLevel(column);
        }

        return null;
    }
}
