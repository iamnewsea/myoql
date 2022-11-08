package nbcp.myoql.db.mongo.enums

/**
 * https://docs.mongodb.com/manual/reference/operator/update-array/
 */
enum class UpdateArrayOperatorEnum(val value: String = "") {
    `$set`("$"),
    `$setAll`("$[]"),
    `$setWithFilter`("$[<>]"),
    addToSet,
    pop,
    pull,
    push,
    pullAll
}

enum class UpdateArrayOperatorModifierEnum{
    each,
    position,
    slice,
    sort
}


/**
 * https://docs.mongodb.com/manual/reference/operator/update-field/
 */
enum class UpdateFieldOperatorEnum{
    currentDate,
    inc,
    min,
    max,
    mul,
    rename,
    set,
    setOnInsert,
    unset,
    //https://docs.mongodb.com/manual/reference/operator/update-bitwise/
    bit
}