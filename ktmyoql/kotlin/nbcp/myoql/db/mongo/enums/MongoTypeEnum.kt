package nbcp.myoql.db.mongo.enums

/**
 * Created by yuxh on 2019/1/31
 */

/**
 * MongoTypeEnum
 */
enum class MongoTypeEnum(var value:kotlin.Int,var alias:kotlin.String){
    `Double`(1,"double"),
    `String`(2,"string"),
    `Object`(3,"object"),
    `Array`(4,"array"),
    `BinData`(5,"binData"),
    `Undefined`(6,"undefined"),
    `ObjectId`(7,"objectId"),
    `Bool`(8,"bool"),
    `Date`(9,"date"),
    `Null`(10,"null"),
    `Regex`(11,"regex"),
    `DbPointer`(12,"dbPointer"),
    `Javascript`(13,"javascript"),
    `Symbol`(14,"symbol"),
    `JavascriptWithScope`(15,"javascriptWithScope"),
    `Int`(16,"int"),
    `Timestamp`(17,"timestamp"),
    `Long`(18,"long"),
    `Decimal`(19,"decimal"),
    `MinKey`(-1,"minKey"),
    `MaxKey`(127,"maxKey")
}