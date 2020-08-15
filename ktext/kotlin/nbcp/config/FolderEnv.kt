package nbcp

import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.boot.env.YamlPropertySourceLoader
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import java.io.File

/**
 * 按spring.profiles.active加载对应文件夹的配置文件。需要配合：
 * 在 resources/META-INF/spring.factories 配置：
 * org.springframework.boot.env.EnvironmentPostProcessor=nbcp.FolderEnv
 */
class FolderEnv : EnvironmentPostProcessor {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    override fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication) {
        var resourceResolver = PathMatchingResourcePatternResolver()
        var loader = YamlPropertySourceLoader();
        environment.activeProfiles.forEach { active ->
            try {
                //先加载调试模式，再加载运行Jar模式。
                var list = resourceResolver.getResources(active + File.separator + "*.yml").toMutableList()
                if (File.separator != "/") {
                    list.addAll(resourceResolver.getResources(active + "/*.yml"))
                }

                list.forEach { resource ->
                    loader.load(active, resource).forEach {
                        environment.propertySources.addLast(it)
                    }
                }
            } catch (e: Exception) {
                logger.error("跳过 /${active}/*.yml 配置文件,原因:${e.message}");
            }
        }
    }
}