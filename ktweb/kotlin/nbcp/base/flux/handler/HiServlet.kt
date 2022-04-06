package nbcp.base.flux.handler

import nbcp.comm.*
import nbcp.db.db
import nbcp.utils.ClassUtil
import nbcp.utils.SpringUtil
import nbcp.base.mvc.*
import org.reactivestreams.Publisher
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
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
@ConditionalOnClass(Publisher::class)
@ConditionalOnMissingClass("javax.servlet.http.HttpServletRequest")
open class HiServlet {
    @GetMapping("/hi")
    fun doGet(swe: ServerWebExchange): Mono<String> {

        val json = mutableMapOf<String, String?>();
        val env = SpringUtil.context.environment;

        val jarFile = ClassUtil.getStartingJarFile();
        json["应用名称"] = env.getProperty("app.cn_name");
        json["当前配置"] = env.getProperty("spring.profiles.active");
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



        return Mono.just("""<style>
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

