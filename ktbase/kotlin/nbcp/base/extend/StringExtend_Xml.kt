@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.base.extend

import nbcp.base.comm.JsonMap
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory


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
            subItem.nodeType != Node.CDATA_SECTION_NODE
        ) {
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
            var item = node

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
    var node = document.documentElement.childNodes as Element;

    return JsonMap(node.Xml2Json())
}

//fun String.Text2XmlContent(): String {
//    if (this.isEmpty()) return "";
//
//    val db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
//    val input = ByteArrayInputStream("<r></r>".toByteArray())
//    val doc = db.parse(input)
//    var node = doc.documentElement.childNodes as Element;
//    node.textContent = this;
//
//    var tf = TransformerFactory.newInstance();
//    var t = tf.newTransformer();
//    t.setOutputProperty("encoding", "UTF-8");
//    var bos = ByteArrayOutputStream();
//    t.transform(DOMSource(node), StreamResult(bos));
//    return bos.toString();
//}