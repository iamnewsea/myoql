package nbcp.base.comm

import java.io.NotSerializableException
import nbcp.base.comm.*
import nbcp.base.extend.*


/**
 * 异常的大概性含义表达
 */
class ExceptionGeneralCodeMessage(var code: Int = 0, var msg: String = "", var fixed: Boolean = false) {
    fun setFixed(): ExceptionGeneralCodeMessage {
        this.fixed = true;
        return this;
    }
}

/**
 * 异常进行分类，用于隐藏异常的详细消息。
 *
 * 系统异常 负数表示。
 * 业务异常 正数表示。
 * 0 表示成功。
 * ---
 * Java普通异常 -500
 * 一般编码有三位，每位有不同的含义 ， 最后一位，用于自定义扩展，现返回0
 * 第2位的前3个是一样的：
 * 系统异常 [-1 到 -2000)
 * 其中 < -1000的是能明确分类的。   > -1000 的表示没有明确分类！ 因为有些系统异常可能比较短，没有明确的包名。
 *      第1位， 1Json,正则，Xml   2 安全   ，3 参数非法，   4文件，资源     5 网络Http  ，          9,  OS
 *      第2位，（1配置 ，2安全问题， 3 异常，超时,数据格式，网络 ）
 * 微服务异常   [-2000 到 -3000)
 *      第1位， 表示使用的产品组件： 1 Eureka, 2 nacos , 3 consul, 4 zk , 5k8s , 6Apollo
 *      第2位， 表示微服务组件 ： （1配置 ，2安全问题， 3 异常，超时,数据格式，网络 ）     + 4网关 ， 5 配置中心, 6Feign   7Hystrix断路器 ， 8Ribbon负载
 *
 * 数据库异常   [-3000 到 -4000)
 *      如果为0, 表示未知异常，处理的异常数从1开始。
 *      第1位， 数据库种类：分别表示： 1mysql(任意关系型数据),2redis,3mongo,4mq(任意Mq),5es, 9cache,  0 表示其它数据库
 *      第2位， （1配置异常, 2安全问题 ， 3 异常，超时,数据格式，网络 ）                 + 4连接异常, 5执行异常, 6数据异常（重复键，类型不对等）
 */
fun Throwable.GetExceptionTypeCode(): ExceptionGeneralCodeMessage? {
    var errorTypeName = this::class.java.name;

    (getFixError(this)?.setFixed() ?: getDbCode(errorTypeName) ?: getMsCode(errorTypeName) ?: getSysCode(errorTypeName))
        .apply {
            if (this == null) return null;
            if (this.msg.last().IsSpecialChar) return this;
            this.msg += " 错误!"
            return this;
        }
}


private class WordsErrorTypeDef(var word: String, var cn: String, var value: Int) {}

private fun String.contains_words(vararg words: String): Boolean {
    return words.any { this.contains(it, true) }
}


private fun getFixError(error: Throwable): ExceptionGeneralCodeMessage? {
    if (error is NotImplementedError) {
        return ExceptionGeneralCodeMessage(-1000, "方法未实现!")
    }
    if (error is ClassNotFoundException) {
        return ExceptionGeneralCodeMessage(-1000, "找不到类 ${error.message}!")
    }
    if (error is NotSerializableException) {
        return ExceptionGeneralCodeMessage(-1000, "没有实现序列化 ${error.message}!")
    }

    if (error is CloneNotSupportedException) {
        return ExceptionGeneralCodeMessage(-1000, "不支持克隆!")
    }
    if (error is TypeNotPresentException) {
        return ExceptionGeneralCodeMessage(-1000, "类型不存在 ${error.typeName()}!")
    }
    if (error is NullPointerException) {
        return ExceptionGeneralCodeMessage(-1000, "空指针!")
    }
    return null;
}

private fun getCommonCode2(errorTypeName: String): WordsErrorTypeDef? {
    return listOf(
        WordsErrorTypeDef("config", "配置", 1),
        WordsErrorTypeDef("property", "属性", 1),
        WordsErrorTypeDef("properties", "属性", 1),
        WordsErrorTypeDef("Define", "定义", 1),

        WordsErrorTypeDef("Security", "安全", 2),
        WordsErrorTypeDef("Authorization", "认证", 2),
        WordsErrorTypeDef("Certificat", "证书", 2),


        WordsErrorTypeDef("ServiceUnavailable", "服务不可用!", 3),
        WordsErrorTypeDef("ServerUnavailable", "服务器不可用!", 3),
        WordsErrorTypeDef("Timeout", "超时", 3),
        WordsErrorTypeDef("socket", "网络", 3),
        WordsErrorTypeDef("Channel", "网络", 3),
        WordsErrorTypeDef("Http", "网络", 3),
        WordsErrorTypeDef("Connection", "网络", 3),
        WordsErrorTypeDef("Proxy", "网络", 3),
//        WordsErrorTypeDef("NotFound", "不存在", 3),
//        WordsErrorTypeDef("NotExists", "不存在", 3),
//        WordsErrorTypeDef("Exists", "已存在", 3),
        WordsErrorTypeDef("Lock", "锁", 3),
        WordsErrorTypeDef("Encoder", "编码", 3),
        WordsErrorTypeDef("Decoder", "解码", 3),


        WordsErrorTypeDef("Verify", "验证", 3),
        WordsErrorTypeDef("Format", "格式", 3),
    ).firstOrNull { errorTypeName.contains_words(it.word) }
}


private fun getSysCode(errorTypeName: String): ExceptionGeneralCodeMessage? {
    listOf(
        WordsErrorTypeDef("Json", "", 1),
        WordsErrorTypeDef("jackson", "", 1),
        WordsErrorTypeDef("gson", "", 1),
        WordsErrorTypeDef("regex", "", 1),
        WordsErrorTypeDef("xml", "", 1),
        WordsErrorTypeDef("html", "", 1),

        WordsErrorTypeDef("security", "安全", 2),

        WordsErrorTypeDef("Argument", "参数", 3),
        WordsErrorTypeDef("Argument", "参数", 3),

        WordsErrorTypeDef("File", "文件", 4),
        WordsErrorTypeDef("resource", "资源", 4),

        WordsErrorTypeDef("net", "网络", 5),
        WordsErrorTypeDef("http", "HTTP", 5),

        WordsErrorTypeDef("memory", "内存", 9),
    ).firstOrNull { errorTypeName.contains_words(it.word) }
        .apply {
            if (this == null) {
                var c3 = getCommonCode2(errorTypeName)
                if (c3 == null) {
                    return null;
                }
                return ExceptionGeneralCodeMessage(
                    c3.value.AsInt(0) * 100,
                    c3.cn.AsString(c3.word.AsString())
                );
            }
            this.value = 10 + this.value;

            var c2 = getSysCode2(errorTypeName) ?: getCommonCode2(errorTypeName)
            return ExceptionGeneralCodeMessage(
                this.value * 1000 + c2?.value.AsInt(0) * 100,
                this.cn.AsString(this.word) + c2?.cn.AsString(c2?.word.AsString())
            );
        }
}

private fun getSysCode2(errorTypeName: String): WordsErrorTypeDef? {
    return listOf(
        WordsErrorTypeDef("BadPadding", "RSA", 2)
    ).firstOrNull { errorTypeName.contains_words(it.word) }
}


private fun getMsCode(errorTypeName: String): ExceptionGeneralCodeMessage? {
    listOf(
        WordsErrorTypeDef("Eureka", "", 1),
        WordsErrorTypeDef("Netfetflix", "", 1),
        WordsErrorTypeDef("nacos", "", 2),
        WordsErrorTypeDef("alibaba", "", 2),
        WordsErrorTypeDef("consul", "", 3),
        WordsErrorTypeDef("Zookeeper", "", 4),
        WordsErrorTypeDef("k8s", "", 5),
        WordsErrorTypeDef("kubernetes", "", 5),
        WordsErrorTypeDef("apollo", "", 6),

        ).firstOrNull { errorTypeName.contains_words(it.word) }
        .apply {
            if (this == null) return null
            this.value = 20 + this.value;

            var c2 = getMsCode2(errorTypeName) ?: getCommonCode2(errorTypeName)
            return ExceptionGeneralCodeMessage(
                this.value * 1000 + c2?.value.AsInt(0) * 100,
                this.cn.AsString(this.word) + c2?.cn.AsString(c2?.word.AsString())
            );
        }
}

private fun getMsCode2(errorTypeName: String): WordsErrorTypeDef? {
    return listOf(
        WordsErrorTypeDef("gateway", "网关", 4),
        WordsErrorTypeDef("zuul", "网关", 4),
        WordsErrorTypeDef("config", "", 5),
        WordsErrorTypeDef("feign", "服务调用", 6),
        WordsErrorTypeDef("Request", "服务调用", 6),
        WordsErrorTypeDef("Response", "服务调用", 6),
        WordsErrorTypeDef("Retryable", "服务调用", 6),
        WordsErrorTypeDef("hystrix", "断路器", 7),
        WordsErrorTypeDef("NoFallback", "断路器", 7),
        WordsErrorTypeDef("ribbon", "负载均衡", 8)
    ).firstOrNull { errorTypeName.contains_words(it.word) }
}


private fun getDbCode(errorTypeName: String): ExceptionGeneralCodeMessage? {
    listOf(
        WordsErrorTypeDef("mysql", "", 1),
        WordsErrorTypeDef("maria", "mariadb", 1),
        WordsErrorTypeDef("mariadb", "", 1),
        WordsErrorTypeDef("sqlserver", "", 1),
        WordsErrorTypeDef("oracle", "", 1),
        WordsErrorTypeDef("postgres", "postgresql", 1),
        WordsErrorTypeDef("postgresql", "", 1),
        WordsErrorTypeDef("sqlite", "", 1),
        WordsErrorTypeDef("excel", "", 1),
        WordsErrorTypeDef("sql", "", 1),

        WordsErrorTypeDef("redis", "", 2),
        WordsErrorTypeDef("jedis", "redis", 2),


        WordsErrorTypeDef("mongodb", "", 3),
        WordsErrorTypeDef("mongo", "mongodb", 3),

        WordsErrorTypeDef("rabbit", "", 4),
        WordsErrorTypeDef("activemq", "", 4),
        WordsErrorTypeDef("rocketmq", "", 4),
        WordsErrorTypeDef("kafka", "", 4),
        WordsErrorTypeDef(".mq.", "mq", 4),


        WordsErrorTypeDef("elasticsearch", "", 5),
        WordsErrorTypeDef(".es.", "es", 5),


        WordsErrorTypeDef("cache", "缓存", 9)

    ).firstOrNull { errorTypeName.contains_words(it.word) }
        .apply {
            if (this == null) return null

            this.value = 30 + this.value;

            var c2 = getDbCode2(errorTypeName) ?: getCommonCode2(errorTypeName)
            return ExceptionGeneralCodeMessage(
                this.value * 1000 + c2?.value.AsInt(0) * 100,
                this.cn.AsString(this.word) + c2?.cn.AsString(c2?.word.AsString())
            );
        }
}


private fun getDbCode2(errorTypeName: String): WordsErrorTypeDef? {
    return listOf(

        WordsErrorTypeDef("connection", "连接", 4),
        WordsErrorTypeDef("pool", "连接池", 4),
        WordsErrorTypeDef("client", "客户端连接", 4),
        WordsErrorTypeDef("open", "打开连接", 4),
        WordsErrorTypeDef("close", "关闭连接", 4),


        WordsErrorTypeDef("command", "命令", 5),
        WordsErrorTypeDef("execute", "执行", 5),
        WordsErrorTypeDef("Execution", "执行", 5),
        WordsErrorTypeDef("query", "查询", 5),
        WordsErrorTypeDef("write", "写入", 5),
        WordsErrorTypeDef("insert", "插入", 5),
        WordsErrorTypeDef("read", "读取", 5),
        WordsErrorTypeDef("update", "更新", 5),
        WordsErrorTypeDef("delete", "删除", 5),
        WordsErrorTypeDef("NotSupported", "明确不支持", 5),
        WordsErrorTypeDef("Syntax", "语法", 5),
        WordsErrorTypeDef("Transaction", "事务", 5),
        WordsErrorTypeDef("DataAccess", "数据访问", 5),
        WordsErrorTypeDef("Request", "请求", 5),
        WordsErrorTypeDef("Response", "响应", 5),
        WordsErrorTypeDef("Aggregation", "聚合", 5),
        WordsErrorTypeDef("Pipeline", "流水线", 5),


        WordsErrorTypeDef("DuplicateKey", "数据键重复", 6),
        WordsErrorTypeDef("SQLData", "数据", 6),
    )
        .firstOrNull { errorTypeName.contains_words(it.word) }
}


