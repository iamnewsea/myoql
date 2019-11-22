package nbcp.db.mongo

import nbcp.db.db
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import nbcp.db.mongo.*

/**
 * Created by udi on 17-4-17.
 */

/**
 * MongoDelete
 */
class MongoDeleteClip<M : MongoBaseEntity<out IMongoDocument>>(var moerEntity: M) : MongoClipBase(moerEntity.tableName), IMongoWhereable {
    private var whereData = mutableListOf<Criteria>()

    fun where(whereData: Criteria): MongoDeleteClip<M> {
        this.whereData.add(whereData);
        return this;
    }

    fun where(where: (M) -> Criteria): MongoDeleteClip<M> {
        this.whereData.add(where(moerEntity));
        return this;
    }


    fun exec(): Int {
        var criteria = db.getMongoCriteria(*whereData.toTypedArray());

        var result = mongoTemplate.remove(
                Query.query(criteria),
                collectionName);

        db.affectRowCount = result.deletedCount.toInt()
        return result.deletedCount.toInt();
    }
}