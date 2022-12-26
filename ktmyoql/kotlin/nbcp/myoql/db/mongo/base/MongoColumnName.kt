package nbcp.myoql.db.mongo.base

import com.mongodb.BasicDBList
import nbcp.base.extend.AsString
import nbcp.myoql.db.db
import nbcp.myoql.db.mongo.MongoWhereClip
import nbcp.myoql.db.mongo.enums.MongoTypeEnum
import org.bson.BasicBSONObject
import org.springframework.data.mongodb.core.query.Criteria
import java.util.regex.Pattern

/**
 * Mongo列
 */
open class MongoColumnName @JvmOverloads constructor(private var mongo_column_name: String = "") : java.io.Serializable {

//    val asc: MongoOrderBy
//        get() = MongoOrderBy(true, this)
//
//    val desc: MongoOrderBy
//        get() = MongoOrderBy(false, this)

    override fun hashCode(): Int {
        return mongo_column_name.hashCode()
    }

    override fun toString(): String {
        return mongo_column_name
    }

    infix fun match(to: Any?): Criteria {
        val (key, toValue) = db.mongo.proc_mongo_key_value(this, to);
        return Criteria.where(key.AsString("\$eq")).`is`(toValue);// Pair<String, T>(this, to);
    }

    /**
     * 非。
     * 注意，在数组中判断非，表示的是 全部非   not_equals "" 《===》  ! ( equals "" )
     */
    infix fun match_not_equal(value: Any?): Criteria {
        val (key, toValue) = db.mongo.proc_mongo_key_value(this, value);
        return Criteria.where(key).`ne`(toValue)
    }

    /**
     * 模糊查询，用法：
     * mor.code.qrCodeInfo.product.name match_pattern "国际"
     * 即：内容包含 "国际"。
     *
     * mor.code.qrCodeInfo.product.name match_pattern "^国际"
     * 即：内容以 "国际" 开头。
     *
     * mor.code.qrCodeInfo.product.name match_pattern "国际$"
     * 即：内容以 "国际" 结尾。
     * @param pattern: 不会转义
     */
    infix fun match_pattern(pattern: String): Criteria {
        return Criteria.where(this.toString()).regex(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE))
    }


    private fun getSafeRegText(value: String): String {
        //https://www.cnblogs.com/ysk123/p/9858387.html

        var v = value;

        // 第一个必须是 反斜线！
        """\/|()[]{}*+.?^${'$'}""".forEach {
            v = v.replace(it.toString(), "\\${it}")
        }
        return v;
    }

    /**
     * @param like: 会对查询内容中的特殊字符转义，避免与正则表达式冲突
     */
    infix fun match_like(like: String): Criteria {
        return this match_pattern "${getSafeRegText(like)}"
    }


    infix fun match_gte(to: Any): Criteria {
        val (key, toValue) = db.mongo.proc_mongo_key_value(this, to);
        return Criteria.where(key).gte(toValue!!);
    }

    infix fun match_lte(to: Any): Criteria {
        val (key, toValue) = db.mongo.proc_mongo_key_value(this, to);
        return Criteria.where(key).lte(toValue!!);
    }

    infix fun match_greaterThan(to: Any): Criteria {
        val (key, toValue) = db.mongo.proc_mongo_key_value(this, to);
        return Criteria.where(key).gt(toValue!!);
    }

    infix fun match_lessThan(to: Any): Criteria {
        val (key, toValue) = db.mongo.proc_mongo_key_value(this, to);
        return Criteria.where(key).lt(toValue!!);
    }

    /**
     * 大于等于并且小于。
     */
    infix fun match_between(value: Pair<Any, Any>): Criteria {
        var (key, value2) = db.mongo.proc_mongo_key_value(this, value);
        var pair = value2 as Pair<Any, Any>

        var dict = BasicBSONObject()
        dict.put("\$gte", pair.first)
        dict.put("\$lt", pair.second)
        return Criteria.where(key).`is`(dict)
        //return Criteria.where(key).gte(from).andOperator(Criteria.where(key).lt(to))
    }

    infix fun match_in(to: Collection<*>): Criteria {
        return this.match_in(to.toTypedArray())
    }

    //db.test1.find({"age":{"$in":['值1','值2',.....]}})
    infix fun match_in(to: Array<*>): Criteria {
        var (key, tos) = db.mongo.proc_mongo_key_value(this, to.toSet())
        if (tos is Array<*>) {
            return Criteria.where(key).`in`(*(tos as Array<*>));
        }
        return Criteria.where(key).`in`(*(tos as Collection<*>).toTypedArray());
    }

    infix fun match_notin(to: Array<*>): Criteria {
        var (key, tos) = db.mongo.proc_mongo_key_value(this, to.toSet())
        if (tos is Array<*>) {
            return Criteria.where(key).`nin`(*(tos as Array<*>));
        }
        return Criteria.where(key).`nin`(*(tos as Collection<*>).toTypedArray());
    }

    infix fun match_notin(to: Collection<*>): Criteria {
        return this.match_notin(to.toTypedArray())
    }


    /**
     * 数组长度
     */
    infix fun match_size(value: Int): Criteria {
        return Criteria.where(this.toString()).size(value);
    }

    /**
     * 数组中所有项要满足, 没有对应的 any!
     */
    infix fun match_all(to: Array<*>): Criteria {
        val (key, tos) = db.mongo.proc_mongo_key_value(this, to.toSet())

        if (tos is Array<*>) {
            return Criteria.where(key).`all`(*(tos as Array<*>));
        }
        return Criteria.where(key).`all`(*(tos as Collection<*>).toTypedArray());
    }


    infix fun match_type(to: MongoTypeEnum): Criteria {
        val (key, _) = db.mongo.proc_mongo_key_value(this, to);

        return Criteria.where(key).`type`(to.value);// Pair<String, T>(this, to);
    }


    /**
     * 用于 数组的 elemMatch！
     * @sample
     * 如附件字段
     *  tags: [ {name:"a", score: 10},{name:"b", score: 50} ]
     *
     *  mongoshell 查询
     *  db.getCollection('sysAnnex').find(
     * {   tags:  { $elemMatch: { "score" : 5 } } },
     * {   tags:  { $elemMatch: { "score" : 5 } } }
     * )
     *
     * 程序：
     * mor.base.sysAnnex.query()
     *   .where_select_elemMatch { it.tags  match_elemMatch ( MongoColumName("score") match 5) }
     *   .toList()
     * ---
     * 如果附加字段是简单类型的数组，如:
     * tags: ["a","b"]
     *
     * mongoshell 查询：
     *  db.getCollection('sysAnnex').find(
     * {   tags:  { $elemMatch: { "$eq" : "a" } } },
     * {   tags:  { $elemMatch: { "$eq" : "a" } } }
     * )
     *
     * 程序：
     * mor.base.sysAnnex.query()
     *   .where_select_elemMatch { it.tags  ,  MongoColumName() match "a" }
     *   .toList()
     *
     * https://docs.mongodb.com/manual/reference/operator/query/elemMatch/index.html
     * https://docs.mongodb.com/manual/reference/operator/projection/elemMatch/index.html
     * @param value: 和普通的条件是不一样的。
     */
    infix fun match_elemMatch(value: Map<String, Any?>): Criteria {
        var (key) = db.mongo.proc_mongo_key_value(this, null);
        return Criteria.where(key).`elemMatch`(db.mongo.getMergedMongoCriteria(MongoWhereClip(value)));
    }


    fun MongoColumnName.match_exists(): Criteria {
        return this.match_exists(true)
    }

    /**
     * 用法：
     * 判断数组没有值，好处理： tags match_size 0
     * 判断数组有值,转化为：第一元素是否存在  MongoColumnName("tags.0")  match_exists true
     */
    infix fun match_exists(value: Boolean): Criteria {
        var (key) = db.mongo.proc_mongo_key_value(this, null);
        return Criteria.where(key).`exists`(value);
    }

    /**
     * field match_hasValue  => field exists  and field != null and field != ""
     */
    fun match_hasValue(): Criteria {
        var where = Criteria();
        where.andOperator(this.match_exists(), this.match_not_equal(null), this.match_not_equal(""))
        return where;
    }

    /**
     * field match_isNull => field not exists  or field == null  or field == ""
     */
    fun match_isNullOrEmpty(): Criteria {
        var where = Criteria();
        where.orOperator(this.match_exists(false), this.match(null), this.match(""))
        return where;
    }


    /**
     * Created by udi on 17-7-10.
     */
    /**
     * 另一种形式的条件。值可以是字段。
     */
    fun match_expr(op: String, to: MongoColumnName): Criteria {
        var d2 = BasicDBList();
        d2.add("$" + this.toString())
        d2.add("$" + to)

        var dict = BasicBSONObject()
        dict.put("$" + op, d2)

        return Criteria.where("$" + "expr").`is`(dict);
    }

    /**
     * 慎用，拦截器可能部分失效
     * @sample
     *
     *         db.getCollection('gitData').find({
     *          "$where":  "this.folders.some(it=> it.config && it .config.docker._id.toString().length > 0 )"
     *         })
     *  ===>
     *         db.getCollection('gitData').find(
     *          "this.folders.some(it=> it.config && it .config.docker._id.toString().length > 0 )"
     *         )
     */
    fun match_where_script(script: String): Criteria {
        return Criteria.where("$" + "where").`is`(script);
    }

    infix fun match_expr_equal(to: MongoColumnName): Criteria {
        return this.match_expr("eq", to);
    }


    infix fun match_expr_not_equal(to: MongoColumnName): Criteria {
        return this.match_expr("ne", to);
    }

    infix fun match_expr_gte(to: MongoColumnName): Criteria {
        return this.match_expr("gte", to);
    }

    infix fun match_expr_lte(to: MongoColumnName): Criteria {
        return this.match_expr("lte", to);
    }


    infix fun match_expr_greaterThan(to: MongoColumnName): Criteria {
        return this.match_expr("gt", to);
    }

    infix fun match_expr_lessThan(to: MongoColumnName): Criteria {
        return this.match_expr("lt", to);
    }

    infix fun match_expr_between(to: MongoColumnName): Criteria {
        return this.match_expr("between", to);
    }


    operator fun plus(value: String): MongoColumnName {
        return MongoColumnName(this.toString() + value)
    }

    operator fun plus(value: MongoColumnName): MongoColumnName {
        return MongoColumnName(this.toString() + value.toString())
    }

    fun slice(startIndex: IntRange): MongoColumnName {
        return MongoColumnName(this.toString().slice(startIndex))
    }

    infix fun and(other: MongoColumnName): MongoColumns {
        val ret = MongoColumns()
        ret.add(this);
        ret.add(other);
        return ret;
    }
}