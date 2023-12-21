/**
 * ******************************************************************************
 * Copyright (c) {2023} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support.aas.types.hierarchicalStructure;

import static de.iip_ecosphere.platform.support.aas.IdentifierType.iri;

import java.util.ArrayList;
import java.util.List;

import de.iip_ecosphere.platform.support.Builder;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.RelationshipElement.RelationshipElementBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.Entity;
import de.iip_ecosphere.platform.support.aas.Entity.EntityBuilder;
import de.iip_ecosphere.platform.support.aas.Entity.EntityType;
import de.iip_ecosphere.platform.support.aas.MultiLanguageProperty.MultiLanguagePropertyBuilder;
import de.iip_ecosphere.platform.support.aas.Operation.OperationBuilder;
import de.iip_ecosphere.platform.support.aas.Property.PropertyBuilder;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementContainerBuilder;

/**
 * Support for <a href="https://industrialdigitaltwin.org/wp-content/uploads/2023/04
 * /IDTA-02011-1-0_Submodel_HierarchicalStructuresEnablingBoM.pdf">IDTA 02011-1-0 Hierarchical 
 * Structures enabling Bills of Material</a>.
 * 
 * @author Holger Eichelberger, SSE
 */
public class HierarchicalStructureBuilder implements Builder<Submodel> {

    /**
     * Defines the structure archetypes.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum ArcheType {
        FULL("Full"),
        ONE_DOWN("OneDown"),
        ONE_UP("OneUp");
        
        private String value;
        
        /**
         * Creates a constant.
         * 
         * @param value the value for the AAS
         */
        private ArcheType(String value) {
            this.value = value;
        }
        
        /**
         * Returns the value for the AAS.
         * 
         * @return the value
         */
        public String getValue() {
            return value;
        }
        
    }
    
    private SubmodelBuilder smBuilder;
    private int entryNodeCount = 0;
    
    /**
     * Creates a handover documentation builder.
     * 
     * @param aasBuilder the parent AAS
     * @param idShort the idShort of the submodel to create.
     * @param identifier the identifier of the submodel to create.
     * @param archeType the archetype
     */
    public HierarchicalStructureBuilder(AasBuilder aasBuilder, String idShort, String identifier, ArcheType archeType) {
        smBuilder = aasBuilder.createSubmodelBuilder(idShort, identifier);
        smBuilder.setSemanticId(iri("https://admin-shell.io/idta/HierarchicalStructures/1/0/Submodel"));
        smBuilder.createPropertyBuilder("ArcheType")
            .setSemanticId(iri("https://admin-shell.io/idta/HierarchicalStructures/ArcheType/1/0"))
            .setValue(Type.STRING, archeType.getValue())
            .build();
    }
    
    /**
     * Creates a node entry builder.
     * 
     * @param idShort the idShort of the node entry
     * @param type the entity type of the node
     * @return the node entry builder
     * @param asset the asset, may be <b>null</b>
     */
    public EntryNodeBuilder createEntryNodeBuilder(String idShort, EntityType type, Reference asset) {
        entryNodeCount++; // in build?
        return new EntryNodeBuilder(smBuilder, idShort, type, asset);
    }
    
    /**
     * An abstract node builder used to build the two different node kinds in this specification.
     * 
     * @author Holger Eichelberger, SSE
     */
    public abstract class AbstractNodeBuilder implements Builder<SubmodelElementCollection> {

        private EntityBuilder builder;
        private List<RelationshipElementBuilder> hasPart = new ArrayList<>();
        private List<RelationshipElementBuilder> sameAs = new ArrayList<>();
        private List<RelationshipElementBuilder> isPartOf = new ArrayList<>();

        /**
         * Creates an instance.
         * 
         * @param smBuilder the parent builder
         * @param idShort the idShort of this node
         * @param semanticId the semanticId of this node
         * @param type the entity type of the node
         * @param asset the asset, may be <b>null</b>
         */
        private AbstractNodeBuilder(SubmodelElementContainerBuilder smBuilder, String idShort, String semanticId, 
            EntityType type, Reference asset) {
            builder = smBuilder
                .createEntityBuilder(idShort, type, asset)
                .setSemanticId(semanticId);
        }
        
        /**
         * Returns the parent builder.
         * 
         * @return the builder
         */
        protected SubmodelElementContainerBuilder getBuilder() {
            return builder;
        }
        
        /**
         * Creates a nested node builder.
         * 
         * @param idShort the idShort of the node
         * @param type the entity type of the node
         * @return the node builder
         * @param asset the asset, may be <b>null</b>
         */
        public abstract AbstractNodeBuilder createNodeBuilder(String idShort, EntityType type, Reference asset);
        
        /**
         * Creates a relationship.
         * 
         * @param first the first reference
         * @param second the second reference
         * @param idShortPrefix the idShort prefix
         * @param builders the list of builders to be modified as a side effect
         * @param semanticId the semantic id
         * @return <b>this</b>
         */
        private AbstractNodeBuilder addRel(Reference first, Reference second, String idShortPrefix, 
            List<RelationshipElementBuilder> builders, String semanticId) {
            builders.add(builder
                .createRelationshipElementBuilder(idShortPrefix + String.format("%02d", builders.size() + 1), 
                    first, second)
                .setSemanticId(semanticId));
            return this;
        }
        
        /**
         * Creates a sameAs relation to {@code reference}.
         * 
         * @param first the first reference
         * @param second the second reference
         * @return <b>this</b>
         */
        public AbstractNodeBuilder addSameAs(Reference first, Reference second) {
            return addRel(first, second, "sameAs_", sameAs, 
                iri("https://admin-shell.io/idta/HierarchicalStructures/IsPartOf/1/0"));
        }

        /**
         * Creates a isPartOf relation to {@code reference}.
         * 
         * @param first the first reference
         * @param second the second reference
         * @return <b>this</b>
         */
        public AbstractNodeBuilder addIsPartOf(Reference first, Reference second) {
            return addRel(first, second, "isPartOf_", isPartOf, 
                iri("https://admin-shell.io/idta/HierarchicalStructures/IsPartOf/1/0"));
        }
        
        /**
         * Creates a hasPartOf relation to {@code reference}.
         * 
         * @param first the first reference
         * @param second the second reference
         * @return <b>this</b>
         */
        public AbstractNodeBuilder addHasPartOf(Reference first, Reference second) {
            return addRel(first, second, "hasPart_", hasPart, 
                iri("https://admin-shell.io/idta/HierarchicalStructures/HasPart/1/0"));
        }
        

        /**
         * Creates a property builder on this node.
         * 
         * @param idShort the idShort of the property
         * @return the builder
         */
        public PropertyBuilder createPropertyBuilder(String idShort) {
            return builder.createPropertyBuilder(idShort);
        }

        /**
         * Creates a multi-language property builder on this node.
         * 
         * @param idShort the idShort of the property
         * @return the builder
         */
        public MultiLanguagePropertyBuilder createMultiLanguagePropertyBuilder(String idShort) {
            return builder.createMultiLanguagePropertyBuilder(idShort);
        }

        /**
         * Creates an operation builder on this node.
         * 
         * @param idShort the idShort of the operation
         * @return the builder
         */
        public OperationBuilder createOperationBuilder(String idShort) {
            return builder.createOperationBuilder(idShort);
        }
        
        // more builders may follow
        
        /**
         * Returns the reference to the AAS.
         * 
         * @return the reference
         */
        public Reference createReference() {
            return builder.createReference();
        }
        
        /**
         * Calls build on all elements of {@code builders}.
         * 
         * @param builders the builders to build
         */
        private void build(List<RelationshipElementBuilder> builders) {
            builders.forEach(b -> b.build());
        }

        @Override
        public Entity build() {
            build(hasPart);
            build(isPartOf);
            build(sameAs);
            return builder.build();
        }
        
    }

    /**
     * Creates an EntryNode.
     * 
     * @author Holger Eichelberger, SSE
     */
    public class EntryNodeBuilder extends AbstractNodeBuilder {

        private int nodeCount = 0;

        /**
         * Creates an instance.
         * 
         * @param smBuilder the parent builder
         * @param idShort the idShort of this node
         * @param type the entity type
         * @param asset the asset, may be <b>null</b>
         */
        private EntryNodeBuilder(SubmodelBuilder smBuilder, String idShort, EntityType type, Reference asset) {
            super(smBuilder, idShort, iri("https://admin-shell.io/idta/HierarchicalStructures/EntryNode/1/0"), 
                type, asset);
        }

        @Override
        public AbstractNodeBuilder createNodeBuilder(String idShort, EntityType type, Reference asset) {
            nodeCount++; // in build?
            return new NodeBuilder(getBuilder(), idShort, type, asset);
        }
        
        @Override
        public Entity build() {
            assertThat(nodeCount >= 1, "There must be at least one Node.");
            return super.build();
        }

        /**
         * Creates a Node.
         * 
         * @author Holger Eichelberger, SSE
         */
        public class NodeBuilder extends AbstractNodeBuilder {

            /**
             * Creates an instance.
             * 
             * @param smBuilder the parent builder
             * @param idShort the idShort of this node
             * @param type the entity type
             * @param asset the asset, may be <b>null</b>
             */
            private NodeBuilder(SubmodelElementContainerBuilder smBuilder, String idShort, EntityType type, 
                Reference asset) {
                super(smBuilder, idShort, iri("https://admin-shell.io/idta/HierarchicalStructures/Node/1/0"), 
                    type, asset);
            }

            @Override
            public AbstractNodeBuilder createNodeBuilder(String idShort, EntityType type, Reference asset) {
                return new NodeBuilder(getBuilder(), idShort, type, asset);
            }

        }
        
    }
    
    /**
     * Assert that {@code valid} else emits an {@link IllegalArgumentException} with text 
     * {@code exception}.
     * 
     * @param valid the validity criteria
     * @param exception the exception text
     * @throws IllegalArgumentException if not {@code valid}
     */
    private static void assertThat(boolean valid, String exception) {
        if (!valid) {
            throw new IllegalArgumentException(exception);
        }
    }
    
    @Override
    public Submodel build() {
        assertThat(entryNodeCount == 1, "There must be exactly one EntryNode");
        return smBuilder.build();
    }

}
