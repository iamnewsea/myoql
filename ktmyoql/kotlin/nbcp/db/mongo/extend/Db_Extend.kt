package nbcp.db.mongo

import nbcp.db.db
import nbcp.db.mongo.entity.BasicUserLoginInfo
import nbcp.db.mongo.table.MongoBaseGroup


fun MongoBaseGroup.BasicUserLoginInfoEntity.queryByUserId(userId: String):
        MongoQueryClip<MongoBaseGroup.BasicUserLoginInfoEntity, BasicUserLoginInfo> {
    return this.query().where { it.userId match userId }
}
