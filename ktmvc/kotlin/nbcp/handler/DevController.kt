package nbcp.handler

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import nbcp.comm.*
import nbcp.db.db
import nbcp.db.mongo.MongoBaseMetaCollection
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaType


/**
 * Created by udi on 17-3-19.
 */
@OpenAction
@RestController
@ConditionalOnProperty("server.dev")
@JsonpMapping("/dev/mongo")
class DevController {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    @GetMapping("/groups")
    fun getGroup(): ListResult<String> {
        db.mongo.groups.apply {
            return ListResult.of(this.map {
                var name = it::class.java.simpleName
                return@map name[0].toLowerCase() + name.Slice(1, -5)
            })
        }
    }

    @GetMapping("/entities")
    fun getEntities(group: String): ListResult<String> {
        var group = group[0].toUpperCase() + group.Slice(1) + "Group"
        var groupObj = db.mongo.groups.firstOrNull { it::class.java.simpleName == group }
        if (groupObj == null) return ListResult("找不到group")

        groupObj.getEntities().map { it.tableName }.apply { return ListResult.of(this) }
    }


    data class FieldModel(
            var name: String,
            var remark: String,
            var type: String,
            var isSimpleType: Boolean
    )

    @GetMapping("/fields")
    fun getEntity(group: String, entity: String): ListResult<FieldModel> {
        var group = group[0].toUpperCase() + group.Slice(1) + "Group"
        var groupObj = db.mongo.groups.firstOrNull { it::class.java.simpleName == group }
        if (groupObj == null) return ListResult("找不到group")

        var entityObj = groupObj.getEntities().firstOrNull { it.tableName == entity };
        if (entityObj == null) return ListResult("找不到entity")


        (entityObj as MongoBaseMetaCollection<*>).entityClass.kotlin.memberProperties.map {
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