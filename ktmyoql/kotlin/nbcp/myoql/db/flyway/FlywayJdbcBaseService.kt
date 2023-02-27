package nbcp.myoql.db.flyway

import com.mongodb.client.model.IndexOptions
import nbcp.base.comm.*
import nbcp.base.db.*
import nbcp.base.db.annotation.DbEntityIndex
import nbcp.base.enums.*
import nbcp.base.extend.*
import nbcp.base.utils.*
import nbcp.myoql.db.*
import nbcp.myoql.db.cache.*
import nbcp.myoql.db.comm.*
import nbcp.myoql.db.enums.*
import nbcp.myoql.db.mongo.batchInsert
import nbcp.myoql.db.mongo.component.MongoBaseMetaCollection
import nbcp.myoql.db.mongo.updateWithEntity
import org.bson.Document
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource

abstract class FlywayJdbcBaseService(val version: Int) {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    abstract fun exec();

}