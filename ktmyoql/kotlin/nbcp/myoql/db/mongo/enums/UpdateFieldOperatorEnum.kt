package nbcp.myoql.db.mongo.enums

/**
 * https://docs.mongodb.com/manual/reference/operator/update-field/
 */
enum class UpdateFieldOperatorEnum(val key:String){
    CURRENT_DATE("currentDate"),
    INC("inc"),
    MIN("min"),
    MAX("max"),
    MUL("mul"),
    RENAME("rename"),
    SET("set"),
    SET_ON_INSERT("setOnInsert"),
    UNSET("unset"),
    //https://docs.mongodb.com/manual/reference/operator/update-bitwise/
    BIT("bit");


    override fun toString(): String {
        return this.key;
    }
}