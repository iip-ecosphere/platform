/**
 * ******************************************************************************
 * Copyright (c) {2022} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.security.services.kodex;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import de.iip_ecosphere.platform.security.services.kodex.KodexRestService;
import de.iip_ecosphere.platform.services.environment.ServiceKind;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.services.environment.YamlProcess;
import de.iip_ecosphere.platform.services.environment.YamlService;
import de.iip_ecosphere.platform.support.TimeUtils;
import de.iip_ecosphere.platform.support.iip_aas.Version;
import de.iip_ecosphere.platform.transport.connectors.ReceptionCallback;

/**
 * Tests the KODEX local server. The utilized REST framework is just for testing, no production use!
 * 
 * @author Marcel Nöhre
 */
public class KodexRestServiceTest {
    
    private static boolean measure = Boolean.valueOf(System.getProperty("kodex.measure", "false"));
    private long startTime;
    private long endTime;
    private long count = 0;
    private ArrayList<Long[]> runtime = new ArrayList<Long[]>();
    
    /**
     * Set the start time of the service.
     * 
     * @param start the start time
     */
    private void setStartTime(long start) {
        startTime = start;
    }
    
    /**
     * Set the new end time of the service.
     * 
     * @param end the end time
     */
    private void setEndTime(long end) {
        endTime = end;
    }
    
    /**
     * Set the new count of the tuple.
     * 
     * @param newCount the count value
     */
    private void setCount(long newCount) {
        count = newCount;
    }
    
    /**
     * Returns the start time of the service.
     * 
     * @return startTime
     */
    private long getStartTime() {
        return startTime;
    }
    
    /**
     * Returns the end time of the service.
     * 
     * @return endTime
     */
    private long getEndTime() {
        return endTime;
    }
    
    /**
     * Returns counter of the tuple.
     * 
     * @return endTime
     */
    private long getCount() {
        return count;
    }
    
    /**
     * Calculate the runtime.
     * 
     * @return the runtime
     */
    private long calcRuntime() {
        return getEndTime() - getStartTime();
    }
    
    /**
     * Save runtime results as excel-file.
     * 
     * @param runtimeList list of runtime measures
     * @throws IOException if creating the file fails
     */
    private void createExcelFile(ArrayList<Long[]> runtimeList) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("RestRuntime");
        String[] columnHeadings = {"Tupel amount", "Runtime in ms"}; 
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerFont.setColor(IndexedColors.BLACK.index);
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < columnHeadings.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columnHeadings[i]);
            cell.setCellStyle(headerStyle);
        }
        CellStyle contentStyle = workbook.createCellStyle();
        contentStyle.setAlignment(HorizontalAlignment.CENTER);
        int rowCount = 1;
        for (Long[] item : runtime) {
            Row row = sheet.createRow(rowCount++);
            Cell tupelAmount = row.createCell(0);
            tupelAmount.setCellValue(item[0]);
            tupelAmount.setCellStyle(contentStyle);
            Cell runtime = row.createCell(1);
            runtime.setCellValue(item[1]);
            runtime.setCellStyle(contentStyle);
        }
        for (int i = 0; i < columnHeadings.length; i++) {
            sheet.autoSizeColumn(i);
        }
        File folder = new File("./measures");
        folder.mkdirs();
        File output = new File(folder, "RestRuntime.xlsx");
        FileOutputStream fileOutputStream = new FileOutputStream(output);
        workbook.write(fileOutputStream);
        fileOutputStream.close();
        workbook.close();
        LoggerFactory.getLogger(KodexRestServiceTest.class).info(
            "Excel file (RestRuntime.xlsx) with runtime measures created in {}", output);
    }
    
    /**
     * Processes {@code data} on {@code service} and logs the sent input.
     * 
     * @param service the service instance
     * @param data the input data
     * @throws IOException if processing/serializing the input data fails
     */
    private static void process(KodexRestService<InData, OutData> service, InData data) throws IOException {
        if (!measure) {
            LoggerFactory.getLogger(KodexRestServiceTest.class).info("Input: {"
                + "id=\"" + data.getId() + "\" name=\"" + data.getName() + "\"}");
        }
        service.process(data);
    }
    
    /**
     * Tests the KODEX local server.
     * 
     * @throws IOException if reading test data fails, shall not occur
     * @throws ExecutionException shall not occur 
     */
    @Test
    public void testKodexRestService() throws IOException, ExecutionException {
        AtomicInteger receivedCount = new AtomicInteger(0);
        ReceptionCallback<OutData> rcp = new ReceptionCallback<OutData>() {

            @Override
            public void received(OutData data) {
                Assert.assertTrue(data.getId() != null && data.getId().length() > 0);
                Assert.assertTrue(data.getName() != null && data.getName().length() > 0);
                Assert.assertTrue(data.getKip() != null && data.getKip().length() > 0);
                receivedCount.incrementAndGet();
                if (!measure) {
                    LoggerFactory.getLogger(KodexRestServiceTest.class).info("Received result: {kip=\"" + data.getKip() 
                        + "\" id=\"" + data.getId() + "\" name=\"" + data.getName() + "\"}");
                }
                setCount(getCount() + 1);
                boolean smallCounts = getCount() == 1 || getCount() == 10 || getCount() == 100;
                if (smallCounts || getCount() == 1000 || getCount() == 10000 || getCount() == 15000) {
                    setEndTime(System.currentTimeMillis());
                    Long[] tmp = {getCount(), calcRuntime()};
                    runtime.add(tmp);
                }
            }

            @Override
            public Class<OutData> getType() {
                return OutData.class;
            }
        };

        // mock the YAML service instance, as if read from a descriptor
        YamlService sDesc = new YamlService();
        sDesc.setName("KodexRestTest");
        sDesc.setVersion(new Version(KodexRestService.VERSION));
        sDesc.setKind(ServiceKind.TRANSFORMATION_SERVICE);
        sDesc.setId("KodexRestTest");
        sDesc.setDeployable(true);
        YamlProcess pDesc = new YamlProcess();
        pDesc.setExecutablePath(new File("./src/main/resources/"));
        pDesc.setHomePath(new File("./src/test/resources"));
        sDesc.setProcess(pDesc);
        
        // just that the constructor is called, throw away
        new KodexRestService<>(new InDataJsonTypeTranslator(), new OutDataJsonTypeTranslator(), rcp, sDesc);
        // test implementation
        KodexRestService<InData, OutData> service = new KodexRestService<>(
            new InDataJsonTypeTranslator(), new OutDataJsonTypeTranslator(), rcp, sDesc, "example-data.yml");
        service.setState(ServiceState.STARTING);
        setCount(0);
        setStartTime(System.currentTimeMillis());
        int max = measure ? 15000 : 100;
        for (int i = 0; i < max; i++) {
            process(service, new InData("test", "test"));
        }
        TimeUtils.sleep(2500);
        LoggerFactory.getLogger(KodexRestServiceTest.class).info("Stopping service, may take two minutes on Windows");
        service.setState(ServiceState.STOPPING);     
        Assert.assertTrue(receivedCount.get() > 0); // fluctuating on Jenkins, = max would be desirable
        LoggerFactory.getLogger(KodexRestServiceTest.class).info("Activating/Passivating");
        service.activate();
        LoggerFactory.getLogger(KodexRestServiceTest.class).info(
            "Passivating service, may take two minutes on Windows");
        service.passivate();
        createExcelFile(runtime);
    }
}