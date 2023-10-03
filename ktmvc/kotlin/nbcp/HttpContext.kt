package nbcp

import nbcp.base.utils.ClassUtil
import nbcp.flux.ClientIp
import nbcp.flux.FluxContext
import nbcp.flux.XTraceId
import nbcp.mvc.comm.HttpFeignLogData
import nbcp.mvc.sys.ClientIp
import nbcp.mvc.sys.MvcContext
import nbcp.mvc.sys.XTraceId

object HttpContext {
    val isWebFluxEnv: Boolean
        get() {
            return ClassUtil.existsClass("org.springframework.web.reactive.config.WebFluxConfigurationSupport");
        }

    val isMvcEnv: Boolean
        get() {
            return ClassUtil.existsClass("org.springframework.web.servlet.HandlerInterceptor")
        }


    private var _last_feign = ThreadLocal.withInitial<HttpFeignLogData?> { null }
    var lastFeign: HttpFeignLogData
        get() {
            var ret = _last_feign.get()
            if (ret == null) {
                throw RuntimeException("找不到 Feign！");
            }
            return ret;
        }
        set(value) {
            _last_feign.set(value);
        }

    val clientIp: String
        get() {
            if (isMvcEnv) {
                if (MvcContext.hasRequest) {
                    return MvcContext.request.ClientIp;
                }
            }

            if (isWebFluxEnv) {
                if (FluxContext.hasRequest) {
                    return FluxContext.exchange.ClientIp
                }
            }

            return "";
        }

    val xTraceId: String
        get() {
            if (isMvcEnv) {
                if (MvcContext.hasRequest) {
                    return MvcContext.request.XTraceId;
                }
            } else if (isWebFluxEnv) {
                if (FluxContext.hasRequest) {
                    return FluxContext.exchange.XTraceId
                }
            }

            return "";
        }

}