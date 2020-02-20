package nbcp

import nbcp.comm.*
import nbcp.base.extend.FieldTypeJsonMapper
import nbcp.base.extend.GetSetTypeJsonMapper
import nbcp.base.extend.ToJson
import nbcp.base.extend.Xml2Json
import nbcp.base.utils.RecursionUtil
import nbcp.db.IdName
import nbcp.db.IdUrl
import org.junit.Test

class testa : TestBase() {

    @Test
    fun abc() {
        var xml = """
<h:table xmlns:h="http://www.w3.org/TR/html4/">
   <h:tr>
        <h:td>Apples</h:td>
        <h:td>Bananas</h:td>
   </h:tr>
</h:table>"""

        var json = xml.Xml2Json()

        println(json.ToJson())
    }
}