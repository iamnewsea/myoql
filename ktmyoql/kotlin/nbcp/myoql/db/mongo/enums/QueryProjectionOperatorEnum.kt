package nbcp.myoql.db.mongo.enums

/**
 * https://docs.mongodb.com/manual/reference/operator/query/
 */
enum class QueryProjectionEnum (val key:String) {
    EQ("eq"),
    GT("gt"),
    GTE("gte"),
    `IN`("in"),
    LT("lt"),
    LTE("lte"),
    NE("ne"),
    NIN("nin"),
    //--------------------
    AND("and"),
    NOT("not"),
    NOR("nor"),
    OR("or"),
    //---------------------
    EXISTS("exists"),
    TYPE("type"),
    //---------------------
    EXPR("expr"),
    JSON_SCHEMA("jsonSchema"),
    MOD("mod"),
    REGEX("regex"),
    TEXT("text"),
    WHERE("where"),
    //---------------------
    GEO_INTERSECTS("geoIntersects"),
    GEO_WITHIN("geoWithin"),
    NEAR("near"),
    NEAR_SPHERE("nearSphere"),
    BOX("box"),
    CENTER("center"),
    CENTER_SPHERE("centerSphere"),
    GEOMETRY("geometry"),
    MAX_DISTANCE("maxDistance"),
    MIN_DISTANCE("minDistance"),
    POLYGON("polygon"),
    UNIQUE_DOCS("uniqueDocs"),
    //---------------------
    ALL("all"),
    ELEM_MATCH("elemMatch"),
    SIZE("size"),
    //---------------------
    BITS_ALL_CLEAR("bitsAllClear"),
    BITS_ALL_SET("bitsAllSet"),
    BITS_ANY_CLEAR("bitsAnyClear"),
    BITS_ANY_SET("bitsAnySet"),
    //---------------------
    COMMENT("comment"),
    //---------------------
    `ARRAY_FIRST_ELEMENT`("$"),
    META("meta"),
    SLICE("slice");
    //---------------------
    //---------------------
    override fun toString(): String {
        return this.key;
    }
}