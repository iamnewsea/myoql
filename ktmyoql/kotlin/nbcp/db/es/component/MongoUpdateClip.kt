package nbcp.db.es

import nbcp.base.extend.*
import nbcp.base.utils.MyUtil
import nbcp.db.db
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

/**
 * Created by udi on 17-4-7.
 */

//根据Id，更新Es的一个键。

/**
 * EsUpdate
 */
class EsUpdateClip<M : EsBaseEntity<out IEsDocument>>(var moerEntity: M) : EsBaseUpdateClip(moerEntity.tableName) {

    fun where(whereData: Any): EsUpdateClip<M> {

        return this;
    }

    fun where(where: (M) -> Any): EsUpdateClip<M> {
        return this;
    }

    fun whereOr(vararg wheres: (M) -> Any): EsUpdateClip<M> {
        return whereOr(*wheres.map { it(moerEntity) }.toTypedArray())
    }

    fun whereOr(vararg wheres: Any): EsUpdateClip<M> {
        if (wheres.any() == false) return this;

        return this;
    }

    /**
     * 如果条件成立,则使用 where
     */
    fun whereIf(whereIf: Boolean, where: ((M) -> SearchBodyClip)): EsUpdateClip<M> {
        if (whereIf == false) return this;


        return this;
    }

    /**
     * 如果条件成立,则使用 set
     */
    fun setIf(setIf: Boolean, valuePair: (M) -> Pair<EsColumnName, Any?>): EsUpdateClip<M> {
        if (setIf == false) return this;
        var v = valuePair(this.moerEntity)


        return this;
    }


    fun set(key: String, value: Any?): EsUpdateClip<M> {
        return set { EsColumnName(key) to value }
    }

    fun set(func: (M) -> Pair<EsColumnName, Any?>): EsUpdateClip<M> {
        var p = func(moerEntity);

        return this;
    }

    fun unset(key: String): EsUpdateClip<M> {

        return this;
    }

    fun unset(keyFunc: (M) -> EsColumnName): EsUpdateClip<M> {

        return this;
    }

    /**
     * 数据加法
     * .inc{ it.incField to 1 }
     */
    fun inc(incData: (M) -> Pair<EsColumnName, Int>): EsUpdateClip<M> {
        var kv = incData(this.moerEntity)

        return this;
    }

    /**
     * 向数组中添加一条。
     * key:是实体的属性，内容是数组，如 roles。
     * value是要插入实体值。如： UserRole
     */
    fun push(pair: (M) -> Pair<EsColumnName, Any>): EsUpdateClip<M> {
        var pairObject = pair(this.moerEntity);

        return this;
    }

    /**
     * 从数组中删除一条。
     * key:是实体的属性，内容是数组，如 roles。
     * pullWhere 是要删除实体的条件。如： _id pair "ab" , name pair "def"
     */
    fun pull(key: (M) -> EsColumnName, vararg pullWhere: Any): EsUpdateClip<M> {

        return this;
    }

    /**
     * 删除数组中的单个值.
     * @param pair  key=删除的数组列表达式， value=删除该列的值。
     */
    fun pull(pair: (M) -> Pair<EsColumnName, String>): EsUpdateClip<M> {
        var pairObject = pair(this.moerEntity);
        return this;
    }
}

