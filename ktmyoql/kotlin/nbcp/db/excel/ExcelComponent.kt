package nbcp.db.excel


import org.apache.poi.hssf.record.chart.NumberFormatIndexRecord
import org.apache.poi.ss.usermodel.*
import nbcp.comm.*
import nbcp.base.extend.*
import nbcp.model.DataTable
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.hssf.usermodel.HSSFCell
import org.apache.poi.ooxml.util.SAXHelper
import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.openxml4j.opc.PackageAccess
import org.apache.poi.poifs.filesystem.FileMagic
import org.apache.poi.ss.util.CellReference
import org.apache.poi.xssf.binary.XSSFBSheetHandler
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable
import org.apache.poi.xssf.eventusermodel.XSSFReader
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFComment
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Component
import org.xml.sax.InputSource
import org.xml.sax.XMLReader
import nbcp.comm.*
import nbcp.comm.*
import nbcp.base.utils.MyUtil
import org.springframework.context.annotation.Lazy
import java.io.*
import java.time.LocalTime
import kotlin.collections.LinkedHashMap


fun Cell?.getStringValue(evaluator: FormulaEvaluator): String {
    if (this == null) return ""
    return when (this.cellType) {
        CellType.BLANK -> ""
        CellType.BOOLEAN -> this.booleanCellValue.AsString()
        CellType.NUMERIC -> {
            var longValue = this.numericCellValue.AsLong()
            if (this.numericCellValue - longValue == 0.0) {
                return longValue.toString()
            }

            this.numericCellValue.AsString()
        }

        CellType.STRING -> this.stringCellValue
        CellType.FORMULA -> evaluator.evaluate(this).formatAsString() //应该求值
        else -> ""
    }
}

/**
 * Excel 导入导出。
 */
class ExcelComponent() {
    private var columns: List<String> = listOf()
    private var sheetName: String = ""
    private var offset_row: Int = 0;
    private var pks: List<String> = listOf();
    private var fileName: String = "";
    val sheetNames: Array<String>
        get() {
            var ret = mutableListOf<String>()

            val file = FileMagic.prepareToCheckMagic(FileInputStream(fileName))
            try {
                val fm = FileMagic.valueOf(file)
                when (fm) {
                    FileMagic.OOXML -> {
                        var book = WorkbookFactory.create(FileInputStream(fileName))
                        for (i in 0..(book.numberOfSheets - 1)) {
                            ret.add(book.getSheetAt(i).sheetName)
                        }

                        book.close()
                    }

                    FileMagic.OLE2 -> {
                        var xlsxPackage = OPCPackage.open(fileName, PackageAccess.READ)

                        try {
                            var xssfReader = XSSFReader(xlsxPackage)
                            var iter = xssfReader.sheetsData as XSSFReader.SheetIterator
                            while (iter.hasNext()) {
                                iter.next().use { stream ->
                                    ret.add(iter.sheetName);
                                }
                            }
                        } finally {
                            xlsxPackage.close();
                        }
                    }
                }

            } finally {
                file.close()
            }

            return ret.toTypedArray()
        }

    fun using(fileName: String){
        this.fileName = fileName;
    }
    fun select(sheetName: String, columns: List<String>, pks: List<String>, offset_row: Int = 0) {
        this.sheetName = sheetName;
        this.columns = columns;
        this.pks = pks;
        this.offset_row = offset_row;
    }


    fun readRowData(sheetName: String, rowIndex: Int): Array<String> {
        var ret = mutableListOf<String>();
        val file = FileMagic.prepareToCheckMagic(FileInputStream(fileName))
        try {
            val fm = FileMagic.valueOf(file)
            when (fm) {
                FileMagic.OOXML -> {
                    var xlsxPackage = OPCPackage.open(fileName, PackageAccess.READ)

                    try {
                        var xssfReader = XSSFReader(xlsxPackage)
                        var iter = xssfReader.sheetsData as XSSFReader.SheetIterator


                        while (iter.hasNext()) {
                            var r = iter.next().use { stream ->

                                if (iter.sheetName == sheetName) {

                                    var strings = ReadOnlySharedStringsTable(xlsxPackage);
                                    var styles = xssfReader.getStylesTable();
                                    var formatter = DataFormatter()
                                    var sheetSource = InputSource(stream);
                                    var sheetParser = SAXHelper.newXMLReader();

                                    try {
                                        sheetParser.contentHandler = XSSFSheetXMLHandler(styles, null, strings,
                                                SheetContentRowArrayDataReader(sheetParser, offset_row, {
                                                    ret.addAll(it);
                                                    return@SheetContentRowArrayDataReader false;
                                                }), formatter, false);
                                        sheetParser.parse(sheetSource)
                                    } catch (e: Exception) {
                                        if (e.cause is ReturnException == false) {
                                            throw e;
                                        }
                                    }
                                    return@use false;
                                }

                                return@use true;
                            }

                            if (r == false) break;
                        }
                    } finally {
                        xlsxPackage.close();
                    }
                    return ret.toTypedArray();
                }
                FileMagic.OLE2 -> {
                    var book = WorkbookFactory.create(FileInputStream(fileName))
                    var sheet: Sheet;

                    try {
                        sheet = book.getSheet(sheetName);
                    } catch (e: java.lang.Exception) {
                        book.close();
                        throw java.lang.Exception("打不开Excel文件的 ${sheetName} ！")
                    }

                    try {
                        //公式执行器
                        var evaluator = book.creationHelper.createFormulaEvaluator()

                        var row = sheet.getRow(offset_row)
                        if (row == null) {
                            return ret.toTypedArray();
                        }


                        for (i in 1..row.lastCellNum) {
                            var cell = row.getCell(i - 1);
                            if (cell == null) {
                                ret.add("")
                                continue
                            }


                            //处理 日期，时间 格式。
                            if (cell.cellType == CellType.NUMERIC) {
                                var value = cell.numericCellValue.toBigDecimal().toPlainString()
                                if (value.indexOf(".") < 0 || "General" == cell.cellStyle.dataFormatString) {
                                    ret.add(value);
                                    continue;
                                }

                                if (cell.cellStyle.dataFormatString.indexOf("yy") >= 0 &&
                                        cell.cellStyle.dataFormatString.indexOf("m") >= 0 &&
                                        cell.cellStyle.dataFormatString.indexOf("d") >= 0 &&
                                        cell.cellStyle.dataFormatString.indexOf("h:mm:ss") >= 0) {

                                    ret.add(cell.dateCellValue.AsLocalDateTime().AsString())
                                    continue;
                                } else if (cell.cellStyle.dataFormatString.indexOf("h:mm:ss") >= 0) {
                                    ret.add(cell.dateCellValue.AsLocalTime().AsString())
                                    continue;
                                } else if (cell.cellStyle.dataFormatString.indexOf("yy") >= 0 &&
                                        cell.cellStyle.dataFormatString.indexOf("m") >= 0 &&
                                        cell.cellStyle.dataFormatString.indexOf("d") >= 0) {
                                    ret.add(cell.dateCellValue.AsLocalDate().AsString())
                                    continue;
                                }
                            }

                            ret.add(cell.getStringValue(evaluator).AsString().trim())
                        }

                    } finally {
                        book.close()
                    }

                }
            }

        } finally {
            file.close()
        }

        return ret.toTypedArray();
    }

    private fun getHeaderColumnsIndexMap(headerRow: Row, columns: List<String>, evaluator: FormulaEvaluator): LinkedHashMap<Int, String> {

        var columnDataIndexs = linkedMapOf<Int, String>()
        for (columnIndex in headerRow.firstCellNum.AsInt()..headerRow.lastCellNum) {
            var columnName = headerRow.getCell(columnIndex).getStringValue(evaluator).trim()
            if (columnName.isEmpty()) {
                continue
            }

            if (columns.contains(columnName) == false) {
                continue
            }
            columnDataIndexs.set(columnIndex, columnName)
        }

        return columnDataIndexs;
    }

    //                        translateRowJson:((JsonMap)->Unit)? = null,
    fun <T> getDataTable(clazz: Class<T>, skip: Int = 0,
                         filter: ((JsonMap) -> Boolean)? = null): DataTable<T> {
        var dt = DataTable<T>(clazz)

        var pk_values = mutableListOf<String>()


        var fields = clazz.declaredFields.map {
            it.isAccessible = true
            return@map it;
        }

        getData(skip) { row ->
            //判断该行是否是主键空值.
            //主键全空.

            var pk_map = row.filterKeys { pks.contains(it) }
            var pk_value = pks.map { pk_map.get(it) }.joinToString(",")

            if (pk_values.contains(pk_value)) {
                throw Exception("发现第 ${pk_values.size + 1} 行存在重复数据!")
            }
            pk_values.add(pk_value);

            if (filter != null) {
                if (filter(row) == false) {
                    return@getData false;
                }
            }

            dt.rows.add(row.ConvertJson(clazz));

            return@getData true;
        }

        pk_values.clear();
        return dt;
    }


    /**读取数据，跳过空行,跳过Header.name.isEmpty 的列。
     * @param columns 列定义. 位置无关.
     * @param offset_row 前面的空行.
     * @param pks: 主键列， 主键列不能为空。
     * @param broke_lines: 允许连续的空行。
     */
    private fun getData(skip: Int = 0,
                        filter: (JsonMap) -> Boolean
    ) {
        if (columns.isEmpty()) {
            return;
        }

        if (pks.isEmpty() == false) {
            var ext_pks = pks.minus(columns);
            if (ext_pks.any()) {
                throw Exception("${sheetName}多余的主键定义:${ext_pks.joinToString(",")}");
            }
        }

        val file = FileMagic.prepareToCheckMagic(FileInputStream(fileName))
        try {
            val fm = FileMagic.valueOf(file)
            var lined = 0;
            var filter2: (JsonMap) -> Boolean = f2@{ row ->
                lined++;
                if (row.any() == false) {
                    return@f2 false;
                }
                var pk_map = row.filterKeys { pks.contains(it) }

                if (pk_map.any() == false) {
                    throw Exception("找不到主键的值!，行：${lined}}")
                }

                var pk_empty_map = pk_map.filter { it.value.AsString().isEmpty() }
                if (pk_empty_map.any()) {
                    throw Exception("发现主键空值，行：${lined}, 列: ${pk_empty_map.map { it.key }.joinToString(",")}")
                }
                return@f2 filter(row);
            }

            when (fm) {
                FileMagic.OOXML -> readOpenXmlExcelData(skip, filter2);
                FileMagic.OLE2 -> readOle2ExcelData(skip, filter2)
            }

        } finally {
            file.close()
        }
    }


    /**回写数据 按 data.tableName == sheetName
     * @param getRowData: 返回 null 停止。
     */
    fun writeNewData(offset_column: Int = 0, getRowData: (Int) -> JsonMap?): ByteArray {
        val book = SXSSFWorkbook(1000)

        //生成一个sheet1
        val sheet = book.createSheet(sheetName);
        var header_row = sheet.createRow(offset_row);

        columns.forEachIndexed { index, columnName ->
            val cell = header_row.createCell(index + offset_column)
            cell.setCellValue(columnName);
        }

        var dataRowIndex = -1;
        while (true) {
            dataRowIndex++;

            var dbRowData = getRowData(dataRowIndex)
            if (dbRowData == null) {
                break;
            }

            var excelRowIndex = dataRowIndex + 1 + offset_row
            var excelRow = sheet.createRow(excelRowIndex)
            if (dbRowData.any() == false) {
                continue;
            }

            for (columnIndex in offset_column..(offset_column + columns.size - 1)) {
                var columnName = columns.get(columnIndex)

                var dbValue = dbRowData.get(columnName)
                var cell = excelRow.createCell(columnIndex)


                if (dbValue == null) {
                    cell.cellType = CellType.BLANK
                } else if (dbValue is String) {
                    cell.setCellValue(dbValue.AsString())
                } else if (dbValue is Number) {
                    cell.setCellValue(dbValue.AsDouble())
                } else if (dbValue is Boolean) {
                    cell.setCellValue(dbValue.AsBoolean())
                } else if (dbValue is LocalDateTime ||
                        dbValue is LocalDate ||
                        dbValue is Date) {
                    cell.setCellValue(dbValue.AsDate())
                } else {
                    cell.setCellValue(dbValue.AsString())
                }
            }
        }


        var outputStream = ByteArrayOutputStream();
        book.write(outputStream)
        book.close()
        return outputStream.toByteArray()
    }


    private fun readOle2ExcelData(skip: Int = 0,
                                  filter: (JsonMap) -> Boolean
    ) {
        var book = WorkbookFactory.create(FileInputStream(fileName))
        var sheet: Sheet;

        try {
            if (book.numberOfSheets == 1) {
                sheet = book.getSheetAt(0)
            } else {
                sheet = book.getSheet(sheetName);
            }
        } catch (e: java.lang.Exception) {
            book.close();
            throw java.lang.Exception("打不开Excel文件的 ${sheetName} ！")
        }

        try {
            //公式执行器
            var evaluator = book.creationHelper.createFormulaEvaluator()
            var header_row = sheet.getRow(offset_row)

            // key: excel 中的 列的索引 , value = column_name
            var columns_index_map = getHeaderColumnsIndexMap(header_row, columns, evaluator);

            if (columns_index_map.size != columns.size) {
                var ext_columns = columns.minus(columns_index_map.values);
                throw Exception("找不到列：${ext_columns.joinToString(",")}")
            }

            for (rowIndex in (offset_row + 1 + skip)..sheet.lastRowNum) {
                var row = sheet.getRow(rowIndex)
                if (row == null) {
                    break
                }
                var rowData = JsonMap();
                for (columnIndex in columns_index_map.keys) {
                    var cell = row.getCell(columnIndex);
                    if (cell == null) {
                        continue
                    }

                    var columnName = columns_index_map.get(columnIndex)!!;

                    //处理 日期，时间 格式。
                    if (cell.cellType == CellType.NUMERIC) {
                        var value = cell.numericCellValue.toBigDecimal().toPlainString()
                        if (value.indexOf(".") < 0 || "General" == cell.cellStyle.dataFormatString) {
                            rowData.set(columnName, value);
                            continue;
                        }

                        if (cell.cellStyle.dataFormatString.indexOf("yy") >= 0 &&
                                cell.cellStyle.dataFormatString.indexOf("m") >= 0 &&
                                cell.cellStyle.dataFormatString.indexOf("d") >= 0 &&
                                cell.cellStyle.dataFormatString.indexOf("h:mm:ss") >= 0) {

                            rowData.set(columnName, cell.dateCellValue.AsLocalDateTime().AsString());
                            continue;
                        } else if (cell.cellStyle.dataFormatString.indexOf("h:mm:ss") >= 0) {
                            rowData.set(columnName, cell.dateCellValue.AsLocalTime().AsString());
                            continue;
                        } else if (cell.cellStyle.dataFormatString.indexOf("yy") >= 0 &&
                                cell.cellStyle.dataFormatString.indexOf("m") >= 0 &&
                                cell.cellStyle.dataFormatString.indexOf("d") >= 0) {
                            rowData.set(columnName, cell.dateCellValue.AsLocalDate().AsString());
                            continue;
                        }
                    }

                    rowData.set(columnName, cell.getStringValue(evaluator).AsString().trim())
                }


                if (filter(rowData) == false) {
                    return;
                }
            }

        } finally {
            book.close()
        }
    }


    private fun readOpenXmlExcelData(
            skip: Int = 0,
            filter: (JsonMap) -> Boolean
    ) {
        var xlsxPackage = OPCPackage.open(fileName, PackageAccess.READ)

        try {
            var xssfReader = XSSFReader(xlsxPackage)
            var iter: XSSFReader.SheetIterator

            var sheetCount = 0;

            iter = xssfReader.sheetsData as XSSFReader.SheetIterator
            while (iter.hasNext()) {
                iter.next().use { stream ->
                    if (sheetName.isEmpty()) {
                        getSheetData(xlsxPackage, xssfReader, stream, columns, offset_row, skip, filter)
                        return;
                    }


                    sheetCount++;
                }

                if (sheetCount > 1) {
                    break;
                }
            }


            iter = xssfReader.sheetsData as XSSFReader.SheetIterator
            while (iter.hasNext()) {
                iter.next().use { stream ->
                    if (sheetCount == 1) {
                        getSheetData(xlsxPackage, xssfReader, stream, columns, offset_row, skip, filter)
                        return;
                    } else {
                        if (iter.sheetName == sheetName) {
                            getSheetData(xlsxPackage, xssfReader, stream, columns, offset_row, skip, filter)
                            return;
                        }
                    }
                }
            }

            throw java.lang.Exception("找不到 Excel文件的 ${sheetName} ！")
        } finally {
            xlsxPackage.close();
        }
    }


    private fun getSheetData(
            xlsxPackage: OPCPackage,
            xssfReader: XSSFReader,
            sheetInputStream: InputStream,
            columns: List<String>,
            offset_row: Int = 0,
            skip: Int = 0,
            filter: ((JsonMap) -> Boolean)
    ) {

        var strings = ReadOnlySharedStringsTable(xlsxPackage);
        var styles = xssfReader.getStylesTable();
        var formatter = DataFormatter()
        var sheetSource = InputSource(sheetInputStream);
        var sheetParser = SAXHelper.newXMLReader();

        try {
            sheetParser.contentHandler = XSSFSheetXMLHandler(styles, null, strings, SheetContentReader(sheetParser, columns, filter, offset_row, skip), formatter, false);
            sheetParser.parse(sheetSource)
        } catch (e: Exception) {
            if (e.cause is ReturnException == false) {
                throw e;
            }
        }
        return;
    }


}

