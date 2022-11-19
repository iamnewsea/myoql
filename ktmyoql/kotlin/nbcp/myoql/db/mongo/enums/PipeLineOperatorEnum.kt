package nbcp.myoql.db.mongo.enums


/**
 * https://docs.mongodb.com/manual/reference/operator/aggregation/
 */
enum class PipeLineOperatorEnum(val key: String) {
    ABS("abs"),
    ACOS("acos"),
    ACOSH("acosh"),
    ADD("add"),
    ADD_TO_SET("addToSet"),
    ALL_ELEMENTS_TRUE("allElementsTrue"),
    AND("and"),
    ANY_ELEMENT_TRUE("anyElementTrue"),
    ARRAY_ELEM_AT("arrayElemAt"),
    ARRAY_TO_OBJECT("arrayToObject"),
    ASIN("asin"),
    ASINH("asinh"),
    ATAN("atan"),
    ATAN2("atan2"),
    ATANH("atanh"),
    AVG("avg"),
    CEIL("ceil"),
    CMP("cmp"),
    CONCAT("concat"),
    CONCAT_ARRAYS("concatArrays"),

    /**
     * @sample
     * $project: {
     *   item: 1,
     *   discount:
     *     {
     *       $cond: { if: { $gte: [ "$qty", 250 ] }, then: 30, else: 20 }
     *     }
     * }
     */
    COND("cond"),
    CONVERT("convert"),
    COS("cos"),
    DATE_FROM_PARTS("dateFromParts"),
    DATE_TO_PARTS("dateToParts"),
    DATE_FROM_STRING("dateFromString"),
    DATE_TO_STRING("dateToString"),
    DAY_OF_MONTH("dayOfMonth"),
    DAY_OF_WEEK("dayOfWeek"),
    DAY_OF_YEAR("dayOfYear"),
    DEGREES_TO_RADIANS("degreesToRadians"),
    DEVIDE("devide"),
    EQ("eq"),
    EXP("exp"),

    /**
     * @sample
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
    FILTER("filter"),
    FIRST("first"),
    FLOOR("floor"),
    GT("gt"),
    GTE("gte"),
    HOUR("hour"),
    IF_NULL("ifNull"),
    `IN`("in"),
    INDEX_OF_ARRAY("indexOfArray"),
    INDEX_OF_BYTES("indexOfBytes"),
    INDEX_OF_CP("indexOfCP"),
    IS_ARRAY("isArray"),
    ISO_DAY_OF_WEEK("isoDayOfWeek"),
    ISO_WEEK("isoWeek"),
    ISO_WEEK_YEAR("isoWeekYear"),
    LAST("last"),
    LET("let"),
    LITERAL("literal"),
    LN("ln"),
    LOG("log"),
    LOG10("log10"),
    LT("lt"),
    LTE("lte"),
    LTRIM("ltrim"),
    MAP("map"),
    MAX("max"),
    MERGE_OBJECTS("mergeObjects"),
    META("meta"),
    MIN("min"),
    MILLI_SECOND("millisecond"),
    MINUTE("minute"),
    MOD("mod"),
    MONTH("month"),
    MULTIPLY("multiply"),
    NE("ne"),
    NOT("not"),
    OBJECT_TO_ARRAY("objectToArray"),
    OR("or"),
    POW("pow"),
    PUSH("push"),
    RADIANS_TO_DEGREES("radiansToDegrees"),
    RANGE("range"),
    REDUCE("reduce"),
    REGEX_FIND("regexFind"),
    REGEX_FIND_ALL("regexFindAll"),
    REGEX_MATCH("regexMatch"),
    REVERSE_ARRAY("reverseArray"),
    ROUND("round"),
    RTRIM("rtrim"),
    SECOND("second"),
    SET_DIFFERENCE("setDifference"),
    SET_EQUALS("setEquals"),
    SET_INTERSECTION("setIntersection"),
    SET_IS_SUBSET("setIsSubset"),
    SET_UNION("setUnion"),
    SIZE("size"),
    SIN("sin"),
    SLICE("slice"),
    SPLIT("split"),
    SQRT("sqrt"),
    STD_DEV_POP("stdDevPop"),
    STD_DEV_SAMP("stdDevSamp"),
    STR_CASE_CMP("strcasecmp"),
    STR_LEN_BYTES("strLenBytes"),
    STR_LEN_CP("strLenCP"),
    SUBSTR("substr"),
    SUBSTR_BYTES("substrBytes"),
    SUBSTR_CP("substrCP"),
    SUBTRACT("subtract"),
    SUM("sum"),
    SWITCH("switch"),
    TAN("tan"),
    TO_BOOL("toBool"),
    TO_DATE("toDate"),
    TO_DECIMAL("toDecimal"),
    TO_DOUBLE("toDouble"),
    TO_INT("toInt"),
    TO_LONG("toLong"),
    TO_OBJECT_ID("toObjectId"),
    TO_STRING("toString"),
    TO_LOWER("toLower"),
    TO_UPPER("toUpper"),
    TRIM("trim"),
    TRUNC("trunc"),
    TYPE("type"),
    WEEK("week"),
    YEAR("year"),
    ZIP("zip");

    override fun toString(): String {
        return this.key;
    }
}

