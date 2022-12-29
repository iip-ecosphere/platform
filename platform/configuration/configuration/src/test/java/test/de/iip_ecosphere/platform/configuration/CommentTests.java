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
import de.iip_ecosphere.platform.configuration.PlatformInstantiator.NonCleaningInstantiationConfigurer;
import de.iip_ecosphere.platform.configuration.StatisticsVisitor;
import de.iip_ecosphere.platform.configuration.StatisticsVisitor.Statistics;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.datatypes.DerivedDatatype;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.datatypes.TypeQueries;

/**
 * Tests the model for provided IVML comments.
 * 
 * @author Holger Eichelberger, SSE
 */
public class CommentTests {
    
    /**
     * Called to record a variable with missing comment.
     * 
     * @param var the variable without comment
     * @param missing the missing set that may be updated as side effect
     * @return {@code true} count as missing, {@code false} ignore in statistics
     */
    private static final boolean recordMissing(AbstractVariable var, SortedSet<String> missing) {
        boolean record = true;
        if (var.getParent() instanceof AbstractVariable) {
            AbstractVariable parentVar = (AbstractVariable) var.getParent();
            IDatatype type = DerivedDatatype.resolveToBasis(parentVar.getType());
            if (TypeQueries.isContainer(type)) {
                record = false;
            }
        }
        if (record) {
            missing.add(var.getQualifiedName() + " = ");
        }
        return record;
    }
    
    /**
     * Tests for comments.
     */
    @Test
    public void testComments() {
        SortedSet<String> missing = new TreeSet<>();
        ConfigurationSetup setup = ConfigurationSetup.getSetup();
        EasySetup easySetup = setup.getEasyProducer();
        easySetup.reset();
        InstantiationConfigurer configurer = new NonCleaningInstantiationConfigurer(EasySetup.PLATFORM_META_MODEL_NAME, 
            easySetup.getIvmlMetaModelFolder(), new File("gen"));
        configurer.configure(setup);
        ConfigurationLifecycleDescriptor lcd = configurer.obtainLifecycleDescriptor();
        lcd.startup(new String[0]); // shall register executor
        StatisticsVisitor vis = new StatisticsVisitor();
        vis.setNoCommentConsumer(v -> recordMissing(v, missing));
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
        Assert.assertEquals("There are variables without comment/description in respective .text file for the "
            + "actual locale. For affected variable names to be fixed, please see above.", stat.getMetaVars(), 
            stat.getMetaVarsWithComment());
    }

}
