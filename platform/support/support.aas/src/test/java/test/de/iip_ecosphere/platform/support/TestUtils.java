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

package test.de.iip_ecosphere.platform.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import de.iip_ecosphere.platform.support.NetUtils;
import junit.framework.JUnit4TestAdapter;
import junit.framework.TestSuite;

/**
 * Some common test utilities. These functions shall not be part of production code!
 * 
 * @author Holger Eichelberger, SSE
 */
public class TestUtils {
    
    /**
     * Returns whether this JVM is currently executing on the SSE CI Jenkins.
     * This may be needed to disable some tests.
     * 
     * @return {@code true} if we are running on SSE CI, {@code false}
     */
    public static boolean isSseCI() {
        return NetUtils.getOwnHostname().indexOf("jenkins") >= 0;
    }
    
    /**
     * Creates a test suite.
     * 
     * @param tests the tests to execute (may be <b>null</b> for an empty tests suite)
     * @return the test suite
     */
    public static TestSuite suite(Class<?>... tests) {
        TestSuite suite = new TestSuite();
        if (null != tests) {
            if (tests.length > 0) {
                Thread.currentThread().setContextClassLoader(tests[0].getClassLoader());
            }
            for (Class<?> t : tests) {
                suite.addTest(new JUnit4TestAdapter(t));
            }
        }
        return suite;
    }    
    
    // checkstyle: stop exception type check
    
    private static class MyRunListener extends RunListener {
        
        private Map<String, Long> times = new HashMap<>();
        private long start;
     
        @Override
        public void testStarted(Description description) throws Exception {
            start = System.currentTimeMillis();
        }

        @Override
        public void testFinished(Description description) throws Exception {
            long duration = System.currentTimeMillis() - start;
            String cls = description.getClassName();
            Long time = times.get(cls);
            if (null == time) {
                time = 0L;
            }
            time += duration;
            times.put(cls, time);
        }

        @Override
        public void testRunFinished(Result result) throws Exception {
            System.out.println("Times: ");
            List<String> keys = new ArrayList<>(times.keySet());
            Collections.sort(keys);
            for (String k: keys) {
                System.out.println(" " + k + ": " + times.get(k) + " ms");
            }
        }

        @Override
        public void testFailure(Failure failure) throws Exception {
            System.out.println("Test failure: " + failure.getMessage());
            if (failure.getException() != null) {
                failure.getException().printStackTrace(System.out);
            }
        }
        
        @Override
        public void testAssumptionFailure(Failure failure) {
            System.out.println("Test assumption failure: " + failure.getMessage());
            if (failure.getException() != null) {
                failure.getException().printStackTrace(System.out);
            }
        }
        
    }

    // checkstyle: resume exception type check

    /**
     * Executes test cases with junit.
     * 
     * @param args the test suites/cases to run
     */
    public static void main(String[] args) {
        JUnitCore core = new JUnitCore();
        core.addListener(new MyRunListener());
        for (String s: args) {
            try {
                Class<?> cls = Class.forName(s);
                core.run(cls);
            } catch (ClassNotFoundException e) {
                System.out.println("Class " + s + " not found.");
            }
        }
    }

}
