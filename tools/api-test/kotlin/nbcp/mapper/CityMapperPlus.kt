package nbcp.mapper

import com.baomidou.mybatisplus.core.mapper.BaseMapper
import nbcp.db.mongo.entity.SysCity
import nbcp.db.mybatis.CacheForMyBatisPlusBaseMapper
import org.apache.ibatis.annotations.*

/**
 * Created by udi on 2017.2.27.
 */
@Mapper
@CacheForMyBatisPlusBaseMapper(SysCity::class)
//@CacheNamespace(implementation=(nbcp.db.mybatis.RedisCacheMyBatis::class))
interface CityMapperPlus : BaseMapper<SysCity> {
    @Select("select name from s_city where code = #{code}")
//    @Results(value = arrayOf(Result(column = "password", property = "password")))
    fun findNameByCode(@Param("code") code: Int): String // SysCity?

    @Update("update s_city set pinyin= #{pinyin} where code = #{code}")
    fun updateByCode(@Param("code") code: String, @Param("pinyin") pinyin: String): Int

    @Delete("delete from s_city where code = #{code}")
    fun deleteByCode(@Param("code") code: String): Int

    /**
     * 应该忽略掉  null 的列. 不好写,就是思路不对.
     */
//    @Insert("insert into s_city (code,name) values ( #{entity.code} ,#{entity.name} )")
//    fun add(@Param("entity") entity: nbcp.db.mysql.entity.s_city): Int
}