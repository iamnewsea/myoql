package nbcp.utils

import nbcp.comm.ListResult
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader

class RuntimeUtil {
}


private val logger = LoggerFactory.getLogger(RuntimeUtil::class.java)

fun execRuntimeCommand(vararg cmds: String): ListResult<String> {
    logger.warn(cmds.joinToString(" "));
    var p = Runtime.getRuntime().exec(cmds);

    var br: BufferedReader? = null;
    try {
        p.waitFor()
        if (p.exitValue() == 0) {
            br = BufferedReader(InputStreamReader(p.inputStream, "utf-8"));
            return ListResult.of(br.readLines());
        } else {
            br = BufferedReader(InputStreamReader(p.errorStream, "utf-8"));
            return ListResult(br.readLines().joinToString(","))
        }
    } catch (e: Exception) {
        return ListResult(e.message ?: "error")
    } finally {
        if (br != null) {
            try {
                br.close();
            } finally {
            }
        }
    }
}