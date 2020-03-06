package nbcp.db.redis

import nbcp.base.extend.*
import nbcp.db.redis.proxy.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.math.exp

class RedisBaseGroup {

    class Oauth2Group {
        val authorizeCode = RedisStringProxy("oauth2-auth", 0, 900)
        /**
         * 5分钟内不能,登录不能超过10次。
         */
        val loginTimes = RedisSortedSetProxy("oauth2-loginTimes", 0, 300,RedisRenewalTypeEnum.Read)
        val token = RedisHashProxy("oauth2-token")

        /**
         * 添加登录记录，如果5分钟内记录数大于10条，返回 false.
         */
        fun addLoginName(loginName:String):Boolean{
            var now = LocalDateTime.now();
            var now_long = now.ToLong();
            loginTimes.add(loginName,now_long.toString(),now_long.toDouble())

            var map = loginTimes.getListByIndex(loginName,0,99).toMutableList()

            //检查。删过期
            var m5 = now.minusMinutes(5).ToLong()
            var expiredKeys =  map.filter { it.AsLong() < m5 }.toMutableList()

            map.removeAll(expiredKeys);
            expiredKeys.addAll(map.Slice(0,-11))

            loginTimes.remove(loginName,*expiredKeys.toTypedArray());


            if( map.size > 10){
                return false;
            }

            return true;
        }
    }

    class CacheGroup {
        val cacheSqlData = RedisStringProxy("")
        fun brokeKeys(table: String) = RedisSetProxy("broke-keys:${table}")
        val borkeKeysChangedVersion = RedisNumberProxy("borke-keys-changed-version")
        val brokingTable = RedisStringProxy("broking-table")
    }


    val oauth2 = Oauth2Group()
    val cache = CacheGroup()
}