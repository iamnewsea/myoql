package nbcp.base.comm

import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

/**
 * Created by udi on 17-4-3.
 */

/**
 * 自定义字符串
 */
open class MyString @JvmOverloads constructor(private var _str_value: String = "") : Comparable<String>, CharSequence,
    Serializable {
    override val length: Int = this._str_value.length

    override fun get(index: Int): Char = this._str_value.get(index)

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence =
        this._str_value.subSequence(startIndex, endIndex)

    override fun compareTo(other: String): Int = this._str_value.compareTo(other)

    override fun toString(): String = this._str_value;


    private fun writeObject(out: ObjectOutputStream) {
        out.writeBytes(this._str_value)
    }

    private fun readObject(oin: ObjectInputStream) {
        this._str_value = oin.readBytes().toString(const.utf8)
    }
}


/**
 * 不解析字符串
 */
class MyRawString @JvmOverloads constructor(_raw_value: String = "") : MyString(_raw_value) {}
