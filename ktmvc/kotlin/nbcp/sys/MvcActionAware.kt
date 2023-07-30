package nbcp.sys

import com.google.common.collect.Lists
import nbcp.base.utils.UrlUtil
import nbcp.mvc.annotation.StopLog
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * 统一日志处理切面
 * Created by iamnewsea
 */
@Component
class MvcActionAware : ApplicationContextAware {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass);

        val stopLogs = mutableListOf<String>()
    }

//    @Around("(@annotation(org.springframework.web.bind.annotation.PostMapping) ||" +
//            " @annotation(org.springframework.web.bind.annotation.GetMapping) ||" +
//            " @annotation(org.springframework.web.bind.annotation.RequestMapping) ) &&" +
//            " execution(void *(..))")
//    fun aopMvcVoid(joinPoint: ProceedingJoinPoint) {
//        mvcAop(joinPoint)
//    }
//
//    @Around("(@annotation(org.springframework.web.bind.annotation.PostMapping) ||" +
//            " @annotation(org.springframework.web.bind.annotation.GetMapping) ||" +
//            " @annotation(org.springframework.web.bind.annotation.RequestMapping) ) &&" +
//            " execution(!void *(..))")
//    fun aopMvcReturn(joinPoint: ProceedingJoinPoint): Any? {
//        return mvcAop(joinPoint)
//    }

//    private fun mvcAop(joinPoint: ProceedingJoinPoint): Any? {
//        val method = (joinPoint.signature as MethodSignature).method
//        val args = joinPoint.args
//
//        var paths = getStopLogPaths(method);
//
//        if (paths.any()) {
//            // 添加到
//            stopLogs.addAll(paths);
//        }
//
//        return joinPoint.proceed(args)
//    }

    private fun getStopLogPaths(method: Method): List<String> {
        if (Modifier.isNative(method.modifiers) || Modifier.isStatic(method.modifiers)) {
            return listOf()
        }


        var anns = method.annotations;
        var objAnns = method.declaringClass.annotations;

        var stopLog = anns.filter { it is StopLog }.firstOrNull()
                ?: objAnns.filter { it is StopLog }.firstOrNull()

        if (stopLog == null) {
            return listOf()
        }

        var basePath = getPath(objAnns)
        var path = getPath(anns);

        if (basePath.isEmpty()) {
            return path.toList()
        }

        //迪卡尔积
        return Lists.cartesianProduct(basePath.toList(), path.toList())
                .map { UrlUtil.joinUrl("", *it.toTypedArray()) }
    }

    private fun getPath(anns: Array<Annotation>): Array<String> {

        var get = anns.filter { it is GetMapping }.firstOrNull();
        if (get != null) {
            return (get as GetMapping).value
        }

        var post = anns.filter { it is PostMapping }.firstOrNull();
        if (post != null) {
            return (post as PostMapping).value
        }

        var request = anns.filter { it is RequestMapping }.firstOrNull();
        if (request != null) {
            return (request as RequestMapping).value
        }

        return arrayOf()
    }


    override fun setApplicationContext(applicationContext: ApplicationContext) {
        var controllers = applicationContext.getBeansWithAnnotation(RestController::class.java);

        controllers.forEach { controller ->
            controller.value.javaClass.methods.forEach { method ->
                var paths = getStopLogPaths(method);

                if (paths.any()) {
                    // 添加到
                    stopLogs.addAll(paths);
                }
            }
        }
    }
}