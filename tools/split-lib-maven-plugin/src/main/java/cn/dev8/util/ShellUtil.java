package cn.dev8.util;

import lombok.SneakyThrows;
import lombok.var;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ShellUtil {

    public static ApiResult<String> execRuntimeCommand(
            List<String> cmds ,
            String path
    ){
        return execRuntimeCommand(cmds, 30,path,new HashMap<String, String>());
    }


    @SneakyThrows
    public static  ApiResult<String> execRuntimeCommand(
            List<String> cmds ,
             int     waitForSeconds ,
            String path ,
            Map<String, String> envs
    )  {

        var processBuilder = new ProcessBuilder( cmds );

        if (envs.size() > 0) {
            var penv = processBuilder.environment();
            penv.putAll(envs);
        }

        if (path != null &&  path.length() > 0) {
            var d = new File(path);
            if (d.exists()) {
                processBuilder.directory(d);
            }
        }
        processBuilder.redirectErrorStream(true);

        var process = processBuilder.start();

        var streamThread = new InputStreamTextReaderThread(process.getInputStream(),1000);
        streamThread.start();

        var result = "";
        if (process.waitFor(waitForSeconds , TimeUnit.SECONDS)) {
            streamThread.done();
            result = String.join( "", streamThread.results );
        } else {
            streamThread.done();
            return  ApiResult.error("超时");
        }

        if (process.exitValue() == 0) {
            return  ApiResult.of(result);
        }
        return  ApiResult.error(result);
    }
}
