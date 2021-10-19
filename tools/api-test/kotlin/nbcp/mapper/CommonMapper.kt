package nbcp.db.mybatis.mapper

import org.apache.ibatis.annotations.*

/**
 * Created by yuxh on 2018/6/26
 */
@Mapper
interface CommonMapper{
    @Select("#{sql}")
    fun<T> select(@Param("sql") sql: String): MutableList<T>

    @Update("#{sql}")
    fun update(@Param("sql") sql: String):Int

    @Delete("#{sql}")
    fun delete(@Param("sql") sql: String):Int

    @Insert("#{sql}")
    fun insert(@Param("sql") sql: String):Int
}