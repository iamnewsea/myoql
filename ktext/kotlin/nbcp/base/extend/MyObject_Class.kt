package nbcp.base.extend

import java.lang.reflect.Field
import java.lang.reflect.Modifier


fun Class<*>.IsSimpleType(): Boolean {
    if (this.isPrimitive) return true;
    if (this.isEnum) return true;
    if (this.name == "java.lang.String") return true;
    if (this.name == "java.lang.Character") return true;
    if (this.name == "java.lang.Boolean") return true;

    if (Number::class.java.isAssignableFrom(this)) {
        return true;
    }

    if (this.name == "java.time.LocalDate") return true;
    if (this.name == "java.time.LocalTime") return true;
    if (this.name == "java.time.LocalDateTime") return true;
    if (this.name == "java.util.Date") return true;
    if (this.name == "org.bson.types.ObjectId") {
        return true;
    }
    return false;
}

fun Class<*>.IsBooleanType(): Boolean {
    if (this.name == "java.lang.Boolean") return true;
    if (this.name == "boolean") return true;
    return false;
}

fun Class<*>.IsListType(): Boolean {
    return Collection::class.java.isAssignableFrom(this)
}

fun Class<*>.IsStringType(): Boolean {
    return this.name == "java.lang.String" || MyString::class.java.isAssignableFrom(this)
}

fun Class<*>.IsNumberType(): Boolean {
    if (this.isPrimitive) {
        if (this.name == "int") return true;
        if (this.name == "float") return true;
        if (this.name == "double") return true;
        if (this.name == "long") return true;
        if (this.name == "short") return true;
        if (this.name == "byte") return true;
    }

    if (Number::class.java.isAssignableFrom(this)) {
        return true;
    }

    return false;
}

fun <T> Class<T>.GetEnumList(): List<T> {
    if (this.isEnum == false) return listOf()


    var values = this.getDeclaredField("\$VALUES");
    values.isAccessible = true;
    return (values.get(null) as Array<T>).toList();
}

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


//如果父类与子类有相同的字段，返回子类字段。
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

//所有可写的字段。
fun Class<*>.FindField(fieldName: String): Field? {
    var ret: Field? = null
    ret = this.declaredFields.find { it.name == fieldName };
    if (ret != null) return ret;


    if (this.superclass == null || this.superclass == Any::class.java) {
        return null;
    }

    return this.superclass.FindField(fieldName);
}

