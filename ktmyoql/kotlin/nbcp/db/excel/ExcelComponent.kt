package nbcp.db.excel


import org.apache.poi.ss.usermodel.*
import nbcp.comm.*
import nbcp.model.DataTable
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import org.apache.poi.ooxml.util.SAXHelper
import org.apache.poi.openxml4j.opc.OPCPackage
import org.apache.poi.poifs.filesystem.FileMagic
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable
import org.apache.poi.xssf.eventusermodel.XSSFReader
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.xml.sax.InputSource
import java.io.*
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
 * 如果是小文件，可以是内存流；如果是大文件，最好是物理磁盘文件流
 */
class ExcelComponent(val excelStream: () -> InputStream) {
    val sheetNames: Array<String>
        get() {
            var ret = mutableListOf<String>()
            var stream = excelStream()

//            if (stream.markSupported()) {
//                stream.reset();
//            }

            WorkbookFactory.create(stream).use { book ->
                for (i in 0..(book.numberOfSheets - 1)) {
                    ret.add(book.getSheetAt(i).sheetName)
                }
            }
            return ret.toTypedArray();
        }


    fun select(sheetName: String = ""): ExcelSheetComponent {
        return ExcelSheetComponent(sheetName.AsString(sheetNames.first()), excelStream);
    }

    fun select(sheetIndex: Int = 0): ExcelSheetComponent {
        return ExcelSheetComponent(sheetNames.elementAt(sheetIndex), excelStream);
    }

    class ExcelSheetComponent(val sheetName: String, val excelStream: () -> InputStream) {
        private var columns: Array<out String> = arrayOf()
        private var rowOffset: Int = 0;
        private var pks: Array<out String> = arrayOf()
        private var strictMode: Boolean = true;


        private fun getStream(): InputStream {
            var stream = excelStream();
//            if (stream.markSupported()) {
//                stream.reset();
//            }
            return stream;
        }

        fun setStrictMode(value: Boolean): ExcelSheetComponent {
            this.strictMode = value;
            return this;
        }

        fun setColumns(vararg columns: String): ExcelSheetComponent {
            this.columns = columns;
            return this;
        }

        fun setRowOffset(rowOffset: Int): ExcelSheetComponent {
            this.rowOffset = rowOffset;
            return this;
        }

        fun setPks(vararg pks: String): ExcelSheetComponent {
            this.pks = pks;
            return this;
        }

        private fun getHeaderColumnsIndexMap(
            headerRow: Row,
            columns: Array<out String>,
            evaluator: FormulaEvaluator
        ): LinkedHashMap<Int, String> {

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

        /**
         * 从Sheet中读出列
         */
        fun readSheetColumns(): List<String> {
            val fm = FileMagic.prepareToCheckMagic(getStream()).use { file ->
                return@use FileMagic.valueOf(file)
            }

            var list = listOf<String>()

            var filter2: (Map<Int, String>) -> Boolean = f2@{ oriData ->
                list = oriData.values.toList();
                return@f2 false
            }


            /*
POI提供了HSSF、XSSF以及SXSSF三种方式操作Excel。
HSSF：Excel97-2003版本，扩展名为.xls。一个sheet最大行数65536，最大列数256。
XSSF：Excel2007版本开始，扩展名为.xlsx。一个sheet最大行数1048576，最大列数16384。
SXSSF：是在XSSF基础上，POI3.8版本开始提供的支持低内存占用的操作方式，扩展名为.xlsx。
 */
            when (fm) {
                FileMagic.OOXML -> readOpenXmlExcelData({ _, _ -> false }, filter2);
                FileMagic.OLE2 -> readOle2ExcelData({ _, _ -> false }, filter2)
                else -> throw RuntimeException("不识别的类型：${fm}")
            }

            return list;
        }

        /**
         * 读取数据
         */
        @JvmOverloads
        fun <T : Any> getDataTable(
            clazz: Class<T>,
            filter: ((JsonMap, Map<Int, String>) -> Boolean)? = null
        ): DataTable<T> {
            var dt = DataTable<T>(clazz)

            var pk_values = mutableListOf<String>()

            readData { row, oriData ->
                //判断该行是否是主键空值.
                //主键全空.
                if (pks.any()) {
                    var pk_map = row.filterKeys { pks.contains(it) }
                    var pk_value = pks.map { pk_map.get(it) }.joinToString(",")

                    if (pk_values.contains(pk_value)) {
                        throw RuntimeException("发现第 ${pk_values.size + 1} 行存在重复数据!")
                    }
                    pk_values.add(pk_value);
                }

                if (filter != null) {
                    if (filter(row, oriData) == false) {
                        return@readData false;
                    }
                }

                dt.rows.add(row.ConvertJson(clazz));

                return@readData true;
            }

            pk_values.clear();
            return dt;
        }


        /**读取数据，跳过 offset_row 。
         * @param filter
         */
        fun readData(filter: (JsonMap, Map<Int, String>) -> Boolean) {
            if (columns.isEmpty()) {
                return;
            }

            if (pks.isEmpty() == false) {
                var ext_pks = pks.toList().minus(columns);
                if (ext_pks.any()) {
                    throw RuntimeException("${sheetName}多余的主键定义:${ext_pks.joinToString(",")}");
                }
            }

            val fm = FileMagic.prepareToCheckMagic(getStream()).use { file ->
                return@use FileMagic.valueOf(file)
            }

            var lined = 0;
            var filter2: (JsonMap, Map<Int, String>) -> Boolean = f2@{ row, oriData ->
                lined++;
                if (row.any() == false) {
                    return@f2 false;
                }

                if (pks.any()) {
                    var pk_map = row.filterKeys { pks.contains(it) }

                    if (pk_map.any() == false) {
                        throw RuntimeException("找不到主键的值!，行：${lined}}")
                    }

                    var pk_empty_map = pk_map.filter { it.value.AsString().isEmpty() }
                    if (pk_empty_map.any()) {
                        throw RuntimeException(
                            "发现主键空值，行：${lined}, 列: ${
                                pk_empty_map.map { it.key }.joinToString(",")
                            }"
                        )
                    }
                }

                return@f2 filter(row, oriData);
            }


            /*
POI提供了HSSF、XSSF以及SXSSF三种方式操作Excel。
HSSF：Excel97-2003版本，扩展名为.xls。一个sheet最大行数65536，最大列数256。
XSSF：Excel2007版本开始，扩展名为.xlsx。一个sheet最大行数1048576，最大列数16384。
SXSSF：是在XSSF基础上，POI3.8版本开始提供的支持低内存占用的操作方式，扩展名为.xlsx。
 */
            when (fm) {
                FileMagic.OOXML -> readOpenXmlExcelData(filter2, { true });
                FileMagic.OLE2 -> readOle2ExcelData(filter2, { true })
                else -> throw RuntimeException("不识别的类型：${fm}")
            }

        }


        /**回写数据 按 data.tableName == sheetName
         * @param getRowData: 返回 null 停止。
         */
        fun writeData(outputStream: OutputStream, getRowData: (Int) -> JsonMap?) {
            OPCPackage.open(getStream()).use { xlsxPackage ->
                var book = SXSSFWorkbook(XSSFWorkbook(xlsxPackage), 1000)

                //公式执行器
                var evaluator = book.creationHelper.createFormulaEvaluator()

                //生成一个sheet1
                val sheet = book.xssfWorkbook.getSheet(sheetName);
                var header_row = sheet.getRow(rowOffset)
                // key: excel 中的 列的索引 , value = column_name
                var columns_index_map = getHeaderColumnsIndexMap(header_row, columns, evaluator);


                var dataRowIndex = -1;
                while (true) {
                    dataRowIndex++;

                    var dbRowData = getRowData(dataRowIndex)
                    if (dbRowData == null) {
                        break;
                    }
                    if (dbRowData.any() == false) {
                        break;
                    }

                    var excelRowIndex = dataRowIndex + 1 + rowOffset
                    var excelRow = sheet.getRow(excelRowIndex)
                    if (excelRow == null) {
                        excelRow = sheet.createRow(excelRowIndex)
                    }


                    for (columnIndex in columns_index_map.keys) {
                        var columnName = columns_index_map.get(columnIndex)

                        var dbValue = dbRowData.get(columnName)
                        if (dbValue == null) {
                            continue;
                        }

                        var cell = excelRow.getCell(columnIndex)
                        if (cell == null) {
                            cell = excelRow.createCell(columnIndex)
                        }

                        if (dbValue is String) {
                            cell.setCellValue(dbValue.AsString())
                        } else if (dbValue is Number) {
                            cell.setCellValue(dbValue.AsDouble())
                        } else if (dbValue is Boolean) {
                            cell.setCellValue(dbValue.AsBoolean())
                        } else if (dbValue is LocalDateTime ||
                            dbValue is LocalDate ||
                            dbValue is Date
                        ) {
                            cell.setCellValue(dbValue.AsDate())
                        } else {
                            cell.setCellValue(dbValue.AsString())
                        }
                    }
                }

                sheet.workbook.write(outputStream)
            }
        }


        fun <T : Any> writeData(outputStream: OutputStream, table: DataTable<T>) {
            writeData(outputStream) { rowIndex ->
                return@writeData table.rows.getOrNull(rowIndex)?.ConvertType(JsonMap::class.java) as JsonMap?
            }
        }


        private fun readOle2ExcelData(
            filter: (JsonMap, Map<Int, String>) -> Boolean,
            sheetColumnsCallback: (Map<Int, String>) -> Boolean
        ) {
            WorkbookFactory.create(getStream()).use { book ->

                var sheet: Sheet;

                try {
                    if (book.numberOfSheets == 1) {
                        sheet = book.getSheetAt(0)
                    } else {
                        sheet = book.getSheet(sheetName);
                    }
                } catch (e: java.lang.Exception) {
                    throw java.lang.Exception("打不开Excel文件的Sheet ${sheetName} ！")
                }


                //公式执行器
                var evaluator = book.creationHelper.createFormulaEvaluator()
                var header_row = sheet.getRow(rowOffset)
                // key: excel 中的 列的索引 , value = column_name
                var columns_index_map = getHeaderColumnsIndexMap(header_row, columns, evaluator);

                if (sheetColumnsCallback(columns_index_map) == false) {
                    return;
                }

                for (rowIndex in (rowOffset + 1)..sheet.lastRowNum) {
                    var row = sheet.getRow(rowIndex)
                    if (row == null) {
                        break
                    }
                    var oriData = mutableMapOf<Int, String>()

                    for (columnIndex in (row.firstCellNum - 1)..(row.lastCellNum - 1)) {
                        var cell = row.getCell(columnIndex);
                        if (cell == null) {
                            continue
                        }

                        //处理 日期，时间 格式。
                        if (cell.cellType == CellType.NUMERIC) {
                            var value = cell.numericCellValue.toBigDecimal().toPlainString()
                            if (value.indexOf(".") < 0 || "General" == cell.cellStyle.dataFormatString) {
                                oriData.set(columnIndex, value);
                                continue;
                            }

                            if (cell.cellStyle.dataFormatString.indexOf("yy") >= 0 &&
                                cell.cellStyle.dataFormatString.indexOf("m") >= 0 &&
                                cell.cellStyle.dataFormatString.indexOf("d") >= 0 &&
                                cell.cellStyle.dataFormatString.indexOf("h:mm:ss") >= 0
                            ) {

                                oriData.set(columnIndex, cell.dateCellValue.AsLocalDateTime().AsString());
                                continue;
                            } else if (cell.cellStyle.dataFormatString.indexOf("h:mm:ss") >= 0) {
                                oriData.set(columnIndex, cell.dateCellValue.AsLocalTime().AsString());
                                continue;
                            } else if (cell.cellStyle.dataFormatString.indexOf("yy") >= 0 &&
                                cell.cellStyle.dataFormatString.indexOf("m") >= 0 &&
                                cell.cellStyle.dataFormatString.indexOf("d") >= 0
                            ) {
                                oriData.set(columnIndex, cell.dateCellValue.AsLocalDate().AsString());
                                continue;
                            }
                        }

                        oriData.set(columnIndex, cell.getStringValue(evaluator).AsString().trim())
                    }


//                if (columns_index_map.size != columns.size) {
//                    var ext_columns = columns.minus(columns_index_map.values);
//                    throw RuntimeException("找不到列：${ext_columns.joinToString(",")}")
//                }

                    var rowData = JsonMap();
                    for (columnIndex in columns_index_map.keys) {
                        var columnName = columns_index_map.get(columnIndex)!!;
                        rowData.set(columnName, oriData[columnIndex])
                    }


                    if (filter(rowData, oriData) == false) {
                        return;
                    }
                }
            }
        }


        private fun readOpenXmlExcelData(
            filter: (JsonMap, Map<Int, String>) -> Boolean,
            sheetColumnsCallback: (Map<Int, String>) -> Boolean
        ) {
            OPCPackage.open(getStream()).use { xlsxPackage ->
                var xssfReader = XSSFReader(xlsxPackage)

                var iter = xssfReader.sheetsData as XSSFReader.SheetIterator
                while (iter.hasNext()) {
                    iter.next().use { stream ->
                        if (iter.sheetName == sheetName) {
                            getSheetData(xlsxPackage, xssfReader, stream, filter, sheetColumnsCallback)
                            return;
                        }
                    }
                }

                throw java.lang.Exception("找不到Excel文件的Sheet ${sheetName} ！")
            }
        }


        private fun getSheetData(
            xlsxPackage: OPCPackage,
            xssfReader: XSSFReader,
            sheetInputStream: InputStream,
            filter: ((JsonMap, Map<Int, String>) -> Boolean),
            sheetColumnsCallback: ((Map<Int, String>) -> Boolean)
        ) {

            var strings = ReadOnlySharedStringsTable(xlsxPackage);
            var styles = xssfReader.getStylesTable();
            var formatter = DataFormatter()
            var sheetSource = InputSource(sheetInputStream);
            var sheetParser = SAXHelper.newXMLReader();

            try {
                sheetParser.contentHandler = XSSFSheetXMLHandler(
                    styles,
                    null,
                    strings,
                    SheetContentReader(
                        sheetParser,
                        columns,
                        filter,
                        sheetColumnsCallback,
                        this.rowOffset,
                        this.strictMode
                    ),
                    formatter,
                    false
                );
                sheetParser.parse(sheetSource)
            } catch (e: Exception) {
                if (e.cause is ReturnException == false) {
                    throw e;
                }
            }
            return;
        }

    }
}


