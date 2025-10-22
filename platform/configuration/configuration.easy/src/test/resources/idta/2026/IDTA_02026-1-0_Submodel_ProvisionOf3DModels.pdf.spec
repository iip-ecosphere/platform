project IDTA_02026_Models3D {

  version v1.0;

  import AASDataTypes;

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasEnumType ObjectTypeValueList = {
    name = "ObjectTypeValueList",
    description = "Object type in terms of CAD structure.",
    versionIdentifier = "IDTA 02026-1-0",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/ObjectType/1/0",
    literals = {
      AasEnumLiteral {
        name = "Component",
        description = "The 3D model file represents a single component.",
        value = "Component"
      },
      AasEnumLiteral {
        name = "Assembly",
        description = "The 3D model file represents an assembly of assemblies or single components.",
        value = "Assembly"
      }
    }
  };

  AasEnumType OriginValueList = {
    name = "OriginValueList",
    description = "Origin on which the model is based on.",
    versionIdentifier = "IDTA 02026-1-0",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/Origin/1/0",
    literals = {
      AasEnumLiteral {
        name = "MeasureOptical",
        description = "The origin of the 3D Model is an optical measurement.",
        value = "MeasureOptical"
      },
      AasEnumLiteral {
        name = "Measureechanical",
        description = "The origin of the 3D Model is a mechanical measurement.",
        value = "Measureechanical"
      },
      AasEnumLiteral {
        name = "DesignEngineering",
        description = "The origin of the 3D Model is an engineering design.",
        value = "DesignEngineering"
      },
      AasEnumLiteral {
        name = "DesignGenerative",
        description = "The origin of the 3D Model is a generative design.",
        value = "DesignGenerative"
      }
    }
  };

  AasEnumType EmbeddedInfoValueList = {
    name = "EmbeddedInfoValueList",
    description = "Further information that are embedded in the 3D model file itself.",
    versionIdentifier = "IDTA 02026-1-0",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/EmbeddedInfo/1/0",
    literals = {
      AasEnumLiteral {
        name = "PMI",
        description = "PMI (product and manufacturing information) are embedded in the model file.",
        value = "PMI"
      },
      AasEnumLiteral {
        name = "ReferencePoints",
        description = "Reference points are embedded in the model file.",
        value = "ReferencePoints"
      },
      AasEnumLiteral {
        name = "Kinematics",
        description = "Model has axis with degrees of freedom that are intendent for kinematics.",
        value = "Kinematics"
      },
      AasEnumLiteral {
        name = "Parametrization",
        description = "At least one geometric parameter is intended to be set by a parametrization rule.",
        value = "Parametrization"
      }
    }
  };

  AasEnumType StateValueList = {
    name = "StateValueList",
    description = "State in the products lifecycle which is represented by the model.",
    versionIdentifier = "IDTA 02026-1-0",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/State/1/0",
    literals = {
      AasEnumLiteral {
        name = "Prototype",
        description = "The 3D model file represents a prototype state of the product.",
        value = "Prototype"
      },
      AasEnumLiteral {
        name = "PilotRun",
        description = "The 3D model file represents a pilot run state of the product.",
        value = "PilotRun"
      },
      AasEnumLiteral {
        name = "InSeries",
        description = "The 3D model file represents the series state of the product.",
        value = "InSeries"
      },
      AasEnumLiteral {
        name = "InterIn",
        description = "The 3D model file represents an input state of an intermediate process (e.g., manufacturing).",
        value = "InterIn"
      },
      AasEnumLiteral {
        name = "InterOut",
        description = "The 3D model file represents an output state of an intermediate process (e.g., manufacturing).",
        value = "InterOut"
      }
    }
  };

  AasEnumType ModelPurposeValueList = {
    name = "ModelPurposeValueList",
    description = "Information about the intended purpose and usage (positive and negative) of the model.",
    versionIdentifier = "IDTA 02026-1-0",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/ModelPurpose/1/0",
    literals = {
      AasEnumLiteral {
        name = "Logistics",
        description = "The model file is/is not suitable for a logistics purpose.",
        value = "Logistics"
      },
      AasEnumLiteral {
        name = "SimuMesh",
        description = "The model file is/is not suitable to be meshed and used in a numerical simulation.",
        value = "SimuMesh"
      },
      AasEnumLiteral {
        name = "VirtualCommisio ning",
        description = "The model file is/is not suitable to do virtual commissioning.",
        value = "VirtualCommisio ning"
      },
      AasEnumLiteral {
        name = "3DPrintingSlicer",
        description = "The model file is/is not suitable to be sliced and used for a 3D printing.",
        value = "3DPrintingSlicer"
      },
      AasEnumLiteral {
        name = "Rendering",
        description = "The model file is/is not suitable to be used for a rendering.",
        value = "Rendering"
      }
    }
  };

  AasEnumType PosModelPurposeValueList = {
    name = "PosModelPurposeValueList",
    description = "Information about the intended purpose and usage (positive and negative) of the model.",
    versionIdentifier = "IDTA 02026-1-0",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/PosModelPurpose/1/0",
    literals = {
      AasEnumLiteral {
        name = "Logistics",
        description = "The model file is/is not suitable for a logistics purpose.",
        value = "Logistics"
      },
      AasEnumLiteral {
        name = "SimuMesh",
        description = "The model file is/is not suitable to be meshed and used in a numerical simulation.",
        value = "SimuMesh"
      },
      AasEnumLiteral {
        name = "VirtualCommisio ning",
        description = "The model file is/is not suitable to do virtual commissioning.",
        value = "VirtualCommisio ning"
      },
      AasEnumLiteral {
        name = "3DPrintingSlicer",
        description = "The model file is/is not suitable to be sliced and used for a 3D printing.",
        value = "3DPrintingSlicer"
      },
      AasEnumLiteral {
        name = "Rendering",
        description = "The model file is/is not suitable to be used for a rendering.",
        value = "Rendering"
      }
    }
  };

  AasEnumType NegModelPurposeValueList = {
    name = "NegModelPurposeValueList",
    description = "Information about the intended purpose and usage (positive and negative) of the model.",
    versionIdentifier = "IDTA 02026-1-0",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/NegModelPurpose/1/0",
    literals = {
      AasEnumLiteral {
        name = "Logistics",
        description = "The model file is/is not suitable for a logistics purpose.",
        value = "Logistics"
      },
      AasEnumLiteral {
        name = "SimuMesh",
        description = "The model file is/is not suitable to be meshed and used in a numerical simulation.",
        value = "SimuMesh"
      },
      AasEnumLiteral {
        name = "VirtualCommisio ning",
        description = "The model file is/is not suitable to do virtual commissioning.",
        value = "VirtualCommisio ning"
      },
      AasEnumLiteral {
        name = "3DPrintingSlicer",
        description = "The model file is/is not suitable to be sliced and used for a 3D printing.",
        value = "3DPrintingSlicer"
      },
      AasEnumLiteral {
        name = "Rendering",
        description = "The model file is/is not suitable to be used for a rendering.",
        value = "Rendering"
      }
    }
  };

  AasEnumType ReducedElementsValueList = {
    name = "ReducedElementsValueList",
    description = "Information about what elements were reduced, in comparison to the model this model was derived from.",
    versionIdentifier = "IDTA 02026-1-0",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/ReducedElements/1/0",
    literals = {
      AasEnumLiteral {
        name = "ScrewJoints",
        description = "In comparison to the 3D model this model was derived from (see [Ref] DerivedFrom), screw joints were reduced.",
        value = "ScrewJoints"
      },
      AasEnumLiteral {
        name = "InnerParts",
        description = "In comparison to the 3D model this model was derived from (see [Ref] DerivedFrom), inner parts were reduced.",
        value = "InnerParts"
      },
      AasEnumLiteral {
        name = "Graphics",
        description = "In comparison to the 3D model this model was derived from (see [Ref] DerivedFrom), graphics were reduced.",
        value = "Graphics"
      }
    }
  };

  AasEnumType StatusValueValueList = {
    name = "StatusValueValueList",
    description = "Each file version represents a point in time in the file lifecycle. This status value refers to the milestones in the file lifecycle.",
    versionIdentifier = "IDTA 02026-1-0",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/StatusValue/1/0",
    literals = {
      AasEnumLiteral {
        name = "InReview",
        description = "The file is currently in the review process.",
        value = "InReview",
        semanticId = "irdi:0173-1#07-ABZ640#001"
      },
      AasEnumLiteral {
        name = "Released",
        description = "The file is released.",
        value = "Released",
        semanticId = "irdi:0173-1#07-ABZ641#001"
      }
    }
  };

  AasEnumType RepresentationValueList = {
    name = "RepresentationValueList",
    description = "Geometric representation of the model.",
    versionIdentifier = "IDTA 02026-1-0",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/Representation/1/0",
    literals = {
      AasEnumLiteral {
        name = "SolidBody",
        description = "The geometric representation is a solid body.",
        value = "SolidBody"
      },
      AasEnumLiteral {
        name = "WireFrame",
        description = "The geometric representation is a wire frame.",
        value = "WireFrame"
      },
      AasEnumLiteral {
        name = "Surface",
        description = "The geometric representation is a surface.",
        value = "Surface"
      },
      AasEnumLiteral {
        name = "Mesh",
        description = "The geometric representation is a mesh.",
        value = "Mesh"
      },
      AasEnumLiteral {
        name = "PointCloud",
        description = "The geometric representation is a point cloud.",
        value = "PointCloud"
      }
    }
  };

  AasEnumType BoundingBoxKindValueList = {
    name = "BoundingBoxKindValueList",
    description = "Information about the kind of bounding box.",
    versionIdentifier = "IDTA 02026-1-0",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/BoundingBoxKind/1/0",
    literals = {
      AasEnumLiteral {
        name = "MinEnvelope",
        description = "The bounding box defines a cartesian minimum envelope around the 3D model.",
        value = "MinEnvelope"
      },
      AasEnumLiteral {
        name = "MaxEnvelope",
        description = "The bounding box defines a cartesian maximum envelope around the 3D model.",
        value = "MaxEnvelope"
      }
    }
  };

  AasSubmodelType Models3D = {
    name = "Models3D",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/1/0",
    description = "This Submodel provides one or more 3D model files or a link to the model files, as well as meta information about them. Aim is to support the search, the integration and the usage of 3D models.",
    versionIdentifier = "IDTA 02026-1-0",
    fields = {
      AasField {
        name = "Model3D",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/1/0",
        type = refBy(Model3D),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "List that contains Model3D entities."
      }
    }
  };

  AasSubmodelElementListType Model3D = {
    name = "Model3D",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/1/0",
    description = "List that contains Model3D entities.",
    versionIdentifier = "IDTA 02026-1-0",
    fields = {
      AasField {
        name = "File",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/1/0",
        type = refBy(File),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Information about the model file entity. Contains the 3D model file, or external link to it."
      },
      AasField {
        name = "Capability",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/1/0",
        type = refBy(Capability),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Information about the model capability."
      },
      AasField {
        name = "Geometry",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/1/0",
        type = refBy(Geometry),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Basic geometric information of the model."
      }
    }
  };

  AasSubmodelElementCollectionType File = {
    name = "File",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/1/0",
    description = "Information about the model file entity. Contains the 3D model file, or external link to it.",
    versionIdentifier = "IDTA 02026-1-0",
    fields = {
      AasField {
        name = "FileId",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileId/1/0",
        type = refBy(FileId),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "List of file identifiers for the file. One ID in this collection should be used as a preferred ID (see isPrimary below)."
      },
      AasField {
        name = "FileVersion",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/1/0",
        type = refBy(FileVersion),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Information elements of file version entities."
      },
      AasField {
        name = "ConsumingApplication",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/ConsumingApplication/1/0",
        type = refBy(ConsumingApplication),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "List of information about the intended consuming applications of the 3D model."
      },
      AasField {
        name = "FileClassification",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileClassification/1/0",
        type = refBy(FileClassification),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "List of information for describing the classification of the file according to ClassificationSystems."
      }
    }
  };

  AasSubmodelElementListType FileId = {
    name = "FileId",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileId/1/0",
    description = "List of file identifiers for the file. One ID in this collection should be used as a preferred ID (see isPrimary below).",
    versionIdentifier = "IDTA 02026-1-0",
    fields = {
      AasField {
        name = "FileDomainId",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileId/FileDomaniId/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Identification of the domain in which the given ValueId is unique. The domain ID can be e.g. the name or acronym of the providing organization."
      },
      AasField {
        name = "ValueId",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileId/ValueId/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Identification value of the file within a given domain, e.g. the providing organization."
      },
      AasField {
        name = "IsPrimary",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileId/IsPrimary/1/0",
        type = refBy(BooleanType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Flag indicating that a FileId within a collection of at least two FileIds is the ‘primary’ identifier for the document. This is the preferred ID of the document (commonly from the point of view of the owner of the asset)."
      }
    }
  };

  AasSubmodelElementListType FileVersion = {
    name = "FileVersion",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/1/0",
    description = "List of information elements of file version entities.",
    versionIdentifier = "IDTA 02026-1-0",
    fields = {
      AasField {
        name = "Title",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/Title/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Exemplary title@en","Deutscher Titel@de"},
        description = "List of language-dependent titles of the file."
      },
      AasField {
        name = "FileName",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/FileName/1",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"example-name"},
        description = "/0 Name of the file."
      },
      AasField {
        name = "FileVersionId",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/FileVersionID/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"4.2.1"},
        description = "Unambiguous identification number of a FileVersion. Recommended versioning scheme is integer, point separated: MAJOR.MINOR.REVISION."
      },
      AasField {
        name = "StatusValue",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/StatusValue/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Released"},
        description = "Each file version represents a point in time in the file lifecycle. This status value refers to the milestones in the file lifecycle. Use ValueList – StatusValue."
      },
      AasField {
        name = "SetDate",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/SetDate/1/0",
        type = refBy(DateTimeType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"2020-02-06"},
        description = "Date when the document status was set. Format is YYYY- MM-dd."
      },
      AasField {
        name = "BasedOn",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/BasedOn/1/0",
        type = refBy(BasedOn),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "List of BasedOn relationships to other 3D Files or 3D FileVersion. Typically states that the content of the file is based on another file. Both have a strong relationship. Constraint: reference targets a [SMC] “File” or a [SMC] “FileVersion”."
      },
      AasField {
        name = "RefersTo",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/RefersTo/1/0",
        type = refBy(RefersTo),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "List of generic RefersTo relationships to other 3D Files or 3D FileVersions. They have a loose relationship. Constraint: reference targets a [SMC] “File” or a [SMC] “FileVersion”."
      },
      AasField {
        name = "PreviewFile",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/PreviewFile",
        type = refBy(AasFileResourceType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "/1/0 Provides a preview image of the DocumentVersion, e.g. first page, in a commonly used image format and in low resolution (< 512 x 512 pixels). Constraint: the MIME-Type needs to match the file type. Allowed file types are JPG, PNG, BMP."
      },
      AasField {
        name = "DigitalFile",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/DigitalFile/1",
        type = refBy(AasFileResourceType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "/0 MIME-Type, file name, and file contents given by the File SubmodelElement. Preferable use “MIME-Type = model/…”. If file type is not defined, use “MIME-Type = application/octet-stream”."
      },
      AasField {
        name = "ExternalFile",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/ExternalFile",
        type = refBy(ExternalFile),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "/1/0 Contains information to retrieve the file from an external source."
      },
      AasField {
        name = "FileFormat",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/FileFormat/1/0",
        type = refBy(FileFormat),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Contains information about the file format."
      },
      AasField {
        name = "SourceApplication",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/SourceApplication/1/0",
        type = refBy(SourceApplication),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Information about the source application the 3D model originated from."
      },
      AasField {
        name = "ProvidingOrganization",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/ProvidingOrganization/1/0",
        type = refBy(ProvidingOrganization),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Information about the model providing organization."
      }
    }
  };

  AasSubmodelElementListType BasedOn = {
    name = "BasedOn",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/BasedOn/1/0",
    description = "List of BasedOn relationships to other 3D Files or 3D FileVersions. Typically states that the content of the file is based on another file. Both have a strong relationship. Constraint: reference targets a [SMC] “File” or a [SMC] “FileVersion”.",
    versionIdentifier = "IDTA 02026-1-0"
  };

  AasSubmodelElementListType RefersTo = {
    name = "RefersTo",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/RefersTo/1/0",
    description = "List of generic RefersTo relationships to other 3D Files or 3D FileVersions. They have a loose relationship. Constraint: reference targets a [SMC] “File” or a [SMC] “FileVersion”.",
    versionIdentifier = "IDTA 02026-1-0"
  };

  AasSubmodelElementListType ExternalFile = {
    name = "ExternalFile",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/ExternalFile/1/0",
    description = "List of information to retrieve the file from an external host.",
    versionIdentifier = "IDTA 02026-1-0",
    fields = {
      AasField {
        name = "ExternalUrl",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/ExternalFile/ExternalUrl/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"https://admin-shell- io.com/5001/"},
        description = "Call link to an external file host. If applicable with identifier of the asset inside the URL."
      },
      AasField {
        name = "FileIdentifier",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/ExternalFile/FileIdentifier/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"https://boschrexroth.com/ids/ aas?p=p652370&m=R90150 807&s=1201694127"},
        description = "Identifier of the file that is unique within the ExternalUrl domain."
      },
      AasField {
        name = "HostOrganization",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/ExternalFile/HostOrganization/1/0",
        type = refBy(HostOrganization),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Information about the external file host organization."
      },
      AasField {
        name = "Api",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/ExternalFile/Api/1/0",
        type = refBy(Api),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "List of information about how the application programming interfaces (APIs) of the external file host is defined."
      }
    }
  };

  AasSubmodelElementCollectionType HostOrganization = {
    name = "HostOrganization",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/ExternalFile/HostOrganization/1/0",
    description = "Information about the external file host organization.",
    versionIdentifier = "IDTA 02026-1-0",
    fields = {
      AasField {
        name = "OrganizationName",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/ExternalFile/HostOrganization/OrganizationName1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"IDTA"},
        description = "Short name of the external file host organization."
      },
      AasField {
        name = "OrganizationOfficialName",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/ExternalFile/HostOrganization/OrganizationName1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Industrial Digital Twin Association e. V."},
        description = "Official name of the external file host organization."
      }
    }
  };

  AasSubmodelElementListType Api = {
    name = "Api",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/ExternalFile/Api/1/0",
    description = "List of Information about how the application programming interfaces (APIs) of the external file host is defined.",
    versionIdentifier = "IDTA 02026-1-0",
    fields = {
      AasField {
        name = "ApiVersion",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/ExternalFile/Api/ApiVersion/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"OpenAPI Specification – Version 3.0.1"},
        description = "Description of the version of the API."
      },
      AasField {
        name = "ApiDocumentationUrl",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/ExternalFile/Api/ApiDocumentationUrl/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"https://learn.openapis.org/"},
        description = "Link to the documentation of the API."
      },
      AasField {
        name = "ApiSpecificationUrl",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/ExternalFile/Api/ApiSpecificationUrl/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"https://github.com/OAI/OpenA PI-","Specification/blob/main/versio ns/3.0.1.md"},
        description = "Link to the specification of the API."
      }
    }
  };

  AasSubmodelElementCollectionType FileFormat = {
    name = "FileFormat",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/FileFormat/1/0",
    description = "Contains information about the file format.",
    versionIdentifier = "IDTA 02026-1-0",
    fields = {
      AasField {
        name = "FormatName",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/FileFormat/FormatName/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"STEP"},
        description = "Name of the file format."
      },
      AasField {
        name = "FormatVersion",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/FileFormat/FileVersion/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"AP242"},
        description = "Version of the file format."
      },
      AasField {
        name = "FormatQualifier",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/FileFormat/FormatQualifier/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"STEP-2.03"},
        description = "Unique qualifier of the file format."
      }
    }
  };

  AasSubmodelElementCollectionType SourceApplication = {
    name = "SourceApplication",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/SourceApplication/1/0",
    description = "Information about the source application the 3D model originated from.",
    versionIdentifier = "IDTA 02026-1-0",
    fields = {
      AasField {
        name = "ApplicationName",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/SourceApplication/ApplicationName/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"STEP"},
        description = "Name of the application."
      },
      AasField {
        name = "ApplicationVersion",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/SourceApplication/ApplicationVersion/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"AP242"},
        description = "Version of the application."
      },
      AasField {
        name = "ApplicationQualifier",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/SourceApplication/ApplicationQualifier/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"STEP-2.03"},
        description = "Unique qualifier of the application."
      },
      AasField {
        name = "Api",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/SourceApplication/Api/1/0",
        type = refBy(Api_2),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "List of information about how the application programming interfaces (APIs) of the source application is defined."
      },
      AasField {
        name = "VendorOrganization",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/SourceApplication/VendorOrganization/1/0",
        type = refBy(VendorOrganization),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Information about the tool vendor organization."
      }
    }
  };

  AasSubmodelElementListType Api_2 = {
    name = "Api_2",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/SourceApplication/Api/1/0",
    description = "List of information about how the application programming interfaces (APIs) of the source application is defined.",
    versionIdentifier = "IDTA 02026-1-0",
    fields = {
      AasField {
        name = "ApiVersion",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/SourceApplication/Api/ApiVersion/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"OpenAPI Specification – Version 3.0.1"},
        description = "Description of the version of the API."
      },
      AasField {
        name = "ApiDocumentationUrl",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/SourceApplication/Api/ApiDocumentationUrl/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"https://learn.openapis.org/"},
        description = "Link to the documentation of the API."
      },
      AasField {
        name = "ApiSpecificationUrl",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/SourceApplication/Api/ApiSpecificationUrl/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"https://github.com/OAI/OpenA PI-","Specification/blob/main/versio ns/3.0.1.md"},
        description = "Link to the specification of the API."
      }
    }
  };

  AasSubmodelElementCollectionType VendorOrganization = {
    name = "VendorOrganization",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/SourceApplication/VendorOrganization/1/0",
    description = "Information about the tool vendor organization.",
    versionIdentifier = "IDTA 02026-1-0",
    fields = {
      AasField {
        name = "OrganizationName",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/SourceApplication/VendorOrganization/OrganizationName/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"IDTA"},
        description = "Short name of the tool vendor organization."
      },
      AasField {
        name = "OrganizationOfficialName",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/SourceApplication/VendorOrganization/OrganizationOfficialName/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Industrial Digital Twin Association e. V."},
        description = "Official name of the tool vendor organization."
      }
    }
  };

  AasSubmodelElementCollectionType ProvidingOrganization = {
    name = "ProvidingOrganization",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/ProvidingOrganization/1/0",
    description = "Information about the model providing organization.",
    versionIdentifier = "IDTA 02026-1-0",
    fields = {
      AasField {
        name = "OrganizationName",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/ProvidingOrganization/OrganizationName/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"IDTA"},
        description = "Short name of the model providing organization."
      },
      AasField {
        name = "OrganizationOfficialName",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/ProvidingOrganization//OrganizationOfficialName/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Industrial Digital Twin Association e. V."},
        description = "Official name of the model providing organization."
      }
    }
  };

  AasSubmodelElementListType ConsumingApplication = {
    name = "ConsumingApplication",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/ConsumingApplication/1/0",
    description = "List of information about the intended consuming applications of the 3D model.",
    versionIdentifier = "IDTA 02026-1-0",
    fields = {
      AasField {
        name = "ApplicationName",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/ConsumingApplication/ApplicationName/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"STEP"},
        description = "Name of the application."
      },
      AasField {
        name = "ApplicationVersion",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/ConsumingApplication/ApplicationVersion/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"AP242"},
        description = "Version of the application."
      },
      AasField {
        name = "ApplicationQualifier",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/ConsumingApplication/ApplicationQualifier1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"STEP-2.03"},
        description = "Unique qualifier of the application."
      },
      AasField {
        name = "Api",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/ConsumingApplication/Api/1/0",
        type = refBy(Api_3),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "List of information about how the application programming interfaces (APIs) of the consuming application is defined."
      },
      AasField {
        name = "VendorOrganization",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileConsumer/ConsumingApplication/VendorOrganization/1/0",
        type = refBy(VendorOrganization),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Information about the tool vendor organization."
      }
    }
  };

  AasSubmodelElementListType Api_3 = {
    name = "Api_3",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/ConsumingApplication/Api/1/0",
    description = "List of information about how the application programming interfaces (APIs) of the consuming application is defined.",
    versionIdentifier = "IDTA 02026-1-0",
    fields = {
      AasField {
        name = "ApiVersion",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/ConsumingApplication/Api/ApiVersion/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"OpenAPI Specification – Version 3.0.1"},
        description = "Description of the version of the API."
      },
      AasField {
        name = "ApiDocumentationUrl",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File//ConsumingApplication/Api/ApiDocumentationUrl/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"https://learn.openapis.org/"},
        description = "Link to the documentation of the API."
      },
      AasField {
        name = "ApiSpecificationUrl",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/ConsumingApplication/Api/ApiSpecificationUrl/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"https://github.com/OAI/OpenA PI-","Specification/blob/main/versio ns/3.0.1.md"},
        description = "Link to the specification of the API."
      }
    }
  };

  AasSubmodelElementCollectionType VendorOrganization_2 = {
    name = "VendorOrganization_2",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/ConsumingApplication/VendorOrganization/1/0",
    description = "Information about the tool vendor organization.",
    versionIdentifier = "IDTA 02026-1-0",
    fields = {
      AasField {
        name = "OrganizationName",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/ConsumingApplication/VendorOrganization/OrganizationName/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"IDTA"},
        description = "Short name of the tool vendor organization."
      },
      AasField {
        name = "OrganizationOfficialName",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/ConsumingApplication/VendorOrganization/OrganizationOfficialName/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Industrial Digital Twin Association e. V."},
        description = "Official name of the tool vendor organization."
      }
    }
  };

  AasSubmodelElementListType FileClassification = {
    name = "FileClassification",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileClassification/1/0",
    description = "List of information for describing the classification of the file according to ClassificationSystems.",
    versionIdentifier = "IDTA 02026-1-0",
    fields = {
      AasField {
        name = "ClassId",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileClassification/ClassId/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"02-02"},
        description = "Unique ID of the document class within a ClassificationSystem."
      },
      AasField {
        name = "ClassName",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileClassification/ClassName/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Drawings, plans"},
        description = "List of language-dependent names of the selected ClassID."
      },
      AasField {
        name = "ClassificationSystem",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/File/ClassificationSystem/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"VDI2770:2020"},
        description = "Identification of the classification system."
      }
    }
  };

  AasSubmodelElementCollectionType Capability = {
    name = "Capability",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/1/0",
    description = "Information about the model capability.",
    versionIdentifier = "IDTA 02026-1-0",
    fields = {
      AasField {
        name = "PosModelPurpose",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/PosModelPurpose/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"FDM Simulation"},
        description = "List of purposes for which the model is explicitly suitable. Use ValueList – ModelPurpose."
      },
      AasField {
        name = "NegModelPurpose",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/NegModelPurpos/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Rendering"},
        description = "List of purposes for which the model is explicitly not suitable. Use ValueList – ModelPurpose."
      },
      AasField {
        name = "EmbeddedInfo",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/EmbeddedInfo/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Reference Points"},
        description = "List of further information that are embedded in the 3D model file itself. Use ValueList – EmbeddedInfo."
      },
      AasField {
        name = "State",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/State/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Manufacturing in"},
        description = "List of states or maturity in the products lifecycle which is represented by the model. Use ValueList – State."
      },
      AasField {
        name = "ObjectType",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/ObjectType/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Assembly"},
        description = "Object type in terms of CAD structure. Use ValueList – ObjectType."
      },
      AasField {
        name = "Origin",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/Origin/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Modeling"},
        description = "Origin on which the model is based on. Use ValueList – Origin."
      },
      AasField {
        name = "Simplification",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/Simplification/1/0",
        type = refBy(Simplification),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Information what was simplified in this [SMC] Model3D compared to a reference [SMC] Model 3D, in this case [REF] DerivedFrom."
      }
    }
  };

  AasSubmodelElementListType PosModelPurpose = {
    name = "PosModelPurpose",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/PosModelPurpose/1/0",
    description = "List of purposes for which the model is explicitly suitable. Use ValueList – ModelPurpose.",
    versionIdentifier = "IDTA 02026-1-0"
  };

  AasSubmodelElementListType NegModelPurpose = {
    name = "NegModelPurpose",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/NegModelPurpose/1/0",
    description = "List of purposes for which the model is explicitly not suitable. Use ValueList – ModelPurpose.",
    versionIdentifier = "IDTA 02026-1-0"
  };

  AasSubmodelElementListType Embedded_Info = {
    name = "Embedded Info",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/EmbeddedInfo/1/0",
    description = "List of further information that are embedded in the 3D model file itself. Use ValueList – EmbeddedInfo.",
    versionIdentifier = "IDTA 02026-1-0"
  };

  AasSubmodelElementCollectionType Simplification = {
    name = "Simplification",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/Simplification/1/0",
    description = "Information what was simplified in this [SMC] Model3D compared to a reference [SMC] Model 3D, in this case [REF] DerivedFrom.",
    versionIdentifier = "IDTA 02026-1-0",
    fields = {
      AasField {
        name = "Description",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/Simplification/LevelDescription/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"In comparison to the 3D model this 3D model was derived from, all screw joints were removed."},
        description = "Textual description of what was simplified."
      },
      AasField {
        name = "ReducedElements",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/Simplification/ReducedElements/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Screw Joints"},
        description = "List of information about what elements were reduced, in comparison to the model this model was derived from. Use ValueList – ReducedElements."
      },
      AasField {
        name = "DerivedFrom",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/Simplification/DerivedFrom/1/0",
        type = refBy(AasReferenceType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Reference points to a [SMC] “Model3D” on which this [SMC] “Model3D” is based on."
      }
    }
  };

  AasSubmodelElementListType ReducedElements = {
    name = "ReducedElements",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/Simplification/ReducedElements/1/0",
    description = "List of information about what elements were reduced, in comparison to the model this model was derived from. Use ValueList – ReducedElements.",
    versionIdentifier = "IDTA 02026-1-0"
  };

  AasSubmodelElementCollectionType Geometry = {
    name = "Geometry",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/1/0",
    description = "Basic geometric information of the model.",
    versionIdentifier = "IDTA 02026-1-0",
    fields = {
      AasField {
        name = "Representation",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/Representation/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Wire Frame"},
        description = "Geometric representation of the asset. Use ValueList – Representation."
      },
      AasField {
        name = "LengthUnit",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/LengthUnit/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"mm"},
        description = "The measurement unit of the length properties within subordinate [Prop] of this [SMC] Geometry."
      },
      AasField {
        name = "CartBoundingBox",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/1/0",
        type = refBy(CartBoundingBox),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "List of information about the cartesian bounding boxes of the asset."
      },
      AasField {
        name = "CartRefSystem",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartRefSystem/1/0",
        type = refBy(CartRefSystem_2),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "List of three cartesian reference systems of the asset."
      }
    }
  };

  AasSubmodelElementListType CartBoundingBox = {
    name = "CartBoundingBox",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/1/0",
    description = "List of information about the cartesian bounding boxes of the asset.",
    versionIdentifier = "IDTA 02026-1-0",
    fields = {
      AasField {
        name = "BoundingBoxKind",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/BoundingBoxKind/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"MaxEnvelope"},
        description = "Information about the kind of bounding box. Use ValueList – BoundingBoxKind."
      },
      AasField {
        name = "CartRefSystem",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/CartRefSystem/1/0",
        type = refBy(CartRefSystem),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Cartesian reference system of the bounding box."
      },
      AasField {
        name = "CartBoundingVector",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/CartBoundingBox/1/0",
        type = refBy(CartBoundingVector),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Cartesian vector that describes the expansion of the bounding box from a cartesian reference system (CartRefSystem)."
      }
    }
  };

  AasSubmodelElementCollectionType CartRefSystem = {
    name = "CartRefSystem",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/CartRefSystem/1/0",
    description = "Cartesian reference system of the cart bounding box of the Asset.",
    versionIdentifier = "IDTA 02026-1-0",
    fields = {
      AasField {
        name = "CartOffsetVector",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/CartRefSystem/CartOffsetVector/1/0",
        type = refBy(CartOffsetVector),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Cartesian offset vector of the cartesian reference system."
      },
      AasField {
        name = "NormOrientationVector",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/CartRefSystem/NormOrientationVector/1/0",
        type = refBy(NormOrientationVector),
        minimumInstances = 3,
        maximumInstances = 3,
        description = "Cartesian norm vector of the cartesian reference system."
      }
    }
  };

  AasSubmodelElementCollectionType CartOffsetVector = {
    name = "CartOffsetVector",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/CartRefSystem/CartOffsetVector/1/0",
    description = "Cartesian offset vector of the cartesian reference system.",
    versionIdentifier = "IDTA 02026-1-0",
    fields = {
      AasField {
        name = "X",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/CartRefSystem/CartOffsetVector/X/1/0",
        type = refBy(DoubleType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"42"},
        description = "X component of the offset vector. Always state the unit."
      },
      AasField {
        name = "Y",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/CartRefSystem/CartOffsetVector/Y/1/0",
        type = refBy(DoubleType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"42"},
        description = "Y component of the offset vector. Always state the unit."
      },
      AasField {
        name = "Z",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/CartRefSystem/CartOffsetVector/Z/1/0",
        type = refBy(DoubleType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"42"},
        description = "Z component of the offset vector. Always state the unit."
      }
    }
  };

  AasSubmodelElementListType NormOrientationVector = {
    name = "NormOrientationVector",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/CartRefSystem/NormOrientationVector/1/0",
    description = "List with three cartesian norm vectors of the cartesian reference system.",
    versionIdentifier = "IDTA 02026-1-0",
    fields = {
      AasField {
        name = "X",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/CartRefSystem/NormOrientationVector/X/1/0",
        type = refBy(DoubleType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"42"},
        description = "X component of the norm vector. Always state the unit."
      },
      AasField {
        name = "Y",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/CartRefSystem/NormOrientationVector/Y/1/0",
        type = refBy(DoubleType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"42"},
        description = "Y component of the norm vector. Always state the unit."
      },
      AasField {
        name = "Z",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/CartRefSystem/NormOrientationVector/Z/1/0",
        type = refBy(DoubleType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"42"},
        description = "Z component of the norm vector. Always state the unit."
      }
    }
  };

  AasSubmodelElementCollectionType CartBoundingVector = {
    name = "CartBoundingVector",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/CartBoundingVector/1/0",
    description = "Cartesian vector that describes the expansion of the bounding box from a cartesian reference system (CartRefSystem).",
    versionIdentifier = "IDTA 02026-1-0",
    fields = {
      AasField {
        name = "X",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/CartBoundingVector/X/1/0",
        type = refBy(DoubleType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"42"},
        description = "X component of the bounding vector. Always state the unit."
      },
      AasField {
        name = "Y",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/CartBoundingVector/Y/1/0",
        type = refBy(DoubleType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"42"},
        description = "Y component of the bounding vector. Always state the unit."
      },
      AasField {
        name = "Z",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/CartBoundingVector/Z/1/0",
        type = refBy(DoubleType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"42"},
        description = "Z component of the bounding vector. Always state the unit."
      }
    }
  };

  AasSubmodelElementListType CartRefSystem_2 = {
    name = "CartRefSystem_2",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartRefSystem/1/0",
    description = "List of cartesian reference system.",
    versionIdentifier = "IDTA 02026-1-0",
    fields = {
      AasField {
        name = "CartOffsetVector",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartRefSystem/CartOffsetVector/1/0",
        type = refBy(CartOffsetVector_2),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Cartesian offset vector of the cartesian reference system."
      },
      AasField {
        name = "NormOrientationVector",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartRefSystem/NormOrientationVector/1/0",
        type = refBy(NormOrientationVector_2),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "List of three cartesian norm vectors of the cartesian reference system."
      }
    }
  };

  AasSubmodelElementCollectionType CartOffsetVector_2 = {
    name = "CartOffsetVector_2",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartRefSystem/CartOffsetVector/1/0",
    description = "Cartesian offset vector of the cartesian reference system.",
    versionIdentifier = "IDTA 02026-1-0",
    fields = {
      AasField {
        name = "X",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartRefSystem/CartOffsetVector/X/1/0",
        type = refBy(DoubleType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"42"},
        description = "X component of the reference vector. Always state the unit."
      },
      AasField {
        name = "Y",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartRefSystem/CartOffsetVector/Y/1/0",
        type = refBy(DoubleType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"42"},
        description = "Y component of the reference vector. Always state the unit."
      },
      AasField {
        name = "Z",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartRefSystem/CartOffsetVector/Z/1/0",
        type = refBy(DoubleType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"42"},
        description = "Z component of the reference vector. Always state the unit."
      }
    }
  };

  AasSubmodelElementListType NormOrientationVector_2 = {
    name = "NormOrientationVector_2",
    semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartRefSystem/NormOrientationVector/1/0",
    description = "List of three norm orientation vectors of the cartesian reference system.",
    versionIdentifier = "IDTA 02026-1-0",
    fields = {
      AasField {
        name = "X",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartRefSystem/NormOrientationVector/X/1/0",
        type = refBy(DoubleType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"42"},
        description = "X component of the norm vector. Always state the unit."
      },
      AasField {
        name = "Y",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartRefSystem/NormOrientationVector/Y/1/0",
        type = refBy(DoubleType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"42"},
        description = "Y component of the norm vector. Always state the unit."
      },
      AasField {
        name = "Z",
        semanticId = "iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartRefSystem/NormOrientationVector/Z/1/0",
        type = refBy(DoubleType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"42"},
        description = "Z component of the norm vector. Always state the unit."
      }
    }
  };

  AasSubmodelElementCollectionType Application = {
    name = "Application",
    semanticId = "iri:https://admin-shell.io/idta/CommonClasses/Application/1/0",
    description = "Information about the application.",
    versionIdentifier = "IDTA 02026-1-0",
    fields = {
      AasField {
        name = "ApplicationName",
        semanticId = "iri:https://admin-shell.io/idta/CommonClasses/Application/ApplicationName/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"STEP"},
        description = "Name of the application."
      },
      AasField {
        name = "ApplicationVersion",
        semanticId = "iri:https://admin-shell.io/idta/CommonClasses/Application/ApplicationVersion/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"SP242"},
        description = "Version of the application."
      },
      AasField {
        name = "ApplicationQualifier",
        semanticId = "iri:https://admin-shell.io/idta/CommonClasses/Application/ApplicationQualifier/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"STEP-2.03"},
        description = "Unique qualifier of the application."
      },
      AasField {
        name = "Api",
        semanticId = "iri:https://admin-shell.io/idta/CommonClasses/Application/Api/1/0",
        type = refBy(Api),
        minimumInstances = 0,
        examples = {"Api]"},
        description = "Information about how the application programming interfaces (APIs) of the application is defined."
      },
      AasField {
        name = "VendorOrganization",
        semanticId = "iri:https://admin-shell.io/idta/CommonClasses/Application/VendorOrganization/1/0",
        type = refBy(VendorOrganization),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Organization]"},
        description = "Information about the tool vendor organization."
      }
    }
  };

  AasSubmodelElementCollectionType Api_4 = {
    name = "Api_4",
    semanticId = "iri:https://admin-shell.io/idta/CommonClasses/Api/1/0",
    description = "Information about how the application programming interfaces (APIs) of the application is defined.",
    versionIdentifier = "IDTA 02026-1-0",
    fields = {
      AasField {
        name = "ApiVersion",
        semanticId = "iri:https://admin-shell.io/idta/CommonClasses/Api/ApiVersion/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"OpenAPI Specification – Version 3.0.1"},
        description = "Description of the version of the API."
      },
      AasField {
        name = "ApiDocumentationUrl",
        semanticId = "iri:https://admin-shell.io/idta/CommonClasses/Api/ApiDocumentationUrl/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"https://learn.openapis.org/"},
        description = "Link to the documentation of the API."
      },
      AasField {
        name = "ApiSpecificationUrl",
        semanticId = "iri:https://admin-shell.io/idta/CommonClasses/Api/ApiSpecificationUrl/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"https://github.com/OAI/OpenA PI-","Specification/blob/main/versio ns/3.0.1.md"},
        description = "Link to the specification of the API."
      }
    }
  };

  AasSubmodelElementCollectionType Organization = {
    name = "Organization",
    semanticId = "iri:https://admin-shell.io/idta/CommonClasses/Organization/1/0",
    description = "Information about the organization in general.",
    versionIdentifier = "IDTA 02026-1-0",
    fields = {
      AasField {
        name = "OrganizationName",
        semanticId = "iri:https://admin-shell.io/idta/CommonClasses/Organization/OrganizationName/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"IDTA"},
        description = "Short name of the organization."
      },
      AasField {
        name = "OrganizationOfficialName",
        semanticId = "iri:https://admin-shell.io/idta/CommonClasses/Organization/OrganizationOfficialName/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Industrial Digital Twin Association e. V."},
        description = "Official name of the organization."
      }
    }
  };

  AasSubmodelElementCollectionType CartRefSystem_3 = {
    name = "CartRefSystem_3",
    semanticId = "iri:https://admin-shell.io/idta/CommonClasses/CartRefSystem/1/0",
    description = "Cartesian reference system.",
    versionIdentifier = "IDTA 02026-1-0",
    fields = {
      AasField {
        name = "CartOffsetVector",
        semanticId = "iri:https://admin-shell.io/idta/CommonClasses/CartRefSystem/CartOffsetVector/1/0",
        type = refBy(CartOffsetVector),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"CartVector]"},
        description = "Cartesian offset vector of the cartesian reference system."
      },
      AasField {
        name = "NormOrientationVector",
        semanticId = "iri:https://admin-shell.io/idta/CommonClasses/CartRefSystem/NormOrientationVector/1/0",
        type = refBy(NormOrientationVector),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"CartVector]"},
        description = "Cartesian norm vector of the cartesian reference system."
      }
    }
  };

  AasSubmodelElementCollectionType CartVector = {
    name = "CartVector",
    semanticId = "iri:https://admin-shell.io/idta/CommonClasses/CartVector/1/0",
    description = "General cartesian vector.",
    versionIdentifier = "IDTA 02026-1-0",
    fields = {
      AasField {
        name = "X",
        semanticId = "iri:https://admin-shell.io/idta/CommonClasses/CartOffsetVector/X/1/0",
        type = refBy(DoubleType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"42"},
        description = "X component of the cart vector. Always state the unit."
      },
      AasField {
        name = "Y",
        semanticId = "iri:https://admin-shell.io/idta/CommonClasses/CartOffsetVector/Y/1/0",
        type = refBy(DoubleType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"42"},
        description = "Y component of the cart vector. Always state the unit."
      },
      AasField {
        name = "Z",
        semanticId = "iri:https://admin-shell.io/idta/CommonClasses/CartOffsetVector/Z/1/0",
        type = refBy(DoubleType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"42"},
        description = "Z component of the cart vector. Always state the unit."
      }
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
