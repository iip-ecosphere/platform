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

package de.iip_ecosphere.platform.support.aas.types.documentation;

import static de.iip_ecosphere.platform.support.aas.IdentifierType.irdi;
import static de.iip_ecosphere.platform.support.aas.types.common.Utils.*;

import java.util.function.Supplier;

import de.iip_ecosphere.platform.support.Builder;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.LangString;
import de.iip_ecosphere.platform.support.aas.Type;
import de.iip_ecosphere.platform.support.aas.types.common.DelegatingSubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.types.common.DelegatingSubmodelElementCollectionBuilder;
import de.iip_ecosphere.platform.support.aas.PersistenceRecipe.FileResource;
import de.iip_ecosphere.platform.support.aas.Reference;
import de.iip_ecosphere.platform.support.aas.Submodel;
import de.iip_ecosphere.platform.support.aas.Submodel.SubmodelBuilder;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection;
import de.iip_ecosphere.platform.support.aas.SubmodelElementCollection.SubmodelElementCollectionBuilder;

/**
/**
 * Support for <a href="https://industrialdigitaltwin.org/wp-content/uploads/2023/03/
 * IDTA-02004-1-2_Submodel_Handover-Documentation.pdf">IDTA 02004-1-2 Handover Documentation</a>.
 * 
 * @author Holger Eichelberger, SSE
 */
public class HandoverDocumentationBuilder extends DelegatingSubmodelBuilder {

    private boolean createMultiLanguageProperties = true;
    private int documentCount;
    private int primaryCount;

    /**
     * Denotes the permissible document status.
     * 
     * @author Holger Eichelberger, SSE
     */
    public enum DocumentStatus {
        
        RELEASED(irdi("0173-1#07-ABZ641#001"), "released"),
        UNDER_REVIEW(irdi("0173-1#07-ABZ640#001"), "under review");
        
        private String semanticId;
        private String value;

        /**
         * Creates a constant.
         * 
         * @param semanticId the semantic id
         * @param value the value
         */
        private DocumentStatus(String semanticId, String value) {
            this.semanticId = semanticId;
            this.value = value;
        }
        
        /**
         * Returns the semantic id.
         * 
         * @return the semantic id
         */
        public String getSemanticId() {
            return semanticId;
        }

        /**
         * Returns the value to be used in the AAS.
         * 
         * @return the value to be used in the AAS
         */
        public String getValue() {
            return value;
        }
        
    }
    
    /**
     * Creates a handover documentation builder.
     * 
     * @param aasBuilder the parent AAS
     * @param identifier the submodel identifier
     */
    public HandoverDocumentationBuilder(AasBuilder aasBuilder, String identifier) {
        super(aasBuilder.createSubmodelBuilder("Documentation", identifier));
        setSemanticId(irdi("0173-1#01-AHF578#001"));
    }

    /**
     * Defines whether multi-language properties shall be created. AASPackageExplorer compliance.
     *
     * @param createMultiLanguageProperties whether multi-language properties shall be created, taints compliance 
     *     if {@code false}
     */
    public void setCreateMultiLanguageProperties(boolean createMultiLanguageProperties) {
        this.createMultiLanguageProperties = createMultiLanguageProperties;
    } 
    
    /**
     * Creates a document builder.
     * 
     * @return the document builder
     */
    public DocumentBuilder createDocumentBuilder() {
        return new DocumentBuilder(getDelegate(), ++documentCount);
    }

    // based on Figure 3 (not fully compliant) of IDTA 02004-1-2 
    
    /**
     * The document builder.
     * 
     * @author Holger Eichelberger, SSE
     */
    public class DocumentBuilder extends DelegatingSubmodelElementCollectionBuilder {
        
        private int documentIdCount;
        private int documentClassificationCount;
        private int documentVersionCount;
        
        /**
         * Creates a document builder.
         * 
         * @param smBuilder the parent submodel builder
         * @param documentNr the document number
         */
        private DocumentBuilder(SubmodelBuilder smBuilder, int documentNr) {
            super(smBuilder.createSubmodelElementCollectionBuilder(
                getCountingIdShort("Document", documentNr), false, false));
            setSemanticId(irdi("0173-1#01-AHF579#001"));
        }

        /**
         * Returns the DocumentId builder.
         * 
         * @return the builder, {@link Builder#build()} must be called
         */
        public DocumentIdBuilder createDocumentIdBuilder() {
            return new DocumentIdBuilder(getDelegate(), ++documentIdCount);
        }

        /**
         * Returns the DocumentClassification builder.
         * 
         * @return the builder, {@link Builder#build()} must be called
         */
        public DocumentClassificationBuilder createDocumentClassificationBuilder() {
            return new DocumentClassificationBuilder(getDelegate(), ++documentClassificationCount);
        }

        /**
         * Returns the DocumentVersion builder.
         * 
         * @return the builder, {@link Builder#build()} must be called
         */
        public DocumentVersionBuilder createDocumentVersionBuilder() {
            return new DocumentVersionBuilder(getDelegate(), ++documentVersionCount);
        }

        @Override
        public SubmodelElementCollection build() {
            // no tests
            return super.build();
        }

        /**
         * Represents the DocumentId substructure.
         * 
         * @author Holger Eichelberger, SSE
         */
        public class DocumentIdBuilder extends DelegatingSubmodelElementCollectionBuilder {
            
            private boolean domainIdPresent = false;
            private boolean valueIdPresent = false;

            /**
             * Creates the builder.
             * 
             * @param smBuilder the parent builder
             * @param nr the structure number
             */
            private DocumentIdBuilder(SubmodelElementCollectionBuilder smBuilder, int nr) {
                super(smBuilder.createSubmodelElementCollectionBuilder(
                    getCountingIdShort("DocumentId", nr), false, false));
                setSemanticId(irdi("0173-1#02-ABI501#001/0173-1#01-AHF580#001"));
            }

            /**
             * Defines the document DomainId.
             * 
             * @param id the id
             * @return <b>this</b>
             */
            public DocumentIdBuilder setDocumentDomainId(String id) {
                domainIdPresent = true;
                createPropertyBuilder("DocumentDomainId")
                    .setSemanticId(irdi("0173-1#02-ABH994#001"))
                    .setValue(Type.STRING, id).build();
                return this;
            }

            /**
             * Defines the document ValueId.
             * 
             * @param id the id
             * @return <b>this</b>
             */
            public DocumentIdBuilder setValueId(String id) {
                valueIdPresent = true;
                createPropertyBuilder("ValueId")
                    .setSemanticId(irdi("0173-1#02-AAO099#002"))
                    .setValue(Type.STRING, id).build();
                return this;
            }        
            
            /**
             * Defines whether this document is the primary document.
             * 
             * @param isPrimary whether it is primary
             * @return <b>this</b>
             */
            public DocumentIdBuilder setIsPrimary(boolean isPrimary) {
                if (isPrimary) {
                    primaryCount++;
                }
                createPropertyBuilder("IsPrimary")
                    .setSemanticId(irdi("0173-1#02-ABH995#001"))
                    .setValue(Type.BOOLEAN, isPrimary).build();
                return this;
            }
            
            @Override
            public SubmodelElementCollection build() {
                assertThat(domainIdPresent, "The DomainId must be specified.");
                assertThat(valueIdPresent, "The ValueId must be specified.");
                return super.build();
            }
            
        }

        /**
         * Represents a DocumentClassification structure.
         * 
         * @author Holger Eichelberger, SSE
         */
        public class DocumentClassificationBuilder extends DelegatingSubmodelElementCollectionBuilder {
            
            private boolean classPresent = false;

            /**
             * Creates the builder.
             * 
             * @param smBuilder the parent builder
             * @param nr the structure number
             */
            private DocumentClassificationBuilder(SubmodelElementCollectionBuilder smBuilder, int nr) {
                super(smBuilder.createSubmodelElementCollectionBuilder(
                    getCountingIdShort("DocumentClassification", nr) , false, false));
                setSemanticId(irdi("0173-1#02-ABI502#001/0173-1#01-AHF581#001"));
            }

            /**
             * Sets document class, classification and multi-language class names.
             * 
             * @param id the ID taken from the classification system
             * @param classificationSystem the classification system
             * @param classNames the class names
             * @return <b>this</b>
             */
            public DocumentClassificationBuilder setDocumentClass(String id, String classificationSystem, 
                LangString... classNames) {
                classPresent = true;
                createPropertyBuilder("DocumentClassId")
                    .setSemanticId(irdi("0173-1#02-ABH996#001"))
                    .setValue(Type.STRING, id).build();
                createMultiLanguageProperty(getDelegate(), createMultiLanguageProperties, "DocumentClassName", 
                    irdi("0173-1#02-AAO102#003"), classNames);
                createPropertyBuilder("ClassificationSystem")
                    .setSemanticId(irdi("0173-1#02-ABH997#001"))
                    .setValue(Type.STRING, classificationSystem).build();
                return this;
            }

            @Override
            public SubmodelElementCollection build() {
                assertThat(classPresent, "The document class information must be specified.");
                return super.build();
            }
            
        }

        /**
         * Represents a DocumentVersion structure.
         * 
         * @author Holger Eichelberger, SSE
         */
        public class DocumentVersionBuilder extends DelegatingSubmodelElementCollectionBuilder {

            private int languageCount;
            private int digitalFileCount;
            private int previewFileCount;
            private boolean documentVersionPresent = false;
            private boolean titelPresent = false;
            private boolean summaryPresent = false;
            private boolean keyWordsPresent = false;
            private boolean statusPresent = false;
            private boolean organizationNamePresent = false;
            private boolean organizationOfficialNamePresent = false;
            private int refersToCount;
            private int basedOnCount;
            private int translationOfCount;

            /**
             * Creates the builder.
             * 
             * @param smBuilder the parent builder
             * @param nr the structure number
             */
            private DocumentVersionBuilder(SubmodelElementCollectionBuilder smBuilder, int nr) {
                super(smBuilder.createSubmodelElementCollectionBuilder(
                    getCountingIdShort("DocumentVersion", nr), false, false));
                setSemanticId(irdi("0173-1#02-ABI503#001/0173-1#01-AHF582#001"));
            }

            /**
             * Sets the document language.
             * 
             * @param language the language
             * @return <b>this</b>
             */
            public DocumentVersionBuilder setLanguage(String language) {
                createPropertyBuilder(getCountingIdShort("Language", ++languageCount))
                    .setSemanticId(irdi("0173-1#02-AAN468#006"))
                    .setValue(Type.STRING, language)
                    .build();
                return this;
            }

            /**
             * Sets the document version id.
             * 
             * @param version the version id
             * @return <b>this</b>
             */
            public DocumentVersionBuilder setDocumentVersionId(String version) {
                documentVersionPresent = true;
                createPropertyBuilder("DocumentVersionId")
                    .setSemanticId(irdi("0173-1#02-AAO100#002"))
                    .setValue(Type.STRING, version)
                    .build();
                return this;
            }
            
            /**
             * Sets the document title(s).
             * 
             * @param titles the titles
             * @return <b>this</b>
             */
            public DocumentVersionBuilder setTitle(LangString... titles) {
                titelPresent = titles.length > 0;
                createMultiLanguageProperty(getDelegate(), createMultiLanguageProperties, "Title", 
                    irdi("0173-1#02-AAO105#002"), titles);
                return this;
            }
            
            /**
             * Sets the document summary/summaries.
             * 
             * @param summaries the summary/summaries
             * @return <b>this</b>
             */
            public DocumentVersionBuilder setSummary(LangString... summaries) {
                summaryPresent = summaries.length > 0;
                createMultiLanguageProperty(getDelegate(), createMultiLanguageProperties, "Summary", 
                    irdi("0173-1#02-AAO106#002"), summaries);
                return this;
            }

            /**
             * Sets the document keyword(s).
             * 
             * @param keywords the keyword(s)
             * @return <b>this</b>
             */
            public DocumentVersionBuilder setKeywords(LangString... keywords) {
                keyWordsPresent = keywords.length > 0;
                createMultiLanguageProperty(getDelegate(), createMultiLanguageProperties, "KeyWords", 
                    irdi("0173-1#02-ABH999#001"), keywords);
                return this;
            }
            
            /**
             * Sets document status and status set date.
             * 
             * @param date the date in format "2023-12-17T00:00:00.000+00:00"
             * @param value the status value
             * @return <b>this</b>
             */
            public DocumentVersionBuilder setStatus(String date, DocumentStatus value) {
                statusPresent = true;
                createPropertyBuilder("StatusSetDate")
                    .setSemanticId(irdi("0173-1#02-ABI000#001"))
                    .setValue(Type.DATE_TIME, date).build();
                createPropertyBuilder("StatusValue")
                    .setSemanticId(irdi("0173-1#02-ABI001#001"))
                    .setValue(Type.STRING, value.getValue())
                    .build();
                return this;
            }
            
            /**
             * Sets the organization short name.
             * 
             * @param organizationName the name
             * @param organizationOfficialName the official name
             * @return <b>this</b>
             */
            public DocumentVersionBuilder setOrganizationName(String organizationName, 
                String organizationOfficialName) {
                organizationNamePresent = true;
                createPropertyBuilder("OrganizationName")
                    .setSemanticId(irdi("0173-1#02-ABI002#001"))
                    .setValue(Type.STRING, organizationName)
                    .build();
                organizationOfficialNamePresent = true;
                createPropertyBuilder("OrganizationOfficialName")
                    .setSemanticId(irdi("0173-1#02-ABI004#001"))
                    .setValue(Type.STRING, organizationOfficialName)
                    .build();
                return this;
            }

            // Role of Figure 3 not in model
            
            /**
             * Sets the digital file. Can be called multiple times for multiple files.
             * 
             * @param file the file, may be <b>null</b> for none
             * @param mimeType the mime type of the file
             * @return <b>this</b>
             */
            public DocumentVersionBuilder addDigitalFile(FileResource file, String mimeType) {
                if (null != file) {
                    createFileDataElementBuilder(getCountingIdShort("DigitalFile", ++digitalFileCount), 
                        file.getPath(), mimeType)
                        .setSemanticId(irdi("0173-1#02-ABI504#001/0173-1#01-AHF583#001"))
                        .build();
                }
                return this;
            }

            /**
             * Sets the preview file.
             * 
             * @param file the file, may be <b>null</b> for none
             * @param mimeType the mime type of the file
             * @return <b>this</b>
             */
            public DocumentVersionBuilder setPreviewFile(FileResource file, String mimeType) {
                if (null != file) {
                    createFileDataElementBuilder(getCountingIdShort("PreviewFile", ++previewFileCount), 
                        file.getPath(), mimeType)
                        .setSemanticId(irdi("0173-1#02-ABI505#001/0173-1#01-AHF584#001"))
                        .build();
                }
                return this;
            }
            
            /**
             * Adds refersTo references.
             * 
             * @param references the references
             * @return <b>this</b>
             */
            public DocumentVersionBuilder addRefersTo(Reference... references) {
                addReferences("RefersTo", () -> ++refersToCount, irdi("0173-1#02-ABI006#001"), references);
                return this;
            }

            /**
             * Adds basedOn references.
             * 
             * @param references the references
             * @return <b>this</b>
             */
            public DocumentVersionBuilder addBasedOn(Reference... references) {
                addReferences("BasedOn", () -> ++basedOnCount, irdi("0173-1#02-ABI007#001"), references);
                return this;
            }

            /**
             * Adds translationOf references.
             * 
             * @param references the references
             * @return <b>this</b>
             */
            public DocumentVersionBuilder addTranslationOf(Reference... references) {
                addReferences("TranslationOf", () -> ++translationOfCount, irdi("0173-1#02-ABI008#001"), references);
                return this;
            }

            /**
             * Adds references.
             * 
             * @param idShortPrefix prefix for idShort
             * @param nr number provider for idShort suffix (incrementing)
             * @param semanticId the semantic id
             * @param references the references 
             */
            private void addReferences(String idShortPrefix, Supplier<Integer> nr, String semanticId, 
                Reference... references) {
                for (Reference ref: references) {
                    createReferenceElementBuilder(getCountingIdShort(idShortPrefix, nr.get()))
                        .setValue(ref)
                        .setSemanticId("0173-1#02-ABI006#001")
                        .build();
                }
            }
            
            @Override
            public SubmodelElementCollection build() {
                assertThat(languageCount >= 1, "There must be at least a language given.");
                assertThat(documentVersionPresent, "The document version must be specified.");
                assertThat(titelPresent, "The title must be specified.");
                assertThat(summaryPresent, "The summary must be specified.");
                assertThat(keyWordsPresent, "The keywords must be specified.");
                assertThat(statusPresent, "The status must be specified.");
                assertThat(organizationNamePresent, "The organization name must be specified.");
                assertThat(organizationOfficialNamePresent, "The organization official name must be specified.");
                assertThat(digitalFileCount >= 1, "There must be at least a digital file.");
                assertThat(previewFileCount < 2, "Not more than one preview file supported.");
                return super.build();
            }
            
        }

    }

    @Override
    public Submodel build() {
        if (documentCount > 0) {
            assertThat(primaryCount > 0, "There must be at least one primary document");
        }
        return super.build();
    }
    
}
