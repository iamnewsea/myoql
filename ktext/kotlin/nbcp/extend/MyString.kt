package nbcp.comm

/**
 * Created by udi on 17-4-3.
 */

/**
 * 自定义字符串
 */
open class MyString(private val value: String) : Comparable<String>, CharSequence {
    override val length: Int = this.value.length

    override fun get(index: Int): Char = this.value.get(index)

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = this.value.subSequence(startIndex, endIndex)

    override fun compareTo(other: String): Int = this.value.compareTo(other)

    override fun toString(): String = this.value;
}
