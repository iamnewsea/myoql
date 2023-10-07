package cn.dev8.util;

import lombok.var;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.QName;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Yuxh
 */
public class XmlUtil {


    /**
     * 按NodeName路径查询节点， 忽略 nammespace。 如果以双斜线开头， 是从任意节点查找；否则从目标节点根节点开始找。
     *
     * @param doc
     * @param tags
     */
    public static List<Element> getElements(Node doc, String... tags) {
        return doc.selectNodes(getXPath(tags)).stream().map(it -> (Element) it).collect(Collectors.toList());
    }


    /**
     * 按NodeName路径查询节点， 忽略 nammespace。 如果以双斜线开头， 是从任意节点查找；否则从目标节点根节点开始找。
     *
     * @param doc
     * @param tags
     * @return
     */
    public static Element getSingleElement(Node doc, String... tags) {
        return (Element) doc.selectSingleNode(getXPath(tags));
    }

    /**
     * 根据每个tag，返回 xpath
     *
     * @param tags
     * @return
     */
    private static String getXPath(String[] tags) {
        var tags2 = Arrays.<String>asList(tags)
                .stream()
                .map(it -> it.split("\\/"))
                .collect(Collectors.toList());
        // project/*[name()='parent']/*[name()='version']
        var xPath = ListUtil
                .unwindArray(tags2)
                .stream()
                .filter(it -> StringUtil.hasValue(it))
                .map(it -> {
                    var attrIndex = it.indexOf('[');
                    if (attrIndex < 0) {
                        return "*[name()='" + it + "']";
                    } else if (attrIndex == 0) {
                        return "*" + it;
                    }

                    var tag = it.substring(0, attrIndex);

                    return "*[name()='" + tag + "']" + it.substring(attrIndex);
                })
                .collect(Collectors.joining("/"));

        var isAnyPath = tags[0].startsWith("//");

        if (isAnyPath) {
            xPath = "//" + xPath;
        }

        return xPath;
    }


    /**
     * 创建一个 同名称空间的元素。没有 xmlns
     *
     * @param anyNode
     * @return
     */
    public static Element createElement(String tag, Node anyNode) {
        return DocumentHelper.createElement(QName.get(tag, anyNode.getDocument().getRootElement().getNamespace()));
    }

    /**
     * 移除元素
     *
     * @param element
     * @return
     */
    public static boolean removeFromParent(Node element) {
        return element.getParent().remove(element);
    }
}
