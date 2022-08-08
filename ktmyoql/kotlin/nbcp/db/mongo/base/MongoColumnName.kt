package nbcp.db.mongo

import nbcp.comm.AsString
import nbcp.comm.MyString
import nbcp.db.db
import org.bson.BasicBSONObject
import org.springframework.data.mongodb.core.query.Criteria
import java.util.regex.Pattern

/**
 * Mongo列
 */
open class MongoColumnName @JvmOverloads constructor(_mongo_value: String = "") : MyString(_mongo_value) {

//    val asc: MongoOrderBy
//        get() = MongoOrderBy(true, this)
//
//    val desc: MongoOrderBy
//        get() = MongoOrderBy(false, this)


    fun match(to: Any?): Criteria {
        val (key, toValue) = db.mongo.proc_mongo_key_value(this, to);
        return Criteria.where(key.AsString("\$eq")).`is`(toValue);// Pair<String, T>(this, to);
    }

    fun match_not_equal(value: Any?): Criteria {
        val (key, toValue) = db.mongo.proc_mongo_key_value(this, value);
        return Criteria.where(key).`ne`(toValue)
    }

    fun match_pattern(pattern: String): Criteria {
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

    fun match_like(like: String): Criteria {
        return this match_pattern "${getSafeRegText(like)}"
    }


    fun match_gte(to: Any): Criteria {
        val (key, toValue) = db.mongo.proc_mongo_key_value(this, to);
        return Criteria.where(key).gte(toValue!!);
    }

    fun match_lte(to: Any): Criteria {
        val (key, toValue) = db.mongo.proc_mongo_key_value(this, to);
        return Criteria.where(key).lte(toValue!!);
    }

    fun match_greaterThan(to: Any): Criteria {
        val (key, toValue) = db.mongo.proc_mongo_key_value(this, to);
        return Criteria.where(key).gt(toValue!!);
    }

    fun match_lessThan(to: Any): Criteria {
        val (key, toValue) = db.mongo.proc_mongo_key_value(this, to);
        return Criteria.where(key).lt(toValue!!);
    }

    /**
     * 大于等于并且小于。
     */
    fun match_between(value: Pair<Any, Any>): Criteria {
        var (key, value2) = db.mongo.proc_mongo_key_value(this, value);
        var pair = value2 as Pair<Any, Any>

        var dict = BasicBSONObject()
        dict.put("\$gte", pair.first)
        dict.put("\$lt", pair.second)
        return Criteria.where(key).`is`(dict)
        //return Criteria.where(key).gte(from).andOperator(Criteria.where(key).lt(to))
    }

    fun match_in(to: Collection<*>): Criteria {
        return this.match_in(to.toTypedArray())
    }

    //db.test1.find({"age":{"$in":['值1','值2',.....]}})
    fun match_in(to: Array<*>): Criteria {
        var (key, tos) = db.mongo.proc_mongo_key_value(this, to.toSet())
        if (tos is Array<*>) {
            return Criteria.where(key).`in`(*(tos as Array<*>));
        }
        return Criteria.where(key).`in`(*(tos as Collection<*>).toTypedArray());
    }

    fun match_notin(to: Array<*>): Criteria {
        var (key, tos) = db.mongo.proc_mongo_key_value(this, to.toSet())
        if (tos is Array<*>) {
            return Criteria.where(key).`nin`(*(tos as Array<*>));
        }
        return Criteria.where(key).`nin`(*(tos as Collection<*>).toTypedArray());
    }

    fun match_notin(to: Collection<*>): Criteria {
        return this.match_notin(to.toTypedArray())
    }



    fun match_size(value: Int): Criteria {
        return Criteria.where(this.toString()).size(value);
    }

    //array_all
    fun match_all(to: Array<*>): Criteria {
        val (key, tos) = db.mongo.proc_mongo_key_value(this, to.toSet())

        if (tos is Array<*>) {
            return Criteria.where(key).`all`(*(tos as Array<*>));
        }
        return Criteria.where(key).`all`(*(tos as Collection<*>).toTypedArray());
    }

    fun match_type(to: MongoTypeEnum): Criteria {
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
    fun match_elemMatch(value: Map<String, Any?>): Criteria {
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
    fun match_exists(value: Boolean): Criteria {
        var (key) = db.mongo.proc_mongo_key_value(this, null);
        return Criteria.where(key).`exists`(value);
    }

    /**
     * field match_hasValue  => field exists  and field != null and field != ""
     */
    fun match_hasValue(): Criteria {
        return this.match_exists().match_and(this.match_not_equal(null).match_or(this.match_not_equal("")));
    }

    /**
     * field match_isNull => field not exists  or field == null  or field == ""
     */
    fun match_isNullOrEmpty(): Criteria {
        return this.match_exists(false).match_or(this.match(null).match_or(this.match("")));
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