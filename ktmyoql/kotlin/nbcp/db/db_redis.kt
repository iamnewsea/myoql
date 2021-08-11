package nbcp.db


import nbcp.comm.*
import nbcp.db.redis.AnyTypeRedisTemplate
import nbcp.utils.*
import org.springframework.core.convert.support.GenericConversionService
import org.springframework.data.redis.core.StringRedisTemplate

/**
 * 请使用 db.mongo
 */
object db_redis {


//    private var dynamicStringRedisMap = StringKeyMap<StringRedisTemplate>();
//    private var dynamicAnyRedisTemplate = StringKeyMap<AnyTypeRedisTemplate>();
//
//    /**
//     * 指派集合到数据库
//     */
//    fun bindGroup2Database(group: String, stringRedisTemplate: StringRedisTemplate) {
//        this.dynamicStringRedisMap.set(group, stringRedisTemplate)
//    }
//
//    fun bindGroup2Database(group: String, stringRedisTemplate: AnyTypeRedisTemplate) {
//        this.dynamicAnyRedisTemplate.set(group, stringRedisTemplate)
//    }
//
//    fun unbindGroup(group: String) {
//        this.dynamicStringRedisMap.remove(group)
//        this.dynamicAnyRedisTemplate.remove(group )
//    }
//
//    fun getStringRedisTemplate(group: String): StringRedisTemplate? {
//        return this.dynamicStringRedisMap.get(group)
//    }
//
//    fun getAnyRedisTemplate(group: String): AnyTypeRedisTemplate? {
//        return this.dynamicAnyRedisTemplate.get(group)
//    }
}