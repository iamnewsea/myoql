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
import cn.dev8.util.ListUtil;
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
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Goal which touches a timestamp file.
 *
 * @deprecated Don't use!
 */
@Mojo(name = "split-lib", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
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

    private String jarPath = "";

    @SneakyThrows
    public void execute()
            throws MojoExecutionException {
        var now = LocalDateTime.now();
        var nowString = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"));

        if (!outputDirectory.exists()) {
            return;
        }

        String[] groupIds;
        if (keepGroupIds == null || keepGroupIds.length() == 0) {
            groupIds = new String[]{project.getGroupId()};
        } else {
            groupIds = keepGroupIds.split(",");
        }

        getLog().info("split-lib 拆分的包名为：" + String.join(",", groupIds));

        var javaHomePath = System.getenv("JAVA_HOME");
        var osName = System.getProperty("os.name").toLowerCase();
        jarPath = FileUtil.joinPath(javaHomePath, "bin", "jar");
        if (osName.contains("windows")) {
            jarPath += ".exe";
        }

        if (new File(jarPath).exists() == false) {
            throw new RuntimeException("找不到 jar 命令：" + jarPath);
        }

        var splitLibPath = new File(FileUtil.joinPath(outputDirectory.getPath(), "split-lib"));
        if (splitLibPath.exists() && FileUtil.deleteAll(splitLibPath, false)) {
            throw new RuntimeException("删除文件夹: " + splitLibPath.getPath() + " 失败！");
        }

        splitLibPath.mkdirs();


        var writer = new FileWriter(FileUtil.joinPath(splitLibPath.getPath(), "readme.txt"));
        writer.write("[" + project.getName() + "]" + FileUtil.LINE_BREAK + "execute split-lib-maven-plugin at : " + nowString + FileUtil.LINE_BREAK + FileUtil.LINE_BREAK);
        writer.write("keepGroupIds : " + String.join(",", groupIds));
        writer.flush();
        writer.close();


        extractJar(FileUtil.joinPath(outputDirectory.getPath(), project.getArtifactId() + "-" + project.getVersion() + ".jar"));


        var libFile = new File(FileUtil.joinPath(splitLibPath.getPath(), "jar", "BOOT-INF", "lib"));
        if (libFile.exists() == false) {
            getLog().error(project.getArtifactId() + "-" + project.getVersion() + ".jar 中不存在 BOOT-INF/lib !");
            return;
        }
        var libJars = libFile.listFiles();

        var count = 0;
        getLog().info(StringUtil.fillWithPad("包名", 48, '-') + "groupId");
        for (var jar : libJars) {
            var groupId = getJarGroupId(jar);

            if (ListUtil.indexOf(groupIds, groupId) < 0) {
                jar.renameTo(new File(FileUtil.joinPath(splitLibPath.getPath(), "lib", jar.getName())));
                var key = StringUtil.fillWithPad(jar.getPath().substring(libFile.getPath().length() + 1), 48, ' ');

                getLog().info(key + groupId);
                count++;
            } else {
                var key = StringUtil.fillWithPad(jar.getPath().substring(libFile.getPath().length() + 1), 48, '-');

                getLog().info(key + StringUtil.fillWithPad(groupId, 16, ' ') + " ✔");
            }
        }

        getLog().info("split-lib 共拆出 " + count + " 个Jar包！");

        if (FileUtil.deleteAll(new File(FileUtil.joinPath(splitLibPath.getPath(), "tmp")), true) == false) {
            getLog().warn("清理 tmp  失败！");
        }
    }

    private void extractJar(String jarFileName) {
        var splitLibPath = new File(FileUtil.joinPath(outputDirectory.getPath(), "split-lib"));


        var workJar = new File(FileUtil.joinPath(splitLibPath.getPath(), "jar"));
        workJar.mkdirs();
        new File(FileUtil.joinPath(splitLibPath.getPath(), "lib")).mkdirs();
        new File(FileUtil.joinPath(splitLibPath.getPath(), "tmp")).mkdirs();

        var cmd = new ArrayList<String>();
        cmd.add(jarPath);
        cmd.add("xf");
        cmd.add(jarFileName);

        var result = ShellUtil.execRuntimeCommand(cmd, workJar.getPath());
        if (result.hasError()) {
            throw new RuntimeException(result.getMsg());
        }

    }

    private String getJarGroupId(File jar) {
        var tmpPath = new File(FileUtil.joinPath(outputDirectory.getPath(), "split-lib", "tmp", jar.getName()));
        if (tmpPath.mkdirs() == false) {
            throw new RuntimeException("创建文件夹失败：" + tmpPath.getPath());
        }

        var cmd = new ArrayList<String>();
        cmd.add(jarPath);
        cmd.add("xf");
        cmd.add(jar.getPath());

        var result = ShellUtil.execRuntimeCommand(cmd, tmpPath.getPath());
        if (result.hasError()) {
            throw new RuntimeException(result.getMsg());
        }

        var startFile = new File(FileUtil.joinPath(tmpPath.getPath(), "META-INF", "maven"));
        if (startFile.exists() == false) {
            return "";
        }
        var pomPropertyFile = getPomPropertyFile(startFile);

        if (pomPropertyFile == null) {
            return "";
        }

        var content = FileUtil.readFileContent(pomPropertyFile.getPath());
        var lines = content.split("\n");
        for (var l : lines) {
            if (l.startsWith("groupId=")) {
                return l.split("=")[1].trim();
            }
        }
        return "";
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
