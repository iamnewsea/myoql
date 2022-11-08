package nbcp.myoql.db.es.base


/*
insert 返回结果：
{
	"took": 0,
	"errors": true,
	"items": [{
		"create": {
			"_index": "nginx",
			"_type": "_doc",
			"_id": "5a8rxp2ri6f4",
			"status": 400,
			"error": {
				"type": "mapper_parsing_exception",
				"reason": "failed to parse field [createAt] of type [date] in document with id '5a8rxp2ri6f4'. Preview of field's value: '2021/01/07 20:50:36'",
				"caused_by": {
					"type": "illegal_argument_exception",
					"reason": "failed to parse date field [2021/01/07 20:50:36] with format [strict_date_optional_time||epoch_millis]",
					"caused_by": {
						"type": "date_time_parse_exception",
						"reason": "Failed to parse with all enclosed parsers"
					}
				}
			}
		}
	}]
}
 */
data class EsResultMsg @JvmOverloads constructor(
    var error: Boolean = true,
    var took: Int = 0,
    var action: String = "", //create
    var index: String = "",  // _index
    var id: String = "",     // _id
    var status: Int = 0,    //status
    var type: String = "",   //type
    var msg: String = "" //第一条错误信息, reason
)