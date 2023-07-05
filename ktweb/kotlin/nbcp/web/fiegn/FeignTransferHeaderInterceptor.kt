package nbcp.web.fiegn

import feign.RequestInterceptor
import feign.RequestTemplate
import nbcp.base.extend.AsString
import nbcp.base.extend.HasValue
import nbcp.mvc.flux.ClientIp
import nbcp.mvc.flux.XTraceId
import nbcp.mvc.flux.getHeader
import nbcp.mvc.mvc.ClientIp
import nbcp.mvc.mvc.HttpContext
import nbcp.mvc.mvc.XTraceId
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component


@Component
class FeignTransferHeaderInterceptor : RequestInterceptor {


    override fun apply(template: RequestTemplate) {
        transferXTraceId(template);
        transferXRealIp(template);
    }

    private fun transferXRealIp(template: RequestTemplate) {
        var ip = getClientIp();
        if (ip.HasValue) {
            template.header("X-Real-Ip", ip);
        }
    }

    private fun transferXTraceId(template: RequestTemplate) {
        var value = getXTraceId();
        if (value.HasValue) {
            template.header("X-Trace-Id", value);
        }
    }

    private fun getClientIp(): String {
        if (HttpContext.hasRequest) {
            return HttpContext.request.ClientIp;
        } else if (nbcp.mvc.flux.HttpContext.hasRequest) {
            return nbcp.mvc.flux.HttpContext.exchange.ClientIp;
        }
        return "";
    }

    private fun getXTraceId(): String {
        if (HttpContext.hasRequest) {
            return HttpContext.request.XTraceId;
        } else if (nbcp.mvc.flux.HttpContext.hasRequest) {
            return nbcp.mvc.flux.HttpContext.exchange.XTraceId;
        }
        return "";
    }
}