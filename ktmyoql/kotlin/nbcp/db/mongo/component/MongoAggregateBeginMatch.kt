package nbcp.db.mongo

import org.springframework.data.mongodb.core.query.Criteria


class MongoAggregateBeginMatch<M : MongoBaseMetaCollection<E>, E : Any>(var aggregate: MongoAggregateClip<M, E>) {
    private var wheres = mutableListOf<Criteria>()
    fun where(where: (M) -> Criteria): MongoAggregateBeginMatch<M, E> {
        wheres.add(where(this.aggregate.moerEntity))
        return this;
    }

    fun endMatch(): MongoAggregateClip<M, E> {
        return this.aggregate.wheres(*this.wheres.toTypedArray())
    }
}