package nbcp.db.es


import nbcp.comm.*
import nbcp.db.*
import nbcp.db.es.*
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.time.LocalDateTime

/**
 * Created by udi on 17-4-17.
 */

/**
 * EsDelete
 */
class EsDeleteClip<M : EsBaseEntity<out IEsDocument>>(var eserEntity: M) : EsClipBase(eserEntity.tableName), IEsWhereable {
    val whereData = mutableListOf<Any>()

    companion object {
        private var logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    fun where(whereData: Any): EsDeleteClip<M> {
        this.whereData.add(whereData);
        return this;
    }

    fun where(where: (M) -> Any): EsDeleteClip<M> {
        this.whereData.add(where(eserEntity));
        return this;
    }

    fun whereOr(vararg wheres: (M) -> Any): EsDeleteClip<M> {
        return whereOr(*wheres.map { it(eserEntity) }.toTypedArray())
    }

    fun whereOr(vararg wheres: Any): EsDeleteClip<M> {
        if (wheres.any() == false) return this;

        return this;
    }

    fun exec(): Int {
        db.affectRowCount = -1;
        var criteria = ""

        var settingResult = db.es.esEvents.onDeleting(this)
        if (settingResult.any { it.second.result == false }) {
            return 0;
        }

        var ret = 0;
        var startAt = LocalDateTime.now();
        try {
//            var result = esTemplate.remove(
//                    Query.query(criteria),
//                    collectionName);
//            db.executeTime = LocalDateTime.now() - startAt
//
//            ret = result.deletedCount.toInt()
//            db.affectRowCount = ret;
//
//            if (ret > 0) {
//                using(OrmLogScope.IgnoreAffectRow) {
//                    using(OrmLogScope.IgnoreExecuteTime) {
//                        settingResult.forEach {
//                            it.first.delete(this, it.second)
//                        }
//                    }
//                }
//            }
        } catch (e: Exception) {
            ret = -1;
            throw e;
        } finally {
            logger.InfoError(ret < 0) { "delete:[" + this.collectionName + "] " + criteria   + ",result:${ret}" };
        }


        return ret;
    }
}