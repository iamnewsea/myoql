package nbcp.db.mongo.event;

import nbcp.db.mongo.*;
import nbcp.db.*
import org.springframework.stereotype.Component
import java.time.LocalDateTime

/**
 * 同步处理，更新的实体，添加 updateAt 字段。
 */
@Component
class MongoUpdateAtEvent : IMongoEntityUpdate {
    override fun beforeUpdate(update: MongoBaseUpdateClip): EventResult {
        update.setData.remove("createAt")
        update.setValue("updateAt", LocalDateTime.now())

        //补全 CityCodeName 中的 name
        update.setData.forEach { it ->
            if (it.value == null) {
                return@forEach
            }

//            db.fillCityName(it.value!!);

            return@forEach
        }


        return EventResult(true, null)
    }

    override fun update(update: MongoBaseUpdateClip, eventData: EventResult) {
    }
}