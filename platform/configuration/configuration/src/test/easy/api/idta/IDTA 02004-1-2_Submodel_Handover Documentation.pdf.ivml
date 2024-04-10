project IDTA_02004_HandoverDocumentation {

  version v1.2;

  import AASDataTypes;

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasEnumType StatusValue = {
    name = "StatusValue",
    description = "Each document version represents a point in time in the document lifecycle. This status value refers to the milestones in the document lifecycle. The following two values should be used for the application of this guideline: InReview (under review), Released (released).",
    versionIdentifier = "IDTA 02004-1-2",
    literals = {
      AasEnumLiteral {
        name = "under review",
        semanticId = "0173-1#07-ABZ640#001"
      },
      AasEnumLiteral {
        name = "released",
        semanticId = "0173-1#07-ABZ641#001"
      }
    }
  };

  AasSubmodelType HandoverDocumentation = {
    name = "HandoverDocumentation",
    semanticId = "irdi:0173-1#01-AHF578#001",
    description = "The Submodel defines a set meta data for the handover of documentation from the manufacturer to the operator for industrial equipment.",
    versionIdentifier = "IDTA 02004-1-2",
    fields = {
      AasField {
        name = "Document",
        semanticId = "irdi:0173-1#01-AHF579#001",
        counting = true,
        type = refBy(Document),
        minimumInstances = 0,
        description = "[IRDI for number of Documents (optional)]: 0173-1#02-ABH990#001 Each SMC describes a Document (see IEC 82045-1 and IEC 8245-2), which is associated with the particular Asset Administration Shell."
      },
      AasField {
        name = "Entity",
        semanticId = "iri:https://admin-shell.io/vdi/2770/1/0/EntityForDocumentation",
        counting = true,
        type = refBy(AasGenericEntityType),
        minimumInstances = 0,
        examples = {"Entity for an important sealing or bearing of the equipment."},
        description = "States that the described Entity is an important entity for documentation of the superordinate Asset of the Asset Administration Shell."
      }
    }
  };

  AasSubmodelElementCollectionType Document = {
    name = "Document",
    semanticId = "irdi:0173-1#01-AHF579#001",
    description = "This SubmodelElementCollection holds the information for a VDI 2770 Document entity.",
    versionIdentifier = "IDTA 02004-1-2",
    fields = {
      AasField {
        name = "DocumentId",
        semanticId = "irdi:0173-1#01-AHF580#001",
        counting = true,
        type = refBy(DocumentId),
        minimumInstances = 1,
        description = "[IRDI for number of DocumentIds (optional)]: 0173-1#02-ABH991#001 Set of document identifiers for the Document. One ID in this collection should be used as a preferred ID (see isPrimary below)."
      },
      AasField {
        name = "DocumentClassification",
        semanticId = "irdi:0173-1#01-AHF581#001",
        counting = true,
        type = refBy(DocumentClassification),
        minimumInstances = 1,
        description = "[IRDI for number of DocumentClassifications (optional)]: 0173-1#02-ABH992#001 Set of information for describing the classification of the Document according to ClassificationSystems. Constraint: at least one classification according to VDI 2770 shall be provided."
      },
      AasField {
        name = "DocumentVersion",
        counting = true,
        type = refBy(DocumentVersion),
        minimumInstances = 0,
        description = "[IRDI for number of DocumentVersions (optional)]: 0173-1#02-ABH993#001 Information elements of individual VDI 2770 DocumentVersion entities."
      },
      AasField {
        name = "DocumentedEntity",
        multiSemanticIds = true,
        counting = true,
        type = refBy(AasReferenceType),
        minimumInstances = 0,
        description = "[IRDI PATH] 0173-1#02-ABI501#001/0173-1#01- AHF580#001*00 For DocumentClassification{00}: [IRDI PATH] 0173-1#02-ABI502#001/0173-1#01- AHF581#001*00 For DocumentVersion{00}: [IRDI PATH] 0173-1#02-ABI503#001/0173-1#01- AHF582#001*00 Identifies entities, which are subject to the Document."
      }
    }
  };

  AasSubmodelElementCollectionType DocumentId = {
    name = "DocumentId",
    semanticId = "irdi:0173-1#01-AHF580#001",
    description = "This SubmodelElementCollection holds the information for a VDI 2770 DocumentIdDomain entity and the DocumentId property.",
    versionIdentifier = "IDTA 02004-1-2",
    fields = {
      AasField {
        name = "DocumentDomainId",
        semanticId = "irdi:0173-1#02-ABH994#001",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"1213455566"},
        description = "Identification of the domain in which the given DocumentId is unique. The domain ID can be e.g. the name or acronym of the providing organization."
      },
      AasField {
        name = "ValueId",
        semanticId = "irdi:0173-1#02-AAO099#002",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"1213455566","XF90-884"},
        description = "Identification number of the Document within a given domain, e.g. the providing organization."
      },
      AasField {
        name = "IsPrimary",
        semanticId = "irdi:0173-1#02-ABH995#001",
        type = refBy(BooleanType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"true"},
        description = "Flag indicating that a DocumentId within a collection of at least two DocumentIds is the ‘primary’ identifier for the document. This is the preferred ID of the document (commonly from the point of view of the owner of the asset)."
      }
    }
  };

  AasSubmodelElementCollectionType DocumentClassification = {
    name = "DocumentClassification",
    semanticId = "irdi:0173-1#01-AHF581#001",
    description = "This SubmodelElementCollection holds the information for a VDI 2770 DocumentClassification entity.",
    versionIdentifier = "IDTA 02004-1-2",
    fields = {
      AasField {
        name = "ClassId",
        semanticId = "irdi:0173-1#02-ABH996#001",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"03-02","BB"},
        description = "Unique ID of the document class within a ClassificationSystem. Constraint: if ClassificationSystem is set to “VDI2770 Blatt 1:2020”, the given IDs of VDI2770 Blatt 1:2020 shall be used (see Table 1)."
      },
      AasField {
        name = "ClassName",
        semanticId = "irdi:0173-1#02-AAO102#003",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Operation@en","Bedienung@de","Berichte@de","Reports@de"},
        description = "List of language-dependent names of the selected ClassID. Constraint: if ClassificationSystem is set to “VDI2770 Blatt 1:2020”, the given names of VDI2770:2020 need be used (see Table 1). Constraint: languages shall match at least the language specifications of the included DocumentVersions (below)."
      },
      AasField {
        name = "ClassificationSystem",
        semanticId = "irdi:0173-1#02-ABH997#001",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"VDI2770 Blatt 1:2020","[string]","IEC61355-1:2008"},
        description = "Identification of the classification system. For classifications according to VDI 2270 Blatt 1, always set to “VDI2770 Blatt 1:2020”. Further classification systems are commonly used, such as “IEC61355-1:2008”."
      }
    }
  };

  AasSubmodelElementCollectionType DocumentVersion = {
    name = "DocumentVersion",
    semanticId = "irdi:0173-1#01-AHF582#001",
    description = "This SubmodelElementCollection holds the information for a VDI2770 DocumentVersion entity.",
    versionIdentifier = "IDTA 02004-1-2",
    fields = {
      AasField {
        name = "Language",
        semanticId = "irdi:0173-1#02-AAN468#006",
        counting = true,
        type = refBy(StringListType),
        minimumInstances = 1,
        examples = {"en"},
        description = "This property contains a list of languages used within the DocumentVersion. Each property codes one language identification according to ISO 639-1 or ISO 639-2 used in the Document."
      },
      AasField {
        name = "DocumentVersionId",
        semanticId = "irdi:0173-1#02-AAO100#002",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"V1.2"},
        description = "Unambiguous identification number of a DocumentVersion."
      },
      AasField {
        name = "Title",
        semanticId = "irdi:0173-1#02-AAO105#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Exemplary title@en","Deutscher Titel@de"},
        description = "List of language-dependent titles of the Document. Constraint: for each language-dependent Title, a Summary and at least one KeyWord shall exist for the given language."
      },
      AasField {
        name = "SubTitle",
        semanticId = "irdi:0173-1#02-ABH998#001",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Exemplary subtitle@en","Deutscher Untertitel@de"},
        description = "List of language-dependent subtitles of the Document."
      },
      AasField {
        name = "Summary",
        semanticId = "irdi:0173-1#02-AAO106#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Abstract@en","Deutsche Zusammenfassung@de"},
        description = "List of language-dependent summaries of the Document. Constraint: for each language-dependent Summary, a Title and at least one KeyWord shall exist for the given language."
      },
      AasField {
        name = "KeyWords",
        semanticId = "irdi:0173-1#02-ABH999#001",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Exemplary keywords@en","Deutsche Stichwörter@de"},
        description = "List of language-dependent keywords of the Document."
      },
      AasField {
        name = "StatusSetDate",
        semanticId = "irdi:0173-1#02-ABI000#001",
        type = refBy(DateTimeType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"2020-02-06"},
        description = "Date when the document status was set. Format is YYYY-MM-dd."
      },
      AasField {
        name = "StatusValue",
        semanticId = "irdi:0173-1#02-ABI001#001",
        type = refBy(StatusValue),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Released"},
        description = "Each document version represents a point in time in the document lifecycle. This status value refers to the milestones in the document lifecycle. The following two values should be used for the application of this guideline: InReview (under review), Released (released)."
      },
      AasField {
        name = "OrganizationName",
        semanticId = "irdi:0173-1#02-ABI002#001",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Example company"},
        description = "Organization short name of the author of the Document."
      },
      AasField {
        name = "OrganizationOfficialName",
        semanticId = "irdi:0173-1#02-ABI004#001",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Example company Ltd."},
        description = "Official name of the organization of author of the Document."
      },
      AasField {
        name = "DigitalFile",
        semanticId = "irdi:0173-1#01-AHF583#001",
        multiSemanticIds = true,
        counting = true,
        type = refBy(AasFileResourceType),
        minimumInstances = 1,
        examples = {"MIME-Type = application/pdf value = /aasx/documentation/ docu_cecc_fullmanual_DE.PDF"},
        description = "[IRDI for number of DigitalFiles (optional)]: 0173-1#02-ABI003#001 [IRDI] 0173-1#02-AAO214#002 (MIME-Type) [IRDI] 0173-1#02-ABI005#001 (Documentpath) MIME-Type, file name, and file contents given by the File SubmodelElement. Constraint: the MIME-Type needs to match the file type. Constraint: at least one PDF/A file type shall be provided."
      },
      AasField {
        name = "PreviewFile",
        semanticId = "irdi:0173-1#01-AHF584#001",
        multiSemanticIds = true,
        counting = true,
        type = refBy(AasFileResourceType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"MIME-Type = image/jpg value = /aasx/documentation/ preview/docu_cecc_fullmanual_DE.jpg"},
        description = "[IRDI] 0173-1#02-AAO214#002 (MIME-Type) [IRDI] 0173-1#02-ABI005#001 (Documentpath) Provides a preview image of the DocumentVersion, e.g. first page, in a commonly used image format and in low resolution."
      },
      AasField {
        name = "RefersTo",
        semanticId = "irdi:0173-1#02-ABI006#001",
        counting = true,
        type = refBy(AasReferenceType),
        minimumInstances = 0,
        description = "Forms a generic RefersTo relationship to another Document or DocumentVersion. They have a loose relationship. Constraint: reference targets a SMC “Document” or a “DocumentVersion”."
      },
      AasField {
        name = "BasedOn",
        semanticId = "irdi:0173-1#02-ABI007#001",
        counting = true,
        type = refBy(AasReferenceType),
        minimumInstances = 0,
        description = "Forms a BasedOn relationship to another Document or DocumentVersion. Typically states that the content of the document is based on another document (e.g. specification requirements). Both have a strong relationship. Constraint: reference targets a SMC “Document” or a “DocumentVersion”."
      },
      AasField {
        name = "TranslationOf",
        semanticId = "irdi:0173-1#02-ABI008#001",
        counting = true,
        type = refBy(AasReferenceType),
        minimumInstances = 0,
        description = "Forms a TranslationOf relationship to another Document or DocumentVersion. Both have a strong relationship. Constraint: the (language-independent)  content must be identical in both Documents or DocumentVersions. Constraint: reference targets a SMC “Document” or a “DocumentVersion”."
      }
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
