package nbcp.myoql.db.excel

import nbcp.base.extend.AsDouble
import nbcp.base.extend.AsInt
import nbcp.base.extend.AsLocalDateTime
import nbcp.base.extend.AsString
import nbcp.base.utils.MyUtil
import org.apache.poi.ss.util.CellReference
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler
import org.apache.poi.xssf.usermodel.XSSFComment
import org.xml.sax.XMLReader

class SheetContentRowArrayDataReader(
        var xmlReader: XMLReader,
        var offset_row: Int,
        var filter: ((Array<String>) -> Boolean)) : XSSFSheetXMLHandler.SheetContentsHandler {
    var currentRowIndex = -1;
    var currentDataRow = linkedMapOf<Int, String>()
    var row_can_reading = false;
    var skipped = 0;

    init {
    }

    override fun endRow(rowNum: Int) {
        if (row_can_reading == false) return;

        var ret = mutableListOf<String>()
        var maxLength = currentDataRow.keys.maxOrNull() ?: -1

        for( i in 0..maxLength){
            ret.add( currentDataRow[i] ?: "" )
        }

        if (filter.invoke(ret.toTypedArray()) == false) {
            throw ReturnException();
        }
    }

    override fun startRow(rowNum: Int) {
        currentRowIndex = rowNum;
        row_can_reading = false;
        if (currentRowIndex < offset_row) {
            return
        }

        row_can_reading = true;
        currentDataRow = linkedMapOf()
    }

    override fun cell(cellReference: String, formattedValue: String, comment: XSSFComment?) {
        if (row_can_reading == false) return;
        var text = formattedValue.trim();

        val columnIndex = CellReference(cellReference).getCol().toInt()


        var handler = (xmlReader.contentHandler as XSSFSheetXMLHandler)


        var formatIndex = MyUtil.getValueByWbsPath(handler, "formatIndex").AsInt()
        var formatString = MyUtil.getValueByWbsPath(handler, "formatString").AsString()
        var nextDataType = MyUtil.getValueByWbsPath(handler, "nextDataType").AsString()
        var value = MyUtil.getValueByWbsPath(handler, "value").AsDouble()
        //[$-F400]h:mm:ss\ AM/PM = 3:20:39 下午

        if (value >= 0 && nextDataType == "NUMBER" && org.apache.poi.ss.usermodel.DateUtil.isADateFormat(formatIndex, formatString)) {
            currentDataRow.set(columnIndex, org.apache.poi.ss.usermodel.DateUtil.getJavaDate(value).AsLocalDateTime().AsString());
        } else {
            currentDataRow.set(columnIndex, text);
        }
    }
}