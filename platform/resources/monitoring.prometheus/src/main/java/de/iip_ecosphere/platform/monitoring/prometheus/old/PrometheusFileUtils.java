package de.iip_ecosphere.platform.monitoring.prometheus.util;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.LoggerFactory;
import de.iip_ecosphere.platform.monitoring.prometheus.PrometheusLifecycleDescriptor;

/** Utility class purposes such as adding an exporter to the prometheus config-file or alerting rules.
 * Intended use is to invoke it once before the prometheus and altermananger etc. start 
 * and adding/removing/changing client information while the Server is running.
 * @author bettelsc
 */
public class PrometheusFileUtils {

    private String pathToPrometheusConfig;
    private File prometheusConfig;
    private String content;
    
    /** Basic Constructor for path.
     * @param pathToPrometheusConfig
     */
    public PrometheusFileUtils(String pathToPrometheusConfig) {
        this.pathToPrometheusConfig = pathToPrometheusConfig;
    }
    /** Constructor in case the file is already made locally.
     * 
     * @param prometheusConfig
     */
    public PrometheusFileUtils(File prometheusConfig) {
        
    }
    /** Stores the content of a file as a String.
     * JDK 8 or higher required. 
     * @return content
     * @throws IOException 
     */
    public String contentAsString() throws IOException {        
        content = new String(Files.readAllBytes(Paths.get(pathToPrometheusConfig)));
        return content;
    }
    
    /** Displays all the lines.
     * 
     */
    public void displayLines() {
        ArrayList<String> lines = getLinesAsArray();
        int i = 1;
        for (String line : lines) {
            System.out.println("Line " + i + ": " + line);
            i++;
        }
    }
    /** extracts Relevant information from config-file e.g the line where the clients description begins.
     * 
     * @return configRelevancies
     */
    @SuppressWarnings("unused")
    private ArrayList<ConfigTriplet> extractInformation() {
        ArrayList<String> lines = getLinesAsArray();
        ArrayList<ConfigTriplet> triplets = new ArrayList<>();  
        return triplets;
    }
    
    /** Get all lines as a array.
     * 
     * @return lines
     */
    private ArrayList<String> getLinesAsArray() {
        ArrayList<String> lines = new ArrayList<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(
                    pathToPrometheusConfig));
            String line = reader.readLine();
            lines.add(line);
            while (line != null) {
                line = reader.readLine();
                lines.add(line);
            }
            reader.close(); 
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    } 
    /**
     * Alternative copy method.
     * @param inBin
     * @param inYml
     * @param promWorkingDirectory
     * @param exeName
     * @throws IOException 
     */
    public static void filesCopy(
            InputStream inBin, InputStream inYml, File promWorkingDirectory, String exeName) throws IOException {
        java.nio.file.Files.copy(
                inBin, 
                new File(promWorkingDirectory, exeName).toPath(),  
                StandardCopyOption.REPLACE_EXISTING);
        java.nio.file.Files.copy(
                inYml,
                new File(promWorkingDirectory, "prometheus.yml").toPath(), 
                StandardCopyOption.REPLACE_EXISTING);
    }
    /**
     * Copy method.
     * @param src
     * @param dest
     * @throws IOException
     */
    public static void copy(File src, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(src);
            os = new FileOutputStream(dest);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buf)) > 0) {
                os.write(buf, 0, bytesRead);
            }
        } finally {
            is.close();
            os.close();
        }
    }
    /**
     * Delete File.
     * @param file
     * @throws IOException 
     */
    public static void deleteFile(File file) throws IOException {
        Files.deleteIfExists(file.toPath());
    }
    /**
     * Method to unzip archive.
     * @param zipFilePath
     * @param destDir
     */
    public static void unzip(String zipFilePath, String destDir) {
        File dir = new File(destDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        FileInputStream fis;
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(destDir + File.separator + fileName);
                LoggerFactory.getLogger(
                        PrometheusLifecycleDescriptor.class)
                        .info("Unzipping to " + newFile.getAbsolutePath());
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    /**
     * Getter for the path.
     * @return pathToPrometheusConfig
     */
    public String getPathToPrometheusConfig() {
        return pathToPrometheusConfig;
    }
    /**
     * Getter for the file.
     * @return prometheusConfig
     */
    public File getPrometheusConfig() {
        return prometheusConfig;
    }
}