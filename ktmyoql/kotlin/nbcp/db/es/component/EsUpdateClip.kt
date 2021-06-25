package nbcp.db.es

/**
 * Created by udi on 17-4-7.
 */

//根据Id，更新Es的一个键。

/**
 * EsUpdate
 * https://www.elastic.co/guide/en/elasticsearch/reference/7.6/docs-update-by-query.html
 */
class EsUpdateClip<M : EsBaseMetaEntity<E>,E : IEsDocument>(var moerEntity: M)
    : EsBaseUpdateClip(moerEntity.tableName) {

    fun add(entity: E): EsUpdateClip<M, E> {
        super.addEntity(entity)
        return this;
    }

    @JvmOverloads
    fun routing(routing:String = ""):  EsUpdateClip<M, E> {
        this.withRouting(routing)
        return this;
    }

    @JvmOverloads
    fun pipeline(pipeline:String = ""): EsUpdateClip<M, E> {
        this.withPipeLine(pipeline)
        return this;
    }

    fun refresh(refresh:EsPutRefreshEnum): EsUpdateClip<M, E> {
        this.withRefresh(refresh)
        return this;
    }

}

