package nbcp.db.es.component

/**
 * 查询分为：
 * 分页:from/size、字段:fields、排序sort、查询:query、过滤:filter、高亮:highlight、统计:facet
 */
class EsQuery {
    protected var skip: Int = 0;
    protected var take: Int = -1;

    fun limit(skip: Int, take: Int): EsQuery {
        this.skip = skip;
        this.take = take;
        return this;
    }

    fun selectField():EsQuery{
        return this;
    }
    fun select ():EsQuery{
        return this;
    }

    /**
     * 升序
     */
    fun orderByAsc( ): EsQuery {
        return this
    }

    /**
     * 降序
     */
    fun orderByDesc( ): EsQuery {
        return this
    }

    fun query():EsQuery{
        return this;
    }

    /**
     * 如果可能，请使用filter过滤器上下文而不是query查询上下文。
     */
    fun filter():EsQuery{
        return this;
    }
    fun highlight():EsQuery{
        return this;
    }

    fun facet():EsQuery{
        return this;
    }
}