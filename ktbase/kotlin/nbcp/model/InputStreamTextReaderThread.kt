package nbcp.model

import okhttp3.internal.wait
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class InputStreamTextReaderThread(var inputStream:InputStream ) : Thread() {
    private  var br = BufferedReader(InputStreamReader(inputStream, "utf-8"));


    override fun run() {
        while (true) {
            Thread.sleep(100);
            try {
                result += br.readText()

                if(done){
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
    fun done(){
        this.done = true;
        this.join(150);
    }
}