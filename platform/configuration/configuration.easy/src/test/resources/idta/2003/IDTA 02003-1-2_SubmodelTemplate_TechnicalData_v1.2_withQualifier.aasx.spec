project IDTA_02003_TechnicalData {

  version v1.2;

  import AASDataTypes;

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasSubmodelType TechnicalData = {
    name = "TechnicalData",
    semanticId = "iri:https://admin-shell.io/ZVEI/TechnicalData/Submodel/1/2",
    description = "Submodel containing techical data of the asset and associated product classificatons.",
    fields = {
      AasField {
        name = "GeneralInformation",
        semanticId = "iri:https://admin-shell.io/ZVEI/TechnicalData/GeneralInformation/1/1",
        type = refBy(GeneralInformation),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "General information, for example ordering and manufacturer information."
      },
      AasField {
        name = "ProductClassifications",
        semanticId = "iri:https://admin-shell.io/ZVEI/TechnicalData/ProductClassifications/1/1",
        type = refBy(ProductClassifications),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Product classifications by association of product classes with common classification systems."
      },
      AasField {
        name = "TechnicalProperties",
        semanticId = "iri:https://admin-shell.io/ZVEI/TechnicalData/TechnicalProperties/1/1",
        type = refBy(TechnicalProperties),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Individual characteristics that describe the product and its technical properties."
      },
      AasField {
        name = "FurtherInformation",
        semanticId = "iri:https://admin-shell.io/ZVEI/TechnicalData/FurtherInformation/1/1",
        type = refBy(FurtherInformation),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Further information on the product, the validity of the information provided and this data record."
      }
    }
  };

  AasSubmodelElementCollectionType GeneralInformation = {
    name = "GeneralInformation",
    semanticId = "iri:https://admin-shell.io/ZVEI/TechnicalData/GeneralInformation/1/1",
    description = "General information, for example ordering and manufacturer information.",
    fields = {
      AasField {
        name = "ManufacturerName",
        semanticId = "irdi:0173-1#02-AAO677#002",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Example Company"},
        description = "Legally valid designation of the natural or judicial body which is directly responsible for the design, production, packaging and labeling of a product in respect to its being brought into the market."
      },
      AasField {
        name = "ManufacturerLogo",
        semanticId = "iri:https://admin-shell.io/ZVEI/TechnicalData/ManufacturerLogo/1/1",
        type = refBy(AasFileResourceType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Imagefile for logo of manufacturer provided in common format (.png, .jpg)."
      },
      AasField {
        name = "ManufacturerProductDesignation",
        semanticId = "irdi:0173-1#02-AAW338#001",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Electrical energy accelerator@en"},
        description = "Product designation as given by the mnaufacturer. Short description of the product, product group or function (short text) in common language."
      },
      AasField {
        name = "ManufacturerArticleNumber",
        semanticId = "irdi:0173-1#02-AAO676#003",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"A123-456"},
        description = "unique product identifier of the manufacturer."
      },
      AasField {
        name = "ManufacturerOrderCode",
        semanticId = "iri:0173-1#02-AAO227#002",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"EEA-EX-200-S/47-Q3"},
        description = "By manufactures issued unique combination of numbers and letters used to identify the device for ordering."
      },
      AasField {
        name = "ProductImage",
        semanticId = "iri:https://admin-shell.io/ZVEI/TechnicalData/ProductImage/1/1",
        type = refBy(AasFileResourceType),
        minimumInstances = 0,
        description = "Image file for associated product provided in common format (.png, .jpg)."
      }
    }
  };

  AasSubmodelElementCollectionType ProductClassifications = {
    name = "ProductClassifications",
    semanticId = "iri:https://admin-shell.io/ZVEI/TechnicalData/ProductClassifications/1/1",
    description = "Product classifications by association of product classes with common classification systems.",
    fields = {
      AasField {
        name = "ProductClassificationItem",
        semanticId = "iri:https://admin-shell.io/ZVEI/TechnicalData/ProductClassificationItem/1/1",
        type = refBy(ProductClassificationItem),
        minimumInstances = 0,
        description = "Single product classification item by association with product class in a particular classification system or property dictionary."
      }
    }
  };

  AasSubmodelElementCollectionType ProductClassificationItem = {
    name = "ProductClassificationItem",
    semanticId = "iri:https://admin-shell.io/ZVEI/TechnicalData/ProductClassificationItem/1/1",
    description = "Single product classification item by association with product class in a particular classification system or property dictionary.",
    fields = {
      AasField {
        name = "ProductClassificationSystem",
        semanticId = "iri:https://admin-shell.io/ZVEI/TechnicalData/ProductClassificationSystem/1/1",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"ECLASS"},
        description = "Common name of the classification system."
      },
      AasField {
        name = "ClassificationSystemVersion",
        semanticId = "iri:https://admin-shell.io/ZVEI/TechnicalData/ClassificationSystemVersion/1/1",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"9.0 (BASIC)"},
        description = "Common version identifier of the used classification system, in order to distinguish different version of the property dictionary."
      },
      AasField {
        name = "ProductClassId",
        semanticId = "iri:https://admin-shell.io/ZVEI/TechnicalData/ProductClassId/1/1",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"27-01-88-77","0112/2///61987#ABA827#003"},
        description = "Class of the associated product or industrial equipment in the classification system. According to the notation of the system."
      }
    }
  };

  AasSubmodelElementCollectionType TechnicalProperties = {
    name = "TechnicalProperties",
    semanticId = "iri:https://admin-shell.io/ZVEI/TechnicalData/TechnicalProperties/1/1",
    description = "Individual characteristics that describe the product and its technical properties.",
    fields = {
      AasField {
        name = "arbitrary",
        displayName = "{arbitrary}",
        semanticId = "iri:",
        isGeneric = true,
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        examples = {"Width@en= 32 [mm]"},
        description = "Arbitrary SubmodelElement with semanticId possibly referring to a ConceptDescription can be used within the Technical Properties."
      },
      AasField {
        name = "arbitrary_1",
        displayName = "{arbitrary}",
        semanticId = "iri:https://admin-shell.io/SemanticIdNotAvailable/1/1",
        isGeneric = true,
        type = refBy(StringListType),
        minimumInstances = 0,
        examples = {"Length"},
        description = "Represents a SubmodelElement that is not described using a common classification system, a consortium specification, an open community standard, a published manufacturer specification or such."
      },
      AasField {
        name = "MainSection",
        semanticId = "iri:https://admin-shell.io/ZVEI/TechnicalData/MainSection/1/1",
        type = refBy(MainSection),
        minimumInstances = 0,
        description = "Main subdivision possibility for properties."
      },
      AasField {
        name = "SubSection",
        semanticId = "iri:https://admin-shell.io/ZVEI/TechnicalData/SubSection/1/1",
        type = refBy(SubSection),
        minimumInstances = 0,
        description = "Subordinate subdivision possibility for properties."
      }
    }
  };

  AasSubmodelElementCollectionType MainSection = {
    name = "MainSection",
    semanticId = "iri:https://admin-shell.io/ZVEI/TechnicalData/MainSection/1/1",
    description = "Main subdivision possibility for properties.",
    fields = {
      AasField {
        name = "SubSection",
        semanticId = "iri:https://admin-shell.io/ZVEI/TechnicalData/SubSection/1/1",
        type = refBy(SubSection),
        minimumInstances = 0,
        description = "Subordinate subdivision possibility for properties."
      }
    }
  };

  AasSubmodelElementCollectionType SubSection = {
    name = "SubSection",
    semanticId = "iri:https://admin-shell.io/ZVEI/TechnicalData/SubSection/1/1",
    description = "Subordinate subdivision possibility for properties."
  };

  AasSubmodelElementCollectionType FurtherInformation = {
    name = "FurtherInformation",
    semanticId = "iri:https://admin-shell.io/ZVEI/TechnicalData/FurtherInformation/1/1",
    description = "Further information on the product, the validity of the information provided and this data record.",
    fields = {
      AasField {
        name = "TextStatement",
        semanticId = "iri:https://admin-shell.io/ZVEI/TechnicalData/TextStatement/1/1",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        examples = {"Restricted use@en"},
        description = "Statement by the manufacturer in text form, e.g. scope of validity of the statements, scopes of application, conditions of operation."
      },
      AasField {
        name = "ValidDate",
        semanticId = "iri:https://admin-shell.io/ZVEI/TechnicalData/ManufacturerOrderCode/1/1",
        type = refBy(DateTimeType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"5/28/2021"},
        description = "Denotes a date on which the data specified in the Submodel was valid from for the associated asset."
      }
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
