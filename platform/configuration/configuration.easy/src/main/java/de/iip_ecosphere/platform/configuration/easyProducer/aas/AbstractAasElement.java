/**
 * ******************************************************************************
 * Copyright (c) {2024} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.configuration.easyProducer.aas;

import de.iip_ecosphere.platform.support.aas.IdentifierType;

/**
 * Abstract superclass for AAS elements.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AbstractAasElement {

    private String idShort;
    private String semanticId;
    private String isCaseOf;
    private String description;
    private boolean hasMultiSemanticIds;
    private boolean isGeneric;
    private String displayName;
    // -> copy
    
    /**
     * Creates an instance.
     */
    public AbstractAasElement() {
    }

    /**
     * Copies values of {@code elt}.
     * 
     * @param elt the source element
     */
    public AbstractAasElement(AbstractAasElement elt) {
        this.idShort = elt.idShort;
        this.semanticId = elt.semanticId;
        this.isCaseOf = elt.isCaseOf;
        this.description = elt.description;
        this.isGeneric = elt.isGeneric;
        this.displayName = elt.displayName;
    }
    
    /**
     * Defines the idShort.
     * 
     * @param idShort the idShort without multi-value counting suffix.
     */
    void setIdShort(String idShort) {
        this.idShort = idShort;
    }
    
    /**
     * Returns the idShort without multi-value counting suffix.
     * 
     * @return the idShort
     */
    public String getIdShort() {
        return idShort;
    }
    
    /**
     * Sets the semantic id of this field.
     * 
     * @param semanticId the semantic id in format of the AAS abstraction of the platform, i.e.,
     *    prefixed with identifier from {@link IdentifierType}, may be <b>null</b> for none
     */
    void setSemanticId(String semanticId) {
        this.semanticId = semanticId;
    }

    /**
     * Returns the semantic id of this field.
     * 
     * @return the semantic id in format of the AAS abstraction of the platform, i.e.,
     *    prefixed with identifier from {@link IdentifierType}, may be <b>null</b> for none
     */
    public String getSemanticId() {
        return semanticId;
    }

    /**
     * Defines whether this field has multiple semantic ids, i.e., {@link #getSemanticId()} returns a possible one.
     * 
     * @param hasMultiSemanticIds if the field has multiple semantic ids
     */
    void setMultiSemanticIds(boolean hasMultiSemanticIds) {
        this.hasMultiSemanticIds = hasMultiSemanticIds;
    }

    /**
     * Returns whether this field has multiple semantic ids, i.e., {@link #getSemanticId()} returns a possible one.
     * 
     * @return if the field has multiple semantic ids
     */
    public boolean hasMultiSemanticIds() {
        return hasMultiSemanticIds;
    }
    

    /**
     * Sets the "isCaseOf" semantic id.
     * 
     * @param isCaseOf the "isCaseOf" semantic id in format of the AAS abstraction of the platform, i.e.,
     *    prefixed with identifier from {@link IdentifierType}
     */
    void setIsCaseOf(String isCaseOf) {
        this.isCaseOf = isCaseOf;
    }
    
    /**
     * Returns "isCaseOf" semantic id.
     * 
     * @return the semantic id in format of the AAS abstraction of the platform, i.e.,
     *    prefixed with identifier from {@link IdentifierType}, may be <b>null</b> for none
     */
    public String getIsCaseOf() {
        return isCaseOf;
    }
    
    /**
     * Returns whether this field is generic, i.e., an indication that further fields may be added.
     * 
     * @return if the field is generic
     */
    public boolean isGeneric() {
        return isGeneric;
    }

    /**
     * Changes whether this field is generic, i.e., an indication that further fields may be added.
     * 
     * @param isGeneric if the field is generic
     */
    void setGeneric(boolean isGeneric) {
        this.isGeneric = isGeneric;
    }    
    
    /**
     * Sets the description/explanation of this field. Adds a full stop to the end if there is none.
     * 
     * @param description the description/explanation
     */
    void setDescription(String description) {
        this.description = description;
        if (this.description != null && description.length() > 0 && !description.endsWith(".")) {
            this.description += "."; // IDTA-02003-1-2
        }
    }

    /**
     * Returns the description/explanation.
     * 
     * @return the description/explanation
     */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return getIdShort();
    }

    /**
     * Sets the display name.
     * 
     * @param displayName the type name
     */
    void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Returns the display name if defined or {@link #getIdShort()}.
     * 
     * @return the display name
     */
    public String getDisplayName() {
        return null == displayName ? getIdShort() : displayName;
    }
}
