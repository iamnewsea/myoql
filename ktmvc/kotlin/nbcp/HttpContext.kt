package nbcp

import nbcp.base.db.LoginUserModel
import nbcp.base.utils.ClassUtil
import nbcp.flux.ClientIp
import nbcp.flux.FluxContext
import nbcp.flux.XTraceId
import nbcp.mvc.comm.HttpFeignLogData
import nbcp.mvc.mvc.ClientIp
import nbcp.mvc.mvc.MvcContext
import nbcp.mvc.mvc.XTraceId

object HttpContext {
    val isWebFluxEnv:Boolean
        get() {
            return ClassUtil.existsClass("org.springframework.web.reactive.config.WebFluxConfigurationSupport");
        }

    val isMvcEnv :Boolean
        get(){
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

    val clientIp:String
        get(){
            if( isMvcEnv){
                return MvcContext.request.ClientIp;
            }

            if( isWebFluxEnv){
                return FluxContext.exchange.ClientIp
            }

            return "";
        }

    val xTraceId:String
        get(){
            if( isMvcEnv){
                return MvcContext.request.XTraceId;
            }

            if( isWebFluxEnv){
                return FluxContext.exchange.XTraceId
            }

            return "";
        }

}