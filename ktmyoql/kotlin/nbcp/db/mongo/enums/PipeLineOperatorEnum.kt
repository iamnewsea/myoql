package nbcp.db.mongo

import nbcp.base.extend.MyRawString
import nbcp.base.extend.MyString
import nbcp.base.extend.ToJson
import nbcp.comm.JsonMap

/**
 * https://docs.mongodb.com/manual/reference/operator/aggregation/
 */
enum class PipeLineOperatorEnum {
    abs,
    acos,
    acosh,
    add,
    addToSet,
    allElementsTrue,
    and,
    anyElementTrue,
    arrayElemAt,
    arrayToObject,
    asin,
    asinh,
    atan,
    atan2,
    atanh,
    avg,
    ceil,
    cmp,
    concat,
    concatArrays,
    cond,
    convert,
    cos,
    dateFromParts,
    dateToParts,
    dateFromString,
    dateToString,
    dayOfMonth,
    dayOfWeek,
    dayOfYear,
    degreesToRadians,
    devide,
    eq,
    exp,
    filter,
    first,
    floor,
    gt,
    gte,
    hour,
    ifNull,
    `in`,
    indexOfArray,
    indexOfBytes,
    indexOfCP,
    isArray,
    isoDayOfWeek,
    isoWeek,
    isoWeekYear,
    last,
    let,
    literal,
    ln,
    log,
    log10,
    lt,
    lte,
    ltrim,
    map,
    max,
    mergeObjects,
    meta,
    min,
    millisecond,
    minute,
    mod,
    month,
    multiply,
    ne,
    not,
    objectToArray,
    or,
    pow,
    push,
    radiansToDegrees,
    range,
    reduce,
    regexFind,
    regexFindAll,
    regexMatch,
    reverseArray,
    round,
    rtrim,
    second,
    setDifference,
    setEquals,
    setIntersection,
    setIsSubset,
    setUnion,
    size,
    sin,
    slice,
    split,
    sqrt,
    stdDevPop,
    stdDevSamp,
    strcasecmp,
    strLenBytes,
    strLenCP,
    substr,
    substrBytes,
    substrCP,
    subtract,
    sum,
    switch,
    tan,
    toBool,
    toDate,
    toDecimal,
    toDobule,
    toInt,
    toLong,
    toObjectId,
    toString,
    toLower,
    toUpper,
    trim,
    trunc,
    type,
    week,
    year,
    zip
}

/**
 * https://docs.mongodb.com/manual/reference/operator/aggregation/group/#accumulators-group
 */
enum class PipeLineAccumulatorOperatorEnum {
    addToSet,
    avg,
    first,
    last,
    max,
    mergeObjects,
    min,
    push,
    stdDevPop,
    stdDevSamp,
    sum
}

/**
 * 以 totalSaleAmount: { $sum: { $multiply: [ "$price", "$quantity" ] } } 为例 。
 * PipeLineGroupExpression()
 *  .op(PipeLineOperatorEnum.multiply, arrayOf("$price","$quantity"))
 *  .done(PipeLineAccumulatorOperatorEnum.sum,"totalSaleAmount")
 */
class PipeLineGroupExpression(value: String = "") : MyRawString(value) {
    fun op(operator: PipeLineOperatorEnum, rawValue: String): PipeLineGroupExpression {
        return PipeLineGroupExpression("""{$${operator}:"${rawValue}"""")
    }

    fun op(operator: PipeLineOperatorEnum, rawValue: PipeLineGroupExpression): PipeLineGroupExpression {
        return PipeLineGroupExpression("{$${operator}:${rawValue.toString()}")
    }

    fun op(operator: PipeLineOperatorEnum, rawValue: Array<*>): PipeLineGroupExpression {
        return PipeLineGroupExpression("{$${operator}:${rawValue.ToJson()}")
    }


    fun op(operator: PipeLineOperatorEnum, rawValue: JsonMap): PipeLineGroupExpression {
        return PipeLineGroupExpression("{$${operator}:${rawValue.ToJson()}")
    }


    /**
     * 聚合
     */
    fun accumulate(operator: PipeLineAccumulatorOperatorEnum, columnName:String): MyRawString {
        return MyRawString(""""${columnName}":{$${operator}:"${this.toString()}"}""")
    }
}
