package nbcp.base.handler

import ch.qos.logback.classic.Level
import com.wf.captcha.ArithmeticCaptcha
import com.wf.captcha.utils.CaptchaUtil
import nbcp.comm.*
import nbcp.db.db
import nbcp.utils.ClassUtil
import nbcp.utils.SpringUtil
import nbcp.web.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.RuntimeException
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
open class HiServlet {
    @GetMapping("/hi")
    fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        proc(request, response)
    }

    private fun proc(request: HttpServletRequest, response: HttpServletResponse) {
        val json = StringMap();
        val env = SpringUtil.context.environment;

        val jarFile = ClassUtil.getStartingJarFile();
        json["应用名称"] = env.getProperty("app.cn_name").AsString();
        json["当前配置"] = env.getProperty("spring.profiles.active").AsString();
        json["产品线"] =
            env.getProperty("app.product-line.name").AsString() + "(" +
                    env.getProperty("app.product-line.code") + ")";

        json["启动文件"] = jarFile.name;
        json["启动文件时间"] = Date(jarFile.lastModified()).AsString();
//        json["登录用户Id"] = request.UserId;
//        json["登录用户名称"] = request.UserName;
        json["JAVA_VERSION"] = System.getProperty("java.version");
        json["JAVA_OPTS"] = System.getenv("JAVA_OPTS");
        json["HOST名称"] = System.getenv("HOSTNAME");

        json["镜像版本号"] = env.getProperty("app.docker-image-version").AsString();
        json["Git提交Id"] = env.getProperty("app.git-commit-id").AsString();
        json["Git提交时间"] = env.getProperty("app.git-commit-time").AsString();


        val sleep = (request.findParameterValue("sleep").AsFloat() * 1000).toLong();
        if (sleep > 0 && sleep <= 3600_000) {
            Thread.sleep(sleep);
        }

        val status = request.findParameterValue("status").AsInt()
        if (status.HasValue) {
            response.status = status
        }


        response.WriteHtmlBodyValue("""<style>
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
                    .joinToString(""));
    }
}

