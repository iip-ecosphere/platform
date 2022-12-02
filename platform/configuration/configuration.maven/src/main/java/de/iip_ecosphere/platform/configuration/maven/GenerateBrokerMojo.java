
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

package de.iip_ecosphere.platform.configuration.maven;

import java.io.File;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Generates the default/testing broker.
 * 
 * @author Holger Eichelberger, SSE
 */
@Mojo(name = "generateBroker", defaultPhase = LifecyclePhase.PACKAGE)
public class GenerateBrokerMojo extends AbstractConfigurationMojo {

    @Override
    public String getStartRule() {
        return "generateBroker";
    }

    @Override
    protected String adjustOutputDir(String outputDir) {
        return new File(findGenParent(outputDir), "broker").toString();
    }

}
