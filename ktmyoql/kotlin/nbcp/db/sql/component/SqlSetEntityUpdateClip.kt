package nbcp.db.sql


import nbcp.comm.*
import nbcp.utils.*
import nbcp.db.db
import org.slf4j.LoggerFactory

/**
 * Created by udi on 17-4-7.
 */

//根据Id，更新Mongo的一个键。

/**
 * MongoUpdate
 * 不会更新 id
 */
class SqlSetEntityUpdateClip<M : SqlBaseMetaTable<out T>, T : ISqlDbEntity>(var mainEntity: M, var entity: T) : SqlBaseExecuteClip(mainEntity.tableName) {
    companion object {
    }

    private var whereColumns = mutableSetOf<SqlColumnName>()
    private var setColumns = mutableSetOf<SqlColumnName>()
    private var unsetColumns = mutableSetOf<SqlColumnName>()
    private var sets = mutableMapOf<SqlColumnName, Any?>()

    fun withColumn(setFunc: (M) -> SqlColumnName): SqlSetEntityUpdateClip<M, T> {
        this.setColumns.add(setFunc(this.mainEntity))
        return this;
    }

    fun withoutColumn(unsetFunc: (M) -> SqlColumnName): SqlSetEntityUpdateClip<M, T> {
        this.unsetColumns.add(unsetFunc(this.mainEntity))
        return this;
    }

    fun whereColumn(whereFunc: (M) -> SqlColumnName): SqlSetEntityUpdateClip<M, T> {
        this.whereColumns.add(whereFunc(this.mainEntity))
        return this;
    }

//    /**
//     * 不应该依赖客户端，不应该使用这个方法
//     */
//    fun withRequestParams(keys: Set<String>): SqlSetEntityUpdateClip<M, T> {
//        var columns = this.mainEntity.getColumns();
//        keys.forEach { key ->
//            var column = columns.firstOrNull { it.name == key }
//            if (column != null) {
//                withColumn { column }
//            }
//        }
//        return this
//    }

    //额外设置
    fun set(setItemAction: (M) -> Pair<SqlColumnName, Any?>): SqlSetEntityUpdateClip<M, T> {
        var setItem = setItemAction(this.mainEntity);
        this.sets.put(setItem.first, setItem.second);
        return this;
    }

    /**
     * 更新，默认按 id 更新
     */
    override fun exec(): Int {
        this.toSql();
        return sqlUpdate.exec();
    }

    private var sqlUpdate = SqlUpdateClip<M, T>(mainEntity);
    override fun toSql(): SingleSqlData {
        sqlUpdate = SqlUpdateClip<M, T>(mainEntity);

        var columns = this.mainEntity.getColumns()

        var setValues = mutableMapOf<SqlColumnName, Any?>()

        var field_names = mutableListOf<String>();
        entity::class.java.AllFields.forEach {field->

//            var ann = field.getAnnotation(SqlSpreadColumn::class.java);
//            if( ann!= null){
//                var ent_field_value = field.get(entity);
//                field.type.AllFields.forEach { subField ->
//                    var value = subField.get(ent_field_value);
//                    setValues.put(columns.first { it.name == field.name +"_" + subField.name }, value)
//                }
//            }

            if( this.mainEntity.getSpreadColumns().contains(field.name)){
                var ent_field_value = field.get(entity);
                field.type.AllFields.forEach { subField ->
                    var value = subField.get(ent_field_value);
                    setValues.put(columns.first { it.name == field.name +"_" + subField.name }, value)
                }
            }

             field_names .add(field.name);
        };


        //自增 id 不能更新。
        var auKey = columns.firstOrNull { it.name == this.mainEntity.getAutoIncrementKey() };
        var where = WhereData();

        var whereColumns2 = whereColumns.map { it }.toMutableList();

        if (whereColumns2.any() == false) {
            if (auKey != null) {
                whereColumns2.add(auKey)
            } else {
                var uks = this.mainEntity.getUks().toList();
                var pks = uks.groupBy { it.size }.minBy { it.key }?.value?.firstOrNull()
                if (pks != null) {
                    whereColumns2.addAll(columns.filter { pks.contains(it.name) })
                }
            }
        }


        whereColumns2.forEach { column ->
            var value = MyUtil.getPrivatePropertyValue(entity, column.name)

            where.and(WhereData("${column.fullName} = {${column.jsonKeyName}}", JsonMap(column.jsonKeyName to value)))
        }


        var unsetColumn_names = unsetColumns.map { it.name }
        var setColumn_names = listOf<String>()

        if (setColumns.any() == false) {
            setColumn_names = columns.map { it.name }
        } else {
            setColumn_names = setColumns.map { it.name }
        }

        columns.minus(whereColumns2)
                .filter { column ->
                    column.name != auKey?.name
                            && field_names.contains(column.name)
                            && !unsetColumn_names.contains(column.name)
                            && setColumn_names.contains(column.name)
                }
                .forEach { key ->
                    var value = MyUtil.getPrivatePropertyValue(entity, key.name)
                    if (value == null) {
                        setValues.put(key,null);
                        return@forEach
                    }

                    setValues.put(key, proc_value(value));
                }

        this.sets.forEach {
            setValues.put(it.key, it.value)
        }
        setValues.forEach {
            this.sqlUpdate.sets.put(it.key, it.value);
        }

        this.sqlUpdate.whereDatas.and(where);

        return sqlUpdate.toSql()
    }

    /**
     * 执行更新, == exec ,语义清晰
     */
    fun execUpdate(): Int {
        return this.exec();
    }

    /**
     * 执行插入
     */
    fun execInsert(): Int {
        var batchInsert = SqlInsertClip(mainEntity)
        batchInsert.addEntity(entity);

        return batchInsert.exec();
    }

    /**
     * 先更新，如果不存在，则插入。
     * @return: 返回插入的Id，如果是更新则返回空字串
     */
    fun updateOrAdd(): Int {
        //有一个问题，可能是阻止更新了。所以导致是0。
        if (this.execUpdate() == 0) {
            return this.execInsert()
        }
        return db.affectRowCount;
    }
}

