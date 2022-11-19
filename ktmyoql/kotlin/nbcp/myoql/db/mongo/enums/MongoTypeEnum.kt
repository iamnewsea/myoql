package nbcp.myoql.db.mongo.enums

/**
 * Created by yuxh on 2019/1/31
 */

/**
 * https://www.mongodb.com/docs/v6.0/reference/operator/query/type/
 */
enum class MongoTypeEnum(var value:kotlin.Int,var alias:kotlin.String){
    `DOUBLE`(1,"double"),
    `STRING`(2,"string"),
    `OBJECT`(3,"object"),
    `ARRAY`(4,"array"),
    `BIN_DATA`(5,"binData"),
    `UNDEFINED`(6,"undefined"),
    `OBJECT_ID`(7,"objectId"),
    `BOOL`(8,"bool"),
    `DATE`(9,"date"),
    `NULL`(10,"null"),
    `REGEX`(11,"regex"),
    `DB_POINTER`(12,"dbPointer"),
    `JAVA_SCRIPT`(13,"javascript"),
    `SYMBOL`(14,"symbol"),
    `JAVA_SCRIPT_WITH_SCOPE`(15,"javascriptWithScope"),
    `INT`(16,"int"),
    `TIMESTAMP`(17,"timestamp"),
    `LONG`(18,"long"),
    `DECIMAL`(19,"decimal"),
    `MIN_KEY`(-1,"minKey"),
    `MAX_KEY`(127,"maxKey")
}