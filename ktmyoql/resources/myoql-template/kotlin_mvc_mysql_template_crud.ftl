package nbcp.mvc.${w(group)}

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.web.bind.annotation.RestController
import nbcp.base.extend.*
import nbcp.comm.*
import nbcp.db.*
import nbcp.db.sql.*
import nbcp.db.sql.entity.*
import nbcp.db.mysql.*
import nbcp.db.mysql.entity.*
import nbcp.web.*
import javax.servlet.http.HttpServletRequest
import java.time.*

/**
 * Created by CodeGenerator at ${now}
 */
@Api(description = "${title}", tags = arrayOf("${entity}"))
@RestController
@JsonpMapping("${url}")
class ${entity}AutoController {

    @ApiOperation("列表")
    @JsonpMapping("/list")
    fun list(
        ${idKey}: ${kotlin_type(idKey)}, //当列表列新一条后，刷新时使用
<#if has("name")>
        name: String,
</#if>
        @Require skip: Int,
        @Require take: Int,
        request: MyHttpRequestWrapper
    ): ListResult<${entity}> {

        dbr.${w(group)}.${entity}.query()
            .apply {
                if (${idKey}.HasValue) {
                    this.where{it.${idKey} match ${idKey}}
                }
<#if has("name")>
                if (name.HasValue) {
                    this.where { it.name like name }
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
    @JsonpMapping("/detail/{id}")
    fun detail(
        @Require ${idKey}: ${kotlin_type(idKey)},
        request: MyHttpRequestWrapper
    ): ApiResult<${entity}> {
        dbr.${w(group)}.${entity}.queryBy${W(idKey)}(${idKey})
            .toEntity()
            .apply {
                if (this == null) {
                    return ApiResult<${entity}>("找不到数据")
                }

                return ApiResult.of(this)
            }
    }

    @ApiOperation("更新")
    @JsonpMapping("/save")
    fun save(
        @JsonModel entity: ${entity},
        request: MyHttpRequestWrapper
    ): ApiResult<${kotlin_type(idKey)}> {
        //鉴权
        var userId = request.UserId

        dbr.${w(group)}.${entity}.updateWithEntity(entity)
            .withRequestParams(request.json.keys)
            .run {
                if (entity.${idKey}.HasValue) {
                    return@run this.execUpdate()
                } else {
                    return@run this.execInsert()
                }
            }
            .apply {
                if (this == 0) {
                    return ApiResult("更新失败")
                }

                return ApiResult.of(entity.${idKey})
            }
    }
<#if has("status")>

    @ApiOperation("更新状态，更新一个字段")
    @JsonpMapping("/set-status")
    fun set(
        @Require ${idKey}: ${kotlin_type(idKey)},
        @Require status: ${status_enum_class},
        request: MyHttpRequestWrapper
    ): JsonResult {
        //鉴权
        var userId = request.UserId

        dbr.${w(group)}.${entity}.updateBy${W(idKey)}(${idKey})
            .set { it.status to status }
            .set { it.updateAt to LocalDateTime.now() }
            .exec()
            .apply {
                if (this == 0) {
                    return JsonResult("更新失败")
                }

                return JsonResult()
            }
    }
</#if>

    @ApiOperation("删除")
    @JsonpMapping("/delete/{id}")
    fun delete(
        @Require ${idKey}: ${kotlin_type(idKey)},
        request: MyHttpRequestWrapper
    ): JsonResult {
        //鉴权
        var userId = request.UserId

        var entity = dbr.${w(group)}.${entity}.queryBy${W(idKey)}(${idKey}).toEntity()
        if (entity == null) {
            return JsonResult("找不到数据")
        }
<#if has_dustbin()>
        //实体上配置了垃圾箱功能，物理删除后会自动移到垃圾箱。
<#else>
        //实体上没有配置垃圾箱功能，物理删除后会丢失数据！
</#if>
        dbr.${w(group)}.${entity}.deleteBy${W(idKey)}(${idKey})
            .exec()
            .apply {
                if (this == 0) {
                    return JsonResult("删除失败")
                }

                return JsonResult()
            }
    }
}