package nbcp.db.mongo

import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.repository.MongoRepository
import nbcp.base.extend.HasValue
import nbcp.base.extend.IsSimpleType
import nbcp.base.extend.ToJson
import nbcp.base.extend.*
import nbcp.base.utils.MyUtil
import nbcp.db.db
import nbcp.db.mongo.*
import nbcp.db.mongo.component.MongoBaseUpdateClip
import org.slf4j.LoggerFactory
import org.springframework.data.mongodb.core.query.CriteriaDefinition
import java.time.LocalDateTime

/**
 * Created by udi on 17-4-7.
 */

//根据Id，更新Mongo的一个键。

/**
 * MongoUpdate
 */
class MongoUpdateClip<M : MongoBaseEntity<out IMongoDocument>>(var moerEntity: M) : MongoBaseUpdateClip(moerEntity.tableName) {

    fun where(whereData: Criteria): MongoUpdateClip<M> {
        this.whereData.add(whereData);
        return this;
    }

    fun where(where: (M) -> Criteria): MongoUpdateClip<M> {
        this.whereData.add(where(moerEntity));
        return this;
    }

    fun whereOr(vararg wheres: (M) -> Criteria): MongoUpdateClip<M> {
        return whereOr(*wheres.map { it(moerEntity) }.toTypedArray())
    }

    fun whereOr(vararg wheres: Criteria): MongoUpdateClip<M> {
        if (wheres.any() == false) return this;
        var where = Criteria();
        where.orOperator(*wheres)
        this.whereData.add(where);
        return this;
    }

    /**
     * 如果条件成立,则使用 where
     */
    fun whereIf(whereIf: Boolean, where: ((M) -> Criteria)): MongoUpdateClip<M> {
        if (whereIf == false) return this;

        this.whereData.add(where(moerEntity));
        return this;
    }

    /**
     * 如果条件成立,则使用 set
     */
    fun setIf(setIf: Boolean, valuePair: (M) -> Pair<MongoColumnName, Any?>): MongoUpdateClip<M> {
        if (setIf == false) return this;
        var v = valuePair(this.moerEntity)

        this.setData.put(v.first.toString(), v.second);
        return this;
    }


    fun set(key: String, value: Any?): MongoUpdateClip<M> {
        return set { MongoColumnName(key) to value }
    }

//    fun set(key: MongoColumnName, value: Any?): MongoUpdateClip<M> {
//        this.setData.put(key.toString(), value);
//        return this;
//    }
//    fun set(func: (M) -> MongoColumnName, value: Any?): MongoUpdateClip<M> {
//        var p = func(moerEntity);
//        this.setData.put(p.toString(), value);
//        return this;
//    }

    fun set(func: (M) -> Pair<MongoColumnName, Any?>): MongoUpdateClip<M> {
        var p = func(moerEntity);
        this.setData.put(p.first.toString(), p.second);
        return this;
    }

    //判断是否设置了某个值.
//    fun setted(key: String, value: Any): Boolean {
//        return this.setData.get(key) == value;
//    }

    fun unset(key: String): MongoUpdateClip<M> {
        this.unsetData.add(key);
        return this;
    }

    fun unset(keyFunc: (M) -> MongoColumnName): MongoUpdateClip<M> {
        this.unsetData.add(keyFunc(this.moerEntity).toString());
        return this;
    }

//    fun set(actionIf: () -> Pair<String, Any?>?): MongoUpdateClip<T> {
//        var setData = actionIf();
//
//        if (setData != null) {
//            this.setData.put(setData.first, setData.second);
//        }
//        return this;
//    }

    /**
     * 数据加法
     * .inc{ it.incField to 1 }
     */
    fun inc(incData: (M) -> Pair<MongoColumnName, Int>): MongoUpdateClip<M> {
        var kv = incData(this.moerEntity)
        this.incData.put(kv.first.toString(), kv.second);
        return this;
    }

    /**
     * 向数组中添加一条。
     * key:是实体的属性，内容是数组，如 roles。
     * pushItem是要插入实体的各个值。如： _id pair "ab" , name pair "def"
     */
    //    fun push(key: String, vararg pushItem: Pair<String, Any?>): MongoUpdateClip<T> {
//        this.pushData.put(key, LinkedHashMap<String, Any?>(pushItem.toMap()));
//        return this;
//    }

    /**
     * 向数组中添加一条。
     * key:是实体的属性，内容是数组，如 roles。
     * value是要插入实体值。如： UserRole
     */
    fun push(pair: (M) -> Pair<MongoColumnName, Any>): MongoUpdateClip<M> {
        var pairObject = pair(this.moerEntity);
        this.pushData.put(pairObject.first.toString(), pairObject.second);
        return this;
    }

    /**
     * 从数组中删除一条。
     * key:是实体的属性，内容是数组，如 roles。
     * pullWhere 是要删除实体的条件。如： _id pair "ab" , name pair "def"
     */
    fun pull(key: (M) -> MongoColumnName, vararg pullWhere: Criteria): MongoUpdateClip<M> {
        this.pullData.put(key(this.moerEntity).toString(), this.moerEntity.getMongoCriteria(*pullWhere));
        return this;
    }

    /**
     * 删除数组中的单个值.
     * @param pair  key=删除的数组列表达式， value=删除该列的值。
     */
    fun pull(pair: (M) -> Pair<MongoColumnName, String>): MongoUpdateClip<M> {
        var pairObject = pair(this.moerEntity);
        this.pullData.put(pairObject.first.toString(), pairObject.second);
        return this;
    }


    /**
     *@param define: 更新的参数，应该使用 Where 表达式子类 Criteria 传递
     */
    fun arrayFilter(define: Criteria): MongoUpdateClip<M> {
        this.arrayFilters.add(define)
        return this;
    }
}

