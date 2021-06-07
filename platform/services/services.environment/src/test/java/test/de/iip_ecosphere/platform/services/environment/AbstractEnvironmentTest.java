/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package test.de.iip_ecosphere.platform.services.environment;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import org.junit.Assert;

import de.iip_ecosphere.platform.services.environment.Service;
import de.iip_ecosphere.platform.services.environment.ServiceState;
import de.iip_ecosphere.platform.support.Endpoint;
import de.iip_ecosphere.platform.support.aas.Aas;
import de.iip_ecosphere.platform.support.aas.AasFactory;
import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.iip_aas.Version;

/**
 * The common test code for all environments. Maximize code here rather than in specific environment tests.
 * 
 * @author Holger Eichelberger, SSE
 */
public abstract class AbstractEnvironmentTest {

    /**
     * Tests the agreed AAS behavior created by {@link AasCreator}.
     * 
     * @param registry the registry endpoint
     * @param expected expected service (with values)
     * @throws IOException if accessing the AAS fails
     * @throws ExecutionException if accessing AAS property values or performing operation invocations fails
     */
    public static void testAas(Endpoint registry, Service expected) throws IOException, ExecutionException {
        AasFactory factory = AasFactory.getInstance();
        Aas aas = factory.obtainRegistry(registry).retrieveAas(AasCreator.URN_AAS);
        Assert.assertNotNull("Aas " + AasCreator.URN_AAS + " shall be there", aas);
        Submodel submodel = aas.getSubmodel(AasCreator.AAS_SUBMODEL_NAME);
        Assert.assertNotNull("Submodel " + AasCreator.AAS_SUBMODEL_NAME + " shall be there", submodel);

        assertProperty(submodel, AasCreator.AAS_SUBMODEL_PROPERTY_NAME, o -> checkString(o, expected.getName()));
        assertProperty(submodel, AasCreator.AAS_SUBMODEL_PROPERTY_DESCRIPTION, 
            o -> checkString(o, expected.getDescription()));
        assertProperty(submodel, AasCreator.AAS_SUBMODEL_PROPERTY_VERSION, o -> checkVersion(o, expected.getVersion()));
        assertProperty(submodel, AasCreator.AAS_SUBMODEL_PROPERTY_STATE, o -> checkStateString(o));
        
        // do not make too many assumptions for now
        assertOperation(submodel, AasCreator.AAS_SUBMODEL_OPERATION_SETSTATE, null, ServiceState.RUNNING.name());
        assertProperty(submodel, AasCreator.AAS_SUBMODEL_PROPERTY_STATE, 
            o -> checkStateString(o, ServiceState.RUNNING));
        assertOperation(submodel, AasCreator.AAS_SUBMODEL_OPERATION_PASSIVATE, null);
        assertProperty(submodel, AasCreator.AAS_SUBMODEL_PROPERTY_STATE, o -> checkStateString(o));
        assertOperation(submodel, AasCreator.AAS_SUBMODEL_OPERATION_ACTIVATE, null);
        assertProperty(submodel, AasCreator.AAS_SUBMODEL_PROPERTY_STATE, o -> checkStateString(o));
    }

    /**
     * Asserts a property/value.
     * 
     * @param submodel the submodel
     * @param propertyName the name/short id of the property
     * @param cond a condition to apply on the property value, if <b>null</b> the property value must be <b>null</b>
     * @throws ExecutionException if accessing AAS property values or performing operation invocations fails
     */
    private static void assertProperty(Submodel submodel, String propertyName, Function<Object, Boolean> cond) 
        throws ExecutionException {
        Property prop = submodel.getProperty(propertyName);
        Assert.assertNotNull("Property " + propertyName + " not found", prop);
        if (null == cond) {
            Assert.assertNull("Value of property " + propertyName + " shall be null", prop.getValue());
        } else {
            Assert.assertTrue("Condition on property " + propertyName + " does not hold", cond.apply(prop.getValue()));
        }
    }

    /**
     * Asserts an operation invocation result.
     * 
     * @param submodel the submodel
     * @param operationName the name/short id of the operation
     * @param cond a condition to apply on the result value, if <b>null</b> no assert is performed
     * @param args the operation invocation arguments
     * @throws ExecutionException if accessing AAS property values or performing operation invocations fails
     */
    private static void assertOperation(Submodel submodel, String operationName, Function<Object, Boolean> cond, 
        Object... args) throws ExecutionException {
        Operation op = submodel.getOperation(operationName);
        Assert.assertNotNull("Operation " + operationName + " not found", op);
        Object result = op.invoke(args);
        if (null != cond) {
            Assert.assertTrue("Condition on result of " + operationName + " does not hold", cond.apply(result));
        }
    }

    /**
     * Checks whether {@code obj} is a non-empty string.
     * 
     * @param obj the object to test
     * @return {@code true} if {@code o} fulfills the properties to test, {@code false} else
     */
    private static boolean checkNonEmptyString(Object obj) {
        return obj instanceof String && ((String) obj).length() > 0; 
    }

    /**
     * Checks whether {@code obj} is a non-empty string.
     * 
     * @param obj the object to test
     * @param expected the expected value, may be <b>null</b> then we check for {@code obj} <b>null</b>
     * @return {@code true} if {@code o} fulfills the properties to test, {@code false} else
     */
    private static boolean checkString(Object obj, String expected) {
        if (null == expected) {
            return obj == null;
        } else {
            return checkNonEmptyString(obj) && expected.equals(obj); 
        }
    }
    
    /**
     * Checks whether the given {@code obj} complies with the {@link expected} version.
     * 
     * @param obj the object to test
     * @param expected the expected version, may be <b>null</b> then we check for {@code obj} <b>null</b>
     * @return {@code true} if {@code o} fulfills the properties to test, {@code false} else
     */
    private static boolean checkVersion(Object obj, Version expected) {
        if (null == expected) { 
            return obj == null;
        } else if (obj instanceof String) {
            return expected.toString().equals(obj);
        } else if (obj instanceof Version) {
            return expected.equals(obj);
        } else {
            return false;
        }
    }

    /**
     * Checks whether {@code obj} is a boolean.
     * 
     * @param obj the object to test
     * @return {@code true} if {@code o} fulfills the properties to test, {@code false} else
     */
    private static boolean checkBoolean(Object obj) {
        return obj instanceof Boolean;
    }

    /**
     * Checks whether {@code obj} is a boolean with {@code expectedValue}.
     * 
     * @param obj the object to test
     * @param expectedValue the expected value
     * @return {@code true} if {@code obj} fulfills the properties to test, {@code false} else
     */
    @SuppressWarnings("unused")
    private static boolean checkBoolean(Object obj, boolean expectedValue) {
        return checkBoolean(obj) && ((Boolean) obj) == expectedValue; 
    }

    /**
     * Checks whether {@code obj} is a string representing a value of {@code ServiceState}.
     * 
     * @param obj the object to test
     * @return {@code true} if {@code o} fulfills the properties to test, {@code false} else
     */
    private static boolean checkStateString(Object obj) {
        return checkStateString(obj, null);
    }

    /**
     * Checks whether {@code obj} is a string representing a value of {@code ServiceState}.
     * 
     * @param obj the object to test
     * @param expectedValue the expected state value, no test is performed if <b>null</b>
     * @return {@code true} if {@code o} fulfills the properties to test, {@code false} else
     */
    private static boolean checkStateString(Object obj, ServiceState expectedValue) {
        boolean ok = checkNonEmptyString(obj);
        if (ok) {
            try {
                ServiceState state = ServiceState.valueOf(obj.toString());
                if (null != expectedValue) {
                    ok = expectedValue.equals(state);
                }
            } catch (IllegalArgumentException e) {
                ok = false;
            }
        }
        return ok;
    }
    
}
