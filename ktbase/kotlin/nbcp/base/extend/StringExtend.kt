@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.base.extend

import nbcp.base.comm.*
import nbcp.base.component.YamlObjectMapper
import nbcp.base.enums.*
import nbcp.base.extend.*
import nbcp.base.utils.*
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass


private val logger = LoggerFactory.getLogger("MyHelper.StringExtend")

/**
 * 判断是否有内容：非空且有长度
 */
val String?.HasValue: Boolean
    @JvmName("hasValue")
    get() {
        return !this.isNullOrEmpty()
    }


fun String.WrapWith(tag: String, endTag: String = ""): String {
    var endTag = endTag;
    if (endTag.isEmpty()) {
        endTag = tag;
    }

    return tag + this + endTag;
}

/**
 * 对多行文本进行修改。
 * @param regex:  匹配的正则
 * @param range: 匹配的范围, 默认表示所有, (0)第一个， (-1)最后一个, (-2)倒数第2个, (5,7,8)表示，第5个第7个及第8个
 * @param opSign： +=> 在后面追加。 +=< 在前面添加。 => 替换为新内容。
 */
fun String.regexMultiLineEdit(regex: Regex, range: String, opSign: String, value: String): String {
    if (regex.options.contains(RegexOption.MULTILINE) == false) {
        throw RuntimeException("regex.options 必须是 MULTILINE")
    }
    /*
    ^ENV\s+JAVA_OPTS\s+
    (-1) +=>
    ENV JAVA_OPTS -XX:MaxMetaspaceSize=256m
    */
    var startIndex = 0;
    var list = mutableListOf<String>()

    var results = regex.findAll(this).toList();

    var rangeList = range
        .split(",")
        .filter { it.HasValue }
        .map { it.AsInt() }
        .map {
            if (it < 0) {
                return@map results.size + (it % results.size)
            }
            return@map it;
        }

    results.forEachIndexed { index, matchResult ->

        list.add(this.Slice(startIndex, matchResult.range.first))

        startIndex = matchResult.range.last;
        if (rangeList.any() && !rangeList.contains(index)) {
            list.add(matchResult.value)
            return@forEachIndexed
        }

        var newValue = value;
        matchResult.groups.forEachIndexed { gIndex, groupMatch ->
            newValue = newValue.replace("\$${gIndex}", groupMatch!!.value)
        }

        if (opSign == "+=>") {
            list.add(matchResult.value)
            list.add(newValue)
        } else if (opSign == "+=<") {
            list.add(newValue)
            list.add(matchResult.value)
        } else if (opSign == "=>") {
            list.add(newValue)
        } else {
            throw RuntimeException("不识别的符号: ${opSign}")
        }
    }

    list.add(this.Slice(startIndex))
    return list.joinToString("")
}

fun String.insertAt(index: Int, value: String): String {
    return this.substring(0, index) + value + this.substring(index);
}

inline fun <reified T> String.FromYamlText(): T {
    return this.FromYamlText(T::class.java)
}

fun <T> String.FromYamlText(type: Class<T>): T {
    try {
        return YamlObjectMapper.INSTANCE.readValue(this, type)
    } catch (e: Exception) {
        logger.error(
            """
转换Yaml出错:
${this}
"""
        );
        throw e;
    }
}

fun Char.IsCn(): Boolean {
    return this.code.IfUntil(19968, 40909)
}

/**
 * 是否包含中文
 */
fun String.hasCn(): Boolean {
    return this.any { it.IsCn() }
}

/**
 *@param type: 类型
 */
fun <T> String.FromListYarmText(type: Class<T>): List<T> {
    var mapper = YamlObjectMapper.INSTANCE;
    var t = mapper.getTypeFactory().constructParametricType(ArrayList::class.java, type);

    return mapper.readValue<List<T>>(this, t) ?: listOf()
}


fun <T> String.FromListYarmTextWithSplit(type: Class<T>): List<T> {
    return this.split("\n")
        .SplitList { it == "---" }
        .filter { it.isNotEmpty() }
        .map { it.joinToString("\n").FromYamlText(type) }
}


/**
 * 如果有值 ， 返回计算表达式。 否则返回空。
 */
fun String?.IfHasValue(action: ((String) -> String)): String {
    if (this.isNullOrEmpty()) return "";
    return action(this)
}

/**
 * 是否是数字格式，只能有一个小数点，最前面有一个正负号。
 */
fun String.IsNumberic(): Boolean {
    if (this.isEmpty()) return false;

    var self = this;
    var first = self[0];
    if (first == '+' || first == '-') {
        self = self.substring(1);
        if (self.isEmpty()) return false;
    }

    var hasDot = false;
    if (self.all {
            if (it == '.') {
                if (hasDot == false) {
                    hasDot = true;
                    return@all true;
                }
                return@all false;
            }

            if (it.isDigit()) {
                return@all true;
            }
            return@all false;
        } == false) {
        return false;
    }

    return true;
}


/**
 * 提取出数字
 */
fun String.takeNumber(): Array<String> {
    return this.split(Regex("""\D+""")).filter { it.HasValue }.toTypedArray()
}

/**
 * 按数字分隔，返回数字和非数字各部分
 */
fun Regex.splitBoundary(value: String): Array<String> {
    var ret = mutableListOf<String>()

    var prevIndex = 0;
    this.findAll(value).forEachIndexed { index, matchResult ->
        if (prevIndex != matchResult.range.first) {
            ret.add(value.substring(prevIndex, matchResult.range.first))
        }
        ret.add(matchResult.value)

        prevIndex = matchResult.range.last + 1;
    }

    if (prevIndex != value.length) {
        ret.add(value.substring(prevIndex))
    }
    return ret.filter { it.HasValue }.toTypedArray();
}


/**
 * 高效的批量移除字符串
 */
fun String.remove(vararg removeChars: String, ignoreCase: Boolean = false): String {
    //先计算有哪些长度，按长度倒排。
    var willRemoveChars = removeChars.sortedByDescending { it.length }
    val removeStrings = willRemoveChars.toTypedArray()
    return this.split(*removeStrings, ignoreCase = ignoreCase).joinToString("")
}

/**
 * 高效的批量移除字符串
 */
fun String.remove(vararg removeChars: Char, ignoreCase: Boolean = false): String {
    val removeStrings = removeChars.map { it.toString() }.toTypedArray();
    return this.split(*removeStrings, ignoreCase = ignoreCase).joinToString("")
}

/**
 * 去除空行
 */
fun String.removeEmptyLine(withTrim: Boolean = true): String {
    return this.lineSequence().filter {
        if (withTrim) {
            return@filter it.trim().any()
        } else return@filter it.any()
    }.joinToString(const.line_break)
}

data class CharFlowSetting @JvmOverloads constructor(
    var index: Int = 0,
    var item: Char = 0.toChar(),
    var prevCutIndex: Int = 0,
    //休息状态，如在括号内部
    var sleep: Boolean = false
)

/**
 *
 */
fun String.cutWith(callback: ((CharFlowSetting) -> Boolean)): List<String> {
    var list = mutableListOf<String>();

    var setting = CharFlowSetting();

    for (i in 0 until this.length) {
        setting.index = i;
        setting.item = this.get(i);

        if (callback.invoke(setting)) {
            var item = this.substring(setting.prevCutIndex, i);
            if (item.length > 0) {
                list.add(item)
            }
            setting.prevCutIndex = i;
        }
    }
    var item = this.substring(setting.prevCutIndex);
    if (item.length > 0) {
        list.add(item)
    }
    return list;
}

//fun String.IsMatch(Index: Int, MatchString: String): Boolean {
//    if ((Index + MatchString.length) > this.length) return false;
//
//    for (i in 0..MatchString.length - 1) {
//        if (this.get(Index + i) != MatchString.get(i)) return false;
//    }
//
//    return true;
//}

//private fun _getNextChar(html: String, index: Int, findChar: Char): Int {
//    var len = html.length;
//    var index = index - 1;
//
//    while (true) {
//        index++;
//        if (index >= len) break;
//
//
//        if (html.get(index) == '\\') {
//            index++;
//            continue;
//        }
//
//        if (html.get(index) == findChar) {
//            return index;
//        }
//    }
//
//    return -1;
//}

/**
 * 定义引用定义，开始符号，结束符号，逃逸符号。
 */
data class TokenQuoteDefine @JvmOverloads constructor(
    var start: Char,
    var end: Char = 0.toChar(),
    var escape: Char = '\\'
) {
    init {
        if (end.code == 0) {
            end = start
        }
    }
}

//
///**
// * 找出是哪一个 quote
// */
//private fun getMatchedQuoteKey(value: String, startIndex: Int, quoteKeys: Array<String>): String {
//    var list = quoteKeys.filter { value.IsMatch(startIndex, it) };
//
//    if (list.size == 0) return ""
//    else if (list.size == 1) return list.first();
//    else {
//        throw RuntimeException("QuoteDefine 出现多个 ${list.joinToString(",")}")
//    }
//}
//
///**
// * 找到指定findString的索引.
// * @param value 源字符串
// * @param startIndex 源字符串开始的位置
// * @param findString 要查找的字符串
// */
//private fun getNextIndex(value: String, startIndex: Int, findString: String): Int {
//    if (startIndex >= value.length) return -1;
//    if (startIndex < 0) return -1;
//
//    var index = startIndex - 1;
//    while (true) {
//        index++;
//        if (index >= value.length) break;
//        if (value.IsMatch(index, findString)) {
//            return index;
//        }
//    }
//    return -1;
//}


/**
 * @param until 返回 true 表示命中,即返回该 index
 */
fun String.nextIndexOf(startIndex: Int, until: (Char) -> Boolean): Int {
    var value = this;
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

//
//fun String.getNextMatchedIndex(
//    match: String,
//    escape: String = """\""",
//    quoteDefines: StringMap = StringMap(
//        "`" to "`",
//        "[" to "]",
//        "(" to ")",
//        "{" to "}",
//        """"""""" to """"""""",
//        "'" to "'",
//        """"""" to """""""
//    )
//): Pair<Int, Int> {
//    return 0 to 0;
//}


/** 分词器,用于Sql，分析出关键内容
 * select "id" from `table`  按空格分词, 把字符串 , [] 等单独对待
 * @param wordSplit  单词分隔标志.如 空格 ,.@# 注意, 一定不能是 quoteDefines 的内容.
 * @param quoteDefine ,单词的包装符, key =" , value = "
 * @param only1Blank 去除连续的空白，只保留一个。空白包括： 空格，制表，回车
 * @return
 */
@JvmOverloads
fun String.Tokenizer(
    wordSplit: ((Char) -> Boolean)? = null,
    quoteDefines: Array<TokenQuoteDefine> = arrayOf(
        TokenQuoteDefine('`'),
        TokenQuoteDefine('[', ']'),
        TokenQuoteDefine('"'),
        TokenQuoteDefine('\'')
    ),
    only1Blank: Boolean = true
): List<String> {
    var wordSplitLocal = wordSplit;
    if (wordSplitLocal == null) {
        wordSplitLocal = { it.isLetterOrDigit() == false }
    }
    var list = mutableListOf<String>()

    var length = this.length;

    var index = -1;
    while (true) {
        index++;

        if (index >= length) {
            break;
        }

        var nextIndex = getNextSplitIndex(this, index, quoteDefines, wordSplitLocal);
        if (nextIndex < 0) {
            list.add(this.substring(index, nextIndex));
            break;
        }

        if (nextIndex == index) {
            continue;
        }

        list.add(this.substring(index, nextIndex));

        if (nextIndex >= length) {
            break;
        }

        index = nextIndex - 1;
    }


    if (only1Blank) {
        var blankChars = " \t \n"

        var i = -1;
        while (true) {
            i++;
            if (i == list.size) {
                break;
            }

            var item = list[i];
            if (blankChars.contains(item) == false) {
                continue;
            }

            while (true) {
                if (i + 1 == list.size) {
                    break;
                }

                var other = list[i + 1];

                if (blankChars.contains(other) == false) {
                    break;
                }

                list.removeAt(i + 1);
            }
        }
    }
    return list;
}

/**
 * 找下一个分词的位置，不能==startIndex
 */
private fun getNextSplitIndex(
    value: String,
    startIndex: Int,
    quoteDefines: Array<TokenQuoteDefine>,
    wordSplit: (Char) -> Boolean
): Int {

    var startQuoteKeys = quoteDefines.map { it.start }.toTypedArray();
    var firstChar = value[startIndex];

    if (startQuoteKeys.contains(firstChar) == false && wordSplit(firstChar)) {
        return startIndex + 1;
    }

    var quote = quoteDefines.firstOrNull { it.start == firstChar } ?: TokenQuoteDefine(0.toChar())
    var inQuote = quote.start.code != 0
    var posIndex = startIndex;
    var length = value.length;

    while (true) {
        if (inQuote) {
            //如果在 “”中。找一下个结束号
            var nextIndex = value.nextIndexOf(posIndex + 1) { it == quote.end }
            if (nextIndex < 0) {
                break;
            }

            //判断是否有转义
            var hasEsc = false;
            if (quote.end == quote.escape) {
                //看下一个
                if (nextIndex < length - 1 && value[nextIndex + 1] == quote.end) {
                    posIndex = nextIndex + 1;
                    continue;
                }
            } else {
                hasEsc = value[nextIndex - 1] == quote.escape;
            }

            if (hasEsc) {
                posIndex = nextIndex;
                continue;
            }
            return nextIndex + 1;
        } else {
            //找下一个 \b
            var nextIndex = value.nextIndexOf(posIndex + 1) {
                return@nextIndexOf startQuoteKeys.contains(it) || wordSplit(it)
            }

            if (nextIndex < 0) {
                break;
            }

            return nextIndex;
        }
    }

    return value.length;
}


fun String.Repeat(count: Int): String {
    if (count <= 0) return "";
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


/**
 * 保持和 Js 用法一致. 该方法 == substring, 推荐使用 substring
 * 大于等于开始索引,小于结束索引
 * "abcdef".Slice(3) == "def"
 * "abcdef".Slice(3,4) == "d"
 * "abcdef".Slice(-3) == "def"
 * "abcdef".Slice(-3,-1) == "de"
 * "abcdef".Slice(-3,0) == ""
 * "abcdef".Slice(-300) == "abcdef"
 * "abcdef".Slice(-300,-1) == "abcde"
 */
@JvmOverloads
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
 * 整体向左移(所有行向左移) 几个Tab，每个Tab = 4个空格。
 */
fun String.ToTab(deepth: Int): String {
    if (deepth == 0) return this;

    return lineSequence().map { line ->
        if (line.any() == false) {
            return@map "";
        }
        return@map "    ".Repeat(deepth) + line
    }.joinToString(const.line_break)
}

/**
 * En宽度，一个中文按两个算。多行，按最宽的算。
 */
private var ONE_LEN_CHARS =
    "─━│┃┄┅┆┇┈┉┊┋┌┍┎┏┐┑┒┓└┕┖┗┘┙┚┛├┝┞┟┠┡┢┣┤┥┦┧┨┩┪┫┬┭┮┯┰┱┲┳┴┵┶┷┸┹┺┻┼┽┾┿╀╁╂╃╄╅╆╇╈╉╊╋╌╍╎╏═║╒╓╔╕╖╗╘╙╚╛╜╝╞╟╠╡╢╣╤╥╦╧╨╩╪╫╬╭╮";

val String.EnViewWidth: Int
    get() {
        return this.lineSequence().maxOf { line ->
            return@maxOf line
                .toCharArray()
                .sumOf {
                    if (ONE_LEN_CHARS.contains(it)) {
                        return@sumOf 1;
                    }
                    var v = it.code;
                    if (v < 128) {
                        return@sumOf 1;
                    }
                    return@sumOf 2.AsInt()
                }
        }
    }

/**
 * 按En宽度倍数向右填充空格
 */
fun String.PadStepEnViewWidth(align: AlignDirectionEnum, stepWidth: Int = 4, padChar: Char = ' '): String {
    val len = this.EnViewWidth;
    val padString = padChar.toString();
    if (len == 0) {
        return padString.Repeat(stepWidth);
    }

    val mod = len % stepWidth;
    if (mod == 0) {
        return this;
    }

    return when (align) {
        AlignDirectionEnum.LEFT -> padString.Repeat(stepWidth - mod) + this
        AlignDirectionEnum.CENTER -> {
            var left = (stepWidth - mod) / 2;
            var right = stepWidth - mod - left;

            padString.Repeat(left) + this + padString.Repeat(right)
        }

        AlignDirectionEnum.RIGHT -> this + padString.Repeat(stepWidth - mod)
    }
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


//fun String.toUtf8CharArray(): CharArray {
//    var bytes = this.toByteArray(utf8)
//    return CharArray(bytes.size, { bytes[it].toChar() });
//}

class MatchPatternTokenItem(value: String) : MyString(value) {
    var isToken: Boolean = false
        get
        private set

    init {
        isToken = value.length > 0 && value.first().isLetter()
    }
}

/**
 * 用正则表达式, 把内容匹配出来. 使用非字母分隔。
 * 如： "2019-01-01 12:00:00".matchPattern("yyyy-mm-dd HH:MM:ss")
 */
fun String.MatchPattern(pattern: String): StringMap {
    var tokens = mutableListOf<MatchPatternTokenItem>();
    var prevEndIndex = 0;
    Regex("""\b\w+\b""").findAll(pattern).toList()
        .mapIndexed { _, it ->
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
        tokens.add(MatchPatternTokenItem(pattern.substring(prevEndIndex + 1)))
    }


    var ret = StringMap()

    var src_prev_index = 0;

    tokens.forEachIndexed { index, item ->
        if (item.isToken == false) {
            var src_item = this.substring(src_prev_index, src_prev_index + item.length)
            if (src_item != item.toString()) {
                return ret
            }

            src_prev_index += item.length;
            return@forEachIndexed
        }

        //不是最后一个
        if (index != tokens.size - 1) {
            var next_token = tokens[index + 1].toString();

            var next_index = this.indexOf(next_token, src_prev_index)
            var value = this.substring(src_prev_index, next_index);
            ret[item.toString()] = value;
            src_prev_index = next_index;
        } else {
            ret[item.toString()] = this.substring(src_prev_index);
        }
    }


    return ret;
}

/**
 * 字符串转化为枚举，通过 String name 找. 如果找不到,再通过 numeric 找.
 */
inline fun <reified T> String.ToEnum(): T? {
    return this.ToEnum(T::class.java)
}

fun <T : Any> String.ToEnum(enumClazz: KClass<T>): T? {
    return this.ToEnum(enumClazz.java)
}

/**
 * 字符串转化为枚举，通过 String name 不区分大小写 查找. 如果找不到,再通过 numeric 找.
 */
fun <T> String.ToEnum(enumClazz: Class<T>): T? {
    if (enumClazz.isEnum == false) return null;
    var strValue = this.trim();
    if (strValue.isEmpty()) return null;

    var finded = enumClazz.declaredFields.firstOrNull { it.name basicSame strValue }
    if (finded == null) {
        if (this.IsNumberic()) {
            return this.AsInt().ToEnum(enumClazz)
        }
        return null;
    }
    return finded.get(null) as T?;
}

/**
 * 使用Json格式化
 * @param style: {}格式:{id},{name} 也是默认格式; ${}格式: ${id}, ${name};  @格式:  @id , @name ; @@格式: @id@ , @name@ ;
 */
@JvmOverloads
fun String.formatWithJson(
    json: Map<String, Any?>,
    style: String = "",
    keyCallback: ((String) -> String)? = null,  //参数：原始key , 返回: 取map值的key
    valueCallback: ((String, String?) -> String?)? = null  //参数： 原始key,value , 返回value

): String {
    var styleValue = style;
    if (styleValue.isEmpty()) {
        styleValue = "{}"
    }

    var regexp = "";

    if (styleValue == "{}") {
        regexp = "\\{([^{}]+)}"
    } else if (styleValue == "\${}") {
        regexp = "\\$\\{([^{}]+)}"
    } else if (styleValue == "@") {
        regexp = "@(\\w+)"
    } else if (styleValue == "@@") {
        regexp = "@([^@]+)@"
    } else {
        throw java.lang.RuntimeException("不识别的样式 " + styleValue)
    }


    return this.replace(Regex(regexp, RegexOption.MULTILINE), { result ->
        if (result.groupValues.size != 2) {
            throw java.lang.RuntimeException("匹配出错!")
        }

        val oriKey = result.groupValues.last()

        var value: String? = json.getStringValue(oriKey)

        var key = oriKey
        if (keyCallback != null) {
            key = keyCallback(key);
        }


        if (value == null) {
            value = json.getStringValue(key)
        }

        if (value == null) {
            value = json.getStringValue(*key.split(".").toTypedArray())
        }

        if (valueCallback != null) {
            //value 不能是 null.
            value = valueCallback(oriKey, value)
        }

        if (value == null) {
            return@replace ""
        }

        return@replace value;
    })
}


/**
 * 每一行都居中。
 */
fun String.WrapByRectangle(align: AlignDirectionEnum, paddingWith: Int = 4, stepWidth: Int = 4): String {
    var stepWidth = stepWidth;
    if (stepWidth % 2 != 0) {
        stepWidth += 1;
    }

    var lines = this.split("\n");
    var lineMaxWith =
        Math.ceil((lines.map { it.EnViewWidth }.maxOrNull() ?: stepWidth) * 1.0 / stepWidth)
            .AsInt() * stepWidth + 2 * paddingWith;
    var content = lines.map { "│" + it.PadStepEnViewWidth(align, lineMaxWith) + "│" }.joinToString("\n");

    return """
┌${"─".Repeat(lineMaxWith)}┐
${content}
└${"─".Repeat(lineMaxWith)}┘
"""
}