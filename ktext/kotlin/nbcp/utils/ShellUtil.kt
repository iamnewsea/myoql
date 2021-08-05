package nbcp.utils

import nbcp.comm.ListResult
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader

object ShellUtil {

    private val logger = LoggerFactory.getLogger(ShellUtil::class.java)

    /**
     * 简单的方式是，传递3个参数： "sh","-c","复杂的命令字符串"
     */
    fun execRuntimeCommand(vararg cmds: String): List<String> {
        logger.info(cmds.joinToString(" "));
        var p = Runtime.getRuntime().exec(cmds);

        var br: BufferedReader? = null;
        try {
            p.waitFor()
            if (p.exitValue() == 0) {
                br = BufferedReader(InputStreamReader(p.inputStream, "utf-8"));
                return br.readLines();
            } else {
                br = BufferedReader(InputStreamReader(p.errorStream, "utf-8"));
                throw RuntimeException(br.readLines().joinToString(","))
            }
        } finally {
            if (br != null) {
                try {
                    br.close();
                } finally {
                }
            }
        }
    }
}