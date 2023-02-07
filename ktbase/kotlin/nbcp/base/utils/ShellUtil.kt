package nbcp.base.utils

import nbcp.base.comm.ApiResult
import nbcp.base.extend.AsLong
import nbcp.base.extend.HasValue
import nbcp.base.model.InputStreamTextReaderThread
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

object ShellUtil {

    private val logger = LoggerFactory.getLogger(ShellUtil::class.java)


    /**
     * 简单的方式是，传递3个参数： "sh","-c","复杂的命令字符串"
     * @param cmds，必须是严格的每个部分， 不能整体传一个字符串！
     */
    @JvmOverloads
    @JvmStatic
    fun execRuntimeCommand(
        cmds: List<String>,
        waitForSeconds: Int = 30,
        path: String = "",
        envs: Map<String, String> = mapOf()
    ): ApiResult<String> {
        logger.info(cmds.joinToString("  "));

        var processBuilder = ProcessBuilder(*cmds.toTypedArray())

        if (envs.any()) {
            var penv = processBuilder.environment()
            penv.putAll(envs)
        }

        if (path.HasValue) {
            var d = File(path);
            if (d.exists()) {
                processBuilder.directory(d);
            }
        }
        processBuilder.redirectErrorStream(true)

        var process = processBuilder.start();

        var streamThread = InputStreamTextReaderThread(process.inputStream)
        streamThread.start();

        var result = "";
        if (process.waitFor(waitForSeconds.AsLong(), TimeUnit.SECONDS)) {
            streamThread.done();
            result = streamThread.results.joinToString("");
        } else {
            streamThread.done()
            return ApiResult.error("超时")
        }

        if (process.exitValue() == 0) {
            return ApiResult.of(result)
        }
        return ApiResult.error(result)
    }
}