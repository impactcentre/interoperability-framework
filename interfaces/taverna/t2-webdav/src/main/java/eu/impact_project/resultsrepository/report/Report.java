/*
	
	Copyright 2011 The IMPACT Project
	
	@author Dennis Neumann

	Licensed under the Apache License, Version 2.0 (the "License"); 
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
 
  		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.

*/

package eu.impact_project.resultsrepository.report;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class Report {

	private String excelUrl;
	private String demonstratorId;
	private String workflowId;
	private long processingTime;
	private double imageCount;
	private Workbook workbook;

	private final int STRING = 0;
	private final int DOUBLE = 1;
	private final int DATE = 2;

	private List<AnyTool> tools = new ArrayList<AnyTool>();
	private List<OcrEvalTool> evalTools = new ArrayList<OcrEvalTool>();
	private List<LayoutEvalTool> layoutEvalTools = new ArrayList<LayoutEvalTool>();

	private Properties properties;

	public Report(String workflowId, long processingTime,
			String demonstratorId, double imageCount) throws IOException {

		this.workflowId = workflowId;
		this.processingTime = processingTime;
		this.demonstratorId = demonstratorId;
		this.imageCount = imageCount;

		properties = new Properties();
		URL url = getClass().getResource("/report.properties");
		if (url == null)
			throw new IOException(
					"Property file for report generation not found.");
		InputStream is = url.openStream();
		properties.load(is);
		is.close();

		this.excelUrl = properties.getProperty("excelUrl");
	}

	public void setTools(List<AnyTool> t) {
		tools = new ArrayList<AnyTool>(t);
	}

	public void setOcrEvalTools(List<OcrEvalTool> t) {
		evalTools = new ArrayList<OcrEvalTool>(t);
	}

	public void setLayoutEvalTools(List<LayoutEvalTool> t) {
		layoutEvalTools = new ArrayList<LayoutEvalTool>(t);
	}

	// TODO whole method should be refactored, looks like a lot of duplicate
	// code
	public InputStream asExcel() throws IOException, InvalidFormatException {
		workbook = openExcel(excelUrl);
		ExcelCoordinates coords = new ExcelCoordinates(properties);

		int overviewCol = coords.overviewUpperRight.column;
		int overviewRow = coords.overviewUpperRight.row;
		int overviewSheetNr = coords.overviewUpperRight.sheet;
		Sheet overviewSheet = workbook.getSheetAt(overviewSheetNr);

		insert(overviewSheet, demonstratorId, overviewRow, overviewCol, STRING);
		overviewRow++;
		insert(overviewSheet, workflowId, overviewRow, overviewCol, STRING);
		overviewRow++;
//		insert(overviewSheet, System.currentTimeMillis()+"", overviewRow, overviewCol, DATE);
//		overviewRow++;
		insert(overviewSheet, String.valueOf(processingTime), overviewRow,
				overviewCol, DOUBLE);
		overviewRow++;
		insert(overviewSheet, String.valueOf(imageCount), overviewRow,
				overviewCol, DOUBLE);

		int timesCol = coords.processingTimesUpperLeft.column;
		int timesRow = coords.processingTimesUpperLeft.row;
		int timesSheetNr = coords.processingTimesUpperLeft.sheet;
		Sheet timesSheet = workbook.getSheetAt(timesSheetNr);
		for (AnyTool tool : tools) {
			String service = tool.getName();
			double time = tool.getProcessingTime();

			insert(timesSheet, service, timesRow, timesCol, STRING);
			insert(timesSheet, String.valueOf(time), timesRow, timesCol + 1,
					DOUBLE);

			timesRow++;
		}

		int evalCol = coords.evaluationsUpperLeft.column;
		int evalRow = coords.evaluationsUpperLeft.row;
		int evalSheetNr = coords.evaluationsUpperLeft.sheet;
		Sheet evalSheet = workbook.getSheetAt(evalSheetNr);
		for (OcrEvalTool tool : evalTools) {
			evalCol = coords.evaluationsUpperLeft.column;

			insert(evalSheet, tool.getName(), evalRow, evalCol, STRING);
			insert(evalSheet, tool.getEvaluationId(), evalRow, evalCol + 1,
					STRING);

			for (OcrEvalTool.Evaluation eval : tool.getEvaluations()) {

				evalCol = coords.evaluationsUpperLeft.column + 2;
				insert(evalSheet, eval.characters, evalRow, evalCol, DOUBLE);
				evalCol++;
				insert(evalSheet, eval.errors, evalRow, evalCol, DOUBLE);
				evalCol++;
				if (eval.accuracy.contains("---")) {
					insert(evalSheet, "Not computable", evalRow, evalCol,
							STRING);
				} else {
					insert(evalSheet, eval.accuracy, evalRow, evalCol, DOUBLE);
				}
				evalCol++;
				insert(evalSheet, eval.words, evalRow, evalCol, DOUBLE);
				evalCol++;
				insert(evalSheet, eval.misrecognized, evalRow, evalCol, DOUBLE);
				evalCol++;
				if (eval.wordAccuracy.contains("---")) {
					insert(evalSheet, "Not computable", evalRow, evalCol,
							STRING);
				} else {
					insert(evalSheet, eval.wordAccuracy, evalRow, evalCol,
							DOUBLE);
				}

				evalRow++;
			}
			evalRow++;
		}

		for (LayoutEvalTool tool : layoutEvalTools) {
			evalCol = coords.evaluationsUpperLeft.column;

			insert(evalSheet, tool.getName(), evalRow, evalCol, STRING);
			evalCol++;
			insert(evalSheet, tool.getEvaluationId(), evalRow, evalCol, STRING);

			for (LayoutEvalTool.LayoutEvaluation eval : tool.getEvaluations()) {

				int mycolumn = evalCol + 8;
				insert(evalSheet, eval.overallWeightedAreaSuccessRate, evalRow,
						mycolumn, DOUBLE);
				mycolumn++;
				insert(evalSheet, eval.overallWeightedCountSuccessRate,
						evalRow, mycolumn, DOUBLE);

				evalRow++;
			}
			evalRow++;
		}

		int urlCol = coords.inputUrlsUpperLeftHeader.column;
		int urlRow = coords.inputUrlsUpperLeftHeader.row;
		int urlSheetNr = coords.inputUrlsUpperLeftHeader.sheet;
		Sheet urlSheet = workbook.getSheetAt(urlSheetNr);
		for (AnyTool tool : tools) {
			String service = tool.getName();
			List<URL> urls = tool.getInputUrl();
			if (urls.size() > 0) {
				urlRow = coords.inputUrlsUpperLeftHeader.row;
				insert(urlSheet, service, urlRow, urlCol, STRING);
				urlRow = coords.inputUrlsUpperLeftHeader.row + 1;
				for (URL url : urls) {
					insert(urlSheet, url.toString(), urlRow, urlCol, STRING);
					urlRow++;
				}
				urlCol++;
			}
		}

		evaluateFormulas(overviewSheet);

		return toStream(overviewSheet);
	}

	private Workbook openExcel(String urlString) throws InvalidFormatException,
			IOException {
		URL url;
		Workbook excelWorkbook = null;
		url = new URL(urlString);
		InputStream is = url.openStream();
		excelWorkbook = WorkbookFactory.create(is);
		is.close();

		return excelWorkbook;
	}

	private void insert(Sheet sheet, String value, int line, int column,
			int type) {
		Row row = sheet.getRow(line);
		if (row == null)
			row = sheet.createRow(line);
		Cell cell = row.getCell(column);
		if (cell == null)
			cell = row.createCell(column);

		switch (type) {
		case STRING:
			cell.setCellValue(value);
			break;
		case DOUBLE:
			cell.setCellValue(Double.valueOf(value));
			break;
		case DATE:
			cell.setCellValue(new Date(Long.valueOf(value)));
			break;
		default:
			cell.setCellValue(value);
		}

	}

	private void evaluateFormulas(Sheet sheet) {
		FormulaEvaluator evaluator = sheet.getWorkbook().getCreationHelper()
				.createFormulaEvaluator();

		Row row = null;
		Cell cell = null;

		for (int i = 0; i < 100; i++) {
			row = sheet.getRow(i);
			if (row == null)
				row = sheet.createRow(i);

			for (int j = 0; j < 50; j++) {
				cell = row.getCell(j);
				if (cell == null)
					cell = row.createCell(j);

				evaluator.evaluateFormulaCell(cell);
			}
		}
	}

	private InputStream toStream(Sheet sheet) throws IOException {
		Workbook excelWorkbook = sheet.getWorkbook();

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		excelWorkbook.write(os);
		InputStream is = new ByteArrayInputStream(os.toByteArray());

		os.close();
		return is;
	}

}
