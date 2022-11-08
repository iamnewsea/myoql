package nbcp.myoql.db.redis

import nbcp.base.scope.IScopeData
import org.springframework.data.redis.core.StringRedisTemplate

data class RedisTemplateScope(val value: StringRedisTemplate) : IScopeData