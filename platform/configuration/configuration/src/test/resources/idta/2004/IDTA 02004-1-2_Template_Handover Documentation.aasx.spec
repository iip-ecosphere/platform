project IDTA_02004_HandoverDocumentation {

  version v1.2;

  import AASDataTypes;

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasSubmodelType HandoverDocumentation = {
    name = "HandoverDocumentation",
    semanticId = "irdi:0173-1#01-AHF578#001",
    description = "The Submodel defines a set meta data for the handover of documentation from the manufacturer to the operator for industrial equipment.",
    fields = {
      AasField {
        name = "numberOfDocuments",
        semanticId = "irdi:0173-1#02-ABH990#001",
        type = refBy(IntegerType),
        description = "Number of documents (handover documentation)."
      },
      AasField {
        name = "Document",
        semanticId = "irdi:0173-1#02-ABI500#001/0173-1#01-AHF579#001*01",
        type = refBy(Document),
        minimumInstances = 0,
        description = "Each SMC describes a Document (see IEC 82045-1 and IEC 8245-2), which is associated to the particular Asset Administration Shell."
      },
      AasField {
        name = "Entity",
        semanticId = "iri:https://admin-shell.io/vdi/2770/1/0/EntityForDocumentation",
        type = refBy(Entity),
        minimumInstances = 0,
        description = "States, that the described Entity is an important entity for documentation of the superordinate Asset of the Asset Administration Shell. Note: typically, such Entities are well-identified sub-parts of the Asset, such as supplier parts delivered to the manufacturer of the Asset."
      }
    }
  };

  AasSubmodelElementCollectionType Document = {
    name = "Document",
    semanticId = "irdi:0173-1#02-ABI500#001/0173-1#01-AHF579#001*01",
    description = "Each SMC describes a Document (see IEC 82045-1 and IEC 8245-2), which is associated to the particular Asset Administration Shell.",
    fields = {
      AasField {
        name = "DocumentId",
        semanticId = "irdi:0173-1#02-ABI501#001/0173-1#01-AHF580#001*01",
        type = refBy(DocumentId),
        minimumInstances = 1,
        description = "Set of document identifiers for the Document. One ID in this collection should be used as a preferred ID."
      },
      AasField {
        name = "DocumentClassification",
        semanticId = "irdi:0173-1#02-ABI502#001/0173-1#01-AHF581#001*01",
        type = refBy(DocumentClassification),
        minimumInstances = 1,
        description = "Set of information for describing the classification of the Document according to ClassificationSystems. Constraint: at least one classification according to VDI 2770 shall be provided."
      },
      AasField {
        name = "DocumentVersion",
        semanticId = "irdi:0173-1#02-ABI503#001/0173-1#01-AHF582#001*01",
        type = refBy(DocumentVersion),
        minimumInstances = 0,
        description = "Information elements of individual VDI 2770 DocumentVersion entities. Note: at the time of handover, this collection shall include at least one DocumentVersion."
      },
      AasField {
        name = "DocumentedEntity",
        semanticId = "iri:https://admin-shell.io/vdi/2770/1/0/Document/DocumentedEntity",
        type = refBy(AasReferenceType),
        minimumInstances = 0,
        description = "Identifies entities, which are subject to the Document. Note: can be omitted, if the subject of the Document is the overall Asset of the Asset Administration Shell. Note: if no Entity according clause 2.2 is referenced, this ReferenceElement is not required at all. Note: This mechanism substitutes the ObjectId-provision of the VDI2770 (see section 2.2 and appendix B)."
      }
    }
  };

  AasSubmodelElementCollectionType DocumentId = {
    name = "DocumentId",
    semanticId = "irdi:0173-1#02-ABI501#001/0173-1#01-AHF580#001*01",
    description = "Set of document identifiers for the Document. One ID in this collection should be used as a preferred ID.",
    fields = {
      AasField {
        name = "DocumentDomainId",
        semanticId = "irdi:0173-1#02-ABH994#001",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"1213455566"},
        description = "Identification of the domain in which the given DocumentId is unique. The domain ID can e.g., be the name or acronym of the providing organisation."
      },
      AasField {
        name = "ValueId",
        semanticId = "irdi:0173-1#02-AAO099#002",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"XF90-884"},
        description = "Identification number of the Document within a given domain, e.g. the providing organisation."
      },
      AasField {
        name = "IsPrimary",
        semanticId = "irdi:0173-1#02-ABH995#001",
        type = refBy(BooleanType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"true"},
        description = "Flag indicating that a DocumentId within a collection of at least two DocumentId`s is the ‘primary’ identifier for the document. This is the preferred ID of the document (commonly from the point of view of the owner of the asset). Note: can be omitted, if the ID is not primary. Note: can be omitted, if only one ID is for a Document. Contraint: only one DocumentId in a collection may be marked as primary."
      }
    }
  };

  AasSubmodelElementCollectionType DocumentClassification = {
    name = "DocumentClassification",
    semanticId = "irdi:0173-1#02-ABI502#001/0173-1#01-AHF581#001*01",
    description = "Set of information for describing the classification of the Document according to ClassificationSystems. Constraint: at least one classification according to VDI 2770 shall be provided.",
    fields = {
      AasField {
        name = "ClassId",
        semanticId = "irdi:0173-1#02-ABH996#001",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"03-02"},
        description = "Unique ID of the document class within a ClassficationSystem. Constraint: If ClassificationSystem is set to “VDI2770:2018”, the given IDs of VDI2770:2018 shall be used."
      },
      AasField {
        name = "ClassName",
        semanticId = "irdi:0173-1#02-AAO102#003",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Operation@en"},
        description = "List of language-dependent names of the selected ClassID. Constraint: If ClassificationSystem is set to “VDI2770:2018” then the given names of VDI2770:2018 need be used. Constraint: languages shall match at least the language specifications of the included DocumentVersions (below)."
      },
      AasField {
        name = "ClassificationSystem",
        semanticId = "irdi:0173-1#02-ABH997#001",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"VDI2770:2018"},
        description = "Identification of the classification system. For classifications according VDI 2270 always set to 'VDI2770:2018'. Further classification systems are commonly used, such as 'IEC61355-1:2008'."
      }
    }
  };

  AasSubmodelElementCollectionType DocumentVersion = {
    name = "DocumentVersion",
    semanticId = "irdi:0173-1#02-ABI503#001/0173-1#01-AHF582#001*01",
    description = "Information elements of individual VDI 2770 DocumentVersion entities. Note: at the time of handover, this collection shall include at least one DocumentVersion.",
    fields = {
      AasField {
        name = "Language",
        semanticId = "irdi:0173-1#02-AAN468#006",
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
        description = "Unambigous identification number of a DocumentVersion."
      },
      AasField {
        name = "Title",
        semanticId = "irdi:0173-1#02-AAO105#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Examplary title@en"},
        description = "List of language-dependent titles of the Document. Constraint: For each language-depended Title a Summary and at least one KeyWord shall exist for the given language."
      },
      AasField {
        name = "SubTitle",
        semanticId = "irdi:0173-1#02-ABH998#001",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Examplary subtitle@en"},
        description = "List of language-dependent subtitles of the Document."
      },
      AasField {
        name = "Summary",
        semanticId = "irdi:0173-1#02-AAO106#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Abstract@en"},
        description = "List of language-dependent summaries of the Document. Constraint: For each language-depended Summary a Title and at least one KeyWord shall exist for the given language."
      },
      AasField {
        name = "KeyWords",
        semanticId = "irdi:0173-1#02-ABH999#001",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Examplary keywords@en"},
        description = "List of language-dependent keywords of the Document. Note: Mutiple keywords are given as comma separated list for each language. Constraint: For each language-depended KeyWord a Title and Summary shall exist for the given language. Note: This can be intentionally a blank."
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
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Released"},
        description = "Each document version represents a point in time in the document life cycle. This status value refers to the milestones in the document life cycle. The following two values should be used for the application of this guideline: InReview (under review), Released (released)."
      },
      AasField {
        name = "OrganizationName",
        semanticId = "irdi:0173-1#02-ABI002#001",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Example company"},
        description = "Organiziation short name of the author of the Document."
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
        name = "RefersTo",
        semanticId = "irdi:0173-1#02-ABI006#001",
        type = refBy(AasReferenceType),
        minimumInstances = 0,
        description = "Forms a generic RefersTo-relationship to another Document or DocumentVersion. They have a loose relationship. Constraint: reference targets a SMC 'Document' or a “DocumentVersion”."
      },
      AasField {
        name = "BasedOn",
        semanticId = "irdi:0173-1#02-ABI007#001",
        type = refBy(AasReferenceType),
        minimumInstances = 0,
        description = "Forms a BasedOn-relationship to another Document or DocumentVersion. Typically states, that the content of the document bases on another document (e.g. specification requirements). Both have a strong relationship. Constraint: reference targets a SMC 'Document' or a “DocumentVersion”."
      },
      AasField {
        name = "TranslationOf",
        semanticId = "irdi:0173-1#02-ABI008#001",
        type = refBy(AasReferenceType),
        minimumInstances = 0,
        description = "Forms a TranslationOf-relationship to another Document or DocumentVersion. Both have a strong relationship. Constaint: The (language independent) content must be identical in both documents or document versions. Constraint: reference targets a SMC 'Document' or a “DocumentVersion”."
      },
      AasField {
        name = "DigitalFile",
        semanticId = "irdi:0173-1#02-ABI504#001/0173-1#01-AHF583#001",
        type = refBy(DigitalFile),
        minimumInstances = 1,
        examples = {"docu_cecc_fullmanual_DE.PDF"},
        description = "MIME-Type, file name and file contents given by the file SubmodelElement Note: each DigitalFile represents the same content or Document version, but can be provided in different technical formats (PDF, PDFA, html..) or by a link."
      },
      AasField {
        name = "PreviewFile",
        semanticId = "irdi:0173-1#02-ABI505#001 /0173-1#01-AHF584#001",
        type = refBy(PreviewFile),
        minimumInstances = 0,
        examples = {"docu_cecc_fullmanual_DE.jpg"},
        description = "Provides a preview image of the DocumentVersion, e.g. first page, in a commonly used image format and low resolution. Note: low resolution e.g. < 512 x 512 pixels. Constraint: the MIME type needs to match the file type. Allowed file types are JPG, PNG, BMP."
      }
    }
  };

  AasSubmodelElementCollectionType DigitalFile = {
    name = "DigitalFile",
    semanticId = "irdi:0173-1#02-ABI504#001/0173-1#01-AHF583#001",
    description = "MIME-Type, file name and file contents given by the file SubmodelElement Note: each DigitalFile represents the same content or Document version, but can be provided in different technical formats (PDF, PDFA, html..) or by a link.",
    fields = {
      AasField {
        name = "MimeType",
        semanticId = "irdi:0173-1#02-AAO214#002",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "The MIME-Type classifies the data of massage."
      },
      AasField {
        name = "DocumentPath",
        semanticId = "irdi:0173-1#02-ABI005#001",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "The document path leads to the document."
      }
    }
  };

  AasSubmodelElementCollectionType PreviewFile = {
    name = "PreviewFile",
    semanticId = "irdi:0173-1#02-ABI505#001 /0173-1#01-AHF584#001",
    description = "Provides a preview image of the DocumentVersion, e.g. first page, in a commonly used image format and low resolution. Note: low resolution e.g. < 512 x 512 pixels. Constraint: the MIME type needs to match the file type. Allowed file types are JPG, PNG, BMP.",
    fields = {
      AasField {
        name = "MimeType",
        semanticId = "irdi:0173-1#02-AAO214#002",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "The MIME-Type classifies the data of massage."
      },
      AasField {
        name = "DocumentPath",
        semanticId = "irdi:0173-1#02-ABI005#001",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "The document path leads to the document."
      }
    }
  };

  AasEntityType Entity = {
    name = "Entity",
    semanticId = "iri:https://admin-shell.io/vdi/2770/1/0/EntityForDocumentation",
    description = "States, that the described Entity is an important entity for documentation of the superordinate Asset of the Asset Administration Shell. Note: typically, such Entities are well-identified sub-parts of the Asset, such as supplier parts delivered to the manufacturer of the Asset."
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
