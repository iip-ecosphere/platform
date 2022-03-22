package de.iip_ecosphere.platform.monitoring.prometheus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang.SystemUtils;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.services.environment.AbstractProcessService;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.LifecycleDescriptor;

public class PrometheusLifecycleDescriptor implements LifecycleDescriptor {
    
    private static final String PROMETHEUS = "prometheus";
    private static final String PROMETHEUS_VERSION = "2.34.0";
    private static final String PROMETHEUS_CONFIG = "prometheus.yml";
    private static final String PROMETHEUS_ZIP_WINDOWS = "src/main/resources/prometheus-2.34.0-win64.zip";
    private static final String PROMETHEUS_BINARY_WINDOWS = "src/main/resources";
    private static final String PROMETHEUS_ZIP_LINUX = "src/main/resources/prometheus-2.34.0.zip";
    private static final String PROMETHEUS_BINARY_LINUX = "src/main/resources";
    private Process proc;  
    private File prometheusWorkingDirectory;
    private String exeName;
    private Path fromBinary;
    private Path toBinary;
    private Path fromYml;
    private Path toYml;
    @Override
    public void startup(String[] args) {
        if (SystemUtils.IS_OS_WINDOWS) {
            exeName = AbstractProcessService.getExecutableName(PROMETHEUS, PROMETHEUS_VERSION);   
            unzip(PROMETHEUS_ZIP_WINDOWS, PROMETHEUS_BINARY_WINDOWS);
        } else if (SystemUtils.IS_OS_LINUX) {
            exeName = PROMETHEUS + "-" + PROMETHEUS_VERSION + "-linux64";
            unzip(PROMETHEUS_ZIP_LINUX, PROMETHEUS_BINARY_LINUX);
        } else {
            LoggerFactory
            .getLogger(PrometheusLifecycleDescriptor.class)
                .info("Other OS other than Windows and Linux not supported.");
        }
        prometheusWorkingDirectory = FileUtils.createTmpFolder("iip-prometheus");
        try {   
            LoggerFactory
            .getLogger(PrometheusLifecycleDescriptor.class)
                .info(prometheusWorkingDirectory.getAbsolutePath());
            fromBinary = Paths.get(PROMETHEUS_BINARY_WINDOWS, exeName);            
            toBinary = Paths.get(prometheusWorkingDirectory.getAbsolutePath(), exeName);
            fromYml = Paths.get(PROMETHEUS_BINARY_WINDOWS, PROMETHEUS_CONFIG);                
            toYml = Paths.get(prometheusWorkingDirectory.getAbsolutePath(), PROMETHEUS_CONFIG);
            copy(fromBinary.toFile(), toBinary.toFile());
            copy(fromYml.toFile(), toYml.toFile());
            ProcessBuilder processBuilder = null;
            if (SystemUtils.IS_OS_WINDOWS) { 
                processBuilder = new ProcessBuilder(
                        new File(prometheusWorkingDirectory, exeName)
                        .getAbsolutePath()
                        , "--config.file=prometheus.yml"
                        );        
                processBuilder.directory(prometheusWorkingDirectory);
                processBuilder.inheritIO();   
            } else if (SystemUtils.IS_OS_LINUX) {
                @SuppressWarnings("unused")
                Process p = Runtime.getRuntime().exec(
                        "chmod u+x " + new File(prometheusWorkingDirectory, exeName)
                        .getAbsolutePath());
                processBuilder = new ProcessBuilder(
                        new File(prometheusWorkingDirectory, exeName)
                        .getAbsolutePath()
                        , "--config.file=prometheus.yml");
                processBuilder.directory(prometheusWorkingDirectory);
                processBuilder.inheritIO();
            } else {
                LoggerFactory.getLogger(
                        PrometheusLifecycleDescriptor.class)
                        .info("Other OS other than Windows and Linux not supported.");
            }
            proc = processBuilder.start();
            LoggerFactory.getLogger(PrometheusLifecycleDescriptor.class)
                .info(PROMETHEUS + " " +  PROMETHEUS_VERSION + " started");
        } catch (IOException e) {
            LoggerFactory.getLogger(PrometheusLifecycleDescriptor.class).error(e.getMessage(), e);
        }
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
     * Deletes all files used in prometheus run.
     */
    public void deleteWorkingFiles() {
        try {
            deleteFile(fromBinary.toFile());
            deleteFile(toBinary.toFile());
            deleteFile(toYml.toFile());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    /**
     * Delete File.
     * @param file
     * @throws IOException 
     */
    private static void deleteFile(File file) throws IOException {
        Files.deleteIfExists(file.toPath());
    }

    /**
     * Method to unzip archive.
     * @param zipFilePath
     * @param destDir
     */
    private static void unzip(String zipFilePath, String destDir) {
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
    
    @Override
    public void shutdown() {
        proc.destroyForcibly();
        LoggerFactory.getLogger(PrometheusLifecycleDescriptor.class)
            .info(PROMETHEUS + " " +  PROMETHEUS_VERSION + " shutdown");
    }

    @Override
    public Thread getShutdownHook() {
        return null;
    }

    @Override
    public int priority() {
        return LifecycleDescriptor.INIT_PRIORITY;
    }
}
