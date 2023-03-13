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

package de.iip_ecosphere.platform.support.iip_aas;

import java.util.concurrent.ExecutionException;

import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Submodel;

/**
 * Basic class for submodel clients, i.e., classes that act as frontend for an (active) AAS.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SubmodelClient {

    private Submodel submodel;
    
    /**
     * Creates an instance.
     * 
     * @param submodel defines the submodel for querying/execution
     */
    protected SubmodelClient(Submodel submodel) {
        this.submodel = submodel;
    }
    
    /**
     * Returns the submodel.
     * 
     * @return the submodel
     */
    public Submodel getSubmodel() {
        return submodel;
    }
    
    /**
     * Returns the operation for the given {@code idShort} defined on {@link #submodel}.
     * 
     * @param idShort the short id
     * @return the operation
     * @throws ExecutionException if the operation was not found
     * @see #getOperation(Submodel, String)
     */
    protected Operation getOperation(String idShort) throws ExecutionException {
        return getOperation(submodel, idShort);
    }

    /**
     * If there is caching, force re-caching.
     */
    public void clear() {
    }
    
    /**
     * Returns the property for the given {@code idShort} defined on {@link #submodel}.
     * 
     * @param idShort the short id
     * @return the property
     * @throws ExecutionException if the property was not found
     * @see #getProperty(Submodel, String)
     */
    protected Property getProperty(String idShort) throws ExecutionException {
        return getProperty(submodel, idShort);
    }
    
    /**
     * Returns the operation for the given {@code idShort} defined on {@link #submodel}.
     * 
     * @param submodel the submodel to query
     * @param idShort the short id
     * @return the operation
     * @throws ExecutionException if the operation was not found
     */
    public static Operation getOperation(Submodel submodel, String idShort) throws ExecutionException {
        Operation result = submodel.getOperation(idShort);
        if (null == result) {
            throw new ExecutionException("Operation '" + idShort + "' not found", null);
        }
        return result;
    }
    
    /**
     * Returns the property for the given {@code idShort} defined on {@link #submodel}.
     * 
     * @param submodel the submodel to query
     * @param idShort the short id
     * @return the property
     * @throws ExecutionException if the property was not found
     */
    public static Property getProperty(Submodel submodel, String idShort) throws ExecutionException {
        Property result = submodel.getProperty(idShort);
        if (null == result) {
            throw new ExecutionException("Property '" + idShort + "' not found", null);
        }
        return result;
    }
    
    /**
     * Returns the value of the property {@code name} from {@link #submodel} as String.
     * 
     * @param name the name 
     * @param dflt the default value to be used if reading the value fails
     * @return the string value or {@code dflt}
     */
    protected String getPropertyStringValue(String name, String dflt) {
        String result = dflt;
        try {
            result = getProperty(name).getValue().toString();
        } catch (ExecutionException e) {
            // dflt
        }
        return result;
    }

    /**
     * Checks for that {@code obj} is a non-empty string.
     * 
     * @param obj the object representing the string
     * @return the string
     * @throws IllegalArgumentException if there is no valid string
     */
    public static String checkString(Object obj) {
        String result = null == obj ? null : obj.toString();
        if (null == result || result.length() == 0) {
            throw new IllegalArgumentException("Not valid string/response");
        }
        return result;
    }

    /**
     * Checks for that {@code result} is not null.
     * 
     * @param <T> the type of object
     * @param obj the object to check
     * @return {@code obj}
     * @throws IllegalArgumentException if {@code obj} is <b>null</b>
     */
    public static <T> T checkNotNull(T obj) {
        if (null == obj) {
            throw new IllegalArgumentException("No valid object");
        }
        return obj;
    }

}
