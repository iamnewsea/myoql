@file:JvmName("MyHelper")
@file:JvmMultifileClass

package nbcp.base.extend

import java.lang.reflect.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.Temporal
import java.util.*


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
 * UUID,ObjectId
 * clazzesIsSimpleDefine 中的类型
 */
fun Class<*>.IsSimpleType(): Boolean {
    if (this.isPrimitive) return true;
    if (this.isEnum) return true;

    if (this.IsNumberType) {
        return true;
    }

    if (this.IsStringType) return true;
    if (this == java.lang.Character::class.java) return true;
    if (this == java.lang.Boolean::class.java) return true;
    if (this.IsAnyDateOrTimeType) return true;
    if (this == UUID::class.java) return true;

    if (this.name == "org.bson.types.ObjectId") {
        return true;
    }
    if (clazzesIsSimpleDefine.any { it.isAssignableFrom(this) }) {
        return true;
    }
    return false;
}

/**
 * 代码生成器使用，获取Java的类型名称
 */
val Class<*>.javaTypeName: String
    get() {

        if (this.isArray) {
            if (this.componentType.isPrimitive) {
                var name = this.componentType.simpleName;
                return name.first().uppercaseChar() + name.substring(1) + "[]";
            }
        }

        return this.simpleName;
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
                return name.first().uppercaseChar() + name.substring(1) + "Array";
            }
        }

        if (this.isPrimitive) {
            var name = this.simpleName;
            return name.first().uppercaseChar() + name.substring(1);
        }
        return this.simpleName;
    }

/**
 * 是否是类型 (不能判断接口，不区分大小写。)
 */
fun Class<*>.IsType(value: String): Boolean {
    if (this.name basicSame value || this.simpleName basicSame value) return true;


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

val Class<*>.IsAnyDateOrTimeType: Boolean
    get() {
        if (this == LocalDate::class.java) return true;
        if (this == LocalDateTime::class.java) return true;
        if (this == LocalTime::class.java) return true;
        if (Date::class.java.isAssignableFrom(this)) return true;
        if (Calendar::class.java.isAssignableFrom(this)) return true;
        if (Temporal::class.java.isAssignableFrom(this)) return true;

        return false;
    }

/**
 * 类型是否是Collection, List,Set都是。
 */
val Class<*>.IsCollectionType: Boolean
    get() {
        return Collection::class.java.isAssignableFrom(this)
    }


fun Field.IsCollectionType(type: String): Boolean {
    var field = this;
    return field.type.IsCollectionType &&
            (field.genericType as ParameterizedType).GetActualClass(
                    0
            ).IsType(type)

}

/**
 * 是否是 List枚举
 */
val Field.IsCollectionEnum: Boolean
    get() {
        val field = this;
        return (field.type.IsCollectionType && (field.genericType as ParameterizedType).GetActualClass(0).isEnum) ||
                (field.type.isArray && field.type.componentType.javaClass.isEnum)
    }


fun Field.IsArrayType(type: String): Boolean {
    var field = this;
    return field.type.isArray && field.type.componentType.javaClass.IsType(type)
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

    var list = this.enumConstants

    if (values.HasValue) {
        return values.split(",")
                .map { v -> list.find { it.toString() == v } }
                .filterNotNull()
    }

    return list.toList()
}

/**
 * 获取枚举类的数字类型的字段。
 */
fun <T> Class<T>.GetEnumNumberField(): Field? {
    if (this.isEnum == false) return null


    this.declaredFields
            .filter {
                it.IsPrivate && it.IsStatic && it.type.IsNumberType
            }
            .let { ret_fields ->
                if (ret_fields.size == 1) {
                    var ret = ret_fields.first();
                    ret.isAccessible = true;
                    return ret;
                }
            }


    return null;
}

val Method.IsPrivate: Boolean
    get() {
        return Modifier.isPrivate(this.modifiers)
    }

val Method.IsPublic: Boolean
    get() {
        return Modifier.isPublic(this.modifiers)
    }

val Method.IsStatic: Boolean
    get() {
        return Modifier.isStatic(this.modifiers)
    }

val Method.IsTransient: Boolean
    get() {
        return Modifier.isTransient(this.modifiers)
    }


val Field.IsStatic: Boolean
    get() {
        return Modifier.isStatic(this.modifiers)
    }

val Field.IsTransient: Boolean
    get() {
        return Modifier.isTransient(this.modifiers)
    }

val Field.IsPrivate: Boolean
    get() {
        return Modifier.isPrivate(this.modifiers)
    }

val Field.IsPublic: Boolean
    get() {
        return Modifier.isPublic(this.modifiers)
    }

/**
 * 获取枚举类的第一个String类型的字段。
 */
fun <T> Class<T>.GetEnumStringField(): Field? {
    if (this.isEnum == false) return null


    this.declaredFields
            .filter {
                it.IsPrivate && it.IsStatic && it.type.IsStringType
            }
            .let { ret_fields ->
                if (ret_fields.any()) {
                    var ret = ret_fields.first();
                    ret.isAccessible = true;
                    return ret;
                }
            }


    return null;
}

private fun getPropertySetMethod(methods: List<Method>, fieldType: Class<*>, methodName: String): Method? {
    return methods.filter { it.name == methodName }
            .filter { it.parameterCount == 1 && it.parameters.first().type == fieldType }
            .firstOrNull()
}


private fun Class<*>.getPropertyGetMethods(): List<Method> {
    var methods = this.methods.filter {
        if (it.IsStatic) return@filter false;
        if (it.IsPrivate) return@filter false;
        if (it.IsTransient) return@filter false;

        return@filter it.name.startsWith("is") || it.name.startsWith("get")
    }

    return methods.filter {
        if (it.parameters.any()) return@filter false;
        if (it.name.startsWith("get")) {
            //忽略中文属性
            if (it.name.length > 3 && !it.name[3].isUpperCase()) {
                return@filter false;
            }

            return@filter true
        }

        if (it.returnType.IsBooleanType) {
            if (it.name.startsWith("is")) {
                //忽略中文属性
                if (it.name.length > 2 && !it.name[2].isUpperCase()) {
                    return@filter false;
                }
                return@filter true
            }

            return@filter false;
        }
        return@filter false;
    }
}

/**
 * 仅返回  getMethod / isMethod
 */
val Class<*>.AllGetPropertyMethods: List<Method>
    get() {
        var ret = mutableListOf<Method>();
        if (this.IsSimpleType()) return ret;

        ret.addAll(this.getPropertyGetMethods())


        if (LinkedHashMap::class.java.isAssignableFrom(this)) {
            var baseMethods = LinkedHashMap::class.java.getPropertyGetMethods().map { it.name };
            ret.removeAll { it.name.IsIn(baseMethods) }
        } else if (HashMap::class.java.isAssignableFrom(this)) {
            var baseMethods = HashMap::class.java.getPropertyGetMethods().map { it.name };
            ret.removeAll { it.name.IsIn(baseMethods) }
        } else if (Hashtable::class.java.isAssignableFrom(this)) {
            var baseMethods = Hashtable::class.java.getPropertyGetMethods().map { it.name };
            ret.removeAll { it.name.IsIn(baseMethods) }
        } else if (List::class.java.isAssignableFrom(this)) {
            var baseMethods = List::class.java.getPropertyGetMethods().map { it.name };
            ret.removeAll { it.name.IsIn(baseMethods) }
        }

        return ret;
    }

/** 获取该类以及基类的所有字段。 并设置为可写。
 * 如果父类与子类有相同的字段，返回子类字段。
 * 移除 IsTransient 非序列化字段
 */
val Class<*>.AllFields: List<Field>
    get() {
        var ret = mutableListOf<Field>();
        if (this.IsSimpleType()) return ret;

        if (
                this.isArray ||
                this.IsCollectionType ||
                this.IsMapType
        ) {
            return ret;
        }

        //如果是Map

        var fields = this.declaredFields
                .filter {
                    if (it.IsStatic) return@filter false;
                    if (it.IsTransient) return@filter false;

                    it.isAccessible = true;

                    return@filter true
                };

        ret.addAll(fields);

        if (this.superclass == null || this.superclass == Any::class.java) {
            return ret;
        }

        var allKeys = ret.map { it.name };
        ret.addAll(this.superclass.AllFields.filter { it.name.IsIn(allKeys) == false });
        return ret;
    }

/**
 * 一直向父类查找。
 * Jdk17,在反射非导出包的时候，要添加 --add-opens java.base/sun.reflect.annotation=ALL-UNNAMED
 */
fun Class<*>.FindField(fieldName: String, ignoreCase: Boolean = false): Field? {
    val ret = this.declaredFields.find { it.name.compareTo(fieldName, ignoreCase) == 0 };
    if (ret != null) {
        ret.isAccessible = true;
        return ret;
    }


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
    this.declaredFields.find {
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
 * 向上查找任意满足的类或接口。
 */
fun Class<*>.AnySuperClass(filter: (Class<*>) -> Boolean): Boolean {
    if (filter(this)) return true;
    val intes = this.interfaces;
    if (intes.any()) {
        if (intes.any { filter(it) }) return true;
    }
    var superClass = this.superclass;
    if (superClass == null) return false;
    return superclass.AnySuperClass(filter);
}

/**
 * 按路径找。
 */
fun Class<*>.GetFieldPath(vararg fieldNames: String): Field? {
//    var ret: Field? = null
    var fieldName = fieldNames.first();

    var field = this.FindField(fieldName);
    if (field == null) return null;

    if (fieldNames.size == 1) return field;

    return field.type.GetFieldPath(*fieldNames.ArraySlice(1).toTypedArray());
}

/**
 * 获取泛型参数的实际类型，兼容枚举类型
 */
fun ParameterizedType.GetActualClass(index: Int, callback: (() -> Class<*>?)? = null): Class<*> {
    var a1 = this.actualTypeArguments[index];
    if (a1 is Class<*>) {
        return a1
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

// todo 这个方法之后应该去除。
fun Class<*>.GetFirstTypeArguments(): Array<Type> {
    if (this.genericSuperclass is ParameterizedType) {
        return (this.genericSuperclass as ParameterizedType).actualTypeArguments
    }
    if (this.superclass == null) return arrayOf()
    return this.superclass.GetFirstTypeArguments();
}


/**
 * 类型是否是泛型
 */
val Class<*>.IsGenericType: Boolean
    get() {
        return this.typeParameters.size > 0;
    }

/**
 * 获取方法参数名
 */
fun Method.getParameterNames(): List<String> {
    return this.parameters.map { it.name }

    //还有： LocalVariableTableParameterNameDiscoverer().getParameterNames(method)
}