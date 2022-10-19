package nbcp.model

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class InputStreamTextReaderThread(var inputStream: InputStream, var bufferTime: Int = 1000) : Thread() {
    private var br = BufferedReader(InputStreamReader(inputStream, "utf-8"), 1024);


    override fun run() {
        while (true) {
            try {
                var line = br.readText()
                if (line.isEmpty()) {
                    break;
                }

                if (line.startsWith("Progress (") && line.contains("/")) {
                    continue;
                }

                this.results += line

                if (done) {
                    break;
                }
            } catch (e: Exception) {
                error = e;
                break;
            } finally {
                if (done) {
                    br.close();
                }
            }
        }
    }

    var error: Exception? = null
        get
        private set

    var results = mutableListOf<String>()
        get
        private set

    private var done = false;
    fun done() {
        this.done = true;
        this.join(bufferTime.toLong() * 2);
    }
}