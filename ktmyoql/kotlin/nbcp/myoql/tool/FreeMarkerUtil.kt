package nbcp.myoql.tool

import freemarker.cache.ClassTemplateLoader
import freemarker.template.*
import nbcp.base.comm.*
import nbcp.base.db.*
import nbcp.base.db.annotation.DbEntityIndex
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
    private fun processTemplate(template: Template, params: BaseFreemarkerModel): String {
        val result = StringWriter()
        template.process(params, result)
        return result.toString()
    }





    /**
     * 按模板的内容执行
     */
    @JvmStatic
    fun processContent(
        content: String,
        params: BaseFreemarkerModel,
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
        params: BaseFreemarkerModel,
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



    /**
     * 查找所有唯一索引，每组用逗号分隔。
     */
    @JvmOverloads
    @JvmStatic
    fun getEntityUniqueIndexesDefine(
        entType: Class<*>,
        procedClasses: MutableSet<String> = mutableSetOf()
    ): Set<String> {
        procedClasses.add(entType.name)

        val uks = mutableSetOf<String>()

        entType.getAnnotationsByType(DbEntityIndex::class.java).forEach {
            if (it.unique) {
                uks.add(it.value.joinToString(","))
            }
        }

        if (entType.superclass != null && !procedClasses.contains(entType.superclass.name)) {
            uks.addAll(getEntityUniqueIndexesDefine(entType.superclass, procedClasses))
        }
        return uks;
    }
}
