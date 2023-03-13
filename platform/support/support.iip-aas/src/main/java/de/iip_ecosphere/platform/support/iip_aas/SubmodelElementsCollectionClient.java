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

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iip_ecosphere.platform.support.aas.Operation;
import de.iip_ecosphere.platform.support.aas.Property;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;

/**
 * A {@link SubmodelClient} which obtains properties and operations defined in a submodel elements collection.
 * 
 * @author Holger Eichelberger, SSE
 */
public class SubmodelElementsCollectionClient extends SubmodelClient {

    private String collectionId;
    
    /**
     * Creates a client instance based on a deployed IIP-AAS from {@link AasPartRegistry} based on a specified
     * submodel and a collection id.
     * 
     * @param submodel the submode to refer to
     * @param collectionId the id used as key in {@code submodel} to denote the collection to operate on
     * @throws IOException if retrieving the IIP-AAS or the respective submodel fails
     */
    public SubmodelElementsCollectionClient(String submodel, String collectionId) throws IOException {
        this(ActiveAasBase.getSubmodel(submodel), collectionId);
    }

    /**
     * Creates a client instance based on a deployed IIP-AAS from {@link AasPartRegistry} based on a specified
     * submodel and a collection id.
     * 
     * @param submodel the submode to refer to
     * @param collectionId the id used as key in {@code submodel} to denote the collection to operate on
     * @param fallback submodel to be used if retrieving {@code sumbodel} fails
     */
    public SubmodelElementsCollectionClient(String submodel, String collectionId, Submodel fallback) {
        this(getSubmodel(submodel, fallback), collectionId);
    }

    /**
     * Creates a client instance based on the given {@code submodel}. Operation and properties will be taken from the 
     * submodel elements collection {@code collectionId} within {@code submodel},
     * 
     * @param submodel the submodel to use
     * @param collectionId the id used as key in {@code submodel} to denote the resource 
     *   to operate on
     */
    public SubmodelElementsCollectionClient(Submodel submodel, String collectionId) {
        super(submodel);
        this.collectionId = collectionId;
    }
    
    /**
     * Returns the submodel with name {@code submodel} or of retrieving it fails {@code fallback}.
     * 
     * @param submodel the submodel name
     * @param fallback the fallback submodel, may be <b>null</b>
     * @return the submodel or {@code fallback}
     */
    private static Submodel getSubmodel(String submodel, Submodel fallback) {
        Submodel result;
        try {
            result = ActiveAasBase.getSubmodel(submodel);
        } catch (IOException e) {
            result = fallback;
        }
        return result;
    }

    /**
     * Returns the actual submodel elements collection to use.
     * 
     * @return the collection
     */
    protected SubmodelElementCollection getSubmodelElementCollection() {
        return getSubmodel().getSubmodelElementCollection(AasUtils.fixId(collectionId));
    }

    @Override
    protected Operation getOperation(String idShort) throws ExecutionException {
        Operation result = null;
        SubmodelElementCollection resource = getSubmodelElementCollection();
        if (resource != null) {
            result = resource.getOperation(idShort);
        }
        if (null == result) {
            throw new ExecutionException("Operation `" + idShort + "` on resource `" + collectionId 
                + "` not found.", null); 
        }
        return result;
    }

    @Override
    protected Property getProperty(String idShort) throws ExecutionException {
        Property result = null;
        SubmodelElementCollection resource = getSubmodelElementCollection();
        if (resource != null) {
            result = resource.getProperty(idShort);
        }
        if (null == result) {
            throw new ExecutionException("Property `" + idShort + "` on resource `" + collectionId 
                + "` not found.", null); 
        }
        return result;
    }

    /**
     * Returns the logger instance.
     * 
     * @return the logger
     */
    protected Logger getLogger() {
        return LoggerFactory.getLogger(getClass());
    }
    
}
