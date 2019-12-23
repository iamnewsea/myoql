package nbcp.db.mongo

/**
 * https://docs.mongodb.com/manual/reference/operator/aggregation-pipeline/
 */
enum class PipeLineEnum {
    addFields,
    bucket,
    bucketAuto,
    collStats,
    count,
    facet,
    geoNear,
    graphLookup,
    group,
    indexStats,
    limit,
    listSessions,
    lookup,
    match,
    merge,
    out,
    planCacheStats,
    project,
    redact,
    replaceRoot,
    replaceWith,
    sample,
    set,
    skip,
    sort,
    sortByCount,
    unset,
    unwind
}