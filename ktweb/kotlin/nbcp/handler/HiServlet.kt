package nbcp.handler

import ch.qos.logback.classic.Level
import com.wf.captcha.ArithmeticCaptcha
import com.wf.captcha.utils.CaptchaUtil
import nbcp.comm.*
import nbcp.db.db
import nbcp.utils.ClassUtil
import nbcp.utils.SpringUtil
import nbcp.web.*
import org.springframework.beans.factory.annotation.Value
import java.lang.RuntimeException
import java.util.*
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServlet

/**
 * Created by udi on 17-4-6.
 * 1. HandlerInterceptorAdapter 不会拦截 HttpServlet。
 * 2. 不使用 @Controller 注解，不能生成Bean，不能使用 Aop
 */
@WebServlet(urlPatterns = ["/hi"])
open class HiServlet : HttpServlet() {
    public override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        proc(request, response)
    }

    public override fun doPost(request: HttpServletRequest, response: HttpServletResponse) {
        proc(request, response)
    }

    private fun proc(request: HttpServletRequest, response: HttpServletResponse) {
        val json = JsonMap();

        val jarFile = ClassUtil.getStartingJarFile();
        json["应用名称"] = SpringUtil.context.environment.getProperty("app.cn_name");
        json["spring.application.name"] = SpringUtil.context.environment.getProperty("spring.application.name");
        json["当前配置"] = SpringUtil.context.environment.getProperty("spring.profiles.active");
        json["产品线"] =
            SpringUtil.context.environment.getProperty("app.product-line.name") + " : " +
                SpringUtil.context.environment.getProperty("app.product-line.code");

        json["启动文件名"] = jarFile.name;
        json["启动文件生成时间"] = Date(jarFile.lastModified()).AsString();
        json["登录用户Id"] = request.UserId;
        json["登录用户名称"] = request.UserName;
        json["JAVA_VERSION"] = System.getenv("JAVA_VERSION");
        json["JAVA_OPTS"] = System.getenv("JAVA_OPTS");
        json["POD名称"] = System.getenv("HOSTNAME");

        json["镜像版本号"] = System.getenv("DOCKER_IMAGE_VERSION").AsString();

        val gitCommitId = System.getenv("GIT_COMMIT_ID").AsString();
        if (gitCommitId.HasValue) {
            json["Git提交Id"] = gitCommitId;
        }
        val gitCommitTime = System.getenv("GIT_COMMIT_TIME").AsString();
        if (gitCommitTime.HasValue) {
            json["Git提交时间"] = gitCommitTime;
        }

        val sleep = (request.findParameterValue("sleep").AsFloat() * 1000).toLong();
        if (sleep > 0 && sleep <= 3600_000) {
            Thread.sleep(sleep);
        }

        val status = request.findParameterValue("status").AsInt()
        if (status.HasValue) {
            response.status = status
        }


        response.WriteHtmlBodyValue("""<style>div{margin:10px;} span{margin:5px;font-size:16px;display:inline-block}</style>""" +
            "<div>" + json
            .map { "<span>" + it.key + " : " + it.value.AsString() + "</span>" }
            .joinToString("<br />") + "</div>");
    }
}

