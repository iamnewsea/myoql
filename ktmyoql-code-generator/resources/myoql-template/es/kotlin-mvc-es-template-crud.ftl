package @pkg@.mvc.${sc(group)}

import io.swagger.annotations.*
import org.springframework.data.mongodb.core.query.*
import org.springframework.web.bind.annotation.*
import @pkg@.db.es.*
import @pkg@.db.es.entity.*
import nbcp.base.comm.*
import nbcp.myoql.db.*
import nbcp.db.es.*
import nbcp.db.es.entity.*
import nbcp.base.mvc.*
import javax.servlet.http.*
import java.time.*

/**
 * Created by CodeGenerator at ${now}
 */
@Api(description = "${title}", tags = arrayOf("${entity}"))
@RestController
@RequestMapping("${url}")
class ${entity}AutoController {

    @ApiOperation("列表")
    @PostMapping("/list")
    fun list(
        id: String, //当列表列新一条后，刷新时使用
<#if hasField(entity, "name")>
        name: String,
</#if>
        @Require skip: Int,
        @Require take: Int,
        request: HttpServletRequest
    ): ListResult<${entity}> {

        mor.${sc(group)}.${sc(entity)}.query()
            .apply {
                if (id.HasValue) {
                    this.where { it.id esEquals id }
                }
<#if hasField(entity,"name")>
                if (name.HasValue) {
                    this.where { it.name matchLike name }
                }
</#if>
            }
            .limit(skip, take)
            .toListResult()
            .apply {
                return this
            }
    }

    @ApiOperation("详情")
    @PostMapping("/detail/{id}")
    fun detail(
        @Require id: String,
        request: HttpServletRequest
    ): ApiResult<${entity}> {
        mor.${sc(group)}.${sc(entity)}.queryById(id)
            .toEntity()
            .apply {
                if (this == null) {
                    return ApiResult<${entity}>("找不到数据")
                }

                return ApiResult.of(this)
            }
    }

    @ApiOperation("更新")
    @PostMapping("/save")
    fun save(
        @JsonModel entity: ${entity},
        request: HttpServletRequest
    ): ApiResult<String> {
        //鉴权
        var userId = request.UserId

        mor.${sc(group)}.${sc(entity)}.updateWithEntity(entity)
            .withRequestParams()
            .run {
                if (entity.id.HasValue) {
                    return@run this.execUpdate()
                } else {
                    return@run this.execInsert()
                }
            }
            .apply {
                if (this == 0) {
                    return ApiResult.error("更新失败")
                }

                return ApiResult.of(entity.id)
            }
    }
<#if hasField(entity,"status")>

    @ApiOperation("更新状态，更新一个字段")
    @PostMapping("/set-status")
    fun set(
        @Require id: String,
        @Require status: ${status_enum_class},
        request: HttpServletRequest
    ): JsonResult {
        //鉴权
        var userId = request.UserId

        mor.${sc(group)}.${sc(entity)}.updateById(id)
            .set { it.status to status }
            .exec()
            .apply {
                if (this == 0) {
                    return JsonResult.error("更新失败")
                }

                return JsonResult()
            }
    }
</#if>

    @ApiOperation("删除")
    @PostMapping("/delete/{id}")
    fun delete(
        @Require id: String,
        request: HttpServletRequest
    ): JsonResult {
        //鉴权
        var userId = request.UserId

        var entity = mor.${sc(group)}.${sc(entity)}.queryById(id).toEntity()
        if (entity == null) {
            return JsonResult.error("找不到数据")
        }

        mor.${sc(group)}.${sc(entity)}.deleteById(id)
            .exec()
            .apply {
                if (this == 0) {
                    return JsonResult.error("删除失败")
                }
                //实体上配置垃圾箱功能，可物理删除，会自动移到垃圾箱。
                return JsonResult()
            }

    }
}