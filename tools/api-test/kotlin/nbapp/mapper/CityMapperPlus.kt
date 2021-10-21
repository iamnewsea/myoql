package nbapp.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import nbcp.db.cache.BrokeRedisCache
import nbcp.db.cache.FromRedisCache
import nbcp.db.mongo.entity.SysCity
import nbcp.db.mybatis.CacheForMyBatisPlusBaseMapper
import org.apache.ibatis.annotations.*
import org.springframework.context.annotation.Primary


class s_city {
    var code: Int = 0
    var name: String = ""
    var fullName: String = ""
    var level: Int = 0
    var pinyin: String = ""
    var pcode = 0
}

/**
 * Created by udi on 2017.2.27.
 */
@Mapper
@CacheForMyBatisPlusBaseMapper(s_city::class)
//@CacheNamespace(implementation=(nbcp.db.mybatis.RedisCacheMyBatis::class))
interface CityMapperPlus : BaseMapper<s_city> {


    @FromRedisCache(tableClass = s_city::class,groupKey = "code",groupValue = "#code")
    @Select("select name from s_city where code = #{code}")
//    @Results(value = arrayOf(Result(column = "password", property = "password")))
    fun findNameByCode(@Param("code") code: Int): String // SysCity?

    @BrokeRedisCache(tableClass = s_city::class,groupKey = "code",groupValue = "#code")
    @Update("update s_city set pinyin= #{pinyin} where code = #{code}")
    fun updateByCode(@Param("code") code: Int, @Param("pinyin") pinyin: String): Int

    @BrokeRedisCache(tableClass = s_city::class)
    @Delete("delete from s_city where code = #{code}")
    fun deleteByCode(@Param("code") code: Int): Int

    /**
     * 应该忽略掉  null 的列. 不好写,就是思路不对.
     */
//    @Insert("insert into s_city (code,name) values ( #{entity.code} ,#{entity.name} )")
//    fun add(@Param("entity") entity: nbcp.db.mysql.entity.s_city): Int
}