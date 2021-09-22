package nbcp.db.redis

import nbcp.scope.IScopeData
import org.springframework.data.redis.core.StringRedisTemplate

data class RedisTemplateScope(val value: StringRedisTemplate) : IScopeData