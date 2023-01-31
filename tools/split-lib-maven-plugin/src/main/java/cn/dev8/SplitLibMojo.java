package cn.dev8;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import cn.dev8.util.FileUtil;
import cn.dev8.util.ShellUtil;
import cn.dev8.util.StringUtil;
import lombok.SneakyThrows;
import lombok.var;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.jar.JarFile;

/**
 * Goal which touches a timestamp file.
 *
 * @deprecated Don't use!
 */
@Mojo(name = "split-lib", defaultPhase = LifecyclePhase.PACKAGE)
public class SplitLibMojo
        extends AbstractMojo {
    /**
     * Location of the file.
     */
    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    private File outputDirectory;


    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * 默认仅保留和该项目相同的 groupId
     */
    @Parameter(property = "keepGroupIds", defaultValue = "")
    private String keepGroupIds;

    @Parameter(property = "skip", defaultValue = "false")
    private Boolean skip;

    private String jarExePath = "";

    private File jarFile = null;

    private String nowString = "";

    @SneakyThrows
    public void execute()
            throws MojoExecutionException {
        if (skip) {
            return;
        }

        var now = LocalDateTime.now();
        nowString = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"));

        if (!outputDirectory.exists()) {
            throw new RuntimeException("找不到 " + outputDirectory + " !");
        }

        File splitLibPath = initWorkPathAndGetLibFile();
        checkLayout();

        jarExePath = getJarExePath();

        String[] groupIds = getKeepGroupIds();

        jarFile = new File(FileUtil.resolvePath(outputDirectory.getPath(), project.getArtifactId() + "-" + project.getVersion() + ".jar"));
        var jarBakFile = new File(jarFile.getPath() + ".split-lib.bak");

        if (jarBakFile.exists() == false) {
            jarFile.renameTo(jarBakFile);
        }

        if (jarBakFile.exists() == false) {
            throw new RuntimeException("找不到 " + jarFile.getPath() + "!");
        }
        extractJar(jarBakFile.getPath());


        var libFile = new File(FileUtil.resolvePath(splitLibPath.getPath(), "jar", "BOOT-INF", "lib"));
        if (libFile.exists() == false) {
            getLog().error(jarFile.getName() + " 中不存在 BOOT-INF/lib !");
            return;
        }
        var libJars = libFile.listFiles();

        var keepCount = 0;
        var splitCount = 0;
        getLog().info(StringUtil.fillWithPad("包名", 48, ' ') + "匹配的groupId");
        getLog().info(StringUtil.fillWithPad("", 64, '-'));
        for (var jar : libJars) {
            var groupId = getJarGroupId(jar);

            if (indexOfItemStartWith(groupIds, groupId) < 0) {
                jar.renameTo(new File(FileUtil.resolvePath(splitLibPath.getPath(), "lib", jar.getName())));
//                var key = StringUtil.fillWithPad(jar.getPath().substring(libFile.getPath().length() + 1), 48, ' ');
                splitCount++;
            } else {
                var key = StringUtil.fillWithPad(jar.getPath().substring(libFile.getPath().length() + 1), 48, ' ');

                getLog().info(key + StringUtil.fillWithPad(groupId, 16, ' ') + " √");
                keepCount++;
            }
        }

        getLog().info("split-lib 拆到 target/lib " + splitCount + " 个Jar包, " + jarFile.getName() + " 中保留了 " + keepCount + " 个包！");

//        if (FileUtil.deleteAll(new File(FileUtil.joinPath(splitLibPath.getPath(), "tmp")), true) == false) {
//            getLog().warn("清理 tmp  失败！");
//        }


        zipJar(FileUtil.resolvePath(splitLibPath.getPath(), "jar"));
        var writer = new OutputStreamWriter(new FileOutputStream(FileUtil.resolvePath(splitLibPath.getPath(), "readme.txt")), StandardCharsets.UTF_8);

        try {
            writer.write("[" + project.getName() + "]" + FileUtil.LINE_BREAK + "execute split-lib-maven-plugin at : " + nowString + FileUtil.LINE_BREAK + FileUtil.LINE_BREAK);
            writer.write("keepGroupIds : " + String.join(",", groupIds) + FileUtil.LINE_BREAK);
            writer.write("共拆出: " + splitCount + " 个包" + FileUtil.LINE_BREAK + FileUtil.LINE_BREAK);
            writer.write("运行：" + FileUtil.LINE_BREAK);
            writer.write("java-" + System.getProperty("java.version") + " -Dloader.path=lib -jar " + jarFile.getName());
        } finally {
            writer.flush();
            writer.close();
        }

        jarFile.delete();

        new File(FileUtil.resolvePath(splitLibPath.getPath(), jarFile.getName())).renameTo(jarFile);
        var libPath = new File(FileUtil.resolvePath(outputDirectory.getPath(), "lib"));
        if (FileUtil.deleteAll(libPath, false) == false) {
            throw new RuntimeException("删除 lib 文件夹出错！");
        }

        new File(FileUtil.resolvePath(splitLibPath.getPath(), "lib")).renameTo(libPath);
    }

    private File initWorkPathAndGetLibFile() {
        var splitLibPath = new File(FileUtil.resolvePath(outputDirectory.getPath(), "split-lib"));
        if (splitLibPath.exists() && FileUtil.deleteAll(splitLibPath, false)) {

            if (splitLibPath.exists() && splitLibPath.list().length > 0) {
                throw new RuntimeException("删除文件夹: " + splitLibPath.getPath() + " 失败！");
            }
        }

        splitLibPath.mkdirs();
        return splitLibPath;
    }

    private String getJarExePath() {
        var osName = System.getProperty("os.name").toLowerCase();
        var javaHomePath = System.getProperty("java.home");

        //修复！
        if (javaHomePath.endsWith("jre")) {
            javaHomePath = FileUtil.resolvePath(javaHomePath, "../");
        }

        var jarExePath = FileUtil.resolvePath(javaHomePath, "bin", "jar");
        if (osName.contains("windows")) {
            jarExePath += ".exe";
        }

        if (new File(jarExePath).exists() == false) {
            getLog().error("JAVA_HOME:" + javaHomePath);
            throw new RuntimeException("找不到 jar 命令：" + jarExePath);
        }

        return jarExePath;
    }

    private String[] getKeepGroupIds() {
        String[] groupIds = null;
        if (keepGroupIds == null || keepGroupIds.length() == 0) {
            groupIds = new String[]{project.getGroupId()};
        } else {
            groupIds = keepGroupIds.split(",");
        }

        getLog().info("split-lib 拆分的包名为：" + String.join(",", groupIds));

        return groupIds;
    }

    @SneakyThrows
    private void checkLayout() {
        var pomContent = FileUtil.readFileContent(project.getFile().getPath());

        var checked = pomContent.contains("org.springframework.boot") &&
                pomContent.contains("spring-boot-maven-plugin") &&
                pomContent.contains("<layout>ZIP</layout>");


        if (checked) {
            return;
        }

        var message = "org.springframework.boot:spring-boot-maven-plugin.configuration.layout must be ZIP!";

        getLog().error("split-lib 检查不通过！ " + message);

        var writer = new FileWriter(FileUtil.resolvePath(outputDirectory.getPath(), "split-lib", "split-lib-check.txt"));
        writer.write("[" + project.getName() + "]" + FileUtil.LINE_BREAK + "execute split-lib-maven-plugin at : " + nowString + FileUtil.LINE_BREAK + FileUtil.LINE_BREAK);
        writer.write(message);
        writer.flush();
        writer.close();

        throw new RuntimeException(message);
    }


    public int indexOfItemStartWith(String[] list, String item) {
        if (item == null) return -1;
        var index = -1;
        for (var it : list) {
            index++;
            if (item.startsWith(it)) {
                return index;
            }
        }
        return -1;
    }

    private void zipJar(String workPath) {
        var cmd = new ArrayList<String>();
        cmd.add(jarExePath);
        cmd.add("cf0M");
        cmd.add(FileUtil.resolvePath(workPath, "..", jarFile.getName()));
        cmd.add("*");

        getLog().info("当前目录：" + workPath);
        getLog().info(String.join(" ", cmd));

        var bash_cmd = new ArrayList<String>();
        if (System.getProperty("os.name").toLowerCase().contains("linux")) {
            bash_cmd.add("/bin/bash");
            bash_cmd.add("-c");
            bash_cmd.add(String.join(" ", cmd));
        } else {
            bash_cmd = cmd;
        }

        var result = ShellUtil.execRuntimeCommand(bash_cmd, workPath);
        if (result.hasError()) {
            throw new RuntimeException(result.getMsg());
        }
    }

    private void extractJar(String jarFileName) {
        var splitLibPath = new File(FileUtil.resolvePath(outputDirectory.getPath(), "split-lib"));


        var workJar = new File(FileUtil.resolvePath(splitLibPath.getPath(), "jar"));
        workJar.mkdirs();
        new File(FileUtil.resolvePath(splitLibPath.getPath(), "lib")).mkdirs();
//        new File(FileUtil.joinPath(splitLibPath.getPath(), "tmp")).mkdirs();


        var cmd = new ArrayList<String>();
        cmd.add(jarExePath);
        cmd.add("xf");
        cmd.add(jarFileName);


        var result = ShellUtil.execRuntimeCommand(cmd, workJar.getPath());
        if (result.hasError()) {
            throw new RuntimeException(result.getMsg());
        }
    }

    @SneakyThrows
    private String getJarGroupId(File file) {
        var jarFile = new JarFile(file);

        var entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            var jarEntry = entries.nextElement();
            if (jarEntry.isDirectory()) {
                continue;
            }
            if (jarEntry.getName().startsWith("META-INF/maven")
                    && jarEntry.getName().endsWith("/pom.properties")) {
                var inputStream = jarFile.getInputStream(jarEntry);

                byte[] bytes = new byte[inputStream.available()];
                inputStream.read(bytes);

                inputStream.close();
                jarFile.close();
                return getGroupIdFromPomProperties(new String(bytes, StandardCharsets.UTF_8));
            }
        }
        jarFile.close();
        return "";
    }

    private String getGroupIdFromPomProperties(String content) {
        var lines = content.split("\n");
        for (var l : lines) {
            if (l.startsWith("groupId=")) {
                return l.split("=")[1].trim();
            }
        }
        return "";
    }

    private String getJarGroupIdWithUnzip(File jar) {
        var tmpPath = new File(FileUtil.resolvePath(outputDirectory.getPath(), "split-lib", "tmp", jar.getName()));
        if (tmpPath.mkdirs() == false) {
            throw new RuntimeException("创建文件夹失败：" + tmpPath.getPath());
        }

        var cmd = new ArrayList<String>();
        cmd.add(jarExePath);
        cmd.add("xf");
        cmd.add(jar.getPath());

        var result = ShellUtil.execRuntimeCommand(cmd, tmpPath.getPath());
        if (result.hasError()) {
            throw new RuntimeException(result.getMsg());
        }

        var startFile = new File(FileUtil.resolvePath(tmpPath.getPath(), "META-INF", "maven"));
        if (startFile.exists() == false) {
            return "";
        }
        var pomPropertyFile = getPomPropertyFile(startFile);

        if (pomPropertyFile == null) {
            return "";
        }

        var content = FileUtil.readFileContent(pomPropertyFile.getPath());
        return getGroupIdFromPomProperties(content);
    }

    private File getPomPropertyFile(File tmpPath) {

        var files = tmpPath.listFiles();
        if (files == null) return null;

        for (var f : files) {
            if (f.isFile()) {
                if (f.getName().equals("pom.properties")) {
                    return f;
                }
            } else {
                var t = getPomPropertyFile(f);
                if (t != null) {
                    return t;
                }
            }
        }

        return null;
    }
}
