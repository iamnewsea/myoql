package nbcp.comm

import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

/**
 * Created by udi on 17-4-3.
 */

/**
 * 自定义字符串
 */
open class MyString @JvmOverloads constructor(private var value: String = "") : Comparable<String>, CharSequence,
    Serializable {
    override val length: Int = this.value.length

    override fun get(index: Int): Char = this.value.get(index)

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence =
        this.value.subSequence(startIndex, endIndex)

    override fun compareTo(other: String): Int = this.value.compareTo(other)

    override fun toString(): String = this.value;


    private fun writeObject(out: ObjectOutputStream) {
        out.writeBytes(this.value)
    }

    private fun readObject(oin: ObjectInputStream) {
        this.value = oin.readBytes().toString(const.utf8)
    }
}


/**
 * 不解析字符串
 */
class MyRawString @JvmOverloads constructor(value: String = "") : MyString(value) {}
