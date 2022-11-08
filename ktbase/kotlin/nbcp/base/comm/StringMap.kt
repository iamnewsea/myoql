package nbcp.base.comm


import org.apache.http.message.BasicNameValuePair

/**
 * Created by udi on 17-4-1.
 */
open class StringMap : StringKeyMap<String> {
    constructor() : super() {
    }

    constructor(data: Map<String, String>) : super(data) {
    }

    constructor(vararg pairs: Pair<String, String>) : super(*pairs) {
    }

    companion object {
    }
}

fun Map<String, *>.ToNameValuePairs(): List<BasicNameValuePair> {
    val list = mutableListOf<BasicNameValuePair>();
    for (i in 0..this.size - 1) {
        list.add(BasicNameValuePair(this.keys.elementAt(i), this.values.elementAt(i).toString()));
    }
    return list;
}