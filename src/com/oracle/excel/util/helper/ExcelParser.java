/**
 *
 */
package com.oracle.excel.util.helper;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.oracle.hed.relops.bean.excel.ExcelBean;
import com.oracle.hed.relops.bean.excel.ExcelColumnPool;
import com.oracle.hed.relops.bean.excel.ExcelRow;
import com.oracle.hed.relops.bean.excel.ExcelSheet;



import javafx.scene.control.TextArea;
import javafx.scene.text.TextFlow;

/**
 * @author raparash
 *
 */
public class ExcelParser {
	private static final Logger LOGGER = Logger.getLogger(ExcelParser.class.getName());
	private TextArea log;
	private int column;

	/*public ExcelParser(TextArea log){
		this.log=log;
	}*/
	public ExcelParser(int column){
		this.column=column;
	}
	//added new constructor
	public ExcelParser(TextArea log,int column)
	{
		this.log=log;
		this.column=column;
	}
	
	/**
	 * Given a file name, the method parses the Excel Sheet
	 *
	 * @param fileName
	 * @param sheetName
	 * @return
	 */
	public ExcelSheet parseExcelSheet(String fileName, String sheetName) {
		ExcelSheet excelSheet = null;
		try {
			excelSheet = parseExcelSheet(new FileInputStream(fileName), sheetName);
			excelSheet.setFileName(fileName);
			LOGGER.log(Level.INFO, "Inside|parseExcelSheet(String fileName, String sheetName)|ExcelParser");
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Exception|parseExcelSheet(String fileName, String sheetName)|ExcelParser ", e);
		}
		return excelSheet;
	}

	/**
	 * Given an inputstream and sheet name, this method parses the excel sheet
	 * @param inputstream
	 * @param sheetName
	 * @return
	 */
	public  ExcelSheet parseExcelSheet(InputStream inputstream, String sheetName) {
		Workbook workbook = null;
		Sheet sheet = null;
		boolean isHeader = true;
		ExcelColumnPool columnPool = new ExcelColumnPool();
		DataFormatter dataFormatter = new DataFormatter();
		ExcelSheet excelSheet = null;
		List<ExcelRow> excelRows = new ArrayList<ExcelRow>();
		try {
			workbook =WorkbookFactory.create(inputstream);
			sheet = workbook.getSheet(sheetName);
			Iterator<Row> rowIterator = sheet.iterator();
			excelSheet=new ExcelSheet();

			while (rowIterator.hasNext()) {
				Row currentRow = rowIterator.next();
			/*	LOGGER.log(Level.INFO,
						"Inside|parseExcelSheet(InputStream inputstream, String sheetName)|ExcelParser| Current row= "
								+ currentRow);*/

				if (currentRow != null) {
					if (isHeader) {
						
						// check that sheet uploaded follows the recommended template
						boolean badTemplate=false;
						String template[]=ExcelColumnPool.COLUMN_HEADER.split(",");
						columnPool.initColumnHeaders(currentRow.getPhysicalNumberOfCells());
						for (int i = 0; i < columnPool.getColumnPool().length; i++) {
							columnPool.getColumn(i).setName(dataFormatter.formatCellValue(currentRow.getCell(i)));
							/*if(!CommonUtil.safeTrim(columnPool.getColumn(i).getName()).equals(CommonUtil.safeTrim(template[i].toUpperCase()))){
								LOGGER.log(Level.INFO,"Check Column Header: Found <"+columnPool.getColumn(i).getName()+">: Expected <"+template[i].toUpperCase()+">");
								//log.appendText("Check Excel Sheet, Column Header: Found <"+columnPool.getColumn(i).getName()+">: Expected <"+template[i].toUpperCase()+">\n");
								badTemplate=true;
							}*/
						}
						if(badTemplate)
							break;
						isHeader = false;
						LOGGER.log(Level.INFO,
								"Inside|parseExcelSheet(InputStream inputstream, String sheetName)|ExcelParser| Headers skipped");
						//Code added for excel sheet validation -- START
						if(columnPool.getColumnPool().length>column){
							LOGGER.log(Level.INFO,
									"Inside|parseExcelSheet(InputStream inputstream, String sheetName)|ExcelParser| Excel sheet not correctly formatted, Expected No. Of Columns: 6");
							log.appendText("Excel sheet is not correctly formatted or you may have choosed wrong template file \n");
							
							break;
						}
						//Code added for excel sheet validation -- STOP
						continue;
					}
					ExcelRow excelRow = new ExcelRow();
					excelRow.setColumnPool(columnPool);
					for (int i = 0; i < columnPool.getColumnPool().length; i++) {
						excelRow.getMapOfColums().put(columnPool.getColumn(i),
								dataFormatter.formatCellValue(currentRow.getCell(i)));
					}
					if(excelRow.isRowEmpty()){
						LOGGER.log(Level.INFO,"Empty Row detected| Row skipped");
						//log.appendText( "Empty Row detected, Row skipped\n");
						continue;
					}
					if(excelRow.isRowFormatted()){
						excelRows.add(excelRow);
					}else{
						LOGGER.log(Level.INFO,"Row not correctly formatted, Detected Empty columns| Row skipped");
						//log.appendText("Current Row: "+excelRow.toString()+"\n");
						//log.appendText("Row not correctly formatted, Detected Empty columns, Row skipped\n");
					}
				}
			}
			excelSheet.setSheetName(sheetName);
			excelSheet.setExcelRows(excelRows);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING,
					"Exception|parseExcelSheet(InputStream inputstream, String sheetName)|ExcelParser ", e);
		} finally {
			CommonUtil.safeClose(workbook, inputstream);
		}
		return excelSheet;
	}

	/**
	 * Parses a given excel sheet stream and returns list of ExcelBean
	 * @param inputstream
	 * @param cls
	 * @return
	 */
	public  <T extends ExcelBean> List<T> parseExcel(InputStream inputstream, Class<T> cls) {
		List<T> listOfExcelBean = null;
		try {
			ExcelSheet excelSheet = parseExcelSheet(inputstream, "Sheet1");
			if (excelSheet != null) {
				listOfExcelBean = new ArrayList<T>();
				LOGGER.log(Level.INFO,"Inside|parseExcel(InputStream inputstream)|ExcelParser| No. of excel rows = "+excelSheet.getExcelRows().size());
				if(!excelSheet.getExcelRows().isEmpty()){
					for (ExcelRow excelRow : excelSheet.getExcelRows()) {
					T excelbean = cls.newInstance();
					excelbean.convertToBean(excelRow);
					listOfExcelBean.add(excelbean);
				}
				}else{
					LOGGER.log(Level.INFO,"Check Excel Sheet| No Rows found or Template not Correct");
				}
			}else{
				LOGGER.log(Level.WARNING,"Inside|parseExcel(InputStream inputstream)|ExcelParser| Excel Sheet not found, Expected Name of Sheet: Sheet1");
				//log.appendText( "Excel Sheet not found, Expected Name of Sheet: Sheet1\n");
			}
		} catch (Exception e) {
			LOGGER.log(Level.WARNING,
					"Exception|parseExcel(InputStream inputstream)|ExcelParser ", e);
		}

		return listOfExcelBean;
	}

}
