package nbcp.handler

import ch.qos.logback.classic.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import nbcp.comm.*
import nbcp.base.extend.*
import nbcp.comm.JsonpMapping
import nbcp.db.mongo.MongoBaseEntity
import nbcp.db.mongo.MongoEntityEvent
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaType


/**
 * Created by udi on 17-3-19.
 */
@OpenAction
@RestController
@JsonpMapping("/dev/mongo")
class DevController {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    @GetMapping("/getGroups")
    fun getGroup(dbType: String): ListResult<String> {
        MongoEntityEvent.groups.apply {
            return ListResult.of(this.map {
                var name = it::class.java.simpleName
                return@map name[0].toLowerCase() + name.Slice(1, -5)
            })
        }
    }

    @GetMapping("/getEntities")
    fun getEntities(dbType: String, group: String): ListResult<String> {
        var group = group[0].toUpperCase() + group.Slice(1) + "Group"
        var groupObj = MongoEntityEvent.groups.firstOrNull { it::class.java.simpleName == group }
        if (groupObj == null) return ListResult("找不到group")

        groupObj.getEntities().map { it.tableName }.apply { return ListResult.of(this) }
    }


    data class FieldModel(
            var name: String,
            var remark: String,
            var type: String,
            var isSimpleType: Boolean
    )

    @GetMapping("/getFields")
    fun getEntity(dbType: String, group: String, entity: String): ListResult<FieldModel> {
        var group = group[0].toUpperCase() + group.Slice(1) + "Group"
        var groupObj = MongoEntityEvent.groups.firstOrNull { it::class.java.simpleName == group }
        if (groupObj == null) return ListResult("找不到group")

        var entityObj = groupObj.getEntities().firstOrNull { it.tableName == entity };
        if (entityObj == null) return ListResult("找不到entity")


        (entityObj as MongoBaseEntity<*>).entityClass.kotlin.memberProperties.map {
            var typeName = it.returnType.javaType.typeName
            var isSimpleType = false;
            if (it.returnType.javaType is Class<*>) {
                isSimpleType = (it.returnType.javaType as Class<*>).IsSimpleType()
            }
            FieldModel(it.name, it.name, typeName, isSimpleType)
        }.apply {
            return ListResult.of(this)
        }
    }

}