package nbcp.web.feign

import feign.Client
import feign.Request
import feign.Response
import nbcp.HttpContext
import nbcp.base.comm.const
import nbcp.base.extend.AsString
import nbcp.base.extend.IsStatic
import nbcp.base.extend.ToMap
import nbcp.mvc.comm.HttpFeignLogData

/**
 * https://zhuanlan.zhihu.com/p/528834129
 */
class TraceFeignClientDecorator(private var delegate: Client) : Client {
    override fun execute(request: Request, options: Request.Options): Response {
        //请求前拦截

        var res = delegate.execute(request, options)!!

        var resBody = res.body().asReader(const.utf8).readText();

        //请求后拦截
        var lastFeign = HttpFeignLogData();
        lastFeign.status = res.status();
        lastFeign.requestUrl = request.url();
        lastFeign.requestHeaders = request.headers().ToMap ({ it.key },{it.value.joinToString(",")})
        lastFeign.responseHeaders = res.headers().ToMap ({ it.key },{it.value.joinToString(",")})
        lastFeign.responseBody = resBody
        HttpContext.lastFeign = lastFeign

        var method = request.requestTemplate().methodMetadata().method()
        var postAnn = method.getAnnotation(FeignResponseBodyDecoder::class.java)
        if (postAnn == null) {
            return getResult(res, resBody);
        }

        var postMethodName = postAnn.value.AsString(method.name + "_posted")

        var postMethod = method.declaringClass.methods.firstOrNull { it.name == postMethodName };
        if (postMethod == null) {
            return getResult(res, resBody);
        }

        if (postMethod.IsStatic == false) {
            throw RuntimeException("后置处理方法 ${postMethodName} 必须是静态方法")
        }

        var res2Body = postMethod.invoke(null, resBody).AsString()

        return getResult(res, res2Body);
    }

    private fun getResult(res: Response, res2Body: String):Response {
        var res2 = Response.builder()
                .status(res.status())
                .reason(res.reason())
                .headers(res.headers())
                .request(res.request())
                .body(res2Body, const.utf8)
                .build()
        return res2;
    }
}