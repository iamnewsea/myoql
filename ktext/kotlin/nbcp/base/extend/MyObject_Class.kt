package nbcp.comm

import java.lang.RuntimeException
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.WildcardType

val clazzesIsSimpleDefine = mutableSetOf<Class<*>>()

/**
 * 判断是否是简单类型：
 * 基元类型
 * 枚举类型
 * 字符串,MyString
 * 字符
 * 布尔
 * 数字
 * LocalDate,LocalTime,LocalDateTime,Date.
 * clazzesIsSimpleDefine 中的类型
 */
fun Class<*>.IsSimpleType(): Boolean {
    if (this.isPrimitive) return true;
    if (this.isEnum) return true;

    if (Number::class.java.isAssignableFrom(this)) {
        return true;
    }

    if (CharSequence::class.java.isAssignableFrom(this)) {
        return true;
    }

    if (this == java.lang.String::class.java) return true;
    if (this == java.lang.Character::class.java) return true;
    if (this == java.lang.Boolean::class.java) return true;
    if (this == java.time.LocalDate::class.java) return true;
    if (this == java.time.LocalTime::class.java) return true;
    if (this == java.time.LocalDateTime::class.java) return true;
    if (this == java.util.Date::class.java) return true;
//    if (this.name == "org.bson.types.ObjectId") {
//        return true;
//    }
    if (clazzesIsSimpleDefine.any { it.isAssignableFrom(this) }) {
        return true;
    }
    return false;
}

/**
 * 类型是否是布尔： boolean,java.lang.Boolean
 */
fun Class<*>.IsBooleanType(): Boolean {
    if (this == Boolean::class.java) return true;
    if (this == java.lang.Boolean::class.java) return true;
    return false;
}

/**
 * 类型是否是List： Collection
 */
fun Class<*>.IsListType(): Boolean {
    return Collection::class.java.isAssignableFrom(this)
}

/**
 * 类型是否是字符串：String,MyString
 */
fun Class<*>.IsStringType(): Boolean {
    return CharSequence::class.java.isAssignableFrom(this) || this == java.lang.String::class.java
}

/**
 * 类型是否是数字：int,float,double,long,short,byte,Number
 */
fun Class<*>.IsNumberType(): Boolean {
    if (this.isPrimitive) {
        if (this == Int::class.java) return true;
        if (this == Float::class.java) return true;
        if (this == Double::class.java) return true;
        if (this == Long::class.java) return true;
        if (this == Short::class.java) return true;
        if (this == Byte::class.java) return true;
    }

    if (Number::class.java.isAssignableFrom(this)) {
        return true;
    }

    return false;
}

/**
 * 获取枚举类的所有成员
 */
fun <T> Class<T>.GetEnumList(): List<T> {
    if (this.isEnum == false) return listOf()


    var values = this.getDeclaredField("\$VALUES");
    values.isAccessible = true;
    return (values.get(null) as Array<T>).toList();
}

/**
 * 获取枚举类的数字类型的字段。
 */
fun <T> Class<T>.GetEnumNumberField(): Field? {
    if (this.isEnum == false) return null


    var ret = this.declaredFields.filter {
        it.modifiers and Modifier.PRIVATE > 0 &&
                (it.modifiers and Modifier.STATIC == 0) &&
                it.type.IsNumberType()
    }
    if (ret.size == 1) {
        return ret.firstOrNull();
    }
    return null;
}

/**
 * 获取枚举类的String类型的字段。
 */
fun <T> Class<T>.GetEnumStringField(): Field? {
    if (this.isEnum == false) return null


    var ret = this.declaredFields.filter {
        it.modifiers and Modifier.PRIVATE == Modifier.PRIVATE &&
                it.modifiers and Modifier.STATIC == 0 &&
                it.type.IsStringType()
    }
    if (ret.size == 1) {
        return ret.firstOrNull();
    }
    return null;
}


/** 获取该类以及基类的所有字段。 并设置为可写。
 * 如果父类与子类有相同的字段，返回子类字段。
 */
val Class<*>.AllFields: List<Field>
    get() {
        var ret = mutableListOf<Field>();

        var fields = this.declaredFields.filter {
            if (it.modifiers and Modifier.STATIC > 0) return@filter false;
            if (it.modifiers and Modifier.TRANSIENT > 0) return@filter false;
            true
        };

        fields.forEach {
            it.isAccessible = true;
        }

        ret.addAll(fields);

        if (this.superclass == null || this.superclass == Any::class.java) {
            return ret;
        }
        ret.addAll(this.superclass.AllFields.filter { it.name.IsIn(ret.map { it.name }) == false });
        return ret;
    }

/**
 * 递归向父类查找字段。
 */
fun Class<*>.FindField(fieldName: String): Field? {
    var ret: Field? = null
    ret = this.declaredFields.find { it.name == fieldName };
    if (ret != null) return ret;


    if (this.superclass == null || this.superclass == Any::class.java) {
        return null;
    }

    return this.superclass.FindField(fieldName);
}

/**
 * 获取泛型参数的实际类型，兼容枚举类型
 */
fun ParameterizedType.GetActualClass(index: Int): Class<*> {
    var a1 = this.actualTypeArguments[index];
    if (a1 is Class<*>) {
        return a1 as Class<*>
    } else if (a1 is WildcardType) {
        //类型是 List<枚举> 时，返回
        return a1.upperBounds[0] as Class<*>
    }
    throw RuntimeException("不识别的类型:${a1.typeName}")
}