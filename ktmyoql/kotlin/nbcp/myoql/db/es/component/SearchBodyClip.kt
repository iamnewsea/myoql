package nbcp.db.es

import nbcp.base.comm.*
import nbcp.db.es.component.EsQueryData

/**
 * https://www.elastic.co/guide/en/elasticsearch/reference/7.6/search-search.html
 */
class SearchBodyClip {
    var skip = 0L;
    var take = -1;
    /*
"query": {
	"bool": {
		"should": [{
				"term": {
					"title": "中国"
				}
			},
			{
				"term": {
					"content": "中国"
				}
			}
		]
	}
},
     */
    var query = EsQueryData()

    /*
"filter": {
	"range": {
		"publish_date": {
			"from": "2010/07/01",
			"to": "2010/07/21",
			"include_lower": true,
			"include_upper": false
		}
	}
}
     */
    val filter = JsonMap()


    /**
     * _source
     */
    val _source = mutableListOf<String>()

    /*
"sort": [{
		"publish_date": {
			"order": "asc"
		}
	},
	"_score"
],
     */
    val sort = mutableListOf<JsonMap>()


    /*
"highlight": {
	"pre_tags": [
		"<tag1>",
		"<tag2>"
	],
	"post_tags": [
		"</tag1>",
		"</tag2>"
	],
	"fields": {
		"title": {},
		"content": {}
	}
},
     */
    val highlight = JsonMap()

    /*
"facets": {
	"cate": {
		"terms": {
			"field": "category"
		}
	}
}
     */
    val facets = JsonMap();

    /*
"aggs":{
  "avg_fees":{"avg":{"field":"fees"}}
}
     */
    var aggs: JsonMap = JsonMap()

    override fun toString(): String {
        val json = JsonMap();
        if (this.skip > 0) {
            json["from"] = this.skip
        }
        if (this.take > 0) {
            json["size"] = this.take
        }
        if (this.query.hasValue()) {
            json["query"] = this.query
        }


        if (this.filter.isNotEmpty()) {
            json["filter"] = this.filter
        }
        if (this._source.isNotEmpty()) {
            json["_source"] = this._source
        }
        if (this.sort.isNotEmpty()) {
            json["sort"] = this.sort
        }
        if (this.highlight.isNotEmpty()) {
            json["highlight"] = this.highlight
        }
        if (this.facets.isNotEmpty()) {
            json["facets"] = this.facets
        }
        if (this.aggs.isNotEmpty()) {
            json["aggs"] = this.aggs
        }

        return json.ToJson()
    }

    fun isEmpty(): Boolean {
        return false;
    }
}