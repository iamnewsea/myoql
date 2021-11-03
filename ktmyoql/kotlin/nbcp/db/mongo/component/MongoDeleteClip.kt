package nbcp.db.mongo


import com.mongodb.client.result.DeleteResult
import nbcp.comm.*
import nbcp.db.MyOqlOrmScope
import nbcp.db.db
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.time.LocalDateTime
import nbcp.scope.*
import java.io.Serializable
/**
 * Created by udi on 17-4-17.
 */

/**
 * MongoDelete
 */
class MongoDeleteClip<M : MongoBaseMetaCollection<out Serializable>>(var moerEntity: M) :
    MongoClipBase(moerEntity.tableName), IMongoWhereable {
    val whereData = mutableListOf<Criteria>()

    companion object {
        private var logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    fun where(whereData: Criteria): MongoDeleteClip<M> {
        this.whereData.add(whereData);
        return this;
    }

    fun where(where: (M) -> Criteria): MongoDeleteClip<M> {
        this.whereData.add(where(moerEntity));
        return this;
    }

    fun whereOr(vararg wheres: (M) -> Criteria): MongoDeleteClip<M> {
        return whereOr(*wheres.map { it(moerEntity) }.toTypedArray())
    }

    fun whereOr(vararg wheres: Criteria): MongoDeleteClip<M> {
        if (wheres.any() == false) return this;
        var where = Criteria();
        where.orOperator(*wheres)
        this.whereData.add(where);
        return this;
    }

    fun exec(): Int {
        db.affectRowCount = -1;
        var criteria = this.moerEntity.getMongoCriteria(*whereData.toTypedArray());

        var settingResult = db.mongo.mongoEvents.onDeleting(this)
        if (settingResult.any { it.second.result == false }) {
            return 0;
        }

        var ret = -1;
        var startAt = LocalDateTime.now();
        var error: Exception? = null;
        var result: DeleteResult? = null;
        var query = Query.query(criteria)
        try {
            result = mongoTemplate.remove(query, collectionName);

            db.executeTime = LocalDateTime.now() - startAt

            ret = result.deletedCount.toInt()
            db.affectRowCount = ret;

            if (ret > 0) {
                usingScope(arrayOf(MyOqlOrmScope.IgnoreAffectRow, MyOqlOrmScope.IgnoreExecuteTime)) {
                    settingResult.forEach {
                        it.first.delete(this, it.second)
                    }
                }
            }
        } catch (e: Exception) {
            error = e
            throw e;
        } finally {
            MongoLogger.logDelete(error,collectionName,query,result);
        }


        return ret;
    }
}