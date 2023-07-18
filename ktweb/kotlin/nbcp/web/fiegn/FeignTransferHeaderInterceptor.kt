package nbcp.web.fiegn

import feign.RequestInterceptor
import feign.RequestTemplate
import nbcp.HttpContext
import nbcp.base.extend.HasValue
import org.springframework.stereotype.Component


@Component
class FeignTransferHeaderInterceptor : RequestInterceptor {


    override fun apply(template: RequestTemplate) {
        transferXTraceId(template);
        transferXRealIp(template);
    }

    private fun transferXRealIp(template: RequestTemplate) {
        var ip = HttpContext.clientIp;
        if (ip.HasValue) {
            template.header("X-Real-Ip", ip);
        }
    }

    private fun transferXTraceId(template: RequestTemplate) {
        var value = HttpContext.xTraceId;
        if (value.HasValue) {
            template.header("X-Trace-Id", value);
        }
    }
}