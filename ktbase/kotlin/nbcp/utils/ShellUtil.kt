package nbcp.utils

import nbcp.comm.ListResult
import nbcp.model.InputStreamTextReaderThread
import okhttp3.internal.wait
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

object ShellUtil {

    private val logger = LoggerFactory.getLogger(ShellUtil::class.java)


    /**
     * 简单的方式是，传递3个参数： "sh","-c","复杂的命令字符串"
     */
    @JvmOverloads
    @JvmStatic
    fun execRuntimeCommand(vararg cmds: String, waitSeconds: Int = 30): List<String> {
        var p = Runtime.getRuntime().exec(cmds);

        var t1 = InputStreamTextReaderThread(p.inputStream)
        var t2 = InputStreamTextReaderThread(p.errorStream)

        t1.start();
        t2.start();

        var count = -1;
        while (true) {
            count++;

            if (count > waitSeconds) {
                break;
            }

            if (p.waitFor(1, TimeUnit.SECONDS)) {
                break;
            }
        }

        if (p.exitValue() == 0) {
            t1.done();
            t2.done()

            if (t1.error != null) {
                logger.error(cmds.joinToString(" "));
                throw RuntimeException(t1.error);
            }


            logger.info(cmds.joinToString(" "));
            return t1.results
        } else {
            t1.done()
            t2.done()


            logger.error(cmds.joinToString(" "));
            
            if (t2.error != null) {
                throw RuntimeException(t2.error);
            }

            if (t1.results.any()) {
                throw RuntimeException(t1.results.joinToString("\n"))
            }

            throw RuntimeException(t2.results.joinToString("\n"))
        }
    }
}