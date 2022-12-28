package cn.dev8.util;

import lombok.Data;
import lombok.SneakyThrows;
import lombok.var;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;


@Data
public class InputStreamTextReaderThread  extends Thread  {

    private   InputStream inputStream;
    private   int bufferTime;
    private BufferedReader br = null;


    @SneakyThrows
    public InputStreamTextReaderThread(InputStream  inputStream  , int bufferTime){
        this.inputStream = inputStream;
        this.bufferTime = bufferTime;
        this.br = new BufferedReader(new InputStreamReader(inputStream, "utf-8"), 1024);
    }

    @SneakyThrows
    @Override
    public void run() {
        while (true) {
            try {
                var line = br.readLine();
                if (line.isEmpty()) {
                    break;
                }

                if (line.startsWith("Progress (") && line.contains("/")) {
                    continue;
                }

                this.results .add(line);

                if (done) {
                    break;
                }
            } catch (  Exception e ) {
                error = e;
                break;
            } finally {
                if (done) {
                    br.close();
                }
            }
        }
    }

    private Exception error = null;

    public LinkedList<String> results =  new LinkedList<String>();

    private boolean  done = false;

    @SneakyThrows
    public void  done() {
        this.done = true;
        this.join(bufferTime   * 2);
    }
}