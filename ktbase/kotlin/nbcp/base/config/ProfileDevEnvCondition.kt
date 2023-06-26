package nbcp.base.config

import nbcp.base.comm.config
import nbcp.base.utils.ClassUtil
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

/**
 * 是否是开发环境
 *
 */
class ProfileDevEnvCondition : Condition {
    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        return context.environment.activeProfiles.any { it.contains("dev", true) }
    }
}