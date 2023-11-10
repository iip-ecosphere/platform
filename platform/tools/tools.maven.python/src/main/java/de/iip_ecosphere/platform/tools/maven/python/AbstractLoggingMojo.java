/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.tools.maven.python;

import org.apache.maven.plugin.AbstractMojo;

/**
 * An abstract mojo with logging bridge.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractLoggingMojo extends AbstractMojo implements Logger {

    @Override
    public void warn(String warning) {
        getLog().warn(warning);
    }

    @Override
    public void error(String error) {
        getLog().error(error);
    }
    
    @Override
    public void error(Throwable throwable) {
        getLog().error(throwable);
    }

    @Override
    public void info(String info) {
        getLog().info(info);
    }

}
