package nbcp.web.base.mvc.handler

import ch.qos.logback.classic.Level
import com.wf.captcha.ArithmeticCaptcha
import com.wf.captcha.utils.CaptchaUtil
import nbcp.base.comm.*;
import nbcp.base.db.*;
import nbcp.base.enums.*;
import nbcp.base.extend.*;
import nbcp.base.utils.*;
import nbcp.mvc.comm.OpenAction
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.RuntimeException

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


/**
 * Created by udi on 20-8-27.
 */
@OpenAction
@RestController
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
open class GetEnumItemsServlet {
    data class ValueLabelData(var value: String, var label: String)

    @GetMapping("/open/enum-items/list")
    fun doGet(@nbcp.base.comm.Require enum: String): ListResult<ValueLabelData> {
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

