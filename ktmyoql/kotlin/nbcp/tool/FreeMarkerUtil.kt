package nbcp.tool

import java.io.IOException
import java.io.StringWriter
import java.lang.Exception
import java.util.*
import freemarker.cache.ClassTemplateLoader
import freemarker.ext.beans.StringModel
import freemarker.template.*
import nbcp.comm.AsString
import nbcp.comm.JsonMap
import nbcp.comm.usingScope
import nbcp.utils.MyUtil
import org.slf4j.LoggerFactory
import java.io.StringReader
import java.lang.RuntimeException
import java.lang.reflect.Field
import java.time.LocalDateTime
import nbcp.scope.*

object FreemarkerUtil {
    /**
     * 传入需要转义的字符串进行转义
     */
    fun escapeString(originStr: String): String {
        return originStr.replace("井".toRegex(), "\\#").replace("￥".toRegex(), "\\$")
    }

    /**
     * freemarker config
     */
    private val freemarkerConfig: Configuration
        get() {
            var ret = Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS)

            ret.setClassForTemplateLoading(FreemarkerUtil::class.java, "/myoql-template")
            ret.setTemplateLoader(
                ClassTemplateLoader(
                    FreemarkerUtil::class.java,
                    "/myoql-template"
                )
            )
            //freemarkerConfig.setDirectoryForTemplateLoading(new File(templatePath, "templates/code-generator"));
            ret.setNumberFormat("#")
            ret.setClassicCompatible(true)
            ret.setDefaultEncoding("UTF-8")
            ret.setLocale(Locale.CHINA)
            ret.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER)

            return ret;
        }

    /**
     * process Template Into String
     *
     * @param template
     * @param model
     * @return
     * @throws IOException
     * @throws TemplateException
     */
    private fun processTemplate(template: Template, params: JsonMap): String {
        val result = StringWriter()

        var all_params = JsonMap()
        all_params.putAll(params)

        all_params.put("now", LocalDateTime.now().AsString())
        all_params.put("has_value", Freemarker_HasValue())

        all_params.put("k", Freemarker_KebabCase())
        all_params.put("W", Freemarker_BigCamelCase())
        all_params.put("w", Freemarker_SmallCamelCase())

        all_params.put("field_is_enum_list", Freemarker_Field_IsEnumList())
        all_params.put("field_cn", Freemarker_Field_Cn())
        all_params.put("field_is_list", Freemarker_IsList())
        all_params.put("field_list_type", Freemarker_Field_ListType())

        all_params.put("is_res", Freemarker_IsRes())
        all_params.put("is_in", Freemarker_IsIn())
        all_params.put("is_object", Freemarker_IsObject())
        all_params.put("is_type", Freemarker_IsType())


        template.process(all_params, result)
        return result.toString()
    }

    fun processContent(
        content: String,
        params: JsonMap
    ): String {
        val template: Template = Template("template", StringReader(content), freemarkerConfig, "utf-8")
        return usingScope(ContextMapScope(params)) {
            return escapeString(processTemplate(template, params))
        }
    }

    /**
     * process String
     *
     * @param templateName
     * @param params
     * @return
     * @throws IOException
     * @throws TemplateException
     */
    fun process(
        templateName: String,
        params: JsonMap
    ): String {
        val template: Template = freemarkerConfig.getTemplate(templateName)
        return usingScope(ContextMapScope(params)) {
            return escapeString(processTemplate(template, params))
        }
    }
}
