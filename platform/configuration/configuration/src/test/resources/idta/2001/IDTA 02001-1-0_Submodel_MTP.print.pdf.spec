project IDTA_02001_ModuleTypePackage {

  version v1.0;

  import AASDataTypes;

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasSubmodelType ModuleTypePackage = {
    name = "ModuleTypePackage",
    semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/MTPSubmodel",
    description = "The submodel defines an entrypoint to a MTP environment containing an embedded MTP file as SubmodelElement.",
    versionIdentifier = "IDTA 02001-1-0",
    fixedName = true,
    fields = {
      AasField {
        name = "MTPFile",
        semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/MTPSUCLib/ModuleTypePackage",
        type = refBy(AasFileResourceType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"MimeType = application/mtp Value = /aasx/mtp/package.mtp"},
        description = "ModuleTypePackage file included as a zipped package with."
      },
      AasField {
        name = "MTPReferences",
        semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/MTPReferences",
        type = refBy(MTPReferences),
        minimumInstances = 0,
        description = "Collection containing references to documentation documents which are associated with TagNames within the MTP file."
      },
      AasField {
        name = "BOMReferences",
        semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/MTPReferences",
        type = refBy(BOMReferences),
        minimumInstances = 0,
        description = "Collection containing references to documentation documents which are associated with TagNames within the MTP file."
      },
      AasField {
        name = "DocumentationReferences",
        semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/MTPReferences",
        type = refBy(DocumentationReferences),
        minimumInstances = 0,
        description = "Collection containing references to documentation documents which are associated with TagNames within the MTP file."
      }
    }
  };

  AasSubmodelElementCollectionType MTPReferences = {
    name = "MTPReferences",
    semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/MTPReferences",
    description = "This SubmodelElementCollection holds references to elements from other Submodels, e.g. included into VDI 2770 documentation Submodel.",
    versionIdentifier = "IDTA 02001-1-0",
    fields = {
      AasField {
        name = "MTPReference",
        displayName = "{arbitrary}",
        semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/MTPReference",
        isGeneric = true,
        type = refBy(AasRelationType),
        minimumInstances = 0,
        examples = {"first: (Submodel)(local)[IdShort]Mo duleTypePackage","(File)(local)[idShort]MTPFile (FragmentReference)[Custom","]","CAEX@ModuleTypePackage","/BPXX_Freelance/Communic ationSet/InstanceList/M0013","second:","(Submodel)(local)[IRI] http://example.com/id/instanc e/9992020020616052900001 2810","(SubmodelElementCollection) (local)[idShort]Document01"},
        description = "Reference between (first) an opaque TagName within the MTP file and (second) a documentation element within a documentation Submodel another Submodel."
      }
    }
  };

  AasSubmodelElementCollectionType BOMReferences = {
    name = "BOMReferences",
    semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/MTPReferences",
    description = "This SubmodelElementCollection holds references to elements from other Submodels, e.g. included into VDI 2770 documentation Submodel.",
    versionIdentifier = "IDTA 02001-1-0",
    fields = {
      AasField {
        name = "MTPReference",
        displayName = "{arbitrary}",
        semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/MTPReference",
        isGeneric = true,
        type = refBy(AasRelationType),
        minimumInstances = 0,
        examples = {"first: (Submodel)(local)[IdShort]Mo duleTypePackage","(File)(local)[idShort]MTPFile (FragmentReference)[Custom","]","CAEX@ModuleTypePackage","/BPXX_Freelance/Communic ationSet/InstanceList/M0013","second:","(Submodel)(local)[IRI] http://example.com/id/instanc e/9992020020616052900001 2810","(SubmodelElementCollection) (local)[idShort]Document01"},
        description = "Reference between (first) an opaque TagName within the MTP file and (second) a documentation element within a documentation Submodel another Submodel."
      }
    }
  };

  AasSubmodelElementCollectionType DocumentationReferences = {
    name = "DocumentationReferences",
    semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/MTPReferences",
    description = "This SubmodelElementCollection holds references to elements from other Submodels, e.g. included into VDI 2770 documentation Submodel.",
    versionIdentifier = "IDTA 02001-1-0",
    fields = {
      AasField {
        name = "MTPReference",
        displayName = "{arbitrary}",
        semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/MTPReference",
        isGeneric = true,
        type = refBy(AasRelationType),
        minimumInstances = 0,
        examples = {"first: (Submodel)(local)[IdShort]Mo duleTypePackage","(File)(local)[idShort]MTPFile (FragmentReference)[Custom","]","CAEX@ModuleTypePackage","/BPXX_Freelance/Communic ationSet/InstanceList/M0013","second:","(Submodel)(local)[IRI] http://example.com/id/instanc e/9992020020616052900001 2810","(SubmodelElementCollection) (local)[idShort]Document01"},
        description = "Reference between (first) an opaque TagName within the MTP file and (second) a documentation element within a documentation Submodel another Submodel."
      }
    }
  };

  AasSubmodelType ProcessEquipmentAssembly = {
    name = "ProcessEquipmentAssembly",
    semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/PEASubmodel",
    description = "The Submodel defines a set of PEA-properties specific to module instance Furthermore, we assume that the AAS of the PEA is referencing the AAS of module type, s.t. the relevant MTP file can be accessed by the tools. In exception cases where no AAS of MTP is available, this Submodel can also contain the MTPFile directly as defined in Section 0. In this case the MTPFile can be accessed two times, the MTP file of the Submodel instance shadows the MTPFile contained in ModuleTypePackage Submodel of referenced AAS.",
    versionIdentifier = "IDTA 02001-1-0",
    fixedName = true,
    fields = {
      AasField {
        name = "MTPFile",
        semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/MTPSUCLib/ModuleTypePackage",
        type = refBy(AasFileResourceType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"MimeType = application/mtp Value = /aasx/mtp/package.mtp"},
        description = "ModuleTypePackage file included as a zipped package with (.mtp is preferred)."
      },
      AasField {
        name = "DocumentationReferences",
        semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/MTPReferences",
        type = refBy(DocumentationReferences),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Collection containing references to documentation documents which are associtated with TagNames within the MTP file (defined in Section 0)."
      },
      AasField {
        name = "DisplayName",
        semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/peaSubmodel/DisplayName",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"en, Module 42"},
        description = "Operator-specific module name."
      },
      AasField {
        name = "Description",
        semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/PEASubmodel/Description",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"en, Stirrer module used for process D"},
        description = "Operator-specific module description."
      },
      AasField {
        name = "SourceList",
        semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/MTPSUCLib/CommunicationSet/SourceList",
        type = refBy(SourceList),
        minimumInstances = 0,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType SourceList = {
    name = "SourceList",
    semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/MTPSUCLib/CommunicationSet/SourceList",
    description = "This SMC contains descriptions to OPC UA servers of process equipment assembly. The idShort of the contained SMC could correspond to the respective InternalElement of RefBaseSystemUnitPath='MTPCommunicationSUCLib/ServerAssembly/OPCUAServer MTP file.",
    versionIdentifier = "IDTA 02001-1-0",
    fields = {
      AasField {
        name = "OPCUAServer",
        displayName = "{arbitrary}",
        semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/MTPCommunicationSUCLib/ServerAssembly/OPCUAServer",
        isGeneric = true,
        type = refBy(Generic__arbitrary__1),
        minimumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType Generic__arbitrary__1 = {
    name = "arbitrary_1",
    semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/MTPCommunicationSUCLib/ServerAssembly/OPCUAServer",
    description = "This SMC contains discovery endpoints of OPC UA servers. Note that the DiscoveryUrl is u flexible OPC UA endpoint selection by OPC UA client (e.g. different OPC UA security modes). Additionally, an optional ApplcaitonUri can be included to allow OPC UA clients to select a suitable OPC UA endpoint returned by endpoint discovery.",
    versionIdentifier = "IDTA 02001-1-0",
    isGeneric = true,
    fields = {
      AasField {
        name = "DiscoveryUrl",
        semanticId = "iri:https://admin-shell.io/idta/opcua-server-datasheet/1/0/discovery-url",
        counting = true,
        type = refBy(StringListType),
        minimumInstances = 1,
        examples = {"opc.tcp://localhost:4800"}
      },
      AasField {
        name = "ApplicationUri",
        semanticId = "iri:https://admin-shell.io/idta/opcua-server-datasheet/1/0/application-uri",
        counting = true,
        type = refBy(StringListType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"urn:org.com:PEA1:UA Server"}
      }
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
