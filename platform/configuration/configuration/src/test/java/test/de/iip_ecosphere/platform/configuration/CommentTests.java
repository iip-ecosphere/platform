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

package test.de.iip_ecosphere.platform.configuration;

import java.io.File;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.configuration.ConfigurationLifecycleDescriptor;
import de.iip_ecosphere.platform.configuration.ConfigurationManager;
import de.iip_ecosphere.platform.configuration.ConfigurationSetup;
import de.iip_ecosphere.platform.configuration.EasySetup;
import de.iip_ecosphere.platform.configuration.PlatformInstantiator.InstantiationConfigurer;
import de.iip_ecosphere.platform.configuration.StatisticsVisitor;
import de.iip_ecosphere.platform.configuration.StatisticsVisitor.Statistics;
import net.ssehub.easy.varModel.confModel.Configuration;

/**
 * Tests the model for provided IVML comments.
 * 
 * @author Holger Eichelberger, SSE
 */
public class CommentTests {
    
    /**
     * Tests for comments.
     */
    @Test
    public void testComments() {
        SortedSet<String> missing = new TreeSet<>();
        ConfigurationSetup setup = ConfigurationSetup.getSetup();
        InstantiationConfigurer configurer = new InstantiationConfigurer(EasySetup.PLATFORM_META_MODEL_NAME, 
            setup.getEasyProducer().getIvmlMetaModelFolder(), new File("gen")) {

            @Override
            protected boolean cleanOutputFolder() {
                return false; // we just use the configurer, we do not generate
            }

        };
        configurer.configure(setup);
        ConfigurationLifecycleDescriptor lcd = configurer.obtainLifecycleDescriptor();
        lcd.startup(new String[0]); // shall register executor
        StatisticsVisitor vis = new StatisticsVisitor();
        vis.setNoCommentConsumer(v -> missing.add(v.getQualifiedName() + " = "));
        Configuration cfg = ConfigurationManager.getIvmlConfiguration();
        Assert.assertNotNull(cfg);
        vis.visitConfiguration(cfg);
        Statistics stat = vis.getStatistics();
        lcd.shutdown();
        setup.getEasyProducer().reset();
        System.out.println("metaVars: " + stat.getMetaVars() + ", metaVars (commented): " 
            + stat.getMetaVarsWithComment() + ", used vars: " + stat.noOfVariables() 
            + ", constraints: " + stat.noOfConstraintInstances());
        for (String s: missing) {
            System.out.println(s);
        }
        //Assert.assertEquals("There are variables without comment/description in respective .text file for the "
        //    + "actual locale. For affected variable names to be fixed, please see above.", stat.getMetaVars(), 
        //    stat.getMetaVarsWithComment());
    }

}
