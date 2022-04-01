package nbcp.base.handler

import ch.qos.logback.classic.Level
import com.wf.captcha.ArithmeticCaptcha
import com.wf.captcha.utils.CaptchaUtil
import nbcp.comm.*
import nbcp.db.KeyValueString
import nbcp.db.db
import nbcp.utils.CodeUtil
import nbcp.base.mvc.*
import org.springframework.beans.factory.annotation.Value
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
open class GetEnumItemsServlet {
    data class ValueLabelData(var value: String, var label: String)

    @GetMapping("/open/enum-items/list")
    fun doGet(enum: String): ListResult<ValueLabelData> {
        var clazz = Class.forName(enum);
        var nameField = clazz.GetEnumNumberField();

        var list = clazz.GetEnumList().map {
            val key = it.toString();
            ValueLabelData(key, nameField?.get(it).AsString(key))
        }

        return ListResult.of(list)
    }
}

