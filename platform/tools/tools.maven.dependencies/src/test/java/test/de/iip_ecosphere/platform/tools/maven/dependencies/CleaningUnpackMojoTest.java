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

package test.de.iip_ecosphere.platform.tools.maven.dependencies;

import java.io.File;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.logging.Log;
import org.junit.Test;

import de.iip_ecosphere.platform.tools.maven.dependencies.CleaningUnpackMojo;
import org.junit.Assert;

/**
 * Tests public/static parts of {@link CleaningUnpackMojo}.
 * 
 * @author Holger Eichelberger, SSE
 */
public class CleaningUnpackMojoTest {

    private static final Log LOG = new Log() {
        
        @Override
        public void warn(CharSequence content, Throwable error) {
            System.out.println(content);
        }
        
        @Override
        public void warn(Throwable error) {
            System.out.println(error.getMessage());
        }
        
        @Override
        public void warn(CharSequence content) {
            System.out.println(content);
        }
        
        @Override
        public boolean isWarnEnabled() {
            return true;
        }
        
        @Override
        public boolean isInfoEnabled() {
            return true;
        }
        
        @Override
        public boolean isErrorEnabled() {
            return true;
        }
        
        @Override
        public boolean isDebugEnabled() {
            return true;
        }
        
        @Override
        public void info(CharSequence content, Throwable error) {
            System.out.println(content);
        }
        
        @Override
        public void info(Throwable error) {
            System.out.println(error.getMessage());
        }
        
        @Override
        public void info(CharSequence content) {
            System.out.println(content);
        }
        
        @Override
        public void error(CharSequence content, Throwable error) {
            System.out.println(content);
        }
        
        @Override
        public void error(Throwable error) {
            System.out.println(error.getMessage());
        }
        
        @Override
        public void error(CharSequence content) {
            System.out.println(content);
        }
        
        @Override
        public void debug(CharSequence content, Throwable error) {
            System.out.println(content);
        }
        
        @Override
        public void debug(Throwable error) {
            System.out.println(error.getMessage());
        }
        
        @Override
        public void debug(CharSequence content) {
            System.out.println(content);
        }
        
    };

    /**
     * Tests initially allowed files and their matching.
     */
    @Test
    public void testInitiallyAllowedString() {
        Set<String> allowed = CleaningUnpackMojo.getInitiallyAllowed(
            "test.ivml;a*.ivml:b*.*;apps/App*.*;types\\Type*.*", null, LOG);
        doAssert(allowed);
    }

    /**
     * Tests initially allowed files and their matching.
     */
    @Test
    public void testInitiallyAllowedFile() {
        Set<String> allowed = CleaningUnpackMojo.getInitiallyAllowed(null, 
            new File("src/test/resources/initiallyAllowed.txt"), LOG);
        doAssert(allowed);
    }

    /**
     * Tests initially allowed files and their matching.
     */
    @Test
    public void testInitiallyAllowedFileMixed() {
        Set<String> allowed = CleaningUnpackMojo.getInitiallyAllowed("test.ivml;cde.fgh", 
            new File("src/test/resources/initiallyAllowed.txt"), LOG);
        doAssert(allowed);
    }

    /**
     * Does the expected asserts for file/string based specification.
     * 
     * @param allowed the set of allowed file names/wildcards
     */
    private static void doAssert(Set<String> allowed) {
        Assert.assertTrue(allowed.contains(FilenameUtils.normalize("test.ivml")));
        Assert.assertTrue(allowed.contains(FilenameUtils.normalize("a*.ivml")));
        Assert.assertTrue(allowed.contains(FilenameUtils.normalize("b*.*")));
        Assert.assertTrue(allowed.contains(FilenameUtils.normalize("apps/App*.*")));
        Assert.assertTrue(allowed.contains(FilenameUtils.normalize("types/Type*.*")));
        
        Assert.assertTrue(CleaningUnpackMojo.matches(new File("test.ivml"), allowed));
        Assert.assertFalse(CleaningUnpackMojo.matches(new File("test.text"), allowed));
        Assert.assertTrue(CleaningUnpackMojo.matches(new File("aber.ivml"), allowed));
        Assert.assertFalse(CleaningUnpackMojo.matches(new File("aber.text"), allowed));
        Assert.assertTrue(CleaningUnpackMojo.matches(new File("boah.ivml"), allowed));
        Assert.assertTrue(CleaningUnpackMojo.matches(new File("boah.text"), allowed));
        Assert.assertFalse(CleaningUnpackMojo.matches(new File("AppOld.text"), allowed));
        Assert.assertTrue(CleaningUnpackMojo.matches(new File("apps/AppOld.text"), allowed));
        Assert.assertTrue(CleaningUnpackMojo.matches(new File("types/TypeNew.ivml"), allowed));
    }
    
}
