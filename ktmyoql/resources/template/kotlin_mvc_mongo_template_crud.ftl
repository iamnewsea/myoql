package nbcp.web.${w(group)}

import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.web.bind.annotation.RestController
import nbcp.base.extend.*
import nbcp.comm.*
import nbcp.db.*
import nbcp.db.mongo.*
import nbcp.db.mongo.entity.*
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
        id: String, //当列表列新一条后，刷新时使用
<#if has("name")>
        name: String,
</#if>
        @Require skip: Int,
        @Require take: Int,
        request: MyHttpRequestWrapper
    ): ListResult<${entity}> {

        mor.${w(group)}.${entityField}.query()
            .apply{
                if(id.HasValue){
                    this.where{it.id match id}
                }
<#if has("name")>
                if(name.HasValue){
                    this.where{ it.name match_like name }
                }
</#if>
            }
            .limit(skip,take)
            .toListResult()
            .apply{
                return this;
            }
    }

    @ApiOperation("详情")
    @JsonpMapping("/detail/{id}")
    fun detail(
        @Require id: String,
        request: MyHttpRequestWrapper
    ): ApiResult<${entity}> {
        mor.${w(group)}.${entityField}.queryById(id)
            .toEntity()
            .apply{
                if( this == null){
                    return ApiResult<${entity}>("找不到数据")
                }

                return ApiResult.of(this)
            }
    }

    @ApiOperation("更新")
    @JsonpMapping("/save")
    fun save(
      @JsonModel entity:${entity},
      request: MyHttpRequestWrapper
    ): ApiResult<String> {

    //鉴权
    var userId = request.UserId;


    mor.${w(group)}.${entityField}.updateWithEntity(entity)
        .withRequestParams(request.json.keys)
        .run {
            if (entity.id.HasValue){
                return@run  this.execUpdate()
            }
            else{
                return@run  this.execInsert();
            }
        }
        .apply{
            if(this == 0){
              return ApiResult("更新失败")
            }

            return ApiResult.of(entity.id)
        }
    }
<#if has("status")>
    @ApiOperation("更新状态，更新一个字段")
    @JsonpMapping("/set-status")
    fun set(
        @Require id: String,
        @Require status: ${status_enum_class},
        request: MyHttpRequestWrapper
    ): JsonResult {
        //鉴权
        var userId = request.UserId;


        mor.${w(group)}.${entityField}.updateById(id)
            .set{it.status to status}
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
      @Require id:String,
      request: MyHttpRequestWrapper
    ): JsonResult {
    //鉴权
    var userId = request.UserId;

    var entity = mor.${w(group)}.${entityField}.queryById(id).toEntity();
    if(entity == null){
        return JsonResult("找不到数据")
    }

    mor.${w(group)}.${entityField}.deleteById(id)
        .exec()
        .apply{
            if(this == 0){
                return JsonResult("删除失败")
            }
            //实体上配置垃圾箱功能，可物理删除，会自动移到垃圾箱。
            return JsonResult()
        }
    }
}