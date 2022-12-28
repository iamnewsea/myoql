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
import lombok.SneakyThrows;
import lombok.var;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Goal which touches a timestamp file.
 *
 * @deprecated Don't use!
 */
@Mojo(name = "check-layout", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class CheckMojo
        extends AbstractMojo {
    /**
     * Location of the file.
     */
    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    private File outputDirectory;


    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;


    @SneakyThrows
    public void execute()
            throws MojoExecutionException {
        var now = LocalDateTime.now();
        var nowString = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"));


        var pomContent = FileUtil.readFileContent(project.getFile().getPath());

        var checked = pomContent.contains("org.springframework.boot") &&
                pomContent.contains("spring-boot-maven-plugin") &&
                pomContent.contains("<layout>ZIP</layout>");


        if (checked) {
            return;
        }

        var message = "org.springframework.boot:spring-boot-maven-plugin.configuration.layout 必须为 ZIP!";
        var writer = new FileWriter(new File(outputDirectory, "split-lib-check.txt"));
        writer.write("[" + project.getName() + "]" + FileUtil.LINE_BREAK + "execute split-lib-maven-plugin at : " + nowString + FileUtil.LINE_BREAK + FileUtil.LINE_BREAK);
        writer.write(message);
        writer.flush();
        writer.close();

        throw new RuntimeException(message);
    }
}
