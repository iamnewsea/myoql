@file:JvmName("MyOqlMongo")
@file:JvmMultifileClass

package nbcp.myoql.db.mongo.extend


//import com.mongodb.DBObject
import nbcp.base.db.IdName
import nbcp.base.extend.AsString
import nbcp.base.extend.ConvertType
import nbcp.base.extend.GetActualClass
import nbcp.base.extend.IsSimpleType
import org.bson.BSONObject
import org.bson.Document
import org.springframework.data.mongodb.core.query.Criteria
import java.lang.reflect.ParameterizedType


/**
 *  ( it.name match "a") match_and (it.id match 1)
 */
infix fun Criteria?.linkAnd(to: Criteria): Criteria {
    if (this == null) return to;
    var where = Criteria();

    where.andOperator(this, to)
    return where;
}

/**
 *  ( it.name match "a") match_or (it.id match 1)  match_or (id.age match 18)
 */
infix fun Criteria?.linkOr(to: Criteria): Criteria {
    if (this == null) return to;
    var where = Criteria();

    where.orOperator(this, to)
    return where;
}


fun BSONObject.ReadIdName(): IdName {
    var ret = IdName();
    if (this.containsField("_id")) {
        ret.id = this["_id"].AsString()
    } else if (this.containsField("id")) {
        ret.id = this["id"].AsString()
    }


    if (this.containsField("name")) {
        ret.name = this["name"].AsString()
    }
    return ret;
}

fun <T : Any> Document.ReadAs(value: T): T {
    var ret = value;
    var type = value.javaClass;
    type.declaredFields.forEach {
        var name = it.name;
        it.isAccessible = true;

        if (this.containsKey(name) == false) {
            if (name == "id") {
                name = "_id";
                if (this.containsKey(name) == false) {
                    return@forEach;
                }
            } else {
                return@forEach;
            }
        }

        var fieldValue = this.get(name);
        if (fieldValue == null) return@forEach;

        if (it.type.IsSimpleType()) {
            it.set(ret, fieldValue.ConvertType(it.type));
        } else if (it.type.isArray) {
            var elementType = it.type.componentType;
            if (fieldValue is ArrayList<*>) {
                //List 里有两种情况: DBObject , simpleClass  . BasicDBList 的情况不处理.
                it.set(ret, fieldValue.map {
                    if (it is Document) {
                        it.ReadAs(elementType.newInstance())
                    } else {
                        it.ConvertType(elementType)
                    }
                }.ConvertType(it.type));

            } else {
                return@forEach;
            }
        } else if (Collection::class.java.isAssignableFrom(it.type)) {
            var elementType = (it.genericType as ParameterizedType).GetActualClass(0);
            if (fieldValue is ArrayList<*>) {
                //List 里有两种情况: DBObject , simpleClass  . BasicDBList 的情况不处理.
                it.set(ret, fieldValue.map {
                    if (it is Document) {
                        it.ReadAs(elementType.newInstance())
                    } else {
                        it.ConvertType(elementType)
                    }
                }.ConvertType(it.type));
            }
        } else if (fieldValue is Document) {
            it.set(ret, fieldValue.ReadAs(it.type.newInstance()))
        }
    }
    return ret;
}


//fun Criteria.toDBObject(): BasicDBObject {
//    var ret = BasicDBObject();
//
//    this.criteriaObject.keys.forEach {
//        var value = this.criteriaObject.get(it)
//
//        if (value == null) {
//            ret[it] = null
//        } else if (value is Criteria) {
//            ret[it] = value.toDBObject();
//        } else {
//            ret[it] = value;
//        }
//
//        ret[it] = value;
//    }
//
//    return ret;
//}


fun Criteria.toDocument(): Document {
    return this.criteriaObject;
//    var ret = Document();
//
//    this.criteriaObject.keys.forEach {
//        var value = this.criteriaObject.get(it)
//
//        if (value == null) {
//            ret[it] = null
//        } else if (value is Criteria) {
//            ret[it] = value.toDocument();
//        } else {
//            ret[it] = value;
//        }
//
//        ret[it] = value;
//    }
//
//    return ret;
}


//fun DBObject.toDocument(): Document {
//    var ret = Document();
//
//    this.keySet().forEach {
//        var value = this.get(it);
//
//        if (value is DBObject) {
//            ret[it] = value.toDocument();
//        } else if (value is BsonString) {
//            ret[it] = value.toString()
//        } else {
//            ret[it] = value
//        }
//
//    }
//
//    return ret;
//}



