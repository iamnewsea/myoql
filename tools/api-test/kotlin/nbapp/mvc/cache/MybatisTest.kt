package nbapp.mvc.cache

import nbapp.db.mapper.CityMapperPlus
import nbcp.comm.JsonMap
import nbapp.db.mybatis.mapper.CityMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("/test/kt")
class MybatisTest {
    @Autowired
    lateinit var cityMapper: CityMapper

    @Autowired
    lateinit var cityMapperPlus: CityMapperPlus

    @GetMapping("/batis")
    fun batis(request: HttpServletRequest) {
        var where = JsonMap();
        where["code"] = 110;

        println("查110，220")
        cityMapper.findNameByCode(110);
        cityMapper.findNameByCode(220);

        println("破 110")
        cityMapper.updateByCode(110,"bj")

        println("查110，220")
        cityMapper.findNameByCode(110);
        cityMapper.findNameByCode(220);
    }

    @GetMapping("/batis_plus")
    fun batis_plus() {
        var where = JsonMap();
        where["code"] = 110;

        println("查110，220")
        cityMapperPlus.findNameByCode(110);
        cityMapperPlus.findNameByCode(220);

        println("破 110")
        cityMapperPlus.updateByCode(110,"bj")

        println("查110，220")
        cityMapperPlus.findNameByCode(110);
        cityMapperPlus.findNameByCode(220);
    }
}