@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.comm

import java.lang.RuntimeException
import java.lang.reflect.*

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
 * 代码生成器使用，获取Kotlin的类型名称
 */
val Class<*>.kotlinTypeName: String
    get() {
        if (this == Integer::class.java) return "Int"
        if (this == Object::class.java) return "Any"

        if (this.isArray) {
            if (this.componentType.isPrimitive) {
                var name = this.componentType.simpleName;
                return name.first().toUpperCase() + name.substring(1) + "Array";
            }
        }

        if (this.isPrimitive) {
            var name = this.simpleName;
            return name.first().toUpperCase() + name.substring(1);
        }
        return this.simpleName;
    }

/**
 * 是否是类型
 */
fun Class<*>.IsType(value: String): Boolean {
    if (this.name == value || this.simpleName == value) return true;

    if (this.superclass != null) {
        return this.superclass.IsType(value)
    }
    return false;
}

/**
 * 类型是否是布尔： boolean,java.lang.Boolean
 */
val Class<*>.IsBooleanType: Boolean
    get() {
        if (this == Boolean::class.java) return true;
        if (this == java.lang.Boolean::class.java) return true;
        return false;
    }

/**
 * 类型是否是Collection, List,Set都是。
 */
val Class<*>.IsCollectionType: Boolean
    get() {
        return Collection::class.java.isAssignableFrom(this)
    }

/**
 * 类型是否是 Map
 */
val Class<*>.IsMapType: Boolean
    get() {
        return Map::class.java.isAssignableFrom(this)
    }

/**
 * 类型是否是字符串：String,MyString
 */
val Class<*>.IsStringType: Boolean
    get() {
        return CharSequence::class.java.isAssignableFrom(this) || this == java.lang.String::class.java
    }

/**
 * 类型是否是数字：int,float,double,long,short,byte,Number
 */
val Class<*>.IsNumberType: Boolean
    get() {
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
@JvmOverloads
fun <T> Class<T>.GetEnumList(values: String = ""): List<T> {
    if (this.isEnum == false) return listOf()

    if (values.HasValue) {
        return values.split(",").filter { it.HasValue }.map { it.ToEnum(this)!! }
    }

    var values = this.getDeclaredField("\$VALUES");
    values.isAccessible = true;
    return (values.get(null) as Array<T>).toList();
}

/**
 * 获取枚举类的数字类型的字段。
 */
fun <T> Class<T>.GetEnumNumberField(): Field? {
    if (this.isEnum == false) return null


    var ret_fields = this.declaredFields.filter {
        (it.modifiers and Modifier.PRIVATE) > 0 &&
                (it.modifiers and Modifier.STATIC == 0) &&
                it.type.IsNumberType
    }
    if (ret_fields.size == 1) {
        var ret = ret_fields.first();
        ret.isAccessible = true;
        return ret;
    }
    return null;
}

/**
 * 获取枚举类的第一个String类型的字段。
 */
fun <T> Class<T>.GetEnumStringField(): Field? {
    if (this.isEnum == false) return null


    var ret_fields = this.declaredFields.filter {
        (it.modifiers and Modifier.PRIVATE) > 0 &&
                it.modifiers and Modifier.STATIC == 0 &&
                it.type.IsStringType
    }

    if (ret_fields.any()) {
        var ret = ret_fields.first();
        ret.isAccessible = true;
        return ret;
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
 * 递归向父类查找字段。返回 false 停止递归
 */
fun Class<*>.ForEachField(fieldCallback: (Field) -> Boolean) {
    var callbackValue = true;
    var ret: Field? = null
    ret = this.declaredFields.find {
        callbackValue = fieldCallback(it);
        return@find callbackValue
    }

    if (callbackValue == false) return;


    if (this.superclass == null || this.superclass == Any::class.java) {
        return;
    }

    this.superclass.ForEachField(fieldCallback);
}


/**
 * 向上查找任意满足的类。
 */
fun Class<*>.AnySuperClass(filter: (Class<*>) -> Boolean): Boolean {
    if (filter(this)) return true;
    var superClass = this.superclass;
    if (superClass == null) return false;
    return superclass.AnySuperClass(filter);
}

/**
 * 按路径找。
 */
fun Class<*>.GetFieldPath(vararg fieldNames: String): Field? {
    var ret: Field? = null
    var fieldName = fieldNames.first();

    var field = this.FindField(fieldName);
    if (field == null) return null;

    if (fieldNames.size == 1) return field;

    return field.type.GetFieldPath(*fieldNames.Slice(1).toTypedArray());
}

/**
 * 获取泛型参数的实际类型，兼容枚举类型
 */
fun ParameterizedType.GetActualClass(index: Int, callback: (() -> Class<*>?)? = null): Class<*> {
    var a1 = this.actualTypeArguments[index];
    if (a1 is Class<*>) {
        return a1 as Class<*>
    } else if (a1 is WildcardType) {
        //类型是 List<枚举> 时，返回
        return a1.upperBounds[0] as Class<*>
    }
    var ret = callback?.invoke();
    if (ret == null) {
        throw RuntimeException("不识别的类型:${a1.typeName}")
    }
    return ret;
}

fun Class<*>.GetFirstTypeArguments(): Array<Type> {
    if (this.genericSuperclass is ParameterizedType) {
        return (this.genericSuperclass as ParameterizedType).actualTypeArguments
    }
    if (this.superclass == null) return arrayOf()
    return this.superclass.GetFirstTypeArguments();
}
