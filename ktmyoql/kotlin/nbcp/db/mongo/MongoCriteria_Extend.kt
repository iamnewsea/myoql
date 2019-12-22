package nbcp.db.mongo

import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import nbcp.base.extend.*
import nbcp.db.IdName
import org.bson.BSONObject
import org.bson.BsonString
import org.bson.Document
import org.springframework.data.mongodb.core.query.Criteria
import java.lang.reflect.ParameterizedType


/**
 *  ( it.name match "a") match_and (it.id match 1)
 */
infix fun Criteria?.match_and(to: Criteria): Criteria {
    if (this == null) return to;
    var where = Criteria();

    where.andOperator(this, to)
    return where;
}

/**
 *  ( it.name match "a") match_or (it.id match 1)  match_or (id.age match 18)
 */
infix fun Criteria?.match_or(to: Criteria): Criteria {
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
    var clazz = value.javaClass;
    clazz.declaredFields.forEach {
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
            var elementType = (it.genericType as ParameterizedType).actualTypeArguments[0] as Class<*>
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

/**
 * 根据路径查找值.
 */
fun Document.GetComplexPropertyValue(property: String): String {
    var sects = property.split(".");

    if (sects.size == 0) return "";


    return this.GetComplexPropertyValue(*sects.toTypedArray()).AsString()
}

/**
 * 根据路径查找值.
 */
fun Document.GetComplexPropertyValue(vararg eachProperty: String): Any? {
    if (eachProperty.size == 0) return null;

    var key = eachProperty[0];

    if (key == "id") {
        key = "_id";
    }

    var retVal: Any? = null;
    var aryIndex = key.indexOf('[');

    if (aryIndex > 0) {
        var aryPropertys = key.Slice(aryIndex).Slice(1, -1).split("][").map { it.AsInt() };
        key = key.Slice(0, aryIndex);

        var aryValue = (this.get(key) as ArrayList<*>);

        aryPropertys.take(aryPropertys.size - 1).forEach {
            aryValue = aryValue[it] as ArrayList<*>;
        }

        retVal = aryValue[aryPropertys.last()]
    } else {
        retVal = this.get(key);
    }

    if (eachProperty.size == 1) {
        return retVal;
    }

    return (retVal as Document).GetComplexPropertyValue(*eachProperty.takeLast(eachProperty.size - 1).toTypedArray());
}


fun Criteria.toDBObject(): BasicDBObject {
    var ret = BasicDBObject();

    this.criteriaObject.keys.forEach {
        var value = this.criteriaObject.get(it)

        if (value == null) {
            ret[it] = null
        } else if (value is Criteria) {
            ret[it] = value.toDBObject();
        } else {
            ret[it] = value;
        }

        ret[it] = value;
    }

    return ret;
}


fun Criteria.toDocument(): Document {
    var ret = Document();

    this.criteriaObject.keys.forEach {
        var value = this.criteriaObject.get(it)

        if (value == null) {
            ret[it] = null
        } else if (value is Criteria) {
            ret[it] = value.toDocument();
        } else {
            ret[it] = value;
        }

        ret[it] = value;
    }

    return ret;
}


fun DBObject.toDocument(): Document {
    var ret = Document();

    this.keySet().forEach {
        var value = this.get(it);

        if (value is DBObject) {
            ret[it] = value.toDocument();
        } else if (value is BsonString)
            ret[it] = value;
    }

    return ret;
}



