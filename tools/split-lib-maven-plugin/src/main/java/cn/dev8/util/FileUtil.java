package cn.dev8.util;

import lombok.SneakyThrows;
import lombok.var;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class FileUtil {

    public static final String LINE_BREAK = System.getProperty("line.separator");

    @SneakyThrows
    public static String readFileContent(String fileName) {
        var file = new File(fileName);
        if (file.exists() == false) {
            throw new NoSuchFieldException();
        }
        var reader = new BufferedReader(new FileReader(file));
        var list = new LinkedList<String>();
        while (true) {
            var line = reader.readLine();
            if (line == null) {
                break;
            }

            list.add(line);
        }
        reader.close();

        return String.join(LINE_BREAK, list);
    }

    public static String resolvePath(String... path) {
        if (path.length == 0) {
            return "";
        }

        var firstPath = path[0];
        var isRoot = firstPath.startsWith("/") || firstPath.startsWith("\\");

        var list = new ArrayList<String>();


        var splitList = new ArrayList<List<String>>();

        for (var it : Arrays.asList(path)) {
            splitList.add(ListUtil.<String>fromItems(it.split("[\\\\|/]")));
        }


        var spreadList = ListUtil.<String>unwind(splitList);

        for (var i = spreadList.size() - 1; i >= 0; i--) {
            var it = spreadList.get(i);
            if (it.length() == 0) {
                spreadList.remove(i);
                continue;
            }
            if (it.equals(".")) {
                splitList.remove(i);
            }
        }

        for (var i = 0; i < spreadList.size(); i++) {
            var it = spreadList.get(i);
            if (Objects.equals(it, "..")) {
                if (ListUtil.removeLast(list) == null) {
                    throw new RuntimeException("路径层级溢出");
                }
                continue;
            }

            list.add(it);
        }


        if (isRoot) {
            return File.separator + String.join(File.separator, list);
        }

        return String.join(File.separator, list);
    }

    /**
     * 递归删文件夹
     *
     * @param file
     * @return
     */
    public static boolean deleteAll(File file, boolean ignoreError) {
        if (file.exists() == false) {
            return true;
        }
        var ret = true;

        if (file.isFile()) {
            ret = file.delete();
            if (!ret && !ignoreError) {
                throw new RuntimeException("删除失败: " + file.getPath());
            }
            return true;
        }

        for (var f : file.listFiles()) {
            if (!deleteAll(f, ignoreError) && !ignoreError) {
                return false;
            }
        }


        ret &= file.delete();
        if (!ret && !ignoreError) {
            throw new RuntimeException("删除失败: " + file.getPath());
        }

        return ret;
    }
}
