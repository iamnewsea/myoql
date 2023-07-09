package nbcp.web.fiegn

import nbcp.base.comm.StringMap
import nbcp.mvc.mvc.HttpContext
import nbcp.mvc.comm.HttpFeignLogData
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FeignResponseConfig {
    @Bean
    fun okHttpClientBuilder(): OkHttpClient.Builder {
        return OkHttpClient.Builder().addInterceptor(FeignOkHttpClientResponseInterceptor())
    }

    /**
     * okHttp响应拦截器
     */
    class FeignOkHttpClientResponseInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val originalRequest = chain.request()
            val lastFeign = HttpFeignLogData()
            lastFeign.requestUrl = originalRequest.url().toString()
            val requestHeaders = StringMap()
            val oriRequestHeaders = originalRequest.headers()
            for (name in oriRequestHeaders.names()) {
                val v = oriRequestHeaders.get(name)
                if (v.isNullOrEmpty()) {
                    continue
                }
                requestHeaders.put(name, v)
            }
            lastFeign.requestHeaders = requestHeaders
            var response: Response? = null
            var error: Exception? = null
            try {
                response = chain.proceed(originalRequest)
            } catch (e: Exception) {
                error = e
            }
            if (error != null) {
                lastFeign.status = 500;
                HttpContext.lastFeign = lastFeign;
                throw error
            }
            val mediaType = response!!.body()!!.contentType()
            val content = response!!.body()!!.string()
            //解析content，做你想做的事情！！
            lastFeign.status = response.code()
            lastFeign.responseBody = content
            val responseHeaders = StringMap()
            val oriResponseHeaders = response.headers()
            for (name in oriResponseHeaders.names()) {
                val v = oriResponseHeaders.get(name)
                if (v.isNullOrEmpty()) {
                    continue
                }
                responseHeaders.put(name, v)
            }
            lastFeign.responseHeaders = responseHeaders
            HttpContext.lastFeign = lastFeign
            //生成新的response返回，网络请求的response如果取出之后，直接返回将会抛出异常
            return response.newBuilder()
                    .body(ResponseBody.create(mediaType, content))
                    .build()
        }
    }
}