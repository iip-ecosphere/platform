project IDTA_02012_DEXPI {

  version v1.0;

  import AASDataTypes;

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasSubmodelType DEXPI = {
    name = "DEXPI",
    semanticId = "iri:https://admin-shell.io/idta/DEXPI/1/0/Submodel",
    description = "Submodel containing one or multiple DEXPI models for the asset.",
    versionIdentifier = "IDTA 02012-1-0",
    fields = {
      AasField {
        name = "PlantMetadata",
        semanticId = "iri:https://admin-shell.io/idta/DEXPI/1/0/PlantMetadata",
        type = refBy(PlantMetadata),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Container for the metadata of the plant segment which is described by the supplied DEXPI file."
      },
      AasField {
        name = "Model",
        semanticId = "iri:https://admin-shell.io/idta/DEXPI/1/0/Model",
        counting = true,
        type = refBy(Model),
        minimumInstances = 1,
        description = "Container for the actual DEXPI file, its metadata and its mapping directory. Note that {00} a running counter suffix, e.g., “Model01” for the first element i.e. first DEXPI model and so on (“Model01”, “Model02”, “Model03”, …) in the case of multiple models included in the Submodel."
      }
    }
  };

  AasSubmodelElementCollectionType PlantMetadata = {
    name = "PlantMetadata",
    semanticId = "iri:https://admin-shell.io/idta/DEXPI/1/0/PlantMetadata",
    description = "Metadata attributes of the plant or plant segment. It includes a subset of generic DEXPI Package Metadata (section 5 of the DEXPI specification) plus some additional optional elements. Note: we keep all attributes optional due they optional definition in the DEXPI specification.",
    versionIdentifier = "IDTA 02012-1-0",
    fixedName = true,
    fields = {
      AasField {
        name = "EnterpriseIdentificationCode",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/EnterpriseIdentificationCodeAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"oil-gas-inc"},
        description = "Enterprise Identification Code."
      },
      AasField {
        name = "EnterpriseName",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/EnterpriseNameAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Oil & Gas, Inc."},
        description = "Enterprise Name."
      },
      AasField {
        name = "EnterpriseReference",
        semanticId = "iri:https://admin-shell.io/idta/DEXPI/1/0/EnterpriseReference",
        type = refBy(AasReferenceType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"http://example.com/id/9992 020020616052921"},
        description = "Optional reference to an Entity element representing the enterprise in another Submodel, e.g., BOM."
      },
      AasField {
        name = "SiteIdentificationCode",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/SiteIdentificationCodeAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"DC"},
        description = "Site Identification Code."
      },
      AasField {
        name = "SiteName",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/SiteNameAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Dexpi City"},
        description = "Site Name."
      },
      AasField {
        name = "SiteReference",
        semanticId = "iri:https://admin-shell.io/idta/DEXPI/1/0/SiteReference",
        type = refBy(AasReferenceType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"http://example.com/id/9992 020020616052922"},
        description = "Optional reference to an Entity element representing the site in another Submodel, e.g., BOM."
      },
      AasField {
        name = "IndustrialComplexIdentificationCode",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/IndustrialComplexIdentificationCodeAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"I-Chain"},
        description = "Industrial Complex Identification Code."
      },
      AasField {
        name = "IndustrialComplexName",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/IndustrialComplexNameAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Isophorone Chain"},
        description = "Industrial Complex Name."
      },
      AasField {
        name = "IndustrialComplexReference",
        semanticId = "iri:https://admin-shell.io/idta/DEXPI/1/0/IndustrialComplexReference",
        type = refBy(AasReferenceType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"http://example.com/id/9992 020020616052923"},
        description = "Optional reference to an Entity element representing the industrial complex in another Submodel, e.g., BOM."
      },
      AasField {
        name = "ProcessPlantIdentificationCode",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/ProcessPlantIdentificationCodeAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"ABC"},
        description = "Process Plant Identification Code."
      },
      AasField {
        name = "ProcessPlantName",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/ProcessPlantNameAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"ABC Plant"},
        description = "Process Plant Name."
      },
      AasField {
        name = "ProcessPlantReference",
        semanticId = "iri:https://admin-shell.io/idta/DEXPI/1/0/ProcessPlantReference",
        type = refBy(AasReferenceType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"http://example.com/id/9992 020020616052924"},
        description = "Optional reference to an Entity element representing the process plant in another Submodel, e.g., BOM."
      },
      AasField {
        name = "PlantSectionIdentificationCode",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/PlantSectionIdentificationCodeAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"10"},
        description = "Plant Section Identification Code."
      },
      AasField {
        name = "PlantSectionName",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/PlantSectionNameAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"PlantSectionName"},
        description = "Plant Section Name."
      },
      AasField {
        name = "PlantSectionReference",
        semanticId = "iri:https://admin-shell.io/idta/DEXPI/1/0/PlantSectionReference",
        type = refBy(AasReferenceType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"http://example.com/id/9992 020020616052925"},
        description = "Optional reference to an Entity element representing the plant in section another Submodel, e.g., BOM."
      },
      AasField {
        name = "ProjectNumber",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/ProjectNumberAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"P3.1415"},
        description = "Project Number."
      },
      AasField {
        name = "ProjectName",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/ProjectNameAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"a project"},
        description = "Project Name."
      },
      AasField {
        name = "SubProjectNumber",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/SubProjectNumberAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"P3.1415-SP2"},
        description = "Sub Project Number."
      },
      AasField {
        name = "SubProjectName",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/SubProjectNameAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"a sub-project"},
        description = "Sub Project Name."
      },
      AasField {
        name = "ManufacturerName",
        semanticId = "irdi:0173-1#02-AAO677#002",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Plant Segment Vendor or EPC company name"},
        description = "Legal designation of the natural or judicial body which is directly responsible for the design, production, packaging and labeling of a product in respect to its being brought into the market. We assume that this plant segment vendor is producing or, at least, modifying the P&ID (e.g., as-built documentation)."
      },
      AasField {
        name = "DateOfManufacture",
        semanticId = "irdi:0173-1#02-AAR972#002",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2021-01-01"},
        description = "Date from which the production and / or development process is completed or from which a service is provided completely."
      },
      AasField {
        name = "EndProductName",
        semanticId = "iri:https://admin-shell.io/idta/DEXPI/1/0/EndProductName",
        type = refBy(StringListType),
        minimumInstances = 0,
        examples = {"water"},
        description = "End Product Name of the main product the plant segment is producing."
      },
      AasField {
        name = "EndProductCASName",
        semanticId = "iri:https://admin-shell.io/idta/DEXPI/1/0/EndProductCASName",
        type = refBy(StringListType),
        minimumInstances = 0,
        examples = {"7732-18-5"},
        description = "End Product CAS Name of the main product."
      }
    }
  };

  AasSubmodelElementCollectionType Model = {
    name = "Model",
    semanticId = "iri:https://admin-shell.io/idta/DEXPI/1/0/Model",
    description = "Container for a single DEXPI model.",
    versionIdentifier = "IDTA 02012-1-0",
    fields = {
      AasField {
        name = "ModelMetadata",
        semanticId = "iri:https://admin-shell.io/idta/DEXPI/1/0/ModelMetadata",
        type = refBy(ModelMetadata),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Metadata of the model."
      },
      AasField {
        name = "ModelFile",
        semanticId = "iri:https://admin-shell.io/idta/DEXPI/1/0/ModelFile",
        type = refBy(AasFileResourceType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"mimeType=application/xml C01V04-VER.EX01.xml"},
        description = "Actual DEXPI model, e.g., in ProteusXML serialization."
      },
      AasField {
        name = "ModelRepresentation",
        semanticId = "iri:https://admin-shell.io/idta/DEXPI/1/0/ModelRepresentation",
        type = refBy(AasFileResourceType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"mimeType=application/svg C01V04-VER.EX01.svg"},
        description = "Rendered DEXPI model, e.g., as an SVG file."
      },
      AasField {
        name = "MappingDirectory",
        semanticId = "iri:https://admin-shell.io/idta/DEXPI/1/0/MappingDirectory",
        type = refBy(MappingDirectory),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Directory with model-specific mappings."
      }
    }
  };

  AasSubmodelElementCollectionType ModelMetadata = {
    name = "ModelMetadata",
    semanticId = "iri:https://admin-shell.io/idta/DEXPI/1/0/Model",
    description = "Metadata container for a single DEXPI model. This is a subset of generic DEXPI Package Metadata (section 5 of the specification).",
    versionIdentifier = "IDTA 02012-1-0",
    fixedName = true,
    fields = {
      AasField {
        name = "ApprovalDate",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/ApprovalDateRepresentationAssignmentClass",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"or [string] 2021-01-01"},
        description = "Date of Approval."
      },
      AasField {
        name = "ApprovalDescription",
        semanticId = "iri:https://sandbox.dexpi.org/rdl/ApprovalDescriptionAssignmentClass",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"en, approved"},
        description = "Approval Decision Description."
      },
      AasField {
        name = "ApproverName",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/ApproverNameAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"A. P. Prover"},
        description = "Approver Name."
      },
      AasField {
        name = "ArchiveNumber",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/ArchiveNumberAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"XY923-463"},
        description = "Archive Number."
      },
      AasField {
        name = "CheckerName",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/CheckerNameAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"C. Hecker"},
        description = "Checker Name."
      },
      AasField {
        name = "CreationDate",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/CreationDateRepresentationAssignmentClass",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"or [string] 2021-01-01"},
        description = "Date of Creation."
      },
      AasField {
        name = "CreatorName",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/CreatorNameAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"A. Creator"},
        description = "Creator Name."
      },
      AasField {
        name = "DesignerName",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/DesignerNameAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"D. E. Signer"},
        description = "Designer Name."
      },
      AasField {
        name = "DrawingNumber",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/DrawingNumberAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"123/A93"},
        description = "Number of the drawing."
      },
      AasField {
        name = "DrawingSubTitle",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/DrawingSubTitleAssignmentClass",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"en, DEXPI Example PID"},
        description = "Drawing subtitle."
      },
      AasField {
        name = "LastModificationDate",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/LastModificationDateRepresentationAssignmentClass",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"or [string] 2026-04-02"},
        description = "Last Modification Date."
      }
    }
  };

  AasSubmodelElementCollectionType MappingDirectory = {
    name = "MappingDirectory",
    semanticId = "iri:https://admin-shell.io/idta/DEXPI/1/0/MappingDirectory",
    description = "Container for local-global mappings within the DEXPI model.",
    versionIdentifier = "IDTA 02012-1-0",
    fixedName = true,
    fields = {
      AasField {
        name = "TagMapping",
        displayName = "{LocalIdwithinDEXPI}e.g.,PlateHeatExchanger_1",
        semanticId = "iri:https://admin-shell.io/idta/DEXPI/1/0/Metadata/TagMapping",
        isGeneric = true,
        type = refBy(Generic__LocalId_within_DEXPI__1),
        minimumInstances = 0,
        description = "or [IRI] https://admin- shell.io/idta/DEXPI/1/0/Metadata/SubTagMapping Container for mapping information."
      }
    }
  };

  AasSubmodelElementCollectionType Generic__LocalId_within_DEXPI__1 = {
    name = "LocalId within DEXPI_1",
    semanticId = "iri:https://admin-shell.io/idta/DEXPI/1/0/TagMapping",
    description = "Collection describing tag information.",
    versionIdentifier = "IDTA 02012-1-0",
    isGeneric = true,
    fields = {
      AasField {
        name = "TagName",
        semanticId = "iri:https://admin-shell.io/idta/DEXPI/1/0/TagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"H1007"},
        description = "Tag Name, for exact formulation rules see the description above."
      },
      AasField {
        name = "Class",
        semanticId = "iri:https://admin-shell.io/idta/DEXPI/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"PlateHeatExchanger"},
        description = "Class of the Equipment according to DEXPI."
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:https://admin-shell.io/idta/DEXPI/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"PlateHeatExchanger-1"},
        description = "Local ID of the element within the DEXPI representation, e.g., ID field of XML element within ProteusXML."
      },
      AasField {
        name = "MappingRelationship",
        displayName = "{LocalIdwithinDEXPI}_rele.g.,PlateHeatExchanger_1_rel",
        semanticId = "iri:https://admin-shell.io/idta/DEXPI/1/0/MappingRelationship",
        isGeneric = true,
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"First: (Submodel) (no-local) [id of Submodel]","(SEC) (local) Model01","(SubmodelElement) (local) ModelFile","(FragmentReference) (local) ProteusXML@ID=PlateHe atExchanger-1","Second:","(Asset) (no-local) [id of asset]"},
        description = "Relationship to map the local element to a globally identifiable asset."
      }
    }
  };

  AasSubmodelElementCollectionType Generic__LocalId_within_DEXPI__2 = {
    name = "LocalId within DEXPI_2",
    semanticId = "iri:https://admin-shell.io/idta/DEXPI/1/0/Metadata/SubTagMapping",
    description = "Collection describing subtag information.",
    versionIdentifier = "IDTA 02012-1-0",
    isGeneric = true,
    fields = {
      AasField {
        name = "SubTagName",
        semanticId = "iri:https://admin-shell.io/idta/DEXPI/1/0/SubTagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"N04"},
        description = "Sub tag name."
      },
      AasField {
        name = "ParentLocalId",
        semanticId = "iri:https://admin-shell.io/idta/DEXPI/1/0/ParentLocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"PlateHeatExchanger-1"},
        description = "Local identifier of the parent element within the DEXPI representation, e.g., ID field of XML element within ProteusXML."
      },
      AasField {
        name = "Class",
        semanticId = "iri:https://admin-shell.io/idta/DEXPI/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Nozzle"},
        description = "Class of the equipment according to DEXPI."
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:https://admin-shell.io/idta/DEXPI/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Nozzle-4"},
        description = "Local identifier of the element within the DEXPI representation, e.g., ID field of XML element within ProteusXML."
      },
      AasField {
        name = "MappingRelationship",
        displayName = "{LocalIdwithinDEXPI}_rele.g.,Nozzle_4_rel",
        semanticId = "iri:https://admin-shell.io/idta/DEXPI/1/0/MappingRelationship",
        isGeneric = true,
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"First: (Submodel) (no-local) [id of Submodel]","(SEC) (local) Model01","(SubmodelElement) (local) ModelFile","(FragmentReference) (local) ProteusXML@ID=Nozzle- 4","Second:","(Asset) (no-local) [id of asset]"},
        description = "Relationship to map the local element to a globally identifiable asset."
      }
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
