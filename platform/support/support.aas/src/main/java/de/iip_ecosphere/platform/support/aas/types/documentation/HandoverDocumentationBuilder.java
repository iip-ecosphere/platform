package de.iip_ecosphere.platform.support.aas.types.documentation;

import static de.iip_ecosphere.platform.support.aas.IdentifierType.*;
import static de.iip_ecosphere.platform.support.aas.types.common.Utils.*;

import de.iip_ecosphere.platform.support.Builder;
import de.iip_ecosphere.platform.support.aas.Aas.AasBuilder;
import de.iip_ecosphere.platform.support.aas.Entity.EntityBuilder;
import de.iip_ecosphere.platform.support.aas.*;
import de.iip_ecosphere.platform.support.aas.types.common.*;

/**
* Builder support for the Submodel defines a set meta data for the handover of documentation from the manufacturer to
* the operator for industrial equipment.
* Generated by: EASy-Producer.
*/
public class HandoverDocumentationBuilder extends DelegatingSubmodelBuilder {

    private boolean createMultiLanguageProperties = true;

    private int documentCounter = 0;
    private int entityCounter = 0;

    /**
    * Enumeration support for each document version represents a point in time in the document lifecycle. This status
    * value refers to the milestones in the document lifecycle. The following two values should be used for the 
    * application of this guideline: InReview (under review), Released (released).
    * Generated by: EASy-Producer.
    */
    public enum StatusValue {
    
        UNDER_REVIEW(0, "0173-1#07-ABZ640#001", "under review"),
        RELEASED(0, "0173-1#07-ABZ641#001", "released");
    
        private int valueId;
        private String semanticId;
        private String value;
    
        /**
         * Creates a constant.
         * 
         * @param valueId the value id/given ordinal
         * @param semanticId the semantic id
         * @param value the value
         */
        private StatusValue(int valueId, String semanticId, String value) {
            this.valueId = valueId;
            this.semanticId = semanticId;
            this.value = value;
        }
        
        /**
        * Returns the (optional) value id/given ordinal.
        *
        * @return the (optional) value id/given ordinal
        */
        public int getValueId() {
            return valueId;
        }
        
        /**
        * Returns the (optional) semantic id.
        *
        * @return the (optional) semantic id
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
    * Creates a HandoverDocumentation submodel builder.
    * 
    * @param aasBuilder the parent AAS builder
    * @param identifier the submodel identifier
    */            
    public HandoverDocumentationBuilder(AasBuilder aasBuilder, String identifier) {
        this(aasBuilder, identifier, "HandoverDocumentation");
    }

    /**
    * Creates a HandoverDocumentation submodel builder.
    * 
    * @param aasBuilder the parent AAS builder
    * @param identifier the submodel identifier
    * @param idShort the idShort of the submodel to create
    */            
    public HandoverDocumentationBuilder(AasBuilder aasBuilder, String identifier, String idShort) {
        super(aasBuilder.createSubmodelBuilder(idShort, identifier));
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
    * Creates a builder for Document.
    *
    * @return the builder instance, {@link Builder#build()} must be called
    */
    public DocumentBuilder createDocumentBuilder() {
        // counting -> composition of idShort
        return new DocumentBuilder(this, ++documentCounter);
    }
    
    /**
    * Creates a builder for Entity.
    *
    * @param type the entity type
    * @return the builder instance, {@link Builder#build()} must be called
    */
    public EntityBuilder createEntityBuilder(Entity.EntityType type) {
        // counting -> composition of idShort
        return super.createEntityBuilder(getCountingIdShort("Entity", ++entityCounter), type, null);
    }
    
    @Override
    public Submodel build() {
        assertThat(0 <= documentCounter, "Cardinality {} of Document must be greater or equal 0.", documentCounter);
        assertThat(0 <= entityCounter, "Cardinality {} of Entity must be greater or equal 0.", entityCounter);
        
        return super.build();
    }

    /**
    * Builder support for this SubmodelElementCollection holds the information for a VDI 2770 Document entity.
    * Generated by: EASy-Producer.
    */
    public class DocumentBuilder extends DelegatingSubmodelElementCollectionBuilder {
        
        private int documentIdCounter = 0;
        private int documentClassificationCounter = 0;
        private int documentVersionCounter = 0;
        private int documentedEntityCounter = 0;
    
        /**
        * Creates a builder instance for Document.
        *
        * @param smBuilder the parent submodel builder
        */
        protected DocumentBuilder(DelegatingSubmodelBuilder smBuilder) {
            super(smBuilder.createSubmodelElementCollectionBuilder("Document"), smBuilder);
            setSemanticId(irdi("0173-1#01-AHF579#001"));
        }
        
        /**
        * Creates a builder instance for Document.
        *
        * @param smBuilder the parent submodel builder
        * @param nr the structure number
        */
        protected DocumentBuilder(DelegatingSubmodelBuilder smBuilder, int nr) {
            super(smBuilder.createSubmodelElementCollectionBuilder(getCountingIdShort("Document", nr)), smBuilder);
            setSemanticId(irdi("0173-1#01-AHF579#001"));
        }
        
        /**
        * Creates a builder for DocumentId.
        *
        * @return the builder instance, {@link Builder#build()} must be called
        */
        public DocumentIdBuilder createDocumentIdBuilder() {
            // counting -> composition of idShort
            return new DocumentIdBuilder(this, ++documentIdCounter);
        }
        
        /**
        * Creates a builder for DocumentClassification.
        *
        * @return the builder instance, {@link Builder#build()} must be called
        */
        public DocumentClassificationBuilder createDocumentClassificationBuilder() {
            // counting -> composition of idShort
            return new DocumentClassificationBuilder(this, ++documentClassificationCounter);
        }
        
        /**
        * Creates a builder for DocumentVersion.
        *
        * @return the builder instance, {@link Builder#build()} must be called
        */
        public DocumentVersionBuilder createDocumentVersionBuilder() {
            // counting -> composition of idShort
            return new DocumentVersionBuilder(this, ++documentVersionCounter);
        }
        
        /**
        * Changes [IRDI PATH] 0173-1#02-ABI501#001/0173-1#01- AHF580#001*00 For DocumentClassification{00}: [IRDI
        * PATH] 0173-1#02-ABI502#001/0173-1#01- AHF581#001*00 For DocumentVersion{00}: [IRDI PATH]
        * 0173-1#02-ABI503#001/0173-1#01- AHF582#001*00 Identifies entities, which are subject to the Document..
        *
        * @param reference the target reference
        * @param semanticId the actual semantic ID
        * @return <b>this</b> (builder style)
        */
        public DocumentBuilder setDocumentedEntity(Reference reference, String semanticId) {
            // counting -> composition of idShort
            createReferenceElementBuilder(getCountingIdShort("DocumentedEntity", ++documentedEntityCounter))
                .setSemanticId(semanticId != null && semanticId.length() > 0 ? semanticId : "")
                .setValue(reference).build();
            return this;
        }
        
        @Override
        public SubmodelElementCollection build() {
            assertThat(1 <= documentIdCounter, "Cardinality {} of DocumentId must be greater or equal 1.",
                documentIdCounter);
            assertThat(1 <= documentClassificationCounter, "Cardinality {} of DocumentClassification must be greater or"
                + " equal 1.", documentClassificationCounter);
            assertThat(0 <= documentVersionCounter, "Cardinality {} of DocumentVersion must be greater or equal 0.",
                documentVersionCounter);
            assertThat(0 <= documentedEntityCounter, "Cardinality {} of DocumentedEntity must be greater or equal 0.",
                documentedEntityCounter);
            
            return super.build();
        }
    }
    
    /**
    * Builder support for this SubmodelElementCollection holds the information for a VDI 2770 DocumentIdDomain entity
    * and the DocumentId property.
    * Generated by: EASy-Producer.
    */
    public class DocumentIdBuilder extends DelegatingSubmodelElementCollectionBuilder {
        
        private int documentDomainIdCounter = 0;
        private int valueIdCounter = 0;
        private int isPrimaryCounter = 0;
    
        /**
        * Creates a builder instance for DocumentId.
        *
        * @param smBuilder the parent submodel element container builder
        */
        protected DocumentIdBuilder(DelegatingSubmodelElementContainerBuilder smBuilder) {
            super(smBuilder.createSubmodelElementCollectionBuilder("DocumentId"), smBuilder);
            setSemanticId(irdi("0173-1#01-AHF580#001"));
        }
        
        /**
        * Creates a builder instance for DocumentId.
        *
        * @param smBuilder the parent submodel element container builder
        * @param nr the structure number
        */
        protected DocumentIdBuilder(DelegatingSubmodelElementContainerBuilder smBuilder, int nr) {
            super(smBuilder.createSubmodelElementCollectionBuilder(getCountingIdShort("DocumentId", nr)), smBuilder);
            setSemanticId(irdi("0173-1#01-AHF580#001"));
        }
        
        /**
        * Changes identification of the domain in which the given DocumentId is unique. The domain ID can be e.g. the
        * name or acronym of the providing organization..
        *
        * @param documentDomainId the value for DocumentDomainId
        * @return <b>this</b> (builder style)
        */
        public DocumentIdBuilder setDocumentDomainId(String documentDomainId) {
            documentDomainIdCounter++;
            createPropertyBuilder("DocumentDomainId")
                .setSemanticId(irdi("0173-1#02-ABH994#001"))
                .setValue(Type.STRING, documentDomainId).build();
            return this;
        }
        
        /**
        * Changes identification number of the Document within a given domain, e.g. the providing organization..
        *
        * @param valueId the value for ValueId
        * @return <b>this</b> (builder style)
        */
        public DocumentIdBuilder setValueId(String valueId) {
            valueIdCounter++;
            createPropertyBuilder("ValueId")
                .setSemanticId(irdi("0173-1#02-AAO099#002"))
                .setValue(Type.STRING, valueId).build();
            return this;
        }
        
        /**
        * Changes flag indicating that a DocumentId within a collection of at least two DocumentIds is the "primary"
        * identifier for the document. This is the preferred ID of the document (commonly from the point of view of the 
        * owner of the asset)..
        *
        * @param isPrimary the value for IsPrimary
        * @return <b>this</b> (builder style)
        */
        public DocumentIdBuilder setIsPrimary(boolean isPrimary) {
            isPrimaryCounter++;
            createPropertyBuilder("IsPrimary")
                .setSemanticId(irdi("0173-1#02-ABH995#001"))
                .setValue(Type.BOOLEAN, isPrimary).build();
            return this;
        }
        
        @Override
        public SubmodelElementCollection build() {
            assertThat(documentDomainIdCounter == 1, "Cardinality {} of DocumentDomainId must be equal 1.",
                documentDomainIdCounter);
            assertThat(valueIdCounter == 1, "Cardinality {} of ValueId must be equal 1.", valueIdCounter);
            assertThat(0 <= isPrimaryCounter && isPrimaryCounter <= 1, "Cardinality {} of IsPrimary must be greater or "
                + "equal 0 and less or equal 1.", isPrimaryCounter);
            
            return super.build();
        }
    }
    
    /**
    * Builder support for this SubmodelElementCollection holds the information for a VDI 2770 DocumentClassification
    * entity.
    * Generated by: EASy-Producer.
    */
    public class DocumentClassificationBuilder extends DelegatingSubmodelElementCollectionBuilder {
        
        private int classIdCounter = 0;
        private int classNameCounter = 0;
        private int classificationSystemCounter = 0;
    
        /**
        * Creates a builder instance for DocumentClassification.
        *
        * @param smBuilder the parent submodel element container builder
        */
        protected DocumentClassificationBuilder(DelegatingSubmodelElementContainerBuilder smBuilder) {
            super(smBuilder.createSubmodelElementCollectionBuilder("DocumentClassification"), smBuilder);
            setSemanticId(irdi("0173-1#01-AHF581#001"));
        }
        
        /**
        * Creates a builder instance for DocumentClassification.
        *
        * @param smBuilder the parent submodel element container builder
        * @param nr the structure number
        */
        protected DocumentClassificationBuilder(DelegatingSubmodelElementContainerBuilder smBuilder, int nr) {
            super(smBuilder.createSubmodelElementCollectionBuilder(getCountingIdShort("DocumentClassification", nr)), 
                smBuilder);
            setSemanticId(irdi("0173-1#01-AHF581#001"));
        }
        
        /**
        * Changes unique ID of the document class within a ClassificationSystem. Constraint: if ClassificationSystem
        * is set to "VDI2770 Blatt 1:2020", the given IDs of VDI2770 Blatt 1:2020 shall be used (see Table 1)..
        *
        * @param classId the value for ClassId
        * @return <b>this</b> (builder style)
        */
        public DocumentClassificationBuilder setClassId(String classId) {
            classIdCounter++;
            createPropertyBuilder("ClassId")
                .setSemanticId(irdi("0173-1#02-ABH996#001"))
                .setValue(Type.STRING, classId).build();
            return this;
        }
        
        /**
        * Changes list of language-dependent names of the selected ClassID. Constraint: if ClassificationSystem is set
        * to "VDI2770 Blatt 1:2020", the given names of VDI2770:2020 need be used (see Table 1). Constraint: languages 
        * shall match at least the language specifications of the included DocumentVersions (below)..
        *
        * @param className the value for ClassName
        * @return <b>this</b> (builder style)
        */
        public DocumentClassificationBuilder setClassName(LangString... className) {
            if (className.length > 0) {
                classNameCounter++;
            }
            createMultiLanguageProperty(getDelegate(), createMultiLanguageProperties, "ClassName", 
                irdi("0173-1#02-AAO102#003"), className);
            return this;
        }
        
        /**
        * Changes identification of the classification system. For classifications according to VDI 2270 Blatt 1,
        * always set to "VDI2770 Blatt 1:2020". Further classification systems are commonly used, such as 
        * "IEC61355-1:2008"..
        *
        * @param classificationSystem the value for ClassificationSystem
        * @return <b>this</b> (builder style)
        */
        public DocumentClassificationBuilder setClassificationSystem(String classificationSystem) {
            classificationSystemCounter++;
            createPropertyBuilder("ClassificationSystem")
                .setSemanticId(irdi("0173-1#02-ABH997#001"))
                .setValue(Type.STRING, classificationSystem).build();
            return this;
        }
        
        @Override
        public SubmodelElementCollection build() {
            assertThat(classIdCounter == 1, "Cardinality {} of ClassId must be equal 1.", classIdCounter);
            assertThat(classNameCounter == 1, "Cardinality {} of ClassName must be equal 1.", classNameCounter);
            assertThat(classificationSystemCounter == 1, "Cardinality {} of ClassificationSystem must be equal 1.",
                classificationSystemCounter);
            
            return super.build();
        }
    }
    
    /**
    * Builder support for this SubmodelElementCollection holds the information for a VDI2770 DocumentVersion entity.
    * Generated by: EASy-Producer.
    */
    public class DocumentVersionBuilder extends DelegatingSubmodelElementCollectionBuilder {
        
        private int languageCounter = 0;
        private int documentVersionIdCounter = 0;
        private int titleCounter = 0;
        private int subTitleCounter = 0;
        private int summaryCounter = 0;
        private int keyWordsCounter = 0;
        private int statusSetDateCounter = 0;
        private int statusValueCounter = 0;
        private int organizationNameCounter = 0;
        private int organizationOfficialNameCounter = 0;
        private int digitalFileCounter = 0;
        private int previewFileCounter = 0;
        private int refersToCounter = 0;
        private int basedOnCounter = 0;
        private int translationOfCounter = 0;
    
        /**
        * Creates a builder instance for DocumentVersion.
        *
        * @param smBuilder the parent submodel element container builder
        */
        protected DocumentVersionBuilder(DelegatingSubmodelElementContainerBuilder smBuilder) {
            super(smBuilder.createSubmodelElementCollectionBuilder("DocumentVersion"), smBuilder);
            setSemanticId(irdi("0173-1#01-AHF582#001"));
        }
        
        /**
        * Creates a builder instance for DocumentVersion.
        *
        * @param smBuilder the parent submodel element container builder
        * @param nr the structure number
        */
        protected DocumentVersionBuilder(DelegatingSubmodelElementContainerBuilder smBuilder, int nr) {
            super(smBuilder.createSubmodelElementCollectionBuilder(getCountingIdShort("DocumentVersion", nr)), 
                smBuilder);
            setSemanticId(irdi("0173-1#01-AHF582#001"));
        }
        
        /**
        * Changes this property contains a list of languages used within the DocumentVersion. Each property codes one
        * language identification according to ISO 639-1 or ISO 639-2 used in the Document..
        *
        * @param language the value for Language
        * @return <b>this</b> (builder style)
        */
        public DocumentVersionBuilder setLanguage(String language) {
            // counting -> composition of idShort
            createPropertyBuilder(getCountingIdShort("Language", ++languageCounter))
                .setSemanticId(irdi("0173-1#02-AAN468#006"))
                .setValue(Type.STRING, language).build();
            return this;
        }
        
        /**
        * Changes unambiguous identification number of a DocumentVersion..
        *
        * @param documentVersionId the value for DocumentVersionId
        * @return <b>this</b> (builder style)
        */
        public DocumentVersionBuilder setDocumentVersionId(String documentVersionId) {
            documentVersionIdCounter++;
            createPropertyBuilder("DocumentVersionId")
                .setSemanticId(irdi("0173-1#02-AAO100#002"))
                .setValue(Type.STRING, documentVersionId).build();
            return this;
        }
        
        /**
        * Changes list of language-dependent titles of the Document. Constraint: for each language-dependent Title, a
        * Summary and at least one KeyWord shall exist for the given language..
        *
        * @param title the value for Title
        * @return <b>this</b> (builder style)
        */
        public DocumentVersionBuilder setTitle(LangString... title) {
            if (title.length > 0) {
                titleCounter++;
            }
            createMultiLanguageProperty(getDelegate(), createMultiLanguageProperties, "Title", 
                irdi("0173-1#02-AAO105#002"), title);
            return this;
        }
        
        /**
        * Changes list of language-dependent subtitles of the Document..
        *
        * @param subTitle the value for SubTitle
        * @return <b>this</b> (builder style)
        */
        public DocumentVersionBuilder setSubTitle(LangString... subTitle) {
            if (subTitle.length > 0) {
                subTitleCounter++;
            }
            createMultiLanguageProperty(getDelegate(), createMultiLanguageProperties, "SubTitle", 
                irdi("0173-1#02-ABH998#001"), subTitle);
            return this;
        }
        
        /**
        * Changes list of language-dependent summaries of the Document. Constraint: for each language-dependent
        * Summary, a Title and at least one KeyWord shall exist for the given language..
        *
        * @param summary the value for Summary
        * @return <b>this</b> (builder style)
        */
        public DocumentVersionBuilder setSummary(LangString... summary) {
            if (summary.length > 0) {
                summaryCounter++;
            }
            createMultiLanguageProperty(getDelegate(), createMultiLanguageProperties, "Summary", 
                irdi("0173-1#02-AAO106#002"), summary);
            return this;
        }
        
        /**
        * Changes list of language-dependent keywords of the Document..
        *
        * @param keyWords the value for KeyWords
        * @return <b>this</b> (builder style)
        */
        public DocumentVersionBuilder setKeyWords(LangString... keyWords) {
            if (keyWords.length > 0) {
                keyWordsCounter++;
            }
            createMultiLanguageProperty(getDelegate(), createMultiLanguageProperties, "KeyWords", 
                irdi("0173-1#02-ABH999#001"), keyWords);
            return this;
        }
        
        /**
        * Changes date when the document status was set. Format is YYYY-MM-dd..
        *
        * @param statusSetDate the value for StatusSetDate
        * @return <b>this</b> (builder style)
        */
        public DocumentVersionBuilder setStatusSetDate(java.util.Date statusSetDate) {
            statusSetDateCounter++;
            createPropertyBuilder("StatusSetDate")
                .setSemanticId(irdi("0173-1#02-ABI000#001"))
                .setValue(Type.DATE_TIME, statusSetDate).build();
            return this;
        }
        
        /**
        * Changes each document version represents a point in time in the document lifecycle. This status value refers
        * to the milestones in the document lifecycle. The following two values should be used for the application of 
        * this guideline: InReview (under review), Released (released)..
        *
        * @param statusValue the value for StatusValue
        * @return <b>this</b> (builder style)
        */
        public DocumentVersionBuilder setStatusValue(StatusValue statusValue) {
            statusValueCounter++;
            createPropertyBuilder("StatusValue")
                .setSemanticId(irdi("0173-1#02-ABI001#001"))
                .setValue(Type.STRING, statusValue.getValue()).build();
            return this;
        }
        
        /**
        * Changes organization short name of the author of the Document..
        *
        * @param organizationName the value for OrganizationName
        * @return <b>this</b> (builder style)
        */
        public DocumentVersionBuilder setOrganizationName(String organizationName) {
            organizationNameCounter++;
            createPropertyBuilder("OrganizationName")
                .setSemanticId(irdi("0173-1#02-ABI002#001"))
                .setValue(Type.STRING, organizationName).build();
            return this;
        }
        
        /**
        * Changes official name of the organization of author of the Document..
        *
        * @param organizationOfficialName the value for OrganizationOfficialName
        * @return <b>this</b> (builder style)
        */
        public DocumentVersionBuilder setOrganizationOfficialName(String organizationOfficialName) {
            organizationOfficialNameCounter++;
            createPropertyBuilder("OrganizationOfficialName")
                .setSemanticId(irdi("0173-1#02-ABI004#001"))
                .setValue(Type.STRING, organizationOfficialName).build();
            return this;
        }
        
        /**
        * Changes [IRDI for number of DigitalFiles (optional)]: 0173-1#02-ABI003#001 [IRDI] 0173-1#02-AAO214#002
        * (MIME-Type) [IRDI] 0173-1#02-ABI005#001 (Documentpath) MIME-Type, file name, and file contents given by 
        * the File SubmodelElement. Constraint: the MIME-Type needs to match the file type. Constraint: at least one 
        * PDF/A file type shall be provided..
        *
        * @param file the relative or absolute file name with extension
        * @param mimeType the mime type of the file
        * @param semanticId the actual semantic ID
        * @return <b>this</b> (builder style)
        */
        public DocumentVersionBuilder setDigitalFile(String file, String mimeType, String semanticId) {
            // counting -> composition of idShort
            createFileDataElementBuilder(getCountingIdShort("DigitalFile", ++digitalFileCounter), file, mimeType)
                .setSemanticId(semanticId != null && semanticId.length() > 0 
                    ? semanticId : irdi("0173-1#01-AHF583#001"))
                .build();
            return this;
        }
        
        /**
        * Changes [IRDI] 0173-1#02-AAO214#002 (MIME-Type) [IRDI] 0173-1#02-ABI005#001 (Documentpath) Provides a
        * preview image of the DocumentVersion, e.g. first page, in a commonly used image format and in low resolution..
        *
        * @param file the relative or absolute file name with extension
        * @param mimeType the mime type of the file
        * @param semanticId the actual semantic ID
        * @return <b>this</b> (builder style)
        */
        public DocumentVersionBuilder setPreviewFile(String file, String mimeType, String semanticId) {
            // counting -> composition of idShort
            createFileDataElementBuilder(getCountingIdShort("PreviewFile", ++previewFileCounter), file, mimeType)
                .setSemanticId(semanticId != null && semanticId.length() > 0 ? semanticId : irdi("0173-1#01-AHF584#001"
                    ))
                .build();
            return this;
        }
        
        /**
        * Changes forms a generic RefersTo relationship to another Document or DocumentVersion. They have a loose
        * relationship. Constraint: reference targets a SMC "Document" or a "DocumentVersion"..
        *
        * @param reference the target reference
        * @return <b>this</b> (builder style)
        */
        public DocumentVersionBuilder setRefersTo(Reference reference) {
            // counting -> composition of idShort
            createReferenceElementBuilder(getCountingIdShort("RefersTo", ++refersToCounter))
                .setSemanticId(irdi("0173-1#02-ABI006#001"))
                .setValue(reference).build();
            return this;
        }
        
        /**
        * Changes forms a BasedOn relationship to another Document or DocumentVersion. Typically states that the
        * content of the document is based on another document (e.g. specification requirements). Both have a strong
        * relationship. Constraint: reference targets a SMC "Document" or a "DocumentVersion"..
        *
        * @param reference the target reference
        * @return <b>this</b> (builder style)
        */
        public DocumentVersionBuilder setBasedOn(Reference reference) {
            // counting -> composition of idShort
            createReferenceElementBuilder(getCountingIdShort("BasedOn", ++basedOnCounter))
                .setSemanticId(irdi("0173-1#02-ABI007#001"))
                .setValue(reference).build();
            return this;
        }
        
        /**
        * Changes forms a TranslationOf relationship to another Document or DocumentVersion. Both have a strong
        * relationship. Constraint: the (language-independent)  content must be identical in both Documents or 
        * DocumentVersions.
        * Constraint: reference targets a SMC "Document" or a "DocumentVersion"..
        *
        * @param reference the target reference
        * @return <b>this</b> (builder style)
        */
        public DocumentVersionBuilder setTranslationOf(Reference reference) {
            // counting -> composition of idShort
            createReferenceElementBuilder(getCountingIdShort("TranslationOf", ++translationOfCounter))
                .setSemanticId(irdi("0173-1#02-ABI008#001"))
                .setValue(reference).build();
            return this;
        }
        
        @Override
        public SubmodelElementCollection build() {
            assertThat(1 <= languageCounter, "Cardinality {} of Language must be greater or equal 1.",
                languageCounter);
            assertThat(documentVersionIdCounter == 1, "Cardinality {} of DocumentVersionId must be equal 1.",
                documentVersionIdCounter);
            assertThat(titleCounter == 1, "Cardinality {} of Title must be equal 1.", titleCounter);
            assertThat(0 <= subTitleCounter && subTitleCounter <= 1, "Cardinality {} of SubTitle must be greater or equ"
                + "al 0 and less or equal 1.", subTitleCounter);
            assertThat(summaryCounter == 1, "Cardinality {} of Summary must be equal 1.", summaryCounter);
            assertThat(keyWordsCounter == 1, "Cardinality {} of KeyWords must be equal 1.", keyWordsCounter);
            assertThat(statusSetDateCounter == 1, "Cardinality {} of StatusSetDate must be equal 1.",
                statusSetDateCounter);
            assertThat(statusValueCounter == 1, "Cardinality {} of StatusValue must be equal 1.", statusValueCounter);
            assertThat(organizationNameCounter == 1, "Cardinality {} of OrganizationName must be equal 1.",
                organizationNameCounter);
            assertThat(organizationOfficialNameCounter == 1, "Cardinality {} of OrganizationOfficialName must be equal "
                + "1.", organizationOfficialNameCounter);
            assertThat(1 <= digitalFileCounter, "Cardinality {} of DigitalFile must be greater or equal 1.",
                digitalFileCounter);
            assertThat(0 <= previewFileCounter && previewFileCounter <= 1, "Cardinality {} of PreviewFile must be great"
                + "er or equal 0 and less or equal 1.", previewFileCounter);
            assertThat(0 <= refersToCounter, "Cardinality {} of RefersTo must be greater or equal 0.",
                refersToCounter);
            assertThat(0 <= basedOnCounter, "Cardinality {} of BasedOn must be greater or equal 0.", basedOnCounter);
            assertThat(0 <= translationOfCounter, "Cardinality {} of TranslationOf must be greater or equal 0.",
                translationOfCounter);
            
            return super.build();
        }
    }
    
}
