/**
 * ******************************************************************************
 * Copyright (c) {2026} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.oktoflow.platform.cmdTools.excel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import de.oktoflow.platform.cmdTools.MavenBuildExtractor;

/**
 * Results from {@link MavenBuildExtractor} to Excel converter.
 * 
 * @author Holger Eichelberger, SSE
 */
public class PluginResultsToExcel {
    
    private static Map<String, Row> rows = new HashMap<>();
    private static Set<String> titles = new HashSet<>();

    /**
     * Gets the maximum number of files existing.
     * 
     * @param folder the folder containing the file
     * @param prefix the file name prefix
     * @return the maximum number of files
     */
    private static int maxFile(File folder, String prefix) {
        int result = 0;
        File[] files = folder.listFiles();
        if (null != files) {
            for (File f : files) {
                String name = f.getName();
                if (f.isFile() && name.startsWith(prefix)) {
                    String nr = name.substring(prefix.length());
                    int pos = nr.indexOf(".");
                    if (pos > 0) {
                        nr = nr.substring(0, pos);
                    }
                    try {
                        result = Math.max(result, Integer.parseInt(nr));
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * Gets the file to read.
     * 
     * @param folder the folder containing the file
     * @param prefix the file name prefix
     * @param count the file number
     * @return the file, may not exist
     */
    private static File getFile(File folder, String prefix, int count) {
        String tmp = String.valueOf(count);
        File file = new File(folder, prefix + tmp + ".csv");
        if (!file.exists()) {
            if (count < 10) {
                tmp = "0" + tmp;
            }
            file = new File(folder, prefix + tmp + ".csv");
        }
        return file;
    }
    
    /**
     * Returns the row key for {@code rowNr} in {@code sheet}.
     * 
     * @param sheet the sheet
     * @param rowNr the row number
     * @return the row key
     */
    private static String getRowKey(Sheet sheet, int rowNr) {
        return sheet.getSheetName() + "/" + rowNr;
    }

    /**
     * Returns the row object for {@code rowNr} in {@code sheet}. After creating a row object, somehow it's gone.
     * 
     * @param sheet the sheet
     * @param rowNr the row number
     * @return the row object, may be <b>null</b>
     */
    private static Row getRow(Sheet sheet, int rowNr) {
        return rows.get(getRowKey(sheet, rowNr));
    }

    /**
     * Returns whether there is a known row object for {@code rowNr} in {@code sheet}.
     * 
     * @param sheet the sheet
     * @param rowNr the row number
     * @return {@code true} if there is a row object, {@code false} else
     */
    private static boolean hasRow(Sheet sheet, int rowNr) {
        return getRow(sheet, rowNr) != null;
    }
    
    /**
     * Ensures and gets row {@code rowNr} in {@code sheet}.
     * 
     * @param sheet the sheet to modify
     * @param rowNr the row number
     * @return the row
     */
    private static Row obtainRow(Sheet sheet, int rowNr) {
        Row result = getRow(sheet, rowNr);
        if (null == result) {
            for (int i = sheet.getLastRowNum(); i <= rowNr; i++) {
                if (!hasRow(sheet, i)) {
                    result = sheet.createRow(i);
                    rows.put(getRowKey(sheet, i), result);
                }
            }
        }
        return null == result ? sheet.getRow(rowNr) : result;
    }

    /**
     * Sets the cell value, considering potential numbers, doubles, etc.
     * 
     * @param row the row to set the value
     * @param cNr the cell number
     * @param tok the token to be turned into number, double, string
     */
    private static void setCellValue(Row row, int cNr, String tok) {
        boolean isNr = tok.length() > 0;
        boolean hasDot = false;
        for (int t = 0; isNr && t < tok.length(); t++) {
            char c = tok.charAt(t);
            isNr = Character.isDigit(c) || c == '.';
            hasDot |= c == '.';
        }
        if (isNr) {
            if (hasDot) {
                row.createCell(cNr).setCellValue(Double.valueOf(tok));    
            } else {
                row.createCell(cNr).setCellValue(Integer.valueOf(tok));    
            }
        } else {
            row.createCell(cNr).setCellValue(tok);    
        }
    }

    /**
     * Turns CSV to excel.
     * 
     * @param file the file to read
     * @param sheet the sheet to insert the data into
     * @param space additional space
     */
    private static void csvToExcel(File file, Sheet sheet, int startRow, int rowSpace, int count) {
        System.out.println(" Processing " + file.getName());        
        int colNr = (count - 1) *  5;
        Row titleRow = obtainRow(sheet, 0);
        String titleKey = sheet.getSheetName() + "/" + count;
        if (!titles.contains(titleKey)) {
            titleRow.createCell(colNr).setCellValue("run-" + count);
            titles.add(titleKey);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, colNr, colNr + 4));
        }
        if (file.exists()) {
            try {
                List<String> lines = Files.readAllLines(file.toPath());
                int rowNr = startRow;
                for (String l : lines) {
                    StringTokenizer tokens = new StringTokenizer(l, ",");
                    Row row = obtainRow(sheet, rowNr);
                    int cNr = colNr;
                    while (tokens.hasMoreTokens()) {
                        String tok = tokens.nextToken().trim();
                        setCellValue(row, cNr, tok);
                        cNr++;
                    }
                    rowNr++;
                }
                /*for (int i = rowNr; i < startRow + rowSpace; i++) {
                    sheet.getRow(rowNr);
                    rowNr++;
                }*/
            } catch (IOException e) {
                System.err.println("Cannot read " + file + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Performs the conversion.
     * 
     * @param args command line arguments, input folder and output excel file
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java PluginResultsToExcel <folder> <output.xslx>");
            System.exit(1);
        }

        Workbook workbook = new XSSFWorkbook();
        File inputFolder = new File(args[0]);
        File[] experiments = inputFolder.listFiles();
        if (null != experiments) {
            for (File ex : experiments) {
                if (ex.isDirectory()) {
                    Sheet sheet = workbook.createSheet(ex.getName());
                    int maxFiles = maxFile(ex, "mvn-");
                    System.out.println(ex + " number runs: " + maxFiles);                    
                    for (int i = 1; i <= maxFiles; i++) {
                        csvToExcel(getFile(ex, "mvn-", i), sheet, 1, 15, i);
                        csvToExcel(getFile(ex, "basyx-", i), sheet, 15, 15, i);
                        csvToExcel(getFile(ex, "plugins-", i), sheet, 30, 15, i);
                    }
                }
            }
        } else {
            System.err.println("Excel file will remain empty as no folders found in " + args[0]);
        }
        
        try {
            FileOutputStream fos = new FileOutputStream(new File(args[1]));
            workbook.write(fos);
            fos.close();
            workbook.close();
        } catch (IOException e) {
            System.err.println("Cannot write " + args[1] + ": " + e.getMessage());
        }
    }

}
