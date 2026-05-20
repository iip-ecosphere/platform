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

package de.oktoflow.platform.cmdTools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Parses Jenkins overview pages, in particular tasks and related build id and build time.
 * 
 * @author Holger Eichelberger, SSE
 */
public class JenkinsHtmlParser {

    /**
     * A parse result.
     * 
     * @author Holger Eichelberger, SSE
     */
    private static class ParseResult {
        private int value;
        private String text;
    }
    
    /**
     * Parses a (time) unit.
     * 
     * @param text the text to parse
     * @param units  the available units
     * @return the parse result
     */
    private static ParseResult parseUnit(String text, String... units) {
        ParseResult result = new ParseResult();
        text = text.trim();
        for (String u : units) {
            if (text.endsWith(u)) {
                text = text.substring(0, text.length() - u.length()).trim();
                int pos = text.length() - 1;
                while (pos > 0 && (Character.isDigit(text.charAt(pos)) || text.charAt(pos) == '.')) {
                    pos--;
                }
                String number = text.substring(pos).trim();
                int dot = number.indexOf('.');
                if (dot > 0) {
                    number = number.substring(0, dot);
                }
                result.value = Integer.parseInt(number);
                text = text.substring(0, text.length() - number.length());
            }
        }
        result.text = text;
        return result;
    }
    
    /**
     * Parses time.
     * 
     * @param text the text to be parsed
     * @return the time
     */
    private static int parseTime(String text) {
        ParseResult ms = parseUnit(text, "ms");
        ParseResult sec = parseUnit(ms.text, "Sekunden", "Sekunde");
        ParseResult min = parseUnit(sec.text, "Minuten", "Minute");
        System.out.println(min.value + " min " + sec.value + " sec " + ms.value + " ms");
        return min.value * 60 * 1000 + sec.value * 1000 + ms.value;
    }

    /**
     * Parses a Jenkins HTML page.
     * 
     * @param args the command line arguments, first the file to be parsed, second optional the output file
     * @throws IOException if the file cannot be read
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 1 || args.length > 2) {
            System.out.println("Usage: file.html [out.csv]");
        } else {
            File input = new File(args[0]);
            File output;
            if (args.length == 2) {
                output = new File(args[1]);
            } else {
                String path = input.getPath();
                int pos = path.lastIndexOf(".");
                if (pos > 0) {
                    path = path.substring(0, pos);
                }
                output = new File(path + ".csv");
            }
            int rowCount = 0;
            Document doc = Jsoup.parse(input, "UTF-8");
            Elements elts = doc.select("tr#job_IIP_buildData");
            if (elts.size() > 0) {
                try (FileWriter writer = new FileWriter(output)) {
                    writer.write("Task Name\tBuild ID\tDisplay Build Time [ms]\tBuildTime [ms]\n");
                    Element table = elts.get(0).parent();
                    Elements rows = table.select("tr");
                    for (Element row : rows) {
                        Elements cols = row.select("td");
                        if (cols.size() >= 3) {
                            Element taskCell = cols.get(2);
                            Element taskNameElt = taskCell.selectFirst("span");
                            String taskName = taskNameElt.text().replace("wbr", "");
                            System.out.println("TASK " + taskName);
                            Element buildIdCell = cols.get(3);
                            Element buildIdElt = buildIdCell.selectFirst("a");
                            String buildId = buildIdElt.text();
                            System.out.println("BUILD " + buildId);
                            String time = cols.get(5).text();
                            String timeMs = cols.get(5).attribute("data").getValue();
                            System.out.println("Time " + time);
                            int buildTime = parseTime(time);
                            writer.write(taskName + "\t" + buildId + "\t" + buildTime + "\t" + timeMs + "\n");
                            rowCount++;
                        }
                    }
                }
                System.out.println("Data extracted to " + output + " containing " + rowCount + " data rows");
            } else {
                System.err.println("Build data table elements found. Please check structure!");
            }
        }
    }
    
}
