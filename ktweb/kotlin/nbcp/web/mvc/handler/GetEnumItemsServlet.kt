package nbcp.web.mvc.handler

import nbcp.base.comm.ListResult
import nbcp.base.extend.AsString
import nbcp.base.extend.GetEnumList
import nbcp.base.extend.GetEnumNumberField
import nbcp.mvc.annotation.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


/**
 * Created by udi on 20-8-27.
 */
@OpenAction
@RestController
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
open class GetEnumItemsServlet {
    data class ValueLabelData(var value: String, var label: String)

    @GetMapping("/open/enum-items/list")
    fun doGet(@nbcp.base.annotation.Require enum: String): ListResult<ValueLabelData> {
        if( enum.isEmpty()){
            return ListResult.error("找不到 enum 参数")
        }
        var clazz = Class.forName(enum);
        var nameField = clazz.GetEnumNumberField();

        var list = clazz.GetEnumList().map {
            val key = it.toString();
            ValueLabelData(key, nameField?.get(it).AsString(key))
        }

        return ListResult.of(list)
    }
}

