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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an AAS enumeration.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasEnum extends AbstractAasElement {

    private List<AasEnumLiteral> literals = new ArrayList<>();
    private boolean isOpen;
    private ParsingEnumKind parsingEnumKind;
    
    /**
     * Creates a new AAS enumeration.
     * 
     * @param idShort the idShort denoting the type
     */
    public AasEnum(String idShort) {
        setIdShort(idShort);
    }

    /**
     * Defines whether this enum shall be considered open for additions.
     * 
     * @param isOpen whether this enum shall be considered open
     */
    void setIsOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }
    
    /**
     * Returns whether this enum shall be considered open for additions.
     * 
     * @return whether this enum shall be considered open
     */
    public boolean isOpen() {
        return isOpen;
    }

    /**
     * Defines the internal/temporary parsing enum kind, i.e., the kind of structure this enum was parsed/created from.
     * 
     * @param parsingEnumKind the parsing enum kind
     */
    void setParsingEnumKind(ParsingEnumKind parsingEnumKind) {
        this.parsingEnumKind = parsingEnumKind;
    }
    
    /**
     * Returns the internal/temporary parsing enum kind.
     * 
     * @return the parsing enum kind
     */
    ParsingEnumKind getParsingEnumKind() {
        return parsingEnumKind;
    }
    
    /**
     * Adds a literal.
     * 
     * @param literal the literal
     */
    void addLiteral(AasEnumLiteral literal) {
        literals.add(literal);
    }
    
    /**
     * Returns the literals of this enum.
     * 
     * @return the literals
     */
    public Iterable<AasEnumLiteral> literals() {
        return literals;
    }

    /**
     * Returns whether the given literal is the last one in {@link #literals()}.
     * 
     * @param literal the literal
     * @return {@code true} for the last one, {@code false} else
     */
    boolean isLast(AasEnumLiteral literal) {
        return literals.size() > 0 && literals.get(literals.size() - 1) == literal;
    }

}