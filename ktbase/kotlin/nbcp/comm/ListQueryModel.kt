package nbcp.comm


/**
 * Created by jin on 2017/3/16.
 */


open class ListQueryModel {
    var skip: Int = 0;
    var take: Int = -1;
    var sorts = mutableListOf<SortQueryModel>()

    val firstSort: SortQueryModel
        get() = sorts.firstOrNull() ?: SortQueryModel()
}
