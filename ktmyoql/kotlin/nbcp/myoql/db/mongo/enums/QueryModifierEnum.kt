package nbcp.myoql.db.mongo.enums

/**
 * https://docs.mongodb.com/manual/reference/operator/query-modifier/
 */
enum class QueryModifierEnum(val key: String) {
    COMMENT("comment"),
    EXPLAIN("explain"),
    HINT("hint"),
    MAX("max"),
    MAX_TIME_MS("maxTimeMS"),
    MIN("min"),
    ORDER_BY("orderby"),
    QUERY("query"),
    RETURN_KEY("returnKey"),
    SHOW_DISK_LOC("showDiskLoc"),
    NATURAL("natural");

    override fun toString(): String {
        return this.key;
    }
}