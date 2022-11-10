package nbcp.web.mvc.handler

import nbcp.base.comm.ListResult
import nbcp.base.extend.IsSimpleType
import nbcp.base.extend.Slice
import nbcp.mvc.comm.AdminSysOpsAction
import nbcp.myoql.db.db
import nbcp.myoql.db.mongo.component.MongoBaseMetaCollection
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaType


/**
 * Created by udi on 17-3-19.
 */
@RestController
@AdminSysOpsAction
@RequestMapping("/dev/mongo")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(value = arrayOf(MongoTemplate::class,  db::class))
class DevMongoServlet {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    @GetMapping("/groups")
    fun getGroup(): ListResult<String> {
        db.mongo.groups.apply {
            return ListResult.of(this.map {
                var name = it::class.java.simpleName
                return@map name[0].lowercase() + name.Slice(1, -5)
            })
        }
    }

    @GetMapping("/entities")
    fun getEntities(group: String): ListResult<String> {
        var groupValue = group[0].uppercaseChar() + group.substring(1) + "Group"
        var groupObj = db.mongo.groups.firstOrNull { it::class.java.simpleName == groupValue }
        if (groupObj == null) return ListResult.error("找不到group")

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
        var groupValue = group[0].uppercaseChar() + group.substring(1) + "Group"
        var groupObj = db.mongo.groups.firstOrNull { it::class.java.simpleName == groupValue }
        if (groupObj == null) return ListResult.error("找不到group")

        var entityObj = groupObj.getEntities().firstOrNull { it.tableName == entity };
        if (entityObj == null) return ListResult.error("找不到entity")


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