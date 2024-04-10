project IDTA_02012_DEXPI {

  import AASDataTypes;

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasSubmodelType DEXPI = {
    name = "DEXPI",
    semanticId = "iri:https://epc.org/sm_id/9f236679-e52d-4a52-aa85-dea871a89f9b",
    fields = {
      AasField {
        name = "PlantMetadata",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/PlantMetadata",
        type = refBy(PlantMetadata),
        minimumInstances = 1,
        maximumInstances = 1
      },
      AasField {
        name = "Model01",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Model",
        type = refBy(Model01),
        minimumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType PlantMetadata = {
    name = "PlantMetadata",
    semanticId = "iri:http://admin-shell.io/dexpi/1/0/PlantMetadata",
    ordered = true,
    fields = {
      AasField {
        name = "EnterpriseReference",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/EnterpriseReference",
        type = refBy(AasReferenceType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "SiteReference",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SiteReference",
        type = refBy(AasReferenceType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "IndustrialComplexReference",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/IndustrialComplexReference",
        type = refBy(AasReferenceType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "ProcessPlantReference",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/ProcessPlantReference",
        type = refBy(AasReferenceType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "PlantSectionReference",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/PlantSectionReference",
        type = refBy(AasReferenceType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "EnterpriseIdentificationCode",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/EnterpriseIdentificationCodeAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"oil-gas-inc"}
      },
      AasField {
        name = "EnterpriseName",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/EnterpriseNameAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Oil & Gas, Inc."}
      },
      AasField {
        name = "IndustrialComplexIdentificationCode",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/IndustrialComplexIdentificationCodeAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"I-Chain"}
      },
      AasField {
        name = "IndustrialComplexName",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/IndustrialComplexNameAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Isophorone Chain"}
      },
      AasField {
        name = "PlantSectionIdentificationCode",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/PlantSectionIdentificationCodeAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"10"}
      },
      AasField {
        name = "PlantSectionName",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/PlantSectionNameAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Utilities"}
      },
      AasField {
        name = "ProcessPlantIdentificationCode",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/ProcessPlantIdentificationCodeAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"ABC"}
      },
      AasField {
        name = "ProcessPlantName",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/ProcessPlantNameAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"ABC Plant"}
      },
      AasField {
        name = "ProjectName",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/ProjectNameAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"a project"}
      },
      AasField {
        name = "ProjectNumber",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/ProjectNumberAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"P3.1415"}
      },
      AasField {
        name = "SiteIdentificationCode",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/SiteIdentificationCodeAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"DC"}
      },
      AasField {
        name = "SiteName",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/SiteNameAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Dexpi City"}
      },
      AasField {
        name = "SubProjectName",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/SubProjectNameAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"a sub-project"}
      },
      AasField {
        name = "SubProjectNumber",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/SubProjectNumberAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"P3.1415-SP2"}
      },
      AasField {
        name = "ManufacturerName",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Plant Vendor"}
      },
      AasField {
        name = "DateOfManufacture",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2022-11-10"}
      },
      AasField {
        name = "EndProductCASName",
        type = refBy(StringListType),
        minimumInstances = 0,
        examples = {"7732-18-5"}
      },
      AasField {
        name = "EndProductName",
        type = refBy(StringListType),
        minimumInstances = 0,
        examples = {"Water"}
      }
    }
  };

  AasSubmodelElementCollectionType Model01 = {
    name = "Model01",
    semanticId = "iri:http://admin-shell.io/dexpi/1/0/Model",
    ordered = true,
    fields = {
      AasField {
        name = "ModelMetadata",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/ModelMetadata",
        type = refBy(ModelMetadata),
        minimumInstances = 1,
        maximumInstances = 1
      },
      AasField {
        name = "ModelFile",
        semanticId = "iri:https://admin-shell.io/idta/DEXPI/1/0/ModelFile",
        type = refBy(AasFileResourceType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"/aasx/C01V04-VER.EX01.xml"}
      },
      AasField {
        name = "ModelRepresentation",
        semanticId = "iri:https://admin-shell.io/idta/DEXPI/1/0/ModelRepresentation",
        type = refBy(AasFileResourceType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"/aasx/C01V04-VER.EX01.svg"}
      },
      AasField {
        name = "MappingDirectory",
        semanticId = "iri:http://admin-shell.io/idta/DEXPI/1/0/MappingDirectory",
        type = refBy(MappingDirectory),
        minimumInstances = 0,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType ModelMetadata = {
    name = "ModelMetadata",
    semanticId = "iri:http://admin-shell.io/dexpi/1/0/ModelMetadata",
    ordered = true,
    fields = {
      AasField {
        name = "ApprovalDate",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/ApprovalDateRepresentationAssignmentClass",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2016-04-01"}
      },
      AasField {
        name = "ApprovalDescription",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/ApprovalDescriptionAssignmentClass",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"approved@en"}
      },
      AasField {
        name = "ArchiveNumber",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/ArchiveNumberAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"XY923-463"}
      },
      AasField {
        name = "CheckerName",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/CheckerNameAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"C. Hecker"}
      },
      AasField {
        name = "CreationDate",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/CreationDateRepresentationAssignmentClass",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2016-04-01"}
      },
      AasField {
        name = "CreatorName",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/CreatorNameAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"A. Creator"}
      },
      AasField {
        name = "DesignerName",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/DesignerNameAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"D. E. Signer"}
      },
      AasField {
        name = "DrawingNumber",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/DrawingNumberAssignmentClass",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"123/A93"}
      },
      AasField {
        name = "DrawingSubTitle",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/DrawingSubTitleAssignmentClass",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"DEXPI Example PID@en"}
      },
      AasField {
        name = "LastModificationDate",
        semanticId = "iri:http://sandbox.dexpi.org/rdl/LastModificationDateRepresentationAssignmentClass",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2016-04-02"}
      }
    }
  };

  AasSubmodelElementCollectionType MappingDirectory = {
    name = "MappingDirectory",
    semanticId = "iri:http://admin-shell.io/idta/DEXPI/1/0/MappingDirectory",
    ordered = true,
    fields = {
      AasField {
        name = "ProcessInstrumentationFunction_1",
        semanticId = "iri:http://admin-shell.io/DEXPI/1/0/TagMapping",
        type = refBy(ProcessInstrumentationFunction_1),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "ProcessInstrumentationFunction_2",
        semanticId = "iri:http://admin-shell.io/DEXPI/1/0/TagMapping",
        type = refBy(ProcessInstrumentationFunction_2),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "ProcessInstrumentationFunction_3",
        semanticId = "iri:http://admin-shell.io/DEXPI/1/0/TagMapping",
        type = refBy(ProcessInstrumentationFunction_3),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "ProcessInstrumentationFunction_4",
        semanticId = "iri:http://admin-shell.io/DEXPI/1/0/TagMapping",
        type = refBy(ProcessInstrumentationFunction_4),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "ActuatingFunction_1",
        semanticId = "iri:http://admin-shell.io/DEXPI/1/0/TagMapping",
        type = refBy(ActuatingFunction_1),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "ActuatingFunction_2",
        semanticId = "iri:http://admin-shell.io/DEXPI/1/0/TagMapping",
        type = refBy(ActuatingFunction_2),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "ActuatingFunction_3",
        semanticId = "iri:http://admin-shell.io/DEXPI/1/0/TagMapping",
        type = refBy(ActuatingFunction_3),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "PlateHeatExchanger_1",
        semanticId = "iri:http://admin-shell.io/DEXPI/1/0/TagMapping",
        type = refBy(PlateHeatExchanger_1),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "TubularHeatExchanger_1",
        semanticId = "iri:http://admin-shell.io/DEXPI/1/0/TagMapping",
        type = refBy(TubularHeatExchanger_1),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "CentrifugalPump_1",
        semanticId = "iri:http://admin-shell.io/DEXPI/1/0/TagMapping",
        type = refBy(CentrifugalPump_1),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "ReciprocatingPump_1",
        semanticId = "iri:http://admin-shell.io/DEXPI/1/0/TagMapping",
        type = refBy(ReciprocatingPump_1),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "Tank_1",
        semanticId = "iri:http://admin-shell.io/DEXPI/1/0/TagMapping",
        type = refBy(Tank_1),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "ControlledActuator_1",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
        type = refBy(ControlledActuator_1),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "OperatedValveReference_1",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
        type = refBy(OperatedValveReference_1),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "ControlledActuator_2",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
        type = refBy(ControlledActuator_2),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "OperatedValveReference_2",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
        type = refBy(OperatedValveReference_2),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "ControlledActuator_3",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
        type = refBy(ControlledActuator_3),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "OperatedValveReference_3",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
        type = refBy(OperatedValveReference_3),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "Nozzle_3",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
        type = refBy(Nozzle_3),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "Nozzle_4",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
        type = refBy(Nozzle_4),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "Nozzle_13",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
        type = refBy(Nozzle_13),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "Nozzle_14",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
        type = refBy(Nozzle_14),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "Chamber_1",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
        type = refBy(Chamber_1),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "Chamber_2",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
        type = refBy(Chamber_2),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "Nozzle_10",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
        type = refBy(Nozzle_10),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "Nozzle_11",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
        type = refBy(Nozzle_11),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "Nozzle_16",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
        type = refBy(Nozzle_16),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "Nozzle_15",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
        type = refBy(Nozzle_15),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "Chamber_3",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
        type = refBy(Chamber_3),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "Chamber_4",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
        type = refBy(Chamber_4),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "Nozzle_1",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
        type = refBy(Nozzle_1),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "Nozzle_2",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
        type = refBy(Nozzle_2),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "Nozzle_7",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
        type = refBy(Nozzle_7),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "Nozzle_9",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
        type = refBy(Nozzle_9),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "Nozzle_5",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
        type = refBy(Nozzle_5),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "Nozzle_6",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
        type = refBy(Nozzle_6),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "Nozzle_18",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
        type = refBy(Nozzle_18),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "Nozzle_8",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
        type = refBy(Nozzle_8),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "Nozzle_12",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
        type = refBy(Nozzle_12),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "Nozzle_17",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
        type = refBy(Nozzle_17),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "Nozzle_19",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
        type = refBy(Nozzle_19),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "Chamber_7",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
        type = refBy(Chamber_7),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "Chamber_8",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
        type = refBy(Chamber_8),
        minimumInstances = 0,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType ProcessInstrumentationFunction_1 = {
    name = "ProcessInstrumentationFunction_1",
    semanticId = "iri:http://admin-shell.io/DEXPI/1/0/TagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "TagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/TagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"4712.01"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"ProcessInstrumentationFunction"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"ProcessInstrumentationFunction-1"}
      },
      AasField {
        name = "ProcessInstrumentationFunction_1_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType ProcessInstrumentationFunction_2 = {
    name = "ProcessInstrumentationFunction_2",
    semanticId = "iri:http://admin-shell.io/DEXPI/1/0/TagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "TagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/TagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"4712.02"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"ProcessInstrumentationFunction"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"ProcessInstrumentationFunction-2"}
      },
      AasField {
        name = "ProcessInstrumentationFunction_2_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType ProcessInstrumentationFunction_3 = {
    name = "ProcessInstrumentationFunction_3",
    semanticId = "iri:http://admin-shell.io/DEXPI/1/0/TagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "TagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/TagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"4750.01"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"ProcessInstrumentationFunction"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"ProcessInstrumentationFunction-3"}
      },
      AasField {
        name = "ProcessInstrumentationFunction_3_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType ProcessInstrumentationFunction_4 = {
    name = "ProcessInstrumentationFunction_4",
    semanticId = "iri:http://admin-shell.io/DEXPI/1/0/TagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "TagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/TagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"4750.03"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"ProcessInstrumentationFunction"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"ProcessInstrumentationFunction-4"}
      },
      AasField {
        name = "ProcessInstrumentationFunction_4_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType ActuatingFunction_1 = {
    name = "ActuatingFunction_1",
    semanticId = "iri:http://admin-shell.io/DEXPI/1/0/TagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "TagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/TagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"PV4712.02"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"ActuatingFunction"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"ActuatingFunction-1"}
      },
      AasField {
        name = "ActuatingFunction_1_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType ActuatingFunction_2 = {
    name = "ActuatingFunction_2",
    semanticId = "iri:http://admin-shell.io/DEXPI/1/0/TagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "TagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/TagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"HV4750.01"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"ActuatingFunction"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"ActuatingFunction-2"}
      },
      AasField {
        name = "ActuatingFunction_2_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType ActuatingFunction_3 = {
    name = "ActuatingFunction_3",
    semanticId = "iri:http://admin-shell.io/DEXPI/1/0/TagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "TagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/TagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"TV4750.03"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"ActuatingFunction"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"ActuatingFunction-3"}
      },
      AasField {
        name = "ActuatingFunction_3_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType PlateHeatExchanger_1 = {
    name = "PlateHeatExchanger_1",
    semanticId = "iri:http://admin-shell.io/DEXPI/1/0/TagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "TagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/TagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"H1007"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"PlateHeatExchanger"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"PlateHeatExchanger-1"}
      },
      AasField {
        name = "PlateHeatExchanger_1_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType TubularHeatExchanger_1 = {
    name = "TubularHeatExchanger_1",
    semanticId = "iri:http://admin-shell.io/DEXPI/1/0/TagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "TagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/TagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"H1008"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"TubularHeatExchanger"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"TubularHeatExchanger-1"}
      },
      AasField {
        name = "TubularHeatExchanger_1_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType CentrifugalPump_1 = {
    name = "CentrifugalPump_1",
    semanticId = "iri:http://admin-shell.io/DEXPI/1/0/TagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "TagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/TagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"P4711"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"CentrifugalPump"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"CentrifugalPump-1"}
      },
      AasField {
        name = "CentrifugalPump_1_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType ReciprocatingPump_1 = {
    name = "ReciprocatingPump_1",
    semanticId = "iri:http://admin-shell.io/DEXPI/1/0/TagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "TagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/TagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"P4712"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"ReciprocatingPump"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"ReciprocatingPump-1"}
      },
      AasField {
        name = "ReciprocatingPump_1_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType Tank_1 = {
    name = "Tank_1",
    semanticId = "iri:http://admin-shell.io/DEXPI/1/0/TagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "TagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/TagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"T4750"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Tank"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Tank-1"}
      },
      AasField {
        name = "Tank_1_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType ControlledActuator_1 = {
    name = "ControlledActuator_1",
    semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "SubTagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"PV4712.02_YC"}
      },
      AasField {
        name = "ParentLocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/ParentTagLocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"ActuatingSystem-1"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"ControlledActuator"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"ControlledActuator-1"}
      },
      AasField {
        name = "ControlledActuator_1_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType OperatedValveReference_1 = {
    name = "OperatedValveReference_1",
    semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "SubTagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"PV4712.02_YV"}
      },
      AasField {
        name = "ParentLocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/ParentTagLocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"ActuatingSystem-1"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"OperatedValveReference"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"OperatedValveReference-1"}
      },
      AasField {
        name = "OperatedValveReference_1_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType ControlledActuator_2 = {
    name = "ControlledActuator_2",
    semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "SubTagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"HV4750.01_YC"}
      },
      AasField {
        name = "ParentLocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/ParentTagLocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"ActuatingSystem-2"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"ControlledActuator"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"ControlledActuator-2"}
      },
      AasField {
        name = "ControlledActuator_2_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType OperatedValveReference_2 = {
    name = "OperatedValveReference_2",
    semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "SubTagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"HV4750.01_YV"}
      },
      AasField {
        name = "ParentLocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/ParentTagLocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"ActuatingSystem-2"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"OperatedValveReference"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"OperatedValveReference-2"}
      },
      AasField {
        name = "OperatedValveReference_2_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType ControlledActuator_3 = {
    name = "ControlledActuator_3",
    semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "SubTagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"TV4750.03_YC"}
      },
      AasField {
        name = "ParentLocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/ParentTagLocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"ActuatingSystem-3"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"ControlledActuator"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"ControlledActuator-3"}
      },
      AasField {
        name = "ControlledActuator_3_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType OperatedValveReference_3 = {
    name = "OperatedValveReference_3",
    semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "SubTagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"TV4750.03_YV"}
      },
      AasField {
        name = "ParentLocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/ParentTagLocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"ActuatingSystem-3"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"OperatedValveReference"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"OperatedValveReference-3"}
      },
      AasField {
        name = "OperatedValveReference_3_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType Nozzle_3 = {
    name = "Nozzle_3",
    semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "SubTagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"N1"}
      },
      AasField {
        name = "ParentLocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/ParentTagLocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"PlateHeatExchanger-1"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Nozzle"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Nozzle-3"}
      },
      AasField {
        name = "Nozzle_3_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType Nozzle_4 = {
    name = "Nozzle_4",
    semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "SubTagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"N2"}
      },
      AasField {
        name = "ParentLocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/ParentTagLocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"PlateHeatExchanger-1"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Nozzle"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Nozzle-4"}
      },
      AasField {
        name = "Nozzle_4_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType Nozzle_13 = {
    name = "Nozzle_13",
    semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "SubTagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"N3"}
      },
      AasField {
        name = "ParentLocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/ParentTagLocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"PlateHeatExchanger-1"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Nozzle"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Nozzle-13"}
      },
      AasField {
        name = "Nozzle_13_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType Nozzle_14 = {
    name = "Nozzle_14",
    semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "SubTagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"N4"}
      },
      AasField {
        name = "ParentLocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/ParentTagLocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"PlateHeatExchanger-1"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Nozzle"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Nozzle-14"}
      },
      AasField {
        name = "Nozzle_14_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType Chamber_1 = {
    name = "Chamber_1",
    semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "SubTagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Chamber 1"}
      },
      AasField {
        name = "ParentLocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/ParentTagLocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"PlateHeatExchanger-1"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Chamber"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Chamber-1"}
      },
      AasField {
        name = "Chamber_1_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType Chamber_2 = {
    name = "Chamber_2",
    semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "SubTagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Chamber 2"}
      },
      AasField {
        name = "ParentLocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/ParentTagLocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"PlateHeatExchanger-1"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Chamber"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Chamber-2"}
      },
      AasField {
        name = "Chamber_2_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType Nozzle_10 = {
    name = "Nozzle_10",
    semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "SubTagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"N1"}
      },
      AasField {
        name = "ParentLocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/ParentTagLocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"TubularHeatExchanger-1"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Nozzle"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Nozzle-10"}
      },
      AasField {
        name = "Nozzle_10_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType Nozzle_11 = {
    name = "Nozzle_11",
    semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "SubTagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"N2"}
      },
      AasField {
        name = "ParentLocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/ParentTagLocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"TubularHeatExchanger-1"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Nozzle"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Nozzle-11"}
      },
      AasField {
        name = "Nozzle_11_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType Nozzle_16 = {
    name = "Nozzle_16",
    semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "SubTagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"N3"}
      },
      AasField {
        name = "ParentLocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/ParentTagLocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"TubularHeatExchanger-1"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Nozzle"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Nozzle-16"}
      },
      AasField {
        name = "Nozzle_16_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType Nozzle_15 = {
    name = "Nozzle_15",
    semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "SubTagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"N4"}
      },
      AasField {
        name = "ParentLocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/ParentTagLocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"TubularHeatExchanger-1"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Nozzle"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Nozzle-15"}
      },
      AasField {
        name = "Nozzle_15_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType Chamber_3 = {
    name = "Chamber_3",
    semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "SubTagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Chamber 1"}
      },
      AasField {
        name = "ParentLocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/ParentTagLocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"TubularHeatExchanger-1"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Chamber"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Chamber-3"}
      },
      AasField {
        name = "Chamber_3_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType Chamber_4 = {
    name = "Chamber_4",
    semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "SubTagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Chamber 2"}
      },
      AasField {
        name = "ParentLocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/ParentTagLocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"TubularHeatExchanger-1"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Chamber"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Chamber-4"}
      },
      AasField {
        name = "Chamber_4_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType Nozzle_1 = {
    name = "Nozzle_1",
    semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "SubTagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"N1"}
      },
      AasField {
        name = "ParentLocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/ParentTagLocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"CentrifugalPump-1"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Nozzle"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Nozzle-1"}
      },
      AasField {
        name = "Nozzle_1_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType Nozzle_2 = {
    name = "Nozzle_2",
    semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "SubTagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"N2"}
      },
      AasField {
        name = "ParentLocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/ParentTagLocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"CentrifugalPump-1"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Nozzle"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Nozzle-2"}
      },
      AasField {
        name = "Nozzle_2_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType Nozzle_7 = {
    name = "Nozzle_7",
    semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "SubTagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"N1"}
      },
      AasField {
        name = "ParentLocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/ParentTagLocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"ReciprocatingPump-1"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Nozzle"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Nozzle-7"}
      },
      AasField {
        name = "Nozzle_7_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType Nozzle_9 = {
    name = "Nozzle_9",
    semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "SubTagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"N2"}
      },
      AasField {
        name = "ParentLocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/ParentTagLocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"ReciprocatingPump-1"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Nozzle"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Nozzle-9"}
      },
      AasField {
        name = "Nozzle_9_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType Nozzle_5 = {
    name = "Nozzle_5",
    semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "SubTagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"N1"}
      },
      AasField {
        name = "ParentLocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/ParentTagLocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Tank-1"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Nozzle"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Nozzle-5"}
      },
      AasField {
        name = "Nozzle_5_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType Nozzle_6 = {
    name = "Nozzle_6",
    semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "SubTagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"N2"}
      },
      AasField {
        name = "ParentLocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/ParentTagLocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Tank-1"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Nozzle"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Nozzle-6"}
      },
      AasField {
        name = "Nozzle_6_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType Nozzle_18 = {
    name = "Nozzle_18",
    semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "SubTagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"N3"}
      },
      AasField {
        name = "ParentLocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/ParentTagLocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Tank-1"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Nozzle"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Nozzle-18"}
      },
      AasField {
        name = "Nozzle_18_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType Nozzle_8 = {
    name = "Nozzle_8",
    semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "SubTagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"N5"}
      },
      AasField {
        name = "ParentLocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/ParentTagLocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Tank-1"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Nozzle"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Nozzle-8"}
      },
      AasField {
        name = "Nozzle_8_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType Nozzle_12 = {
    name = "Nozzle_12",
    semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "SubTagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"N6"}
      },
      AasField {
        name = "ParentLocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/ParentTagLocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Tank-1"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Nozzle"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Nozzle-12"}
      },
      AasField {
        name = "Nozzle_12_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType Nozzle_17 = {
    name = "Nozzle_17",
    semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "SubTagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"N7"}
      },
      AasField {
        name = "ParentLocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/ParentTagLocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Tank-1"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Nozzle"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Nozzle-17"}
      },
      AasField {
        name = "Nozzle_17_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType Nozzle_19 = {
    name = "Nozzle_19",
    semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "SubTagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"N8"}
      },
      AasField {
        name = "ParentLocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/ParentTagLocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Tank-1"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Nozzle"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Nozzle-19"}
      },
      AasField {
        name = "Nozzle_19_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType Chamber_7 = {
    name = "Chamber_7",
    semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "SubTagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Chamber 1"}
      },
      AasField {
        name = "ParentLocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/ParentTagLocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Tank-1"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Chamber"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Chamber-7"}
      },
      AasField {
        name = "Chamber_7_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType Chamber_8 = {
    name = "Chamber_8",
    semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagMapping",
    ordered = true,
    fields = {
      AasField {
        name = "SubTagName",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/SubTagName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Chamber 2"}
      },
      AasField {
        name = "ParentLocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/ParentTagLocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Tank-1"}
      },
      AasField {
        name = "Class",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/Class",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Chamber"}
      },
      AasField {
        name = "LocalId",
        semanticId = "iri:http://admin-shell.io/dexpi/1/0/LocalId",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Chamber-8"}
      },
      AasField {
        name = "Chamber_8_rel",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
