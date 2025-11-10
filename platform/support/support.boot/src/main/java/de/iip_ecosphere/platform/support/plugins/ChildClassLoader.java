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

package de.iip_ecosphere.platform.support.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

/**
 * Defines the interface of a child class loader, mixed from {@link java.lang.ClassLoader} and 
 * {@code java.net.URLClassLoader}.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface ChildClassLoader {

    /**
     * Finds and loads the class with the specified name from the URL search
     * path. Any URLs referring to JAR files are loaded and opened as needed
     * until the class is found.
     *
     * @param name the name of the class
     * @return the resulting class
     * @exception ClassNotFoundException if the class could not be found,
     *            or if the loader is closed.
     * @exception NullPointerException if {@code name} is <b>null</b>
     */
    public Class<?> findClass(String name) throws ClassNotFoundException;

    /**
     * Finds the resource with the given name.  A resource is some data
     * (images, audio, text, etc) that can be accessed by class code in a way
     * that is independent of the location of the code.
     *
     * @param  name The resource name
     * @return  A {@code URL} object for reading the resource, or
     *          <b>null</b> if the resource could not be found or the invoker
     *          doesn't have adequate  privileges to get the resource.
     */
    public URL getResource(String name);
    
    /**
     * Finds all the resources with the given name. A resource is some data
     * (images, audio, text, etc) that can be accessed by class code in a way
     * that is independent of the location of the code.
     *
     * @param  name The resource name
     * @return  An enumeration of {@link java.net.URL URL} objects for
     *          the resource.  If no resources could  be found, the enumeration
     *          will be empty.  Resources that the class loader doesn't have
     *          access to will not be in the enumeration.
     * @throws  IOException If I/O errors occur
     */
    public Enumeration<URL> getResources(String name) throws IOException;

    /**
     * Returns an input stream for reading the specified resource.
     *
     * @param  name The resource name
     * @return  An input stream for reading the resource, or <b>null</b>
     *          if the resource could not be found
     */
    public InputStream getResourceAsStream(String name);
    
    /**
     * Sets the desired assertion status for the named top-level class in this
     * class loader and any nested classes contained therein.  This setting
     * takes precedence over the class loader's default assertion status, and
     * over any applicable per-package default.  This method has no effect if
     * the named class has already been initialized.  (Once a class is
     * initialized, its assertion status cannot change.)
     *
     * @param  className The fully qualified class name of the top-level class whose
     *         assertion status is to be set.
     * @param  enabled {@code true} if the named class is to have assertions
     *         enabled when (and if) it is initialized, {@code false} if the
     *         class is to have assertions disabled.
     */
    public void setClassAssertionStatus(String className, boolean enabled);
    
    /**
     * Sets the default assertion status for this class loader to
     * {@code false} and discards any package defaults or class assertion
     * status settings associated with the class loader.  This method is
     * provided so that class loaders can be made to ignore any command line or
     * persistent assertion status settings and "start with a clean slate."
     */
    public void clearAssertionStatus();

    /**
     * Sets the default assertion status for this class loader.  This setting
     * determines whether classes loaded by this class loader and initialized
     * in the future will have assertions enabled or disabled by default.
     *
     * @param  enabled {@code true} if classes loaded by this class loader will
     *         henceforth have assertions enabled by default, {@code false}
     *         if they will have assertions disabled by default.
     */
    public void setDefaultAssertionStatus(boolean enabled);
    
    /**
     * Sets the package default assertion status for the named package.  The
     * package default assertion status determines the assertion status for
     * classes initialized in the future that belong to the named package or
     * any of its "subpackages".
     *
     * @param  packageName The name of the package whose package default assertion status
     *         is to be set. A <b>null</b> value indicates the unnamed
     *         package that is "current"
     * @param  enabled {@code true} if classes loaded by this classloader and
     *         belonging to the named package or any of its subpackages will
     *         have assertions enabled by default, {@code false} if they will
     *         have assertions disabled by default.
     */
    public void setPackageAssertionStatus(String packageName, boolean enabled);

}