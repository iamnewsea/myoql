package nbcp.db.cache

import nbcp.db.redis.scanKeys
import org.springframework.data.redis.core.StringRedisTemplate


object CacheForBrokeDataWorkService {
    fun brokeCacheItem(redisTemplate: StringRedisTemplate, cacheBroke: BrokeRedisCacheData) {
        var pattern = cacheBroke.getTablePattern()

        for (i in 0..999) {
            var all_keys = redisTemplate.scanKeys(pattern);
            if (all_keys.any() == false) {
                break;
            }


            //如果是删除全表。
            if (cacheBroke.key.isEmpty() || cacheBroke.value.isEmpty()) {
                redisTemplate.delete(all_keys);
                continue;
            }

            //先移除不含 ~ 的key
            var like_sql_keys = all_keys.filter { it.contains(FromRedisCacheData.KEY_VALUE_JOIN_CHAR) == false };
            if (like_sql_keys.any()) {
                redisTemplate.delete(like_sql_keys)
            }

            var other_group_keys_pattern =
                "${FromRedisCacheData.GROUP_JOIN_CHAR}${cacheBroke.key}${FromRedisCacheData.KEY_VALUE_JOIN_CHAR}"
            //破坏其它维度的分组
            var other_group_keys =
                all_keys.filter { it.contains(other_group_keys_pattern) == false };
            if (other_group_keys.any()) {
                redisTemplate.delete(other_group_keys);
            }

            var this_group_keys_pattern =
                "${FromRedisCacheData.GROUP_JOIN_CHAR}${cacheBroke.key}${FromRedisCacheData.KEY_VALUE_JOIN_CHAR}${cacheBroke.value}${FromRedisCacheData.GROUP_JOIN_CHAR}"
            //再精准破坏 key,value分组的。
            var this_group_keys =
                all_keys.filter { it.contains(this_group_keys_pattern) };

            if (this_group_keys.any()) {
                redisTemplate.delete(this_group_keys);
            }
        }


        pattern = cacheBroke.getJoinTablePattern();

        for (i in 0..999) {
            var list = redisTemplate.scanKeys(pattern);
            if (list.any() == false) {
                break;
            }

            redisTemplate.delete(list);
        }
    }
}
