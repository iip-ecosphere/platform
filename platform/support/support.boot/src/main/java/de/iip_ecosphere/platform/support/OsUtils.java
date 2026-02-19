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

package de.iip_ecosphere.platform.support;

import java.io.File;

import de.iip_ecosphere.platform.support.commons.Commons;

/**
 * Access to static operating system level information.
 * 
 * @author Holger Eichelberger, SSE
 */
public class OsUtils {

    /**
     * The System property key for the Java home directory.
     */
    public static final String PROP_JAVA_HOME = Commons.PROP_JAVA_HOME;    
    
    /**
     * The System property key for the user home directory.
     */
    public static final String PROP_USER_HOME = Commons.PROP_USER_HOME;     
    
    /**
     * Preventing external creation.
     */
    private OsUtils() {
    }

    /**
     * Returns the {@code user.home} System Property. User's home directory.
     */
    public static final String getUserHome() {
        return Commons.getInstance().getUserHome();
    }
    
    /**
     * Returns the operating system name.
     * 
     * @return the operating system name
     */
    public static final String getOsName() {
        return Commons.getInstance().getOsName();
    }
    
    /**
     * Gets the Java home directory as a {@code File}.
     *
     * @return a directory
     * @throws SecurityException if a security manager exists and its {@code checkPropertyAccess} method doesn't allow
     * access to the specified system property.
     */
    public static File getJavaHome() {
        return Commons.getInstance().getJavaHome();
    }
    
    /**
     * The Java Runtime Environment specification version.
     */
    public static String getJavaSpecificationVersion() {
        return Commons.getInstance().getJavaSpecificationVersion();
    }

    
    /**
     * Returns the operating system architecture.
     * 
     * @return the operating system architecture.
     */
    public static final String getOsArch() {
        return Commons.getInstance().getOsArch();
    }
    
    /**
     * Returns the number of CPU cores.
     * 
     * @return the number of CPU cores
     */
    public static final int getNumCpuCores() {
        return Runtime.getRuntime().availableProcessors();
    }
   
    /**
     * Returns a value from the system environment, either as given or all in capital characters with dots replaced 
     * by underscores.
     * 
     * @param key the key to look for
     * @return the value, may by <b>null</b> for none
     */
    public static String getEnv(String key) {
        String result = System.getenv(key); 
        if (null == result) { // particular for linux
            result = System.getenv(key.toUpperCase().replace('.', '_'));
        }
        return result;
    }

    /**
     * Returns a value from the system environment, either as given or all in capital characters with dots replaced 
     * by underscores.
     * 
     * @param key the key to look for
     * @param dflt the value to use if there is no configured value
     * @return the value, may by {@code dflt} for none
     * @see #getEnv(String)
     */
    public static String getEnv(String key, String dflt) {
        String result = getEnv(key);
        return result == null ? dflt : result;
    }

    /**
     * Returns a boolean value from the system environment, either as given or all in capital characters with dots 
     * replaced by underscores.
     * 
     * @param key the key to look for
     * @param dflt the value to use if there is no configured value
     * @return the value, may by {@code dflt} for none
     * @see #getEnv(String, String)
     */
    public static boolean getBooleanEnv(String key, boolean dflt) {
        return Boolean.valueOf(getEnv(key, String.valueOf(dflt)));
    }

    /**
     * Returns a value from the system properties or system environment, either as given or all in capital 
     * characters with dots replaced by underscores.
     * 
     * @param key the key to look for
     * @param dflt the default value for none
     * @return the value, may by {@code dflt} for none
     * @see #getEnv(String)
     */
    public static String getPropertyOrEnv(String key, String dflt) {
        String result = System.getProperty(key);
        if (null == result) {
            result = getEnv(key); 
        }
        return null == result ? dflt : result;
    }

    /**
     * Returns a boolean value from the system properties or system environment, either as given or all in capital 
     * characters with dots replaced by underscores.
     * 
     * @param key the key to look for
     * @param dflt the default value for none
     * @return the value, may by {@code dflt} for none
     * @see #getPropertyOrEnv(String)
     */
    public static boolean getBooleanPropertyOrEnv(String key, boolean dflt) {
        return Boolean.valueOf(getPropertyOrEnv(key, String.valueOf(dflt)));
    }

    /**
     * Returns a value from the system properties or system environment, either as given or all in capital 
     * characters with dots replaced by underscores.
     * 
     * @param key the key to look for
     * @return the value, may by <b>null</b> for none
     * @see #getEnv(String)
     */
    public static String getPropertyOrEnv(String key) {
        return getPropertyOrEnv(key, null);
    }
    
    /**
     * Returns whether we are running on windows.
     * 
     * @return {@code true} for windows, {@code false} else
     */
    public static boolean isWindows() {
        return Commons.getInstance().isWindows();
    }

    /**
     * Returns whether we are running on Linux.
     * 
     * @return {@code true} for Linux, {@code false} else
     */
    public static boolean isLinux() {
        return Commons.getInstance().isLinux();
    }

    /**
     * Returns whether we are running on Unix.
     * 
     * @return {@code true} for Unix, {@code false} else
     */
    public static boolean isUnix() {
        return Commons.getInstance().isUnix();
    }

    /**
     * Returns whether we are running on Mac.
     * 
     * @return {@code true} for Mac, {@code false} else
     */
    public static boolean isMac() {
        return Commons.getInstance().isMac();
    }
    
    /**
     * Returns whether we are running on Java 1.8.
     * 
     * @return {@code true} for Java 1.8, {@code false} else
     */
    public static boolean isJava1_8() {
        return Commons.getInstance().isJava1_8();
    }
    
    /**
     * Returns whether we are running on Java 9 or newer.
     * 
     * @return {@code true} for Java 9 nor newer, {@code false} else
     */
    public static boolean isAtLeastJava9() {
        return Commons.getInstance().isAtLeastJava9();
    }

}
