package nbcp.myoql.db.excel

import nbcp.base.comm.DiffData
import nbcp.base.comm.JsonMap
import nbcp.base.extend.AsDouble
import nbcp.base.extend.AsInt
import nbcp.base.extend.AsLocalDateTime
import nbcp.base.extend.AsString
import nbcp.base.utils.MyUtil
import org.apache.poi.ss.util.CellReference
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler
import org.apache.poi.xssf.usermodel.XSSFComment
import org.slf4j.LoggerFactory
import org.xml.sax.XMLReader

class SheetContentReader @JvmOverloads constructor(
    var xmlReader: XMLReader,
    var columns: Array<out String>,
    var filter: ((JsonMap, Map<Int, String>) -> Boolean),
    var sheetColumnsCallback: ((Map<Int, String>) -> Boolean),
    var rowOffset: Int = 0,
    var strictMode: Boolean = true
) : XSSFSheetXMLHandler.SheetContentsHandler {
    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java.declaringClass)
    }

    var currentRowIndex = -1;
    var currentDataRow = linkedMapOf<Int, String>();

    // key: excel 中的 列的索引 , value = column_name
    var columns_index_map = linkedMapOf<Int, String>()
    var row_can_reading = false;

    //    var skipped = 0;
    var header_inited = false;

    init {
    }

    override fun endRow(rowNum: Int) {
        if (row_can_reading == false) return;
        if (header_inited == false) {
            if (sheetColumnsCallback(columns_index_map) == false) {
                throw ReturnException();
            }

            //校验列头.
            if (columns.any() && columns_index_map.size != columns.size) {
                var diff = DiffData.load(columns.toList(), columns_index_map.values.toList(), { a, b -> a == b })
                if (diff.isSame() == false) {
                    if (diff.more1.any()) {
                        if (strictMode) {
                            throw RuntimeException("发现缺失列: " + diff.more1.joinToString(","));
                        } else {
                            logger.warn("发现缺失列: " + diff.more1.joinToString(","))
                        }
                    } else if (diff.more2.any()) {
                        if (strictMode) {
                            throw RuntimeException("发现多余列: " + diff.more2.joinToString(","));
                        } else {
                            logger.warn("发现多余列: " + diff.more2.joinToString(","))
                        }
                    }
                }
            }

            header_inited = true;
            return;
        }

        //按 columns 排序.
//            var values = columns.map { column -> columns_index_map.filterValues { it == column }.map { it.key }.first() }
//                    .map { columnIndex -> currentDataRow.get(columnIndex) }

//            container.rows.add(values.toTypedArray());

        var row = JsonMap();

        columns.forEach { column ->
            var columnIndex = columns_index_map.filterValues { it == column }.map { it.key }.firstOrNull()
            if (columnIndex == null) {
                return@forEach
            }
            var value = currentDataRow.getOrDefault(columnIndex, "")
            row.set(column, value);
        }


        //
        if (filter.invoke(row, currentDataRow) == false) {
            throw ReturnException();
        }
    }

    override fun startRow(rowNum: Int) {
        currentRowIndex = rowNum;
        row_can_reading = false;
        if (currentRowIndex < rowOffset) {
            return
        }

//        if (skipped < skip) {
//            skipped++;
//            return;
//        }

        row_can_reading = true;
        currentDataRow = linkedMapOf();
    }

    override fun cell(cellReference: String, formattedValue: String, comment: XSSFComment?) {
        if (row_can_reading == false) return;
        var text = formattedValue.trim();

        val columnIndex = CellReference(cellReference).getCol().toInt()
        if (header_inited == false) {
            if (text.isEmpty()) {
                return;
            }

            columns_index_map.set(columnIndex, text);
            return;
        }

        if (columns_index_map.containsKey(columnIndex) == false) {
            return;
        }

        var handler = (xmlReader.contentHandler as XSSFSheetXMLHandler)


        var formatIndex = MyUtil.getValueByWbsPath(handler, "formatIndex").AsInt()
        var formatString = MyUtil.getValueByWbsPath(handler, "formatString").AsString()
        var nextDataType = MyUtil.getValueByWbsPath(handler, "nextDataType").AsString()
        var value = MyUtil.getValueByWbsPath(handler, "value").AsDouble()
        //[$-F400]h:mm:ss\ AM/PM = 3:20:39 下午

        if (value >= 0 && nextDataType == "NUMBER" && org.apache.poi.ss.usermodel.DateUtil.isADateFormat(
                formatIndex,
                formatString
            )
        ) {
            currentDataRow.set(
                columnIndex,
                org.apache.poi.ss.usermodel.DateUtil.getJavaDate(value).AsLocalDateTime().AsString()
            );
        } else {
            currentDataRow.set(columnIndex, text);
        }
    }
}