package de.iip_ecosphere.platform.monitoring.prometheus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.services.environment.AbstractProcessService;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.LifecycleDescriptor;
import de.iip_ecosphere.platform.support.TimeUtils;

public class PrometheusLifecycleDescriptor implements LifecycleDescriptor {
    
    private static final String PROMETHEUS = "prometheus";
    private static final String VERSION = "2.34.0";
    private Process proc;  
    private File prometheusWorkingDirectory;
    
    @Override
    public void startup(String[] args) {
        
        String exeName = AbstractProcessService.getExecutableName(PROMETHEUS, VERSION);
        //File prometheusExe = new File("src/main/resources", exeName);
        String zipWindows = "src/main/resources/prometheus-2.34.0-win64.zip";
        String targetWindows = "src/main/resources";
        String zipLinux = "src/main/resources/prometheus-2.34.0.zip";
        String targetLinux = "src/main/resources";
        unzip(zipWindows, targetWindows);
        unzip(zipLinux, targetLinux);
        TimeUtils.sleep(5000);
        InputStream in = getClass().getClassLoader().getResourceAsStream(exeName);
        InputStream inYml = getClass().getClassLoader().getResourceAsStream("prometheus.yml");
        prometheusWorkingDirectory = FileUtils.createTmpFolder("iip-prometheus");
        try {   
            LoggerFactory
            .getLogger(PrometheusLifecycleDescriptor.class)
                .info(prometheusWorkingDirectory.getAbsolutePath());
            java.nio.file.Files.copy(
                    in, 
                    new File(prometheusWorkingDirectory, exeName).toPath(),  
                    StandardCopyOption.REPLACE_EXISTING);
            java.nio.file.Files.copy(
                    inYml,
                    new File(prometheusWorkingDirectory, "prometheus.yml").toPath(), 
                    StandardCopyOption.REPLACE_EXISTING);
            
            ProcessBuilder processBuilder = new ProcessBuilder(
                    new File(prometheusWorkingDirectory, exeName)
                    .getAbsolutePath()
                    , "--config.file=prometheus.yml"
                    );        
            processBuilder.directory(prometheusWorkingDirectory);
            processBuilder.inheritIO();
            proc = processBuilder.start();
            LoggerFactory.getLogger(PrometheusLifecycleDescriptor.class).info(PROMETHEUS + " " +  VERSION + " started");
        } catch (IOException e) {
            LoggerFactory.getLogger(PrometheusLifecycleDescriptor.class).error(e.getMessage(), e);
        }
    }
    
    /** Deletion method for testing purposes.
     * @param windowsBinary
     * @param linuxBinary
     * @throws IOException 
     */
    public static void deleteBinaries(File windowsBinary, File linuxBinary) throws IOException {
        if (true) {
            if (windowsBinary != null) {
                if (windowsBinary.isDirectory()) {
                    File[] files = windowsBinary.listFiles();
                    for (File f: files) {
                        deleteFile(f);
                    }
                }
            }
            if (windowsBinary != null) {
                if (windowsBinary.isDirectory()) {
                    File[] files = windowsBinary.listFiles();
                    for (File f: files) {
                        deleteFile(f);
                    }
                }
            }
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
                System.out.println("Unzipping to " + newFile.getAbsolutePath());
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
        LoggerFactory.getLogger(PrometheusLifecycleDescriptor.class).info(PROMETHEUS + " " +  VERSION + " shutdown");
        try {
            deleteBinaries(
                    new File("src/main/resources/prometheus-2.34.0-win64.exe"),
                    new File("src/main/resources/prometheus-2.34.0-linux64"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
