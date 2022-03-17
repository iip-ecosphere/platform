package de.iip_ecosphere.platform.monitoring.prometheus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.StandardCopyOption;

import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.services.environment.AbstractProcessService;
import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.LifecycleDescriptor;

public class PrometheusLifecycleDescriptor implements LifecycleDescriptor {
    
    private static final String PROMETHEUS = "prometheus";
    private static final String VERSION = "2.34.0";
    private Process proc;  
    private File prometheusWorkingDirectory;
    @Override
    public void startup(String[] args) {
        
        String exeName = AbstractProcessService.getExecutableName(PROMETHEUS, VERSION);
        //File prometheusExe = new File("src/main/resources", exeName);
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
   
    @Override
    public void shutdown() {
        proc.destroyForcibly();
        LoggerFactory.getLogger(PrometheusLifecycleDescriptor.class).info(PROMETHEUS + " " +  VERSION + " shutdown");

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
