project IDTA_02034_BackendSpecificMaterialInformation {

  version v1.0;

  import AASDataTypes;

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasSubmodelType BackendSpecificMaterialInformation = {
    name = "BackendSpecificMaterialInformation",
    semanticId = "iri:https://admin-shell.io/idta/BackendSpecificMaterialInformation/1/0/",
    description = "The Submodel should contain a collection which holds all relevant properties to create a material in different applications.",
    versionIdentifier = "IDTA 02034-1-0",
    fixedName = true,
    fields = {
      AasField {
        name = "MaterialSystemProperties",
        semanticId = "iri:https://admin-shell.io/idta/BackendSpecificMaterialInformation/MaterialSystemProperties/1/0",
        type = refBy(MaterialSystemProperties),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "The SMC “MaterialSystemProperties” contains all relevant properties to create a material in different applications."
      }
    }
  };

  AasSubmodelElementCollectionType MaterialSystemProperties = {
    name = "MaterialSystemProperties",
    semanticId = "iri:https://admin-shell.io/idta/BackendSpecificMaterialInformation/MaterialSystemProperties/1/0",
    description = "The SMC “MaterialSystemProperties” contains all relevant properties to create a material in different applications.",
    versionIdentifier = "IDTA 02034-1-0",
    fixedName = true,
    fields = {
      AasField {
        name = "MaterialType",
        semanticId = "iri:https://admin-shell.io/idta/BackendSpecificMaterialInformation/MaterialSystemProperties/MaterialType/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"ROH"},
        description = "Material Type information."
      },
      AasField {
        name = "BaseUnitOfMeasure",
        semanticId = "iri:https://admin-shell.io/idta/BackendSpecificMaterialInformation/MaterialSystemProperties/BaseUnitOfMeasure/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Piece@EN","Stück@DE"},
        description = "Units of Measurement of Various Types."
      },
      AasField {
        name = "MaterialStatus",
        semanticId = "iri:https://admin-shell.io/idta/BackendSpecificMaterialInformation/MaterialSystemProperties/MaterialStatus/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1"},
        description = "Material Status from Materials Management/PPC View."
      },
      AasField {
        name = "Industry",
        semanticId = "iri:https://admin-shell.io/idta/BackendSpecificMaterialInformation/MaterialSystemProperties/Industry/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"A"},
        description = "Industry sector key (material application type)."
      },
      AasField {
        name = "ProductName",
        semanticId = "iri:https://admin-shell.io/idta/BackendSpecificMaterialInformation/MaterialSystemProperties/ProductName/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Sensor@EN"},
        description = "Name of the product."
      },
      AasField {
        name = "MaterialNumber",
        semanticId = "iri:https://admin-shell.io/idta/BackendSpecificMaterialInformation/MaterialSystemProperties/MaterialNumber/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"000256984"},
        description = "Material number."
      },
      AasField {
        name = "Description",
        semanticId = "iri:https://admin-shell.io/idta/BackendSpecificMaterialInformation/MaterialSystemProperties/Description/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Kapazitive Sensoren zur Objekterkennung@DE"},
        description = "For Example the material short text."
      },
      AasField {
        name = "Plant",
        semanticId = "iri:https://admin-shell.io/idta/BackendSpecificMaterialInformation/MaterialSystemProperties/Plant/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"20"},
        description = "Plant in wich the material is added."
      },
      AasField {
        name = "arbitrary",
        displayName = "{arbitrary}",
        isGeneric = true,
        type = refBy(StringListType),
        minimumInstances = 0,
        description = "= {arbitrary, representing information required by specific properties that are needed for the material creation in required system}."
      },
      AasField {
        name = "arbitrary_1",
        displayName = "{arbitrary}",
        isGeneric = true,
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        description = "= {arbitrary, representing information required by specific properties that are needed for the material creation in required system}."
      }
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
