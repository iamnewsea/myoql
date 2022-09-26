package nbcp.utils

import nbcp.TestBase
import nbcp.comm.*
import nbcp.model.InputStreamTextReaderThread
import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.*
import java.util.concurrent.TimeUnit


class ShellUtilTest : TestBase() {
    @Test
    fun abfc() {
        var shell =
            execRuntimeCommand("mvn2 dependency:tree -e -pl ktbase", 30, "/home/udi/IdeaProjects/nancal/open/ktmyoql")

        println(shell.data ?: shell.msg)
    }


    private fun execRuntimeCommand(
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

        var process = try {
            processBuilder.start();
        } catch (e: Exception) {
            return ApiResult.error(e.message ?: "命令错误")
        }

        var streamThread = InputStreamTextReaderThread(process.inputStream)
        streamThread.start();

        var result = "";
        if (process.waitFor(waitForSeconds.AsLong(), TimeUnit.SECONDS)) {
            streamThread.done();
            result = streamThread.results.joinToString("");
        } else {
            streamThread.done()
            return ApiResult.error("命令超时")
        }

        if (process.exitValue() == 0) {
            return ApiResult.of(result)
        }
        return ApiResult.error(result)
    }
}