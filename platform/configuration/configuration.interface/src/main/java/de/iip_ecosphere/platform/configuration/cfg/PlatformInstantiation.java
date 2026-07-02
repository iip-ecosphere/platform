/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.configuration.cfg;

import java.util.concurrent.ExecutionException;

/**
 * Command line interfaces to platform instantiation.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface PlatformInstantiation {
    
    /**
     * Executes the platform instantiation through an on class loader within this JVM. This may fail if there are 
     * significant library overlaps that can also not resolved by creating a dedicated classloader.
     * 
     * @param loader the class loader to load the classpath resource file
     * @param resourcesDir the optional resources directory for the instantiation (may be <b>null</b> for none)
     * @param tracingLevel the tracing level for the instantiation
     * @param mvnArgs optional maven arguments for the instantiation (may be <b>null</b> for none)
     * @param args the instantiation arguments
     */
    public void execute(ClassLoader loader, String resourcesDir, String tracingLevel, 
        String mvnArgs, String... args) throws ExecutionException;

    /**
     * Executes the platform instantiation directly within an own JVM. This may be required if there are significant
     * library overlaps that can also not resolved by creating a dedicated classloader.
     * 
     * @param loader the class loader to load the classpath resource file
     * @param resourcesDir the optional resources directory for the instantiation (may be <b>null</b> for none)
     * @param tracingLevel the tracing level for the instantiation
     * @param mvnArgs optional maven arguments for the instantiation (may be <b>null</b> for none)
     * @param args the instantiation arguments
     */
    public void executeAsProcess(ClassLoader loader, String resourcesDir, String tracingLevel, 
        String mvnArgs, String... args) throws ExecutionException;
    
    /**
     * Sets a JVM system property for execution.
     * 
     * @param key the key
     * @param value the value
     * @return <b>this</b> for chaining
     */
    public PlatformInstantiation setProperty(String key, String value);

    /**
     * Configures whether all types or just referenced types by apps shall be instantiated. 
     * 
     * @param allTypes all types (@code{true}) or just referenced types (@code{false}, the default)
     * @return <b>this</b> for chaining
     */
    public PlatformInstantiation setAllTypes(boolean allTypes);

    /**
     * Configures whether all services or just referenced services by apps shall be instantiated. 
     * 
     * @param allTypes all services (@code{true}) or just referenced services (@code{false}, the default)
     * @return <b>this</b> for chaining
     */
    public PlatformInstantiation setAllServices(boolean allServices);

    /**
     * Configures whether the instantiation shall run in incremental mode.
     * 
     * @param incremental incremental mode or wipe out target folder/generate anew
     * @return <b>this</b> for chaining
     */
    public PlatformInstantiation setIncremental(boolean incremental);
    
    /**
     * Sets the log path for transport-based distributed logging.
     * 
     * @param path the path, may be <b>null</b> for none
     * @return <b>this</b> for chaining
     */
    public PlatformInstantiation setLogPath(String path);
    
    /**
     * Takes over properties from this process.
     */
    public void takeOverProperties();
    
}
