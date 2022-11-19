package nbcp.myoql.db.mongo.enums

/**
 * https://docs.mongodb.com/manual/reference/operator/aggregation-pipeline/
 */
enum class PipeLineEnum(val key: String) {
    ADD_FIELDS("addFields"),
    BUCKET("bucket"),
    BUCKET_AUTO("bucketAuto"),
    COLL_STATS("collStats"),
    COUNT("count"),
    FACET("facet"),
    GEO_NEAR("geoNear"),
    GRAPH_LOOKUP("graphLookup"),
    GROUP("group"),
    INDEX_STATS("indexStats"),
    LIMIT("limit"),
    LIST_SESSIONS("listSessions"),
    LOOKUP("lookup"),
    MATCH("match"),
    MERGE("merge"),
    `OUT`("out"),
    PLAN_CACHE_STATS("planCacheStats"),

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
    PROJECT("project"),
    REDACT("redact"),
    REPLACE_ROOT("replaceRoot"),
    REPLACE_WITH("replaceWith"),
    SAMPLE("sample"),
    SET("set"),
    SKIP("skip"),
    SORT("sort"),
    SORT_BY_COUNT("sortByCount"),
    UNSET("unset"),
    UNWIND("unwind");


    override fun toString(): String {
        return this.key;
    }
}