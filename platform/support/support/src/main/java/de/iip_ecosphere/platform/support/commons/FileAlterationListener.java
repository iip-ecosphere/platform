
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

package de.iip_ecosphere.platform.support.commons;

import java.io.File;

/**
 * Receives events of file system modifications. Abstracted from apache commons.
 * 
 * @author Holger Eichelberger, SSE
 */
public interface FileAlterationListener {

    /**
     * Directory changed Event.
     *
     * @param directory The directory changed (ignored)
     */
    public default void onDirectoryChange(final File directory) {
    }

    /**
     * Directory created Event.
     *
     * @param directory The directory created (ignored)
     */
    public default void onDirectoryCreate(final File directory) {
    }

    /**
     * Directory deleted Event.
     *
     * @param directory The directory deleted (ignored)
     */
    public default void onDirectoryDelete(final File directory) {
    }

    /**
     * File changed Event.
     *
     * @param file The file changed (ignored)
     */
    public default void onFileChange(final File file) {
    }

    /**
     * File created Event.
     *
     * @param file The file created (ignored)
     */
    public default void onFileCreate(final File file) {
    }

    /**
     * File deleted Event.
     *
     * @param file The file deleted (ignored)
     */
    public default void onFileDelete(final File file) {
    }

}
