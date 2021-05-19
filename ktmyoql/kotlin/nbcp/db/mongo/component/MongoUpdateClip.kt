package nbcp.db.mongo

import org.springframework.data.mongodb.core.query.Criteria
import java.io.Serializable

/**
 * Created by udi on 17-4-7.
 */

//根据Id，更新Mongo的一个键。

/**
 * MongoUpdate
 */
class MongoUpdateClip<M : MongoBaseMetaCollection<out IMongoDocument>>(var moerEntity: M) : MongoBaseUpdateClip(moerEntity.tableName) {

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

    fun set(func: (M) -> Pair<MongoColumnName, Any?>): MongoUpdateClip<M> {
        var p = func(moerEntity);
        this.setData.put(p.first.toString(), p.second);
        return this;
    }

    fun unset(key: String): MongoUpdateClip<M> {
        this.unsetData.add(key);
        return this;
    }

    fun unset(keyFunc: (M) -> MongoColumnName): MongoUpdateClip<M> {
        this.unsetData.add(keyFunc(this.moerEntity).toString());
        return this;
    }

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
     * value是要插入实体值。如： UserRole
     */
    fun push(pair: (M) -> Pair<MongoColumnName, Serializable>): MongoUpdateClip<M> {
        var pairObject = pair(this.moerEntity);
        this.pushData.put(pairObject.first.toString(), pairObject.second);
        return this;
    }

    /**
     * 从数组中删除一条。
     * key:是实体的属性，内容是数组，如 roles。
     * pullWhere 是要删除实体的条件。
     * 如：
     * .pull( it.menus, "id" match "ab")
     * .pull( it.roles, "id" match "def")
     * ==>
     * { $pull: { "menus":{"id:"ab"} , "roles":{"id:"def"} } }
     */
    fun pull(key: (M) -> MongoColumnName, pullWhere: Criteria): MongoUpdateClip<M> {
        this.pullData.put(key(this.moerEntity).toString(), pullWhere);
        return this;
    }

    /**
     * 删除数组中的单个值.
     * @param pair  key=删除的数组列表达式， value=删除该列的值。
     * 如：
     * .pull( it.menus  to "1")
     * ==>
     * { $pull :{ "menus": "1" } }
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

