package nbcp.web.mvc.handler

import nbcp.base.extend.AsFloat
import nbcp.base.extend.AsInt
import nbcp.base.extend.AsString
import nbcp.base.extend.HasValue
import nbcp.base.utils.ClassUtil
import nbcp.base.utils.SpringUtil
import nbcp.mvc.mvc.WriteHtmlBodyValue
import nbcp.mvc.mvc.WriteHtmlValue
import nbcp.mvc.mvc.findParameterValue
import nbcp.mvc.annotation.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


/**
 * Created by udi on 17-4-6.
 * 1. HandlerInterceptorAdapter 不会拦截 HttpServlet。
 * 2. 不使用 @Controller 注解，不能生成Bean，不能使用 Aop
 */
@OpenAction
@RestController
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
open class HiServlet {
    @GetMapping("/hi")
    fun doGet(request: HttpServletRequest, response: HttpServletResponse) {

        val sleep = (request.findParameterValue("sleep").AsFloat() * 1000).toLong();
        if (sleep > 0 && sleep <= 3600_000) {
            Thread.sleep(sleep);
        }

        val status = request.findParameterValue("status").AsInt()
        if (status.HasValue) {
            response.status = status
            return;
        }


        var key = request.getParameter("key")
        if (key.HasValue) {
            val env = SpringUtil.context.environment;
            var value = env.getProperty(key)
            if (value != null) {
                response.WriteHtmlValue(value)
                return;
            } else {
                return;
            }
        }


        getHiContent().apply {
            if (this.HasValue) {
                response.WriteHtmlBodyValue(this);
            }
        }
    }

    private fun getHiContent(): String {
        val json = mutableMapOf<String, String?>();
        val env = SpringUtil.context.environment;

        val jarFile = ClassUtil.getStartingJarFile();
        json["应用名称"] = env.getProperty("app.cn_name");
        json["当前配置"] = env.activeProfiles.joinToString(",")
        json["集群"] = env.getProperty("app.group");

        json["产品线"] =
            env.getProperty("app.product-line.name").AsString() + "(" +
                    env.getProperty("app.product-line.code") + ")";

        if (jarFile != null) {
            json["启动文件"] = jarFile.name;
            json["启动文件时间"] = Date(jarFile.lastModified()).AsString();
        }

//        json["登录用户Id"] = request.UserId;
//        json["登录用户名称"] = request.UserName;
        json["JAVA_VERSION"] = System.getProperty("java.version");
        json["JAVA_OPTS"] = System.getenv("JAVA_OPTS");
        json["HOST名称"] = System.getenv("HOSTNAME");

        json["镜像版本号"] = env.getProperty("app.docker-image-version");
        json["Git提交Id"] = env.getProperty("app.git-commit-id");
        json["Git提交时间"] = env.getProperty("app.git-commit-time");


        return """<style>
body{padding:16px;} 
div{margin-top:10px;} 
div>span:first-child{font-size:14px;color:gray} 
div>span:last-child{font-size:16px;} 
div>span:first-child::after{content:":";display:inline-block;margin-right:6px;}
h1{margin:0}
hr{height: 1px;border: none;border-top: 1px dashed gray;}
</style>""" +
                "<h1>" + SpringUtil.context.environment.getProperty("spring.application.name") + "</h1><hr />" +
                json.filter { it.value.HasValue }
                    .map { "<div><span>${it.key}</span><span>${it.value}</span></div>" }
                    .joinToString("");
    }
}
