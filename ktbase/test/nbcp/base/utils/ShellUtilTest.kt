package nbcp.base.utils

import nbcp.base.TestBase
import nbcp.base.comm.*
import nbcp.base.model.InputStreamTextReaderThread
import org.junit.jupiter.api.Test
import java.io.File
import nbcp.base.comm.*;
import nbcp.base.db.*;
import nbcp.base.enums.*;
import nbcp.base.extend.*;
import nbcp.base.utils.*;
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
    ): nbcp.base.comm.ApiResult<String> {

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
            return nbcp.base.comm.ApiResult.error(e.message ?: "命令错误")
        }

        var streamThread = InputStreamTextReaderThread(process.inputStream)
        streamThread.start();

        var result = "";
        if (process.waitFor(waitForSeconds.AsLong(), TimeUnit.SECONDS)) {
            streamThread.done();
            result = streamThread.results.joinToString("");
        } else {
            streamThread.done()
            return nbcp.base.comm.ApiResult.error("命令超时")
        }

        if (process.exitValue() == 0) {
            return nbcp.base.comm.ApiResult.of(result)
        }
        return nbcp.base.comm.ApiResult.error(result)
    }
}