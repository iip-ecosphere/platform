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

package de.iip_ecosphere.platform.configuration.aas;

/**
 * Represents an AAS enumeration literal.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasEnumLiteral extends AbstractAasElement {

    private String identifier;
    private String value;
    private String valueId;
    
    /**
     * Creates an AAS enumeration literal.
     * 
     * @param idShort the idShort/name of the literal
     * @param valueId the semantic id of the value
     * @param description the description
     * @param identifier the optional identifier to be used instead of name to derive the programming
     *   language identifier, may be <b>null</b> for none
     */
    public AasEnumLiteral(String idShort, String valueId, String description, String identifier) {
        setIdShort(idShort);
        this.valueId = valueId;
        this.identifier = identifier;
    }

    /**
     * Returns the identifier to be used instead of {@link #getName()} to form the programming
     * language identifier of the literal.
     * 
     * @return the identifier, may be <b>null</b> to use {@link #getName()} instead
     */
    public String getIdentifier() {
        return identifier;
    }
    
    /**
     * Defines the value.
     * 
     * @param value the value, <b>null</b> for none
     */
    void setValue(String value) {
        this.value = value;
    }
    
    /**
     * Returns the value.
     * 
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the value (semantic) id.
     * 
     * @return the value (semantic) id
     */
    public String getValueId() {
        return valueId;
    }
    
}