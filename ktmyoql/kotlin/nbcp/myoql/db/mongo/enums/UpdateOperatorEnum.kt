package nbcp.myoql.db.mongo.enums

/**
 * https://docs.mongodb.com/manual/reference/operator/update-array/
 */
enum class UpdateArrayOperatorEnum(val key:String, val value: String = "") {
    `SET`("\$set","$"),
    `SET_ALL`("\$setAll","\$[]"),
    `SET_WITH_FILTER`("\$setWithFilter", "$[<>]"),
    ADD_TO_SET("addToSet"),
    POP("pop"),
    PULL("pull"),
    PUSH("push"),
    PULL_ALL("pullAll");



    override fun toString(): String {
        return this.key;
    }
}


