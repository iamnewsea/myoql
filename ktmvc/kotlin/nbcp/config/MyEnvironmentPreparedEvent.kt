package nbcp

import nbcp.comm.JsonMap
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent
import org.springframework.boot.json.YamlJsonParser
import org.springframework.context.ApplicationListener
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.StandardEnvironment

class MyEnvironmentPreparedEvent : ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    override fun onApplicationEvent(event: ApplicationEnvironmentPreparedEvent) {
        val jsonMap = JsonMap();
        val map = MapPropertySource("ops", jsonMap);
        event.environment.propertySources.addBefore(StandardEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, map)
    }
}