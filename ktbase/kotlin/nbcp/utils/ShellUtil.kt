package nbcp.utils

import nbcp.comm.ApiResult
import nbcp.comm.AsLong
import nbcp.comm.HasValue
import nbcp.model.InputStreamTextReaderThread
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

object ShellUtil {

    private val logger = LoggerFactory.getLogger(ShellUtil::class.java)


    /**
     * 简单的方式是，传递3个参数： "sh","-c","复杂的命令字符串"
     */
    @JvmOverloads
    @JvmStatic
    fun execRuntimeCommand(
        cmd: String,
        waitForSeconds: Int = 30,
        path: String = "",
        envs: Map<String, String> = mapOf()
    ): ApiResult<String> {

        var processBuilder = ProcessBuilder(StringTokenizer(cmd).toList().map { it.toString() })

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