package nbcp.model

import okhttp3.internal.wait
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class InputStreamTextReaderThread(var inputStream: InputStream, var bufferTime: Int = 1000) : Thread() {
    private var br = BufferedReader(InputStreamReader(inputStream, "utf-8"), 1024);


    override fun run() {
        while (true) {
            sleep(bufferTime.toLong());
            try {
                var lines = br.readLines()
                if (lines.any() == false) {
                    continue;
                }

                this.result += lines.joinToString("\n");

                if (done) {
                    break;
                }
            } catch (e: Exception) {
                error = e;
                break;
            } finally {
                if (done) {
                    br.close();
                    break;
                }
            }
        }
    }

    var error: Exception? = null
        get
        private set

    var result = ""
        get
        private set

    private var done = false;
    fun done() {
        this.done = true;
        this.join(bufferTime.toLong() * 2);
    }
}