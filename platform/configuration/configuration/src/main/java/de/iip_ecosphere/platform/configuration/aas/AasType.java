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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an AAS type, i.e., Submodel, SubmodelElementCollection, Entity.
 * 
 * @author Holger Eichelberger, SSE
 */
public class AasType extends AbstractAasElement {
    
    public static final String PARENT_AAS = "*AAS*"; // certainly no idShort
    
    private boolean allowDuplicates = false;
    private boolean ordered = false;
    private boolean fixedIdShort;
    private boolean isMultiValued;
    private AasSmeType smeType;
    private String parent;
    private List<AasField> fields = new ArrayList<>();
    private List<AasOperation> operations = new ArrayList<>();
    private EntityType entityType;
    private boolean isAspect;
    private Map<String, String> mappedSemanticIds = null;
    // -> copy

    /**
     * Represents the entity type.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum EntityType {
        
        /**
         * For co-managed entities there is no separate AAS. Co-managed entities need to
         * be part of a self-managed entity.
         */
        COMANAGEDENTITY("CoManagedEntity"),

        /**
         * Self-Managed Entities have their own AAS but can be part of the bill of
         * material of a composite self-managed entity. The asset of an I4.0 Component
         * is a self-managed entity per definition.
         */
        SELFMANAGEDENTITY("SelfManagedEntity");
        
        private String literal;

        /**
         * Creates an instance.
         * 
         * @param literal the identifying literal
         */
        private EntityType(String literal) {
            this.literal = literal;
        }
        
        /**
         * Returns the representing literal.
         * 
         * @return the literal
         */
        public String getLiteral() {
            return literal;
        }
        
        /**
         * Returns a contained constant or <b>null</b>.
         * 
         * @param text the text to look within
         * @return the constant or <b>null</b>
         */
        public static EntityType fromText(String text) {
            EntityType result = null;
            if (null != text) {
                for (EntityType e : values()) {
                    if (text.contains(e.getLiteral())) {
                        result = e;
                        break;
                    }
                }
            }
            return result;
        }
        
        /**
         * Returns a matching constant or <b>null</b>.
         * 
         * @param value the value as text
         * @return the constant or <b>null</b>
         */
        public static EntityType valueOfSafe(String value) {
            EntityType result = null;
            if (null != value) {
                try {
                    result = valueOf(value);
                } catch (IllegalArgumentException ex) {
                    for (EntityType e : values()) {
                        if (e.getLiteral().equalsIgnoreCase(value)) {
                            result = e;
                            break;
                        }
                    }
                }
            }
            return result;
        }

    }
    
    /**
     * Creates an AAS type.
     * 
     * @param idShort the idShort of the type without multi-value counting suffix.
     * @param fixedIdShort whether the idShort shall always be stated as given
     * @param isMultiValued whether the type is multi-valued (with counting idShort)
     */
    public AasType(String idShort, boolean fixedIdShort, boolean isMultiValued) {
        setIdShort(idShort);
        this.fixedIdShort = fixedIdShort;
        this.isMultiValued = isMultiValued;
    }
    
    /**
     * Creates a new instance by deepely copying {@code type}.
     * 
     * @param type the type
     */
    AasType(AasType type) {
        super(type);
        this.allowDuplicates = type.allowDuplicates;
        this.ordered = type.ordered;
        this.fixedIdShort = type.fixedIdShort;
        this.isMultiValued = type.isMultiValued;
        this.smeType = type.smeType;
        this.parent = type.parent;
        for (AasField f : type.fields) {
            this.fields.add(new AasField(f));
        }
        for (AasField o : type.operations) {
            this.operations.add(new AasOperation(o));
        }
        this.entityType = type.entityType;
        this.isAspect = type.isAspect;
        if (null != type.mappedSemanticIds) {
            this.mappedSemanticIds = new HashMap<>(type.mappedSemanticIds);
        }
    }
    
    /**
     * Returns whether the idShort shall always be stated as given.
     * 
     * @return {@code true} if the idShort shall always be stated as given, {@code false} else
     */
    public boolean isFixedIdShort() {
        return fixedIdShort;
    }
    
    /**
     * Returns whether the type is multi-valued (with counting idShort).
     * 
     * @return {@code true} if the type is multi-valued, {@code false} else
     */
    public boolean isMultiValued() {
        return isMultiValued;
    }

    /**
     * Returns whether duplicates are allowed within the type.
     * 
     * @return {@code true} for duplicates, {@code false} else
     */
    public boolean isAllowDuplicates() {
        return allowDuplicates;
    }

    /**
     * Returns whether this type is an aspect (of it's {@link #getIdShort()}).
     * 
     * @return whether this type is an aspect (of it's {@link #getIdShort()}) or not.
     */
    public boolean isAspect() {
        return isAspect;
    }
    
    /**
     * Changes the aspect flag.
     * 
     * @param isAspect whether this type is an aspect (of it's {@link #getIdShort()}) or not.
     */
    void setAspect(boolean isAspect) {
        this.isAspect = isAspect;
    }

    /**
     * Defines whether duplicates are allowed within the type.
     * 
     * @param allowDuplicates {@code true} for duplicates, {@code false} else
     */
    void setAllowDuplicates(boolean allowDuplicates) {
        this.allowDuplicates = allowDuplicates;
    }

    /**
     * Returns whether elements within the type are ordered.
     * 
     * @return {@code true} for ordered, {@code false} else
     */
    public boolean isOrdered() {
        return ordered;
    }

    /**
     * Defines whether elements within the type are ordered.
     * 
     * @param ordered {@code true} for ordered, {@code false} else
     */
    void setOrdered(boolean ordered) {
        this.ordered = ordered;
    }
    
    /**
     * Sets the entity type.
     * 
     * @param entityType the entity type
     */
    void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    /**
     * Returns the entity type.
     * 
     * @return the entity type
     */
    public EntityType getEntityType() {
        return entityType;
    }
    
    /**
     * Returns the SME type.
     * 
     * @return the SME type
     */
    public AasSmeType getSmeType() {
        return smeType;
    }

    /**
     * Defines the SME type.
     * 
     * @param smeType the SME type
     */
    void setSmeType(AasSmeType smeType) {
        this.smeType = smeType;
    }

    /**
     * Sets name of the parent, usually a submodel element collection, may be {@link #PARENT_AAS}.
     * 
     * @param parent the parent, may be {@link #PARENT_AAS} to indicate an unspecified AAS as parent
     * @see #isAasParent()
     */
    void setParent(String parent) {
        this.parent = parent;
    }

    /**
     * The name of the parent.
     * 
     * @return the parent, may be {@link #PARENT_AAS}
     * @see #isAasParent()
     */
    public String getParent() {
        return parent;
    }

    /**
     * Returns whether an AAS shall be the parent of the this type.
     * 
     * @return {@code true} for AAS, {@code false} else
     * @see #getParent()
     */
    public boolean isAasParent() {
        return PARENT_AAS.equals(parent);
    }

    /**
     * Adds an operation.
     * 
     * @param operation the operation to be added (may be <b>null</b>, ignored then)
     */
    void addOperation(AasOperation operation) {
        addField(operation);
    }

    /**
     * Adds a field.
     * 
     * @param field the field to be added (may be <b>null</b>, ignored then)
     */
    void addField(AasField field) {
        if (null != field) {
            if (AasSmeType.OPERATION == field.getSmeType()) {
                operations.add(new AasOperation(field));
            } else {
                fields.add(field);
            }
        }
    }
    
    /**
     * Returns the fields of this type.
     * 
     * @return the fields
     */
    public Iterable<AasField> fields() {
        return fields;
    }
    
    /**
     * Returns whether this type has fields.
     * 
     * @return {@code true} if there are fields, {@code false} else
     */
    public boolean hasFields() {
        return fields.size() > 0;
    }
    
    /**
     * The number of fields declared for this type.
     * 
     * @return the number of fields
     */
    public int getFieldsCount() {
        return fields.size();
    }
    
    /**
     * Returns the operations of this type.
     * 
     * @return the operations
     */
    public Iterable<AasOperation> operations() {
        return operations;
    }
    
    /**
     * The number of operations declared for this type.
     * 
     * @return the number of operations
     */
    public int getOperationsCount() {
        return operations.size();
    }
    
    /**
     * Returns whether the given field is the last one in {@link #fields()}.
     * 
     * @param field the field
     * @return {@code true} for the last one, {@code false} else
     */
    boolean isLast(AasField field) {
        return fields.size() > 0 && fields.get(fields.size() - 1) == field;
    }
    
    /**
     * In case of {@link #isAspect()}, set specific semanticIds per type. Sets the given value
     * if there were no mapped semantic ids before, else merges them into the existing ones.
     * 
     * @param mappedSemanticIds the mapped semantic IDs
     */
    void setMappedSemanticIds(Map<String, String> mappedSemanticIds) {
        if (null != mappedSemanticIds) {
            if (null == this.mappedSemanticIds) {
                this.mappedSemanticIds = mappedSemanticIds;
            } else {
                this.mappedSemanticIds.putAll(mappedSemanticIds);
            }
        }
    }
    
    /**
     * In case of {@link #isAspect()}, return a specific/mapped semanticId for the given type via its {@code idShort}.
     * 
     * @param idShort the target idShort
     * @return the specific/mapped semantic id or <b>null</b> for none
     */
    public String getMappedSemanticId(String idShort) {
        return null == mappedSemanticIds ? null : mappedSemanticIds.get(idShort);
    }
    
    @Override
    public String toString() {
        return "AAStype: " + getIdShort();
    }

}