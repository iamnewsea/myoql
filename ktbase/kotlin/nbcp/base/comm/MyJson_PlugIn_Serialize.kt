package nbcp.base.comm

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import nbcp.base.extend.*



class DateJsonDeserializer : JsonDeserializer<Date>() {
    override fun deserialize(json: JsonParser?, ctxt: DeserializationContext?): Date? {
        if (json == null) {
            return null;
        }

        if (json.currentToken != JsonToken.VALUE_STRING) {
            return null;
        }

        if( json.valueAsString == null){
            return null;
        }

        var stringValue = json.valueAsString
        if (stringValue.contains("-") || stringValue.contains("/")) {
            return stringValue.AsDate();
        }

        return Date(json.longValue);
    }
}


class LocalDateJsonDeserializer : JsonDeserializer<LocalDate>() {
    override fun deserialize(json: JsonParser?, ctxt: DeserializationContext?): LocalDate? {
        if (json == null) {
            return null;
        }

        if (json.currentToken != JsonToken.VALUE_STRING) {
            return null;
        }

        if( json.valueAsString == null){
            return null;
        }

        if (json.valueAsString.contains("-")) {
            return json.valueAsString.AsLocalDate();
        }

        return Date(json.longValue).AsLocalDate();
    }
}


class LocalTimeJsonDeserializer : JsonDeserializer<LocalTime>() {
    override fun deserialize(json: JsonParser?, ctxt: DeserializationContext?): LocalTime? {
        if (json == null) {
            return null;
        }

        if (json.currentToken != JsonToken.VALUE_STRING) {
            return null;
        }

        if( json.valueAsString == null){
            return null;
        }

        return json.valueAsString.AsLocalTime();
    }
}


class LocalDateTimeJsonDeserializer : JsonDeserializer<LocalDateTime>() {
    override fun deserialize(json: JsonParser?, ctxt: DeserializationContext?): LocalDateTime? {
        if (json == null) {
            return null;
        }

        if (json.currentToken != JsonToken.VALUE_STRING) {
            return null;
        }

        if( json.valueAsString == null){
            return null;
        }

        if (json.valueAsString.contains("-") || json.valueAsString.contains("/")) {
            return json.valueAsString.AsLocalDateTime();
        }


        return Date(json.longValue).AsLocalDateTime();
    }
}


class TimestampJsonDeserializer : JsonDeserializer<Timestamp>() {
    override fun deserialize(json: JsonParser?, ctxt: DeserializationContext?): Timestamp? {
        if (json == null) {
            return null;
        }

        if (json.currentToken != JsonToken.VALUE_STRING) {
            return null;
        }

        if( json.valueAsString == null){
            return null;
        }

        if (json.valueAsString.contains("-") || json.valueAsString.contains("/")) {
            return Timestamp.valueOf(json.valueAsString.AsLocalDateTime());
        }

        return Timestamp.valueOf(Date(json.longValue).AsLocalDateTime());
    }
}

class MyStringDeserializer : JsonDeserializer<MyString>() {
    override fun deserialize(json: JsonParser?, p1: DeserializationContext?): MyString? {
        if (json == null) {
            return null;
        }

        if (json.currentToken != JsonToken.VALUE_STRING) {
            return null;
        }

        if( json.valueAsString == null){
            return MyString();
        }
        return MyString(json.valueAsString)
    }
}

class MyRawStringDeserializer : JsonDeserializer<MyRawString>() {
    override fun deserialize(json: JsonParser?, p1: DeserializationContext?): MyRawString? {
        if (json == null) {
            return null;
        }

        if (json.currentToken != JsonToken.VALUE_STRING) {
            return null;
        }

        if( json.valueAsString == null){
            return MyRawString();
        }
        return MyRawString(json.valueAsString)
    }
}