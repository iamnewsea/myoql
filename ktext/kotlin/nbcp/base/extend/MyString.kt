package nbcp.base.extend


import nbcp.comm.*
import java.lang.StringBuilder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import org.w3c.dom.DocumentType
import nbcp.comm.*
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.ByteArrayInputStream
import java.util.regex.Pattern
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element
import kotlin.collections.LinkedHashMap

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

//不区分大小写格式的比较。
infix inline fun String.VbSame(other: String?): Boolean {
    if (other == null) return false;
    return this.compareTo(other, true) == 0;
}

val String?.HasValue: Boolean
    get() {
        return !this.isNullOrEmpty()
    }

//如果有值 ， 返回计算表达式。 否则返回空。
fun String?.IfHasValue(action: ((String) -> String)): String {
    if (this.isNullOrEmpty()) return "";
    return action(this!!)
}

fun String.IsNumberic(): Boolean {
    if (this.length == 0) return false;

    var hasDot = false;
    var hasSigne = false;
    this.all {
        if (it == '.') {
            if (hasDot == false) {
                hasDot = true;
                return@all true;
            }
            return false;
        }
        if (it == '+' || it == '-') {
            if (hasSigne == false) {
                hasSigne = true;
                return@all true;
            }
            return false;
        }

        if (it.isDigit()) {
            return@all true;
        }
        return false;

        return@all true;
    }

    return true;
}

fun Char.NewString(count: Int): String {
    var chrs = StringBuilder();

    for (i in 0..count - 1) {
        chrs.append(this);
    }

    return chrs.toString();
}

////返回最先找到的字符的索引,找不到返回 -1
//@Deprecated("使用系统提供的")
//fun String.IndexOf(startIndex: Int, vararg findChars: Char): Int {
//    for (i in startIndex..this.length - 1) {
//        if (findChars.contains(this[i])) return i;
//    }
//    return -1;
//}

////返回从最后开始最先找到的索引。找不到返回 -1
//@Deprecated("使用系统提供的")
//fun String.LastIndexOf(vararg findChars: Char): Int {
//    var ret = this.reversed().IndexOf(0, *findChars);
//    if (ret == -1) return -1;
//    return this.length - 1 - ret;
//}


fun String.IsMatch(Index: Int, MatchString: String): Boolean {
    if ((Index + MatchString.length) > this.length) return false;

    for (i in 0..MatchString.length - 1) {
        if (this.get(Index + i) != MatchString.get(i)) return false;
    }

    return true;
}

private fun _getNextChar(html: String, index: Int, findChar: Char): Int {
    var len = html.length;
    var index = index - 1;

    while (true) {
        index++;
        if (index >= len) break;


        if (html.get(index) == '\\') {
            index++;
            continue;
        }

        if (html.get(index) == findChar) {
            return index;
        }
    }

    return -1;
}


data class TokenQuoteDefine(
        var start: String,
        var end: String = "",
        var escape: String = "\\"
) {
    init {
        if (end.isEmpty()) {
            end = start
        }
    }
}


/**
 * 找出是哪一个 quote
 */
private fun getMatchedQuoteKey(value: String, startIndex: Int, quoteKeys: Array<String>): String {
    var list = quoteKeys.filter { value.IsMatch(startIndex, it) };

    if (list.size == 0) return ""
    else if (list.size == 1) return list.first();
    else {
        throw RuntimeException("QuoteDefine 出现多个 ${list.joinToString(",")}")
    }
}

/**
 * 找到指定findString的索引.
 * @param value 源字符串
 * @param startIndex 源字符串开始的位置
 * @param findString 要查找的字符串
 */
private fun getNextIndex(value: String, startIndex: Int, findString: String): Int {
    if (startIndex >= value.length) return -1;
    if (startIndex < 0) return -1;

    var index = startIndex - 1;
    while (true) {
        index++;
        if (index >= value.length) break;
        if (value.IsMatch(index, findString)) {
            return index;
        }
    }
    return -1;
}

/**
 * 找到startIndex后,不是findString的索引.
 */
//private fun getNextNotValueIndex(value: String, startIndex: Int, findString: Char): Int {
//    if (startIndex >= value.length) return -1;
//    if (startIndex < 0) return -1;
//
//    var index = startIndex - 1;
//    while (true) {
//        index++;
//        if (index >= value.length) break;
//        if (value[index] != findString) {
//            return index;
//        }
//    }
//    return -1;
//}

/**
 * @param until 返回 true 表示命中,即返回该 index
 */
private fun getNextIndexUntil(value: String, startIndex: Int, until: (Char) -> Boolean): Int {
    if (startIndex >= value.length) return -1;
    if (startIndex < 0) return -1;

    var index = startIndex - 1;
    while (true) {
        index++;
        if (index >= value.length) break;
        if (until(value[index])) {
            return index;
        }
    }
    return -1;
}

/**分词器
 * select "id" from `table`  按空格分词, 把字符串 , [] 等单独对待
 * @param wordSplitFlag 单词分隔标志.如 空格 ,.@# 注意, 一定不能是 quoteDefines 的内容.
 * @param quoteDefine ,单词的包装符, key =" , value = "
 * @param joinSplitFlag 把连续的 splitFlag 合并起来，默认把空白合并起来
 * @return 返回结果会合并 相同 wordSplitFlag 的内容, 如多个连续空格, 多个连续. 多个连续回车
 */
fun String.Tokenizer(
        wordSplitFlag: CharArray = " \t \n,.".toCharArray(),
        quoteDefines: Array<TokenQuoteDefine> = arrayOf(
                TokenQuoteDefine("`"),
                TokenQuoteDefine("[", "]"),
                TokenQuoteDefine("\""),
                TokenQuoteDefine("'")),
        joinSplitFlag: CharArray = " \t \n".toCharArray()
): Array<String> {
    var list = mutableListOf<String>()

    var quoteKeys = quoteDefines.map { it.start }.toTypedArray();
//    var prevIndex = 0
//    var currentStatus = -1; // -1  单词边界,未判定 . 0 判断开始为 lastSplitChar. 1 判断开始非 lastSplitChar .
//    var lastSplitChar = '\u0000'
    var index = -1;
    while (true) {
        index++;

        if (index >= this.length) {
            break;
        }

        var item = this[index];

        var isSplitChar = wordSplitFlag.contains(item)

        if (isSplitChar) {
            var nextIndex = -1;
            if (joinSplitFlag.contains(item)) {
                nextIndex = getNextIndexUntil(this, index) { joinSplitFlag.contains(it) == false }
            } else {
                nextIndex = getNextIndexUntil(this, index) { it != item }
            }

            if (nextIndex < 0) {
                nextIndex = this.length
            }

            list.add(this.Slice(index, nextIndex))

            index = nextIndex - 1
            continue
        } else {
            var nextIndex = getNextIndexUntil(this, index) { wordSplitFlag.contains(it) }
            if (nextIndex < 0) {
                nextIndex = this.length
            }

            list.add(this.Slice(index, nextIndex))
            index = nextIndex - 1
            continue
        }
    }
    return list.toTypedArray();
}

private fun _getIndexString_SkipInQuote(Value: String, index: Int, vararg findStrings: String): Int {

    if (index == Value.length) return -1;
    if (index < 0) return -1;

    var tmpPos = 0;

    var i = index - 1;
    while (true) {
        i++;
        if (i >= Value.length) break;

        var item = Value.get(i);
        if (item == '\\') {
            i++;
            continue;
        }

        if (item == '\'') {
            tmpPos = _getNextChar(Value, i + 1, '\'');
            if (tmpPos < 0) return tmpPos;

            i = tmpPos;
            continue;
        }

        if (item == '"') {
            tmpPos = _getNextChar(Value, i + 1, '"');
            if (tmpPos < 0) return tmpPos;

            i = tmpPos;
            continue;
        }

        for (findString in findStrings) {
            if (Value.IsMatch(i, findString)) {
                return i;
            }
        }
    }


    return -1;
}

fun String.RemoveComment(): String {
    var str = this;
    //去除注释
    var i = -1;
    while (true) {
        i++;
        if (i >= str.length) break;

        var next = _getIndexString_SkipInQuote(str, i, "//", "/*");
        if (next < 0) break;

        if (str.IsMatch(next, "//")) {
            var lineEnd = str.indexOf('\n', next);
            if (lineEnd < 0) {
                str = str.substring(0, next);
                break;
            } else {
                str = str.substring(0, next) + str.substring(lineEnd + 1);
            }
        } else {
            var end = str.indexOf("*/", next);
            if (end < 0) {
                str = str.substring(0, next);
                break;
            } else {
                str = str.substring(0, next) + str.substring(end + 2);
            }
        }

        i = next - 1;
    }

    return str;
}


enum class FileExtentionTypeEnum {
    Image,
    Video,
    Html,
    Office,
    Other
}

data class FileExtentionInfo(private var url: String) {
    var name: String = "";  //不带扩展名
    var extName: String = ""; //不带.的扩展名
    var extType: FileExtentionTypeEnum = FileExtentionTypeEnum.Other;

    init {
        var tailIndex = url.indexOfAny("?#".toCharArray());
        if (tailIndex > -1) {
            url = url.substring(0..tailIndex);
        }

        tailIndex = url.lastIndexOfAny("./\\".toCharArray());
        if (tailIndex > -1) {
            if (url[tailIndex] == '.') {
                extName = url.substring(tailIndex + 1);
                name = url.substring(url.lastIndexOfAny("/\\".toCharArray()) + 1, tailIndex);
            } else {
                name = url.substring(tailIndex + 1);
            }
        } else {
            name = url;
        }

        name = name.Remove('>', '<', '*', '|', ':', '?', '"', '\'');

        if (extName.toLowerCase().IsIn("ico", "icon", "png", "jpg", "jpeg", "gif", "bmp", "ttf", "otf", "tiff")) {
            extType = FileExtentionTypeEnum.Image;
        }
        else if (extName.toLowerCase().IsIn("mp4", "mp3", "avi", "rm", "rmvb", "flv", "flash", "swf", "3gp", "wma", "m3u8", "ts", "hls", "mov")) {
            extType = FileExtentionTypeEnum.Video
        }
        else if (extName.toLowerCase().IsIn("js", "css", "txt", "html", "htm", "xml", "xhtml", "json")) {
            extType = FileExtentionTypeEnum.Html
        }
        else if (extName.toLowerCase().IsIn("doc", "docx", "pdf", "xls", "xlsx", "ppt", "pptx", "rtf")) {
            extType = FileExtentionTypeEnum.Office;
        }
    }

    override fun toString(): String {
        return this.extName;
    }

//    val isStaticURI: Boolean
//        get() {
//            return extType != FileExtentionTypeEnum.Other;
//        }
}


fun String.Repeat(count: Int): String {
    var list = mutableListOf<String>();
    for (i in 1..count) {
        list.add(this);
    }
    return list.joinToString("");
}

/**
 * 前面加 host
 */
fun String.PatchHostUrl(host: String): String {
    if (this.HasValue == false) return "";
    if (this.startsWith("//")) return this;
    if (this.startsWith("http://")) return this;
    if (this.startsWith("https://")) return this;
    return host + this;
}

fun String.Remove(vararg value: String): String {
    var ret = this;
    for (v in value) {
        ret = ret.replace(v, "");
    }
    return ret;
}


fun String.Remove(vararg removeChars: Char): String {
    var ret = this;
    removeChars.forEach {
        ret = ret.replace(it, '\u0000');
    }
    return ret;
}


/**
 * 保持和 Js 用法一致.
 * 大于等于开始索引,小于结束索引
 * "abcdef".Slice(3) == "def"
 * "abcdef".Slice(3,4) == "d"
 * "abcdef".Slice(-3) == "def"
 * "abcdef".Slice(-3,-1) == "de"
 * "abcdef".Slice(-3,0) == ""
 * "abcdef".Slice(-300) == "abcdef"
 * "abcdef".Slice(-300,-1) == "abcde"
 */
fun String.Slice(startIndex: Int, endIndex: Int = Int.MIN_VALUE): String {
    var list = mutableListOf<Char>()
    this.toCharArray().forEach {
        list.add(it)
    }

    return list.Slice(startIndex, endIndex).joinToString("")
}


fun String.IsIn(vararg ts: String, ignoreCase: Boolean): Boolean {
    for (i in ts) {
        if (this.equals(i, ignoreCase)) return true;
    }
    return false;
}

/**
 * 整体向左移动几个Tab，每个Tab = 4个空格。
 */
fun String.ToTab(deepth: Int): String {
    if (deepth == 0) return this;
    return this.Remove('\r').split("\n").map { "    ".Repeat(deepth) + it }.joinToString("\n")
}


//private fun xmlNodeHasTextNode(item: org.w3c.dom.Node): Boolean {
//
//    if (item.nodeType != DocumentType.ELEMENT_NODE) return false;
//    if (item.childNodes.length < 1) return false;
//
//    for (index in 0..item.childNodes.length - 1) {
//        var subItem = item.childNodes.item(index);
//        if (subItem.nodeType == DocumentType.CDATA_SECTION_NODE) return true;
//        if (subItem.nodeType == DocumentType.TEXT_NODE) return true;
//    }
//    return false;
//}

/**
 * 如果仅有一个子元素，且子元素是 Text，CData，返回内容，或空字符串。
 * 其它情况返回 null ,表示不是一个子元素。
 */
private fun getNodeText(node: Element): String? {
    var childNode = node.childNodes;

    //如果仅仅是 343
    var hasNode = false;
    var retValue: String = ""
    for (index in 0..(childNode.length - 1)) {
        var subItem = childNode.item(index);
        if (subItem.nodeType != Node.TEXT_NODE &&
                subItem.nodeType != Node.CDATA_SECTION_NODE) {
            hasNode = true;
            break;
        }

        retValue = subItem.textContent.trim()
        if (retValue.HasValue) {
            break;
        }
    }

    if (hasNode) {
        return null;
    }

    return retValue;
}

fun Element.Xml2Json(): Map<String, Any> {
    var retList = mutableListOf<Pair<String, Any>>()

    var txt = getNodeText(this);
    if (txt != null) {
        if (txt.HasValue) {
            return mapOf(this.nodeName to txt)
        } else {
            return mapOf();
        }
    }

    if (this is NodeList && (this.length > 0)) {
        for (index in 0..this.length - 1) {
            var node = this.item(index);
            if (node is Element == false) {
                continue;
            }
            var item = node as Element

            var itemText = getNodeText(item);
            if (itemText == null) {
                retList.addAll(item.Xml2Json().toList())
                continue;
            }

            if (itemText.HasValue) {
                retList.add(item.nodeName to itemText)
            }
            continue;
        }
        var jsonMap = LinkedHashMap<String, Any>();
        if (retList.count() != retList.map { it.first }.toSet().count()) {
            jsonMap.put(this.nodeName, retList.map { JsonMap(it) });
            return jsonMap;
        }

        jsonMap.put(this.nodeName, retList.toMap())
        return jsonMap;
    } else {
        throw RuntimeException("什么情况!" + this.toString())
    }
}

/**
 * 把Xml转为 JsonMap
 */
fun String.Xml2Json(): JsonMap {
    if (this.isEmpty()) return JsonMap();

    val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val input = ByteArrayInputStream(this.toByteArray())
    val document = db.parse(input)
    var nodes = document.documentElement.childNodes as Element;

    return JsonMap(nodes.Xml2Json())
}


//fun String.toUtf8CharArray(): CharArray {
//    var bytes = this.toByteArray(utf8)
//    return CharArray(bytes.size, { bytes[it].toChar() });
//}


class MatchPatternTokenItem(value: String) : MyString(value) {

    var isToken: Boolean = false
        get() = field
        private set


    init {
        isToken = value.length > 0 && value.first().isLetter()
    }
}

/**
 * 用正则表达式, 把内容匹配出来.
 * 如： "2019-01-01 12:00:00".matchPattern("yyyy-mm-dd HH:MM:ss")
 */
fun String.MatchPattern(pattern: String): StringMap {
    var tokens = mutableListOf<MatchPatternTokenItem>();
    var prevEndIndex = 0;
    Regex("""\b\w+\b""").findAll(pattern).toList()
            .mapIndexed { index, it ->
                var group = it.groups.firstOrNull();
                if (group == null) {
                    return@mapIndexed
                }

                if (group.range.first > prevEndIndex) {
                    tokens.add(MatchPatternTokenItem(pattern.slice(prevEndIndex + 1..group.range.first - 1)))
                }

                tokens.add(MatchPatternTokenItem(group.value))
                prevEndIndex = group.range.last;
            }

    if (prevEndIndex + 1 < this.length) {
        tokens.add(MatchPatternTokenItem(pattern.Slice(prevEndIndex + 1)))
    }


    var ret = StringMap()

    var src_prev_index = 0;
    var next_token = "";
    tokens.forEachIndexed { index, item ->
        if (item.isToken == false) {
            var src_item = this.Slice(src_prev_index, src_prev_index + item.length)
            if (src_item != item.toString()) {
                return ret
            }

            src_prev_index += item.length;
            return@forEachIndexed
        }

        //不是最后一个
        if (index != tokens.size - 1) {
            next_token = tokens[index + 1].toString();

            var next_index = this.indexOf(next_token, src_prev_index)
            var value = this.Slice(src_prev_index, next_index);
            ret[item.toString()] = value;
            src_prev_index = next_index;
        } else {
            ret[item.toString()] = this.Slice(src_prev_index);
        }
    }


    return ret;
}
