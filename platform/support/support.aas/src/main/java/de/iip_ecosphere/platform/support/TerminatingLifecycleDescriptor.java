/**
 * ******************************************************************************
 * Copyright (c) {2021} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support;

/**
 * Specialized {@link LifecycleDescriptor} that can stop {@link LifecycleHandler#waitUntilEnd(String[])}.
 *  
 * @author Holger Eichelberger, SSE
 */
public interface TerminatingLifecycleDescriptor extends LifecycleDescriptor {

    /**
     * Returns whether waiting in {@link LifecycleHandler#waitUntilEnd(String[])} shall continue (from the perspective 
     * of this descriptor). Condition must be based on static values as
     * 
     * @return {@code true} for go on waiting, {@code false} for terminate (one descriptor is sufficient)
     */
    public boolean continueWaiting();

}
