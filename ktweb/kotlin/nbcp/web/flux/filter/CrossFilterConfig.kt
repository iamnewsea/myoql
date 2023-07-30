package nbcp.web.flux.filter

import ch.qos.logback.classic.Level
import nbcp.sys.MvcActionAware
import nbcp.base.comm.config
import nbcp.base.comm.const
import nbcp.base.enums.LogLevelScopeEnum
import nbcp.base.extend.*
import nbcp.flux.FluxContext
import nbcp.flux.findParameterValue
import nbcp.flux.getCorsResponseMap
import nbcp.flux.queryJson
import nbcp.myoql.db.db
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpMethod
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.lang.reflect.UndeclaredThrowableException

//@Configuration
//@ConditionalOnClass(Publisher::class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
//@ConditionalOnMissingClass("javax.servlet.http.HttpServletRequest")
class CrossFilterConfig {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    @Value("\${app.filter.allow-origins:*}")
    var ALLOW_ORIGINS: String = "*";

    /**
     * 可以定义禁止的 header,默认允许通过所有 Header
     */
    @Value("\${app.filter.deny-headers:}")
    var DENY_HEADERS: List<String> = listOf()


    @Bean
    fun getCrossFilter(): WebFilter {
        return WebFilter { exchange_ori, chain ->
            var exchange = exchange_ori

            if ("/health" == exchange.request.path.value()) {
                exchange.response.rawStatusCode = 200;
                return@WebFilter Mono.empty();
            }

            FluxContext.init(exchange)
            db.currentRequestChangeDbTable.clear()

            var logLevel = getLogLevel(exchange_ori.request)
            var ret: Mono<Void>? = null;

            if (logLevel != null) {
                usingScope(logLevel) {
                    ret = invokeFilter(exchange_ori, chain, exchange)
                }
            } else {
                ret = invokeFilter(exchange_ori, chain, exchange)
            }
            return@WebFilter ret;
        }
    }

    private fun invokeFilter(exchange_ori: ServerWebExchange, chain: WebFilterChain, ori_exchange: ServerWebExchange): Mono<Void> {
        if (ignoreFilter(ori_exchange.request)) {
            return chain.filter(ori_exchange)
        }

        var exchange = ori_exchange;
        exchange.request.getCorsResponseMap(ALLOW_ORIGINS.split(","), DENY_HEADERS)
                .apply {
                    if (this.any()) {
                        var originClient = exchange.request.getHeader("origin")

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
            return Mono.empty()
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


            for (key in exchange.request.headers.keys.filter {
                it.IsIn(
                        "token",
                        "api-token",
                        "apiToken",
                        ignoreCase = true
                )
            }) {
                errorInfo.add("\t${key}: ${exchange.request.headers.get(key)?.joinToString(",")}")
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
        return ret
    }

    private fun getLogLevel(httpRequest: ServerHttpRequest): LogLevelScopeEnum? {
        var logLevel: Level? = null;

        var logLevelString = httpRequest.queryJson.findParameterKey("logLevel").AsString();
        if (logLevelString.HasValue &&
                config.adminToken == httpRequest.queryJson.findParameterKey("adminToken")
        ) {
            if (logLevelString.IsNumberic()) {
                var logLevelInt = logLevelString.AsInt()
                if (logLevelInt > 0) {
                    logLevel = Level.toLevel(logLevelInt, Level.WARN)
                }
            } else {
                logLevel = Level.toLevel(logLevelString, Level.WARN)
            }
        } else {
            if (logLevelString.HasValue) {
                logger.Important("admin-token参数值不匹配！忽略 log-level")
            }


            var path = httpRequest.path.value()
            var ignoreLog = matchUrI(path, ignoreLogUrls) ||
                    matchUrI(path, MvcActionAware.stopLogs)

            if (ignoreLog) {
                logLevel = Level.OFF;
            }
        }

        if (logLevel == null) return null;
        return logLevel.levelInt.ToEnum<LogLevelScopeEnum>()
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
        return matchUrI(request.path.value(), ignoreUrls)
    }

    fun ignoreLog(request: ServerHttpRequest): Boolean {
        var path = request.path.value();
        return matchUrI(path, ignoreLogUrls) || matchUrI(path, MvcActionAware.stopLogs)
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