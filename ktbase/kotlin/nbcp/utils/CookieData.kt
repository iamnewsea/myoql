package nbcp.utils

import nbcp.comm.*
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 *
 */
class CookieData {

    var name = ""
    var value = ""
    var expires: LocalDateTime? = null
    var maxAge: Int? = null
    var path = "";
    var domain = "";
    var httpOnly: Boolean? = null
    var secure: Boolean? = null


    override fun toString(): String {
        var ret = mutableListOf<String>();
        ret.add("${name}=${value}")
        ret.add("Domain=${domain}")
        ret.add("Path=${path}")
        if (expires != null) {
            ret.add("Expires=${expires!!.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.RFC_1123_DATE_TIME)}")
        }
        if (maxAge != null) {
            ret.add("Max-Age=${maxAge}")
        }
        if (secure == true) {
            ret.add("Secure")
        }
        if (httpOnly == true) {
            ret.add("HttpOnly")
        }

        return ret.joinToString("; ")
    }

    companion object {
        fun parse(value: String): List<CookieData> {
            var ret = mutableListOf<CookieData>();

            value.cutWith {
                if (it.sleep && it.item == ';') {
                    it.sleep = false;
                } else if (it.item == '=') it.sleep = true;


                if (it.sleep) {
                    return@cutWith false;
                }

                if (it.item == ',') {
                    return@cutWith true;
                }

                return@cutWith false;
            }.forEach {
                var c = CookieData();

                it.split(";").map { it.trim() }.forEach {
                    if (it basicSame "httpOnly") {
                        c.httpOnly = true;
                    } else if (it basicSame "secure") {
                        c.secure = true;
                    } else if (it.contains("=")) {
                        var sects = it.split("=").map { it.trim() };
                        var key = sects[0];
                        var itemValue = "";
                        if (sects.size > 1) {
                            itemValue = sects[1]
                        }

                        if (key basicSame "path") {
                            c.path = itemValue;
                        } else if (key basicSame "Expires") {
                            c.expires = itemValue.AsLocalDateTime()
                        } else if (key basicSame "max-age") {
                            c.maxAge = itemValue.AsInt();
                        } else if (key basicSame "version") {

                        } else if (key basicSame "Comment") {

                        } else {
                            c.name = key;
                            c.value = itemValue;
                        }
                    }
                }
                ret.add(c)
            }

            return ret;
        }
    }
}