package nbcp.myoql.tool

import freemarker.cache.ClassTemplateLoader
import freemarker.template.*
import nbcp.base.comm.*
import nbcp.base.db.*
import nbcp.base.enums.*
import nbcp.base.extend.*
import nbcp.base.utils.*
import nbcp.myoql.db.*
import nbcp.myoql.db.comm.*
import nbcp.myoql.db.enums.*
import nbcp.myoql.tool.freemarker.*
import java.io.IOException
import java.io.StringReader
import java.io.StringWriter
import java.time.LocalDateTime
import java.util.*

object FreemarkerUtil {
    /**
     * 传入需要转义的字符串进行转义
     */
    @JvmStatic
    fun escapeString(originStr: String): String {
        return originStr.replace("井".toRegex(), "\\#").replace("￥".toRegex(), "\\$")
    }

    /**
     * freemarker config
     */
    private fun getFreemarkerConfig(): Configuration {
        var ret = Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS)

        //freemarkerConfig.setDirectoryForTemplateLoading(new File(templatePath, "templates/code-generator"));
        ret.setNumberFormat("#")
        ret.setClassicCompatible(true)
        ret.setDefaultEncoding("UTF-8")
        ret.setLocale(Locale.CHINA)
        ret.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER)

        ret.setClassForTemplateLoading(FreemarkerUtil::class.java, "/")
        ret.setTemplateLoader(
            ClassTemplateLoader(
                FreemarkerUtil::class.java,
                "/"
            )
        )

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
    private fun processTemplate(template: Template, params: Map<String, Any?>): String {
        val result = StringWriter()

        var all_params = JsonMap()
        all_params.putAll(params)

        all_params.put("now", LocalDateTime.now().AsString())
        all_params.put("has_value", FreemarkerHasValue())

        all_params.put("kb", FreemarkerKebabCase())
        all_params.put("bc", FreemarkerBigCamelCase())
        all_params.put("sc", FreemarkerSmallCamelCase())

        all_params.put("is_res", FreemarkerIsRes())
        all_params.put("is_object", FreemarkerIsObject())
        all_params.put("is_type", FreemarkerIsType())

        all_params.put("all_fields", FreemarkerAllField())
        all_params.put("field_is_enum_list", FreemarkerFieldIsEnumList())
        all_params.put("field_cn", FreemarkerFieldCn())
        all_params.put("field_value", FreemarkerFieldValue())
        all_params.put("field_is_list", FreemarkerIsList())
        all_params.put("field_list_type", FreemarkerFieldListType())

        template.process(all_params, result)
        return result.toString()
    }

    /**
     * 按模板的内容执行
     */
    @JvmStatic
    fun processContent(
        content: String,
        params: JsonMap,
        configCallback: ((Configuration) -> Unit)? = null
    ): String {
        val config = getFreemarkerConfig();

        configCallback?.invoke(config);

        val template = Template("template", StringReader(content), config, "utf-8")
//        usingScope(ContextMapScope(params)) {
//
//        }
        return escapeString(processTemplate(template, params))
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
    @JvmStatic
    fun process(
        templateName: String,
        params: JsonMap,
        configCallback: ((Configuration) -> Unit)? = null
    ): String {
        val config = getFreemarkerConfig();

        configCallback?.invoke(config);

        val template: Template = config.getTemplate(templateName)

//        usingScope(ContextMapScope(params)) {
//
//        }
        return escapeString(processTemplate(template, params))
    }
}
