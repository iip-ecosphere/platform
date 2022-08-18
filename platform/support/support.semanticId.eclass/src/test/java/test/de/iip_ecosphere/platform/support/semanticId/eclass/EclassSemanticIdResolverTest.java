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

package test.de.iip_ecosphere.platform.support.semanticId.eclass;

import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;

import de.iip_ecosphere.platform.support.semanticId.DefaultSemanticIdResolutionResult;
import de.iip_ecosphere.platform.support.semanticId.DefaultSemanticIdResolutionResult.DefaultNaming;
import de.iip_ecosphere.platform.support.semanticId.SemanticIdResolver;
import de.iip_ecosphere.platform.support.semanticId.eclass.EclassSemanticIdResolver;
import de.iip_ecosphere.platform.support.semanticId.eclass.model.TranslatableLabel;

/**
 * Tests {@link EclassSemanticIdResolver}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class EclassSemanticIdResolverTest {
    
    /**
     * Template test.
     */
    @Test
    public void testApp() {
        // shall be there via JSL
        Assert.assertTrue(SemanticIdResolver.hasResolver(EclassSemanticIdResolver.class));
        
        final String irdiUnitDegreesCelsius = "0173-1#05-AAA567#004";
        
        // not fully testable, as certificate cannot be added here and Eclass API throws timeouts  
        EclassSemanticIdResolver resolver = new EclassSemanticIdResolver();
        Assert.assertTrue(resolver.isResponsible(irdiUnitDegreesCelsius));
        Assert.assertFalse(resolver.isResponsible("0173-1#05#AAA567-004"));
        Assert.assertFalse(resolver.isResponsible("abba"));
        
        DefaultSemanticIdResolutionResult res = EclassSemanticIdResolver.createInstance(irdiUnitDegreesCelsius);
        Assert.assertNotNull(res);
        Assert.assertEquals(irdiUnitDegreesCelsius, res.getSemanticId());
        Assert.assertNotNull(irdiUnitDegreesCelsius, res.getKind());
        Assert.assertNotNull(irdiUnitDegreesCelsius, res.getPublisher());
        Assert.assertEquals("1", res.getRevision());
        Assert.assertEquals("4", res.getVersion());
        
        Map<String, DefaultNaming> naming = EclassSemanticIdResolver.createNaming(null, null);
        Assert.assertNotNull(naming);
        Assert.assertTrue(naming.isEmpty());
        
        TranslatableLabel p = new TranslatableLabel();
        p.put(Locale.GERMAN.toString(), "Deutsch");
        p.put(Locale.ENGLISH.toLanguageTag(), "English");
        TranslatableLabel s = new TranslatableLabel();
        s.put(Locale.GERMAN.toString(), "Deutsch!");
        s.put(Locale.ENGLISH.toLanguageTag(), "English!");
        
        naming = EclassSemanticIdResolver.createNaming(p , null);
        assertNaming(p, naming, n -> n.getName());
        naming = EclassSemanticIdResolver.createNaming(null , s);
        assertNaming(s, naming, n -> n.getStructuredName());
        naming = EclassSemanticIdResolver.createNaming(p, s);
        assertNaming(p, naming, n -> n.getName());
        assertNaming(s, naming, n -> n.getStructuredName());
    }

    /**
     * Asserts a naming structure for {@code label}.
     * 
     * @param label the label to assert for
     * @param naming the naming structure
     * @param namingGetter a getter which values to assert on {@code naming} with respect to the values in {@code label}
     */
    private void assertNaming(TranslatableLabel label, Map<String, DefaultNaming> naming, 
        Function<DefaultNaming, String> namingGetter) {
        for (Map.Entry<String, String> e: label.entrySet()) {
            Locale loc = new Locale(e.getKey());
            DefaultNaming n = naming.get(loc.getLanguage());
            Assert.assertNotNull(n);
            Assert.assertEquals(e.getValue(), namingGetter.apply(n));
        }
    }
    
}
