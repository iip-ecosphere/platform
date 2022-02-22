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

package iip;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Mimiks a generated starter class.
 * 
 * @author Holger Eichelberger, SSE
 */
@SpringBootApplication
@EnableScheduling
@ComponentScan({"test.de.iip_ecosphere.platform.simpleStream.spring", 
    "de.iip_ecosphere.platform.services.environment.spring", "de.iip_ecosphere.platform.transport.spring"})
public class Starter {

    /**
     * Mimik the starter.
     * 
     * @param args the command line arguments (passed on)
     */
    public static void main(String[] args) {
        test.de.iip_ecosphere.platform.simpleStream.spring.Test.main(args);
    }

}
