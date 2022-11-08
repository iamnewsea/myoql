package nbcp.myoql.db.mongo.enums

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

    /**
     * {
     *     $project: {
     *      tags: {
     *         $filter: {
     *            input: "$tags",
     *            as: "item",
     *            cond:  {
     *                $eq: ["$$item.score" ,  1  ]
     *            }
     *         }
     *      }
     *   }
     * }
     */
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