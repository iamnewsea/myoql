package nbcp.base.flux.filter

import nbcp.base.flux.findParameterValue
import nbcp.base.flux.getCorsResponseMap
import nbcp.comm.*
import org.reactivestreams.Publisher
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.server.WebFilter
import reactor.core.publisher.Mono
import java.lang.Exception
import java.lang.reflect.UndeclaredThrowableException

//@Configuration
@ConditionalOnClass(Publisher::class)
@ConditionalOnMissingClass("javax.servlet.http.HttpServletRequest")
class CrossFilterConfig {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    @Value("\${app.filter.allow-origins:}")
    var allowOrigins: String = "";

    /**
     * 可以定义禁止的 header,默认允许通过所有 Header
     */
    @Value("\${app.filter.deny-headers:}")
    var denyHeaders: List<String> = listOf()


    @Bean
    fun getCrossFilter(): WebFilter {
        return WebFilter { exchange_ori, chain ->
            var exchange = exchange_ori

            if (ignoreFilter(exchange.request)) {
                return@WebFilter chain.filter(exchange)
            }

            exchange.request.getCorsResponseMap(allowOrigins.split(","), denyHeaders)
                .apply {
                    if (this.any()) {
                        var originClient = exchange.request.getHeader("origin") ?: ""

                        var request2 = exchange.request.mutate().headers {
                            it.remove("origin")
                        }.build()

                        if (ignoreLog(exchange.request) == false) {
                            logger.Important("跨域移除(origin)${originClient}, (url)${exchange.request.uri}")
                        }

                        exchange = exchange.mutate().request(request2).build();
                    }
                }.forEach { key, value ->
                    exchange.response.headers.set(key, value);
                }


            if (exchange.request.method == HttpMethod.OPTIONS) {
                exchange.response.rawStatusCode = 204;
                return@WebFilter Mono.empty()
            }

            var token = exchange_ori.findParameterValue("token");

            var ret: Mono<Void> = Mono.empty()
            try {
                ret = chain.filter(exchange);

                if (ignoreLog(exchange.request) == false) {
                    logger.Important("(" + exchange.response.statusCode + ") " + exchange.request.uri.toString() + ", token:" + token)
                }
            } catch (ex: Throwable) {
                var err = getInnerException(ex);
                var errorInfo = mutableListOf<String>()

                errorInfo.add("(" + exchange.response.statusCode + ") " + exchange.request.uri.toString() + ", token:" + token)


                for (key in exchange.request.headers.keys.filter { it.IsIn("token", "api-token", ignoreCase = true) }) {
                    errorInfo.add("\t${key}: ${exchange.request.headers.get(key)?.joinToString()}")
                }

                errorInfo.add(
                    err::class.java.simpleName + ": " + err.Detail.AsString(err.message.AsString()).AsString("(未知错误)")
                        .substring(0, 256)
                )

                errorInfo.addAll(err.stackTrace.map { "\t" + it.className + "." + it.methodName + ": " + it.lineNumber }
                    .take(24))

                var errorMsg = errorInfo.joinToString(const.line_break)


                logger.error(errorMsg);
            }
            return@WebFilter ret;
        }
    }


    @Value("\${app.filter.ignore-log-urls:/health}")
    var ignoreLogUrls: List<String> = listOf()

    @Value("\${app.filter.ignore-urls:/health}")
    var ignoreUrls: List<String> = listOf()


    fun getInnerException(e: Throwable): Throwable {
        var err = e;
        if (err is UndeclaredThrowableException) {
            return err.undeclaredThrowable;
        }

        if (err.cause != null) {
            return getInnerException(err.cause!!)
        }
        return err;
    }

    fun ServerHttpRequest.getHeader(headerName: String): String {
        return this.headers.getFirst(headerName) ?: ""
    }


    fun ignoreFilter(request: ServerHttpRequest): Boolean {
        return matchUrI(request.uri.toString(), ignoreUrls)
    }

    fun ignoreLog(request: ServerHttpRequest): Boolean {
        return matchUrI(request.uri.toString(), ignoreLogUrls) == false
    }


    private fun matchUrI(requestURI: String, defineUris: List<String>): Boolean {
        return defineUris.any {
            var url = it;
            var exact = url.startsWith("(") && url.endsWith(")")
            if (exact) {
                url = url.Slice(1, -1);
            }

            if (url.startsWith("/") == false) {
                url = "/" + url;
            }
            if (exact) {
                return@any requestURI.startsWith(url, true)
            } else {
                return@any requestURI.equals(url, true)
            }
        }
    }
}