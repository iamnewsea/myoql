package nbcp.mysql.test

import nbcp.comm.JsonMap
import nbcp.comm.OpenAction
import nbcp.comm.ToJson
import nbcp.db.mongo.entity.SysCity
import nbcp.db.mybatis.CacheForMyBatisPlusBaseMapper
import nbcp.db.mybatis.mapper.CityMapper
import nbcp.mapper.CityMapperPlus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/test/kt")
class MybatisTest {
    @Autowired
    lateinit var cityMapper: CityMapperPlus;

    @GetMapping("/batis")
    fun batis() {

        var where = JsonMap();
        where["code"]  = 110;
        var d = cityMapper.selectByMap(where);
        println(d.ToJson())
    }
}