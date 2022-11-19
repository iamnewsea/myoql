package nbcp.myoql.db.mongo.enums

enum class UpdateArrayOperatorModifierEnum(val key:String){
    EACH("each"),
    POSITION("position"),
    SLICE("slice"),
    SORT("sort");

    override fun toString(): String {
        return this.key;
    }
}