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

package de.iip_ecosphere.platform.services.spring;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.deployer.spi.app.AppDeployer;
import org.springframework.cloud.deployer.spi.core.AppDeploymentRequest;
import org.springframework.cloud.deployer.spi.local.AbstractLocalDeployerSupport;
import org.springframework.cloud.deployer.spi.local.DebugAddress;
import org.springframework.cloud.deployer.spi.local.JavaCommandBuilder;
import org.springframework.cloud.deployer.spi.local.LocalDeployerProperties;
import org.springframework.core.io.Resource;

import de.iip_ecosphere.platform.support.FileUtils;
import de.iip_ecosphere.platform.support.JarUtils;

/**
 * A command builder that excepts classpath-based Spring service starting for ZIP service artifacts as well as JAR 
 * service artifacts. Unfortunately, the Spring deployers cannot easily be extended through regular mechanisms, so
 * we hook in via reflection (ugly). Unfortunately, the local deployer does not even pass on the work directory, so we 
 * have to "guess" it. The work folder in turn is needed as the services JVM will be started within and we need the JAR
 * files of the application unpacked there. 
 * 
 * @author Holger Eichelberger, SSE
 */
public class ClasspathJavaCommandBuilder extends JavaCommandBuilder {

    public static final String PROP_ZIP_CLASSPATH = "iip.spring.readZipClasspath"; // for testing
    private final LocalDeployerProperties properties;
    private String curDeployerId; // would not work with parallel deployments, hopefully they do not occur
    
    /**
     * Creates the command builder.
     * 
     * @param properties the local properties as setup of the local deployer
     */
    public ClasspathJavaCommandBuilder(LocalDeployerProperties properties) {
        super(properties);
        this.properties = properties;
    }
    
    // checkstyle: stop parameter number check
    
    @Override
    public ProcessBuilder buildExecutionCommand(AppDeploymentRequest request, Map<String, String> appInstanceEnv,
            String deployerId, Optional<Integer> appInstanceNumber, LocalDeployerProperties localDeployerProperties,
            Optional<DebugAddress> debugAddressOption) {
        
        this.curDeployerId = deployerId; // not clean
        
        return super.buildExecutionCommand(request, appInstanceEnv, deployerId, appInstanceNumber, 
            localDeployerProperties, debugAddressOption);
    }

    // checkstyle: resume parameter number check

    /**
     * Guess the working directory. Start with the root from properties, find folders that contain only of numbers
     * and contain {@link #curDeployerId} as sub-folder. Then sort according to the creation timestamp (we may
     * also take the folder name as it is a timestamp) and return the contained folder with {@link #curDeployerId}.
     * 
     * @return returns the working directory or as last resort a temporary one where the execution probably will fail
     */
    private File findWorkingDirectory() {
        File root = properties.getWorkingDirectoriesRoot().toFile();
        File[] cands = root.listFiles(new FilenameFilter() {
            
            @Override
            public boolean accept(File dir, String name) {
                boolean accept = false;
                if (name.matches("^\\d+$")) {
                    File f = new File(dir, name);
                    if (f.isDirectory()) {
                        String[] cont = f.list();
                        if (null != cont) {
                            for (String c : cont) {
                                if (c.equals(curDeployerId)) {
                                    accept = true;
                                }
                            }
                        }
                    }
                }
                return accept;
            }
        });
        if (null == cands || cands.length == 0) {
            return FileUtils.createTmpFolder(curDeployerId);
        } else if (cands.length == 1) {
            return new File(cands[0], curDeployerId);
        } else {
            Arrays.sort(cands, new Comparator<File>() {

                @Override
                public int compare(File o1, File o2) {
                    return -Long.compare(o1.lastModified(), o2.lastModified());
                }
            });
            return new File(cands[0], curDeployerId);
        }
    }

    /**
     * Returns the logger of this class.
     * 
     * @return the logger
     */
    private static Logger getLogger() {
        return LoggerFactory.getLogger(ClasspathJavaCommandBuilder.class);
    }
    
    /**
     * Copy shared JAR-files (non-recursive).
     * 
     * @param sharedLibs the shared libs folder
     * @param target the target folder
     * @return {@code true} if JAR-files were copied, {@code false} else
     */
    private boolean copySharedLibs(File sharedLibs, File target) {
        boolean copied = false;
        if (sharedLibs.exists()) {
            target.mkdirs();
            getLogger().info("Scanning " + sharedLibs + " for shared libraries");
            File[] files = sharedLibs.listFiles();
            if (null != files) {
                for (File f : files) {
                    if (f.getName().endsWith(".jar")) {
                        try {
                            Files.copy(f.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                            copied = true;
                        } catch (IOException e) {
                            getLogger().error("Cannot copy shared JAR file " + f + ": " + e.getMessage());
                        }
                    }
                }
            }
        }
        return copied;
    }
    
    /**
     * Adds shared libraries to the classpath.
     * 
     * @param classpath the base classpath
     * @param request the deployment request
     * @param workDir the work directory
     * @return the modified classpath
     */
    private String add(String classpath, AppDeploymentRequest request, File workDir) {
        File sharedLibs = SpringInstances.getConfig().getSharedLibs();
        if (null != sharedLibs && sharedLibs.toString().length() > 0) {
            if (copySharedLibs(sharedLibs, new File(workDir, "shared"))) {
                classpath += File.pathSeparator + "shared/*";
            }
            sharedLibs = new File(sharedLibs, request.getDefinition().getName());
            if (copySharedLibs(sharedLibs, new File(workDir, "shared-specific"))) {
                classpath += File.pathSeparator + "shared-specific/*";
            }
        }
        return classpath;
    }
    
    /**
     * Checks/rewrites a classpath file.
     * 
     * @param cpFile the (existing) file to check
     * @param request the deployment request object
     * @param workDir the work directory
     * @return {@code true} if the file was modified without problems, {@code false} if the wildcard classpath shall 
     *   be used
     */
    private boolean checkCpFile(File cpFile, AppDeploymentRequest request, File workDir) {
        boolean result = false;
        if (SystemUtils.isJavaVersionAtLeast(JavaVersion.JAVA_9) 
            && Boolean.valueOf(System.getProperty(PROP_ZIP_CLASSPATH, "true"))) {
            try {
                String classpath = org.apache.commons.io.FileUtils.readFileToString(cpFile, "UTF-8");
                String mainJars = "";
                File[] files = workDir.listFiles();
                if (null != files) {  // file may contain only "jars"
                    for (File f : files) {
                        if (f.getName().endsWith(".jar")) {
                            if (mainJars.length() > 0) {
                                mainJars += File.pathSeparator;
                            }
                            mainJars += f.getName();
                        }
                    }
                }
                if (mainJars.length() > 0) {
                    classpath = mainJars + File.pathSeparator + classpath;
                }
                classpath = add(classpath, request, workDir); // add the common folders if needed
                // https://docs.oracle.com/javase/9/tools/java.htm#JSWOR-GUID-4856361B-8BFD-4964-AE84-121F5F6CF111
                String fileSep = File.separator;
                if (fileSep.equals("\\")) {
                    fileSep = "\\\\"; 
                }
                classpath = classpath.replace("/", fileSep).replace(':', File.pathSeparatorChar);
                classpath = "-cp \"" + classpath + "\""; // turn into cp commandline argument
                org.apache.commons.io.FileUtils.writeStringToFile(cpFile, classpath, "UTF-8");
                result = true;
            } catch (IOException e) {
                getLogger().info("Cannot rewrite classpath file: " + e.getMessage() 
                    + " Falling back to wildcard classpath.");
                result = false;
            }
        }
        return result;
    }
    
    @Override
    protected void addJavaExecutionOptions(List<String> commands, AppDeploymentRequest request) {
        try {
            Resource resource = request.getResource();
            String path = resource.getFile().getAbsolutePath();
            if (path.endsWith(".zip")) {
                FileInputStream zis = new FileInputStream(path);
                File workDir = findWorkingDirectory();
                getLogger().info("Unpacking Jars into " + workDir.getAbsolutePath());
                JarUtils.extractZip(zis, workDir.toPath());
                zis.close();

                File cpFile = new File(workDir, "classpath");
                if (cpFile.exists() && checkCpFile(cpFile, request, workDir)) {
                    commands.add("@classpath");
                } else {
                    String classpath = "*" + File.pathSeparator + "jars/*"; // fix after unpacking
                    classpath = add(classpath, request, workDir);
                    commands.add("-cp");
                    commands.add(classpath);
                }
                commands.add("iip.Starter");
            } else { // as in the base class
                commands.add("-jar");
                commands.add(path);
            }
        } catch (IOException e) { // as in the base class
            throw new IllegalStateException(e);
        }
    }
    
    /**
     * Installs an instance of this builder into {@code deployer}.
     * 
     * @param deployer the deployer instance, may be <b>null</b>
     */
    static void installInto(AppDeployer deployer) {
        if (null != deployer) {
            try {
                Field jBuilder = AbstractLocalDeployerSupport.class.getDeclaredField("javaCommandBuilder");
                jBuilder.setAccessible(true);
                
                Method locProps = AbstractLocalDeployerSupport.class.getDeclaredMethod("getLocalDeployerProperties");
                locProps.setAccessible(true);
                LocalDeployerProperties localPropsInst = (LocalDeployerProperties) locProps.invoke(deployer);
                
                jBuilder.set(deployer, new ClasspathJavaCommandBuilder(localPropsInst));
            } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException 
                | InvocationTargetException e) {
                getLogger().error("Cannot reconfigure Spring Java "
                    + "Command Builder, ZIP service artifacts will not work here!");
            }
        }
    }

}
