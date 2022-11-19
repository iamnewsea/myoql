package nbcp.myoql.db.mongo.enums

/**
 * https://docs.mongodb.com/manual/reference/operator/aggregation/group/#accumulators-group
 */
enum class PipeLineAccumulatorOperatorEnum(val key:String) {
    ADD_TO_SET("addToSet"),
    AVG("avg"),
    FIRST("first"),
    LAST("last"),
    MAX("max"),
    MERGE_OBJECTS("mergeObjects"),
    MIN("min"),
    PUSH("push"),
    STD_DEV_POP("stdDevPop"),
    STD_DEV_SAMP("stdDevSamp"),
    SUM("sum");


    override fun toString(): String {
        return this.key;
    }
}