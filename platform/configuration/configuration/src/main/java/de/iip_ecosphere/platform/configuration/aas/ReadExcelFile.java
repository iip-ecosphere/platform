/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.configuration.aas;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import de.iip_ecosphere.platform.support.logging.Logger;
import de.iip_ecosphere.platform.support.logging.LoggerFactory;

/**
 * Reads an IDTA spec with pages in excel sheets and contents/tables in formatted excel cells, as e.g., created by 
 * smallpdf.com.
 * 
 * @author Holger Eichelberger, SSE
 */
public class ReadExcelFile {

    /**
     * Reads data from an excel file, e.g., created by smallpdf.com.
     *  
     * @param fileName the input file name
     * @return an AAS spec summary
     * @throws IOException if the file cannot be read
     */
    public static AasSpecSummary read(String fileName) throws IOException {
        FileInputStream fis = new FileInputStream(fileName);
        
        //Create Workbook instance for xlsx/xls file input stream
        Workbook workbook = null;
        if (fileName.toLowerCase().endsWith("xlsx")) {
            workbook = new XSSFWorkbook(fis);
        } else if (fileName.toLowerCase().endsWith("xls")) {
            workbook = new HSSFWorkbook(fis);
        }

        final DataFormatter fmt = new DataFormatter();
        // iterate sheets, rows cells and add data to rowProcessor
        int numberOfSheets = workbook.getNumberOfSheets();
        RowProcessor rowProcessor = new RowProcessor();
        for (int s = 0; s < numberOfSheets; s++) {
            Sheet sheet = workbook.getSheetAt(s);
            Set<String> merged = new HashSet<>();
            // collect merged cells for easy lookup
            for (int m = 0; m < sheet.getNumMergedRegions(); m++) { // topic: smartpdf merged cells
                CellRangeAddress cra = sheet.getMergedRegion(m);
                for (int r = cra.getFirstRow(); r <= cra.getLastRow(); r++) {
                    for (int c = cra.getFirstColumn(); c <= cra.getLastColumn(); c++) {
                        if (r != cra.getFirstRow() || c != cra.getFirstColumn()) { // keep first for reading
                            merged.add(r + "_" + c); 
                        }
                    }
                }
            }
            getLogger().info("Processing {}", sheet.getSheetName());
            Iterator<Row> rowIterator = sheet.iterator();
            int rowCount = 0;
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                rowProcessor.startRow();
                Iterator<Cell> cellIterator = row.cellIterator();
                int colCount = 0;
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    if (!merged.contains(rowCount + "_" + colCount)) { // excluded merged cells, read just first 
                        String data = null;
                        switch(cell.getCellType()) {
                        case STRING:
                            data = getCellStringValue(cell);
                            break;
                        case BOOLEAN:
                            data = String.valueOf(cell.getBooleanCellValue());
                            break;
                        case BLANK:
                            data = null;
                            break;
                        case NUMERIC:
                            data = fmt.formatCellValue(cell); // turns int from double into int based on format
                            break;
                        default:
                            getLogger().warn("Unexpected XLS cell type: {}", cell.getCellType());
                            break;
                        }
                        rowProcessor.addDataToRow(data);
                    }
                    colCount++;
                }
                rowProcessor.endRow();
                rowCount++;
            }
        }
        rowProcessor.readingCompleted();
        fis.close();
        return rowProcessor.getSummary();
    }
   
    /**
     * Reads data from an excel file, e.g., created by smallpdf.com.
     *  
     * @param fileName the input file name
     * @param outFile the output file, <b>null</b> for sysout
     */
    public static void readExcelData(String fileName, String outFile) {
        readExcelData(fileName, outFile, null);
    }
    
    /**
     * Reads data from an excel file, e.g., created by smallpdf.com.
     *  
     * @param fileName the input file name
     * @param outFile the output file, <b>null</b> for sysout
     * @param idShortPrefix optional prefix for idShorts, may be <b>null</b> (use "name" instead)
     */
    public static void readExcelData(String fileName, String outFile, String idShortPrefix) {
        try {
            AasSpecSummary result = read(fileName);
            result.printStatistics(System.out);
            IvmlWriter writer = new IvmlWriter(outFile)
                .setNamePrefix(idShortPrefix);
            writer.toIvml(result);
            writer.toIvmlText(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Returns the cell string value, considering different font formatting probably indicating footnotes to be removed.
     * Only to be applied if the cell has a string value.
     * 
     * @param cell the cell
     * @return the string value
     */
    private static String getCellStringValue(Cell cell) {
        String data;
        RichTextString rts = cell.getRichStringCellValue();
        if (rts instanceof XSSFRichTextString) { // detect formatting "anomalies" and react on
            XSSFRichTextString xrts = (XSSFRichTextString) rts;
            data = xrts.getString();
            if (null != data && data.length() > 0 && data.trim().startsWith("[")) {
                StringBuilder tmp = new StringBuilder(data);
                XSSFFont startFont = xrts.getFontAtIndex(0);
                XSSFFont currentFont = startFont;
                for (int i = 1; i < tmp.length(); i++) {
                    XSSFFont font = xrts.getFontAtIndex(i);
                    boolean fontDiff = false;
                    if (null == currentFont) {
                        fontDiff = font != null;
                    } else {
                        fontDiff = !currentFont.equals(font);
                    }
                    if (fontDiff) {
                        char c = tmp.charAt(i);
                        if (!startFont.equals(font) 
                            && (startFont.getFontHeight() - font.getFontHeight() > 1) // accidental, IDTA-02006-2-0 
                            && Character.isDigit(c)) { // probably footnote IDTA-02002-1-0
                            tmp.deleteCharAt(i);
                            i--;
                        }
                    }
                    currentFont = font;
                }
                data = tmp.toString();
            }
        } else { // unknown how to extract font types from RichTextString directly
            data = cell.getStringCellValue();
        }
        return data;
    }
 
    /**
     * Returns the logger instance for this class.
     * 
     * @return the logger instance
     */
    private static Logger getLogger() {
        return LoggerFactory.getLogger(ReadExcelFile.class);
    }

    /**
     * Executes the program. 
     * 
     * @param args command line arguments, first the file to parse, second the target file, 
     *     the third the optional idShort prefix
     */
    public static void main(String... args) {
        if (args.length == 1) {
            readExcelData(args[0], null);
        } else if (args.length >= 2) {
            readExcelData(args[0], args[1], args.length >= 3 ? args[2] : null);
        } else {
            String baseDir = "src/test/resources/idta/";
            //readExcelData(baseDir + "2001/IDTA 02001-1-0_Submodel_MTP.print.xlsx", null);
            //readExcelData(baseDir + "2002/IDTA-02002-1-0_Submodel_ContactInformation.xlsx", null);
            //readExcelData(baseDir + "2003/IDTA-02003-1-2_Submodel_TechnicalData.xlsx", null);
            //readExcelData(baseDir + "2004/IDTA 02004-1-2_Submodel_Handover Documentation.xlsx", null);
            //readExcelData(baseDir + "2005/IDTA 02005-1-0_Submodel_ProvisionOfSimulationModels.xlsx", null);
            //readExcelData(baseDir + "2006/IDTA-02006-2-0_Submodel_Digital-Nameplate.xlsx", null);
            //readExcelData(baseDir + "2007/IDTA-02007-1-0_Submodel_Software-Nameplate.xlsx", null);
            //readExcelData(baseDir + "2008/IDTA 02008-1-1_Submodel_TimeSeriesData.xlsx", null);
            //readExcelData(baseDir + "2010/IDTA 02010-1-0_Submodel_ServiceRequestNotification.xlsx", null);
            //readExcelData(baseDir + "2011/IDTA-02011-1-0_Submodel_HierarchicalStructuresEnablingBoM.xlsx", null);
            //readExcelData(baseDir + "2012/IDTA 02012-1-0_Submodel_DEXPI.xlsx", null);
            //readExcelData(baseDir + "2013/IDTA 02013-1-0_Submodel_Reliability.xlsx", null, "Ry");
            //readExcelData(baseDir + "2014/IDTA 02014-1-0_Submodel_FunctionalSafety.xlsx", null, "Fs");
            //readExcelData(baseDir + "2015/IDTA 02015-1-0 _Submodel_ControlComponentType.print.xlsx", null, "Ct");
            //readExcelData(baseDir + "2016/IDTA 02016-1-0 _Submodel_ControlComponentInstance.xlsx", null, "Ci");
            //readExcelData(baseDir + "2017/IDTA 02017-1-0_Submodel_Asset Interfaces Description.mod.xlsx", null);
            //readExcelData(baseDir + "2021/IDTA 02021-1-0_Submodel_Sizing of Power Drive Trains.xlsx", null);
            //readExcelData(baseDir + "2022/IDTA 02022-1-0_Submodel_Wireless Communication.xlsx", null);
            //readExcelData(baseDir + "2023/IDTA 2023-0-9 _Submodel_CarbonFootprint.xlsx", null);
            //readExcelData(baseDir + "2023-01-24 - Draft_IDTA_Submodel_PCF-mod.xlsx", null, "Draft");
            //readExcelData(baseDir + "2026/IDTA_02026-1-0_Submodel_ProvisionOf3DModels.xlsx", null);
            //readExcelData(baseDir + "2027/IDTA 02027-1-0_Submodel_AssetInterfacesMappingConfiguration.xlsx", null);
            //readExcelData(baseDir + "2034/IDTA 02034-1-0 Submodel_CreationAndClassificationOfMaterial.xlsx", null);
            //readExcelData(baseDir + "2045/IDTA 02045-1-0_Submodel_Data Model for Asset Location.xlsx", null);
            //readExcelData(baseDir + "2046/IDTA 02046-1-0_Submodel_WorkstationWorkerMatchingData.xlsx", null);
            readExcelData(baseDir + "2056/IDTA 02056-1-0_Submodel_Data Retention Policies.xlsx", null);
        }
    }

}