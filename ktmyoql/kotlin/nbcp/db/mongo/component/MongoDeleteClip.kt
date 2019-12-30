package nbcp.db.mongo


import nbcp.base.line_break
import nbcp.db.db
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import nbcp.db.mongo.*
import org.slf4j.LoggerFactory
import java.lang.Exception

/**
 * Created by udi on 17-4-17.
 */

/**
 * MongoDelete
 */
class MongoDeleteClip<M : MongoBaseEntity<out IMongoDocument>>(var moerEntity: M) : MongoClipBase(moerEntity.tableName), IMongoWhereable {
    private var whereData = mutableListOf<Criteria>()

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
        if( wheres.any() == false) return this;
        var where = Criteria();
        where.orOperator(*wheres)
        this.whereData.add(where);
        return this;
    }

    fun exec(): Int {
        var criteria = this.moerEntity.getMongoCriteria(*whereData.toTypedArray());
        var ret = 0;
        try {
            var result = mongoTemplate.remove(
                    Query.query(criteria),
                    collectionName);

            ret = result.deletedCount.toInt()
            db.affectRowCount = ret;
        } catch (e: Exception) {
            ret = -1;
            throw e;
        } finally {
            var msg = "delete:[" + this.collectionName + "] " + criteria.criteriaObject.toJson() + ",result:${ret}";

            if (ret < 0) {
                logger.error(msg)
            } else {
                logger.info(msg);
            }
        }

        return ret;
    }
}