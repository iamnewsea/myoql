package nbcp.db.mongo

import nbcp.comm.MyString
import nbcp.comm.ToJson
import nbcp.comm.JsonMap
import nbcp.db.db

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
