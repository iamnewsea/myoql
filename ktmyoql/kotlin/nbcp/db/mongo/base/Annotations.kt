package nbcp.db.mongo


@Repeatable
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS )
@Retention(AnnotationRetention.RUNTIME)
annotation class MongoEntityGroup(val group: String = "")



//对应的表是 mongoEntity_${key}_${key.value}
//如：  mqLog_level_1
//没想好.
@Repeatable
@Target(AnnotationTarget.TYPE, AnnotationTarget.CLASS )
@Retention(AnnotationRetention.RUNTIME)
annotation class MongoVarEntity(val key: String = "")

