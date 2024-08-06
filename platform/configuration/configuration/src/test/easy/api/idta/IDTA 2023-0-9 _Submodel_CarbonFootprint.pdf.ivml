project IDTA_02023_CarbonFootprint {

  version v0.9;

  import AASDataTypes;

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasEnumType PCFCalculationMethod = {
    name = "PCFCalculationMethod",
    description = "Standard, method for determining the greenhouse gas emissions of a product.",
    versionIdentifier = "IDTA 02023-0-9",
    semanticId = "irdi:0173-1#02-ABG854#002",
    literals = {
      AasEnumLiteral {
        name = "EN 15804",
        semanticId = "0173-1#07-ABU223#002"
      },
      AasEnumLiteral {
        name = "GHG Protocol",
        semanticId = "0173-1#07-ABU221#002"
      },
      AasEnumLiteral {
        name = "IEC TS 63058",
        semanticId = "0173-1#07-ABU222#002"
      },
      AasEnumLiteral {
        name = "ISO 14040",
        semanticId = "0173-1#07-ABV505#002"
      },
      AasEnumLiteral {
        name = "ISO 14044",
        semanticId = "0173-1#07-ABV506#002"
      },
      AasEnumLiteral {
        name = "ISO 14067",
        semanticId = "0173-1#07-ABU218#002"
      },
      AasEnumLiteral {
        name = "IEC 63366",
        semanticId = "0173-1#07-ACA792#001"
      },
      AasEnumLiteral {
        name = "PEP Ecopassport",
        semanticId = "0173-1#07-ABU220#002"
      }
    }
  };

  AasEnumType PCFReferenceValueForCalculation = {
    name = "PCFReferenceValueForCalculation",
    description = "Quantity unit of the product to which the PCF information on the CO footprint refers.",
    versionIdentifier = "IDTA 02023-0-9",
    semanticId = "irdi:0173-1#02-ABG856#001",
    literals = {
      AasEnumLiteral {
        name = "g",
        semanticId = "0173-1#07-ABZ596#001"
      },
      AasEnumLiteral {
        name = "kg",
        semanticId = "0173-1#07-ABZ597#001"
      },
      AasEnumLiteral {
        name = "t",
        semanticId = "0173-1#07-ABZ598#001"
      },
      AasEnumLiteral {
        name = "ml",
        semanticId = "0173-1#07-ABZ599#001"
      },
      AasEnumLiteral {
        name = "l",
        semanticId = "0173-1#07-ABZ600#001"
      },
      AasEnumLiteral {
        name = "cbm",
        semanticId = "0173-1#07-ABZ601#001"
      },
      AasEnumLiteral {
        name = "qm",
        semanticId = "0173-1#07-ABZ602#001"
      },
      AasEnumLiteral {
        name = "piece",
        semanticId = "0173-1#07-ABZ603#001"
      }
    }
  };

  AasEnumType PCFLifeCyclePhase = {
    name = "PCFLifeCyclePhase",
    description = "Life cycle stages of the product according to the quantification requirements of the standard to which the PCF carbon footprint statement refers.",
    versionIdentifier = "IDTA 02023-0-9",
    semanticId = "irdi:0173-1#02-ABG858#001",
    literals = {
      AasEnumLiteral {
        name = "A1 - raw material supply and upstream production",
        identifier = "A1",
        value = "A1 - raw material supply (and upstream production)",
        semanticId = "0173-1#07-ABU208#001"
      },
      AasEnumLiteral {
        name = "A2 - cradle-to-gate transport to factory",
        identifier = "A2",
        value = "A2 - cradle-to-gate transport to factory",
        semanticId = "0173-1#07-ABU209#001"
      },
      AasEnumLiteral {
        name = "A3 - production",
        identifier = "A3",
        value = "A3 - production",
        semanticId = "0173-1#07-ABU210#001"
      },
      AasEnumLiteral {
        name = "A4 - transport to final destination",
        identifier = "A4",
        value = "A4 - transport to final destination",
        semanticId = "0173-1#07-ABU211#001"
      },
      AasEnumLiteral {
        name = "B1 - usage phase",
        identifier = "B1",
        value = "B1 - usage phase",
        semanticId = "0173-1#07-ABU212#001"
      },
      AasEnumLiteral {
        name = "B2 - maintenance",
        identifier = "B2",
        value = "B2 - maintenance",
        semanticId = "0173-1#07-ABV498#001"
      },
      AasEnumLiteral {
        name = "B3 - repair",
        identifier = "B3",
        value = "B3 - repair",
        semanticId = "0173-1#07-ABV497#001"
      },
      AasEnumLiteral {
        name = "B6 - usage energy consumption",
        identifier = "B6",
        value = "B6 - usage energy consumption",
        semanticId = "0173-1#07-ABV500#001"
      },
      AasEnumLiteral {
        name = "B7 - usage water consumption",
        identifier = "B7",
        value = "B7 - usage water consumption",
        semanticId = "0173-1#07-ABV501#001"
      },
      AasEnumLiteral {
        name = "C1 - reassembly",
        identifier = "C1",
        value = "C1 - reassembly",
        semanticId = "0173-1#07-ABV502#001"
      },
      AasEnumLiteral {
        name = "C2 - transport to recycler",
        identifier = "C2",
        value = "C2 - transport to recycler",
        semanticId = "0173-1#07-ABU213#001"
      },
      AasEnumLiteral {
        name = "C3 - recycling waste treatment",
        identifier = "C3",
        value = "C3 - recycling, waste treatment",
        semanticId = "0173-1#07-ABV503#001"
      },
      AasEnumLiteral {
        name = "C4 - landfill",
        identifier = "C4",
        value = "C4 - landfill",
        semanticId = "0173-1#07-ABV504#001"
      },
      AasEnumLiteral {
        name = "D - reuse",
        identifier = "D",
        value = "D - reuse",
        semanticId = "0173-1#07-ABU214#001"
      },
      AasEnumLiteral {
        name = "A1-A3",
        identifier = "A1_A3",
        value = "A1-A3",
        semanticId = "0173-1#07-ABZ789#001"
      }
    }
  };

  AasEnumType TCFCalculationMethod = {
    name = "TCFCalculationMethod",
    description = "Standard, method for determining the greenhouse gas emissions for the transport of a product.",
    versionIdentifier = "IDTA 02023-0-9",
    semanticId = "irdi:0173-1#02-ABG859#002",
    literals = {
      AasEnumLiteral {
        name = "EN 16258",
        semanticId = "0173-1#07-ABU224#001"
      }
    }
  };

  AasEnumType TCFReferenceValueForCalculation = {
    name = "TCFReferenceValueForCalculation",
    description = "Amount of product to which the TCF carbon footprint statement relates.",
    versionIdentifier = "IDTA 02023-0-9",
    semanticId = "irdi:0173-1#02-ABG861#002",
    literals = {
      AasEnumLiteral {
        name = "g",
        semanticId = "0173-1#07-ABZ596#001"
      },
      AasEnumLiteral {
        name = "kg",
        semanticId = "0173-1#07-ABZ597#001"
      },
      AasEnumLiteral {
        name = "t",
        semanticId = "0173-1#07-ABZ598#001"
      },
      AasEnumLiteral {
        name = "ml",
        semanticId = "0173-1#07-ABZ599#001"
      },
      AasEnumLiteral {
        name = "l",
        semanticId = "0173-1#07-ABZ600#001"
      },
      AasEnumLiteral {
        name = "cbm",
        semanticId = "0173-1#07-ABZ601#001"
      },
      AasEnumLiteral {
        name = "qm",
        semanticId = "0173-1#07-ABZ602#001"
      },
      AasEnumLiteral {
        name = "piece",
        semanticId = "0173-1#07-ABZ603#001"
      }
    }
  };

  AasEnumType TCFProcessesForGreenhouseGasEmissionInATransportService = {
    name = "TCFProcessesForGreenhouseGasEmissionInATransportService",
    description = "Processes in a transport service to determine the sum of all direct or indirect greenhouse gas emissions from fuel supply and vehicle operation.",
    versionIdentifier = "IDTA 02023-0-9",
    semanticId = "irdi:0173-1#02-ABG863#002",
    literals = {
      AasEnumLiteral {
        name = "WTT - Well-to-Tank",
        identifier = "WTT",
        semanticId = "0173-1#07-ABU216#001"
      },
      AasEnumLiteral {
        name = "TTW - Tank-to-Wheel",
        identifier = "TTW",
        semanticId = "0173-1#07-ABU215#001"
      },
      AasEnumLiteral {
        name = "WTW - Well-to-Wheel",
        identifier = "WTW",
        semanticId = "0173-1#07-ABU217#001"
      }
    }
  };

  AasSubmodelType CarbonFootprint = {
    name = "CarbonFootprint",
    semanticId = "iri:https://admin-shell.io/idta/CarbonFootprint/CarbonFootprint/0/9",
    description = "The Submodel provides the means to access the Carbon Footprint of the asset.",
    versionIdentifier = "IDTA 02023-0-9",
    fields = {
      AasField {
        name = "ProductCarbonFootprint",
        semanticId = "iri:https://admin-shell.io/idta/CarbonFootprint/ProductCarbonFootprint/0/9",
        counting = true,
        type = refBy(ProductCarbonFootprint),
        minimumInstances = 0,
        description = "Balance of greenhouse gas emissions along the entire life cycle of a product in a defined application and in relation to a defined unit of use."
      },
      AasField {
        name = "TransportCarbonFootprint",
        semanticId = "iri:https://admin-shell.io/idta/CarbonFootprint/TransportCarbonFootprint/0/9",
        counting = true,
        type = refBy(TransportCarbonFootprint),
        minimumInstances = 0,
        description = "Balance of greenhouse gas emissions generated by a transport service of a product."
      }
    }
  };

  AasSubmodelElementCollectionType ProductCarbonFootprint = {
    name = "ProductCarbonFootprint",
    semanticId = "irdi:https://admin-shell.io/idta/CarbonFootprint/ProductCarbonFootprint/0/9",
    description = "Balance of greenhouse gas emissions along the entire life cycle of a product in a defined application and in relation to a defined unit of use.",
    versionIdentifier = "IDTA 02023-0-9",
    fields = {
      AasField {
        name = "PCFCalculationMethod",
        semanticId = "irdi:0173-1#02-ABG854#002",
        type = refBy(PCFCalculationMethod),
        minimumInstances = 1,
        examples = {"valueId"},
        description = "Note 2: The usage of values that are not given in this table is possible, but not recommended, because this would reduce the compatibility."
      },
      AasField {
        name = "PCFCO2eq",
        semanticId = "irdi:0173-1#02-ABG855#001",
        type = refBy(DoubleType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"[kg] 17.2"},
        description = "Sum of all greenhouse gas emissions of a product according to the quantification requirements of the standard."
      },
      AasField {
        name = "PCFReferenceValueForCalculation",
        semanticId = "irdi:0173-1#02-ABG856#001",
        type = refBy(PCFReferenceValueForCalculation),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"valueId"},
        description = "Note 1: The usage of values that are not given in this table is possible, but not recommended, because this would reduce the compatibility."
      },
      AasField {
        name = "PCFQuantityOfMeasureForCalculation",
        semanticId = "irdi:0173-1#02-ABG857#001",
        type = refBy(DoubleType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"5.0"},
        description = "Quantity of the product to which the PCF information on the CO  footprint refers."
      },
      AasField {
        name = "PCFLifeCyclePhase",
        semanticId = "irdi:0173-1#02-ABG858#001",
        type = refBy(PCFLifeCyclePhase),
        minimumInstances = 1,
        examples = {"0173-1#07-ABV499#001"},
        description = "Note 1: Multiple lifecycle phases can be listed in the SMC. The interpretation is that the calculated PCF value is the sum of the PCF that has been produced in all the listed lifecycle phases. If the PCF value needs to be supplied for each lifecycle phase separately, multiple SMCs should be created instead. Note 2: The usage of values that are not given in this table is possible, but not recommended, because this would reduce the compatibility."
      },
      AasField {
        name = "ExplanatoryStatement",
        semanticId = "iri:https://admin-shell.io/idta/CarbonFootprint/ExplanatoryStatement/1/0",
        type = refBy(AasFileResourceType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Statement.pdf"},
        description = "Explanation which is needed or given so that a footprint communication can be properly understood by a purchaser, potential purchaser or user of the product definition."
      },
      AasField {
        name = "PCFGoodsAddressHandover",
        semanticId = "irdi:0173-1#02-ABI497#001",
        type = refBy(PCFGoodsAddressHandover),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Indicates the place of hand-over of the goods (use structure defined in section 2.5 SMC Address)."
      },
      AasField {
        name = "PublicationDate",
        semanticId = "iri:https://admin-shell.io/idta/CarbonFootprint/PublicationDate/1/0",
        type = refBy(DateTimeType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Time at which something was first published or made available."
      },
      AasField {
        name = "ExpirationDate",
        semanticId = "iri:https://admin-shell.io/idta/CarbonFootprint/ExpirationnDate/1/0",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Time at which something should no longer be used effectively because it may lose its validity, quality or safety."
      }
    }
  };

  AasSubmodelElementCollectionType TransportCarbonFootprint = {
    name = "TransportCarbonFootprint",
    semanticId = "irdi:https://admin-shell.io/idta/CarbonFootprint/TransportCarbonFootprint/0/9",
    description = "Balance of greenhouse gas emissions generated by a transport service of a product.",
    versionIdentifier = "IDTA 02023-0-9",
    fields = {
      AasField {
        name = "TCFCalculationMethod",
        semanticId = "irdi:0173-1#02-ABG859#002",
        type = refBy(TCFCalculationMethod),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"valueId"},
        description = "value."
      },
      AasField {
        name = "TCFCO2eq",
        semanticId = "irdi:0173-1#02-ABG860#001",
        type = refBy(DoubleType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"[kg] 5.3"},
        description = "Sum of all greenhouse gas emissions from vehicle operation."
      },
      AasField {
        name = "TCFReferenceValueForCalculation",
        semanticId = "irdi:0173-1#02-ABG861#002",
        type = refBy(TCFReferenceValueForCalculation),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"valueId"},
        description = "value."
      },
      AasField {
        name = "TCFQuantityOfMeasureForCalculation",
        semanticId = "irdi:0173-1#02-ABG862#001",
        type = refBy(DoubleType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Quantity of the product to which the TCF information on the CO footprint refers."
      },
      AasField {
        name = "TCFProcessesForGreenhouseGasEmissionInATransportService",
        semanticId = "irdi:0173-1#02-ABG863#002",
        type = refBy(TCFProcessesForGreenhouseGasEmissionInATransportService),
        minimumInstances = 1,
        examples = {"valueId"},
        description = "Value."
      },
      AasField {
        name = "ExplanatoryStatement",
        semanticId = "irdi:https://admin-shell.io/idta/CarbonFootprint/ExplanatoryStatement/1/0",
        type = refBy(AasFileResourceType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Statement.pdf"},
        description = "Explanation which is needed or given so that a footprint communication can be properly understood by a purchaser, potential purchaser or user of the product definition."
      },
      AasField {
        name = "TCFGoodsTransportAddressTakeover",
        semanticId = "irdi:0173-1#02-ABI499#001",
        type = refBy(TCFGoodsTransportAddressHandover),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Indication of the place of receipt of goods (use structure defined in 2.5 SMC Address)."
      },
      AasField {
        name = "TCFGoodsTransportAddressHandover",
        semanticId = "irdi:0173-1#02-ABI498#001",
        type = refBy(TCFGoodsTransportAddressHandover),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Indicates the hand-over address of the goods transport (use structure defined in 2.5 SMC Address)."
      },
      AasField {
        name = "PublicationDate",
        semanticId = "iri:https://admin-shell.io/idta/CarbonFootprint/PublicationDate/1/0",
        type = refBy(DateTimeType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Time at which something was first published or made available."
      },
      AasField {
        name = "ExpirationDate",
        semanticId = "iri:https://admin-shell.io/idta/CarbonFootprint/ExpirationnDate/1/0",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Time at which something should no longer be used effectively because it may lose its validity, quality or safety."
      }
    }
  };

  AasSubmodelElementCollectionType TCFGoodsTransportAddressHandover = {
    name = "TCFGoodsTransportAddressHandover",
    semanticId = "irdi:0173-1#02-ABI499#001",
    description = "Structure to be reused for denoting addresses.",
    versionIdentifier = "IDTA 02023-0-9",
    fields = {
      AasField {
        name = "Street",
        semanticId = "irdi:0173-1#02-ABH956#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Myroad"},
        description = "Street indication of the place of transfer of goods."
      },
      AasField {
        name = "HouseNumber",
        semanticId = "irdi:0173-1#02-ABH957#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1a"},
        description = "Number for identification or differentiation of individual houses of a street."
      },
      AasField {
        name = "ZipCode",
        semanticId = "irdi:0173-1#02-ABH958#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"12345"},
        description = "Zip code of the goods transfer address."
      },
      AasField {
        name = "CityTown",
        semanticId = "irdi:0173-1#02-ABH959#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Mytown"},
        description = "Indication of the city or town of the transfer of goods."
      },
      AasField {
        name = "Country",
        semanticId = "irdi:0173-1#02-AAO259#005",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Mycountry"},
        description = "Country where the product is transmitted."
      }
    }
  };

  AasSubmodelElementCollectionType PCFGoodsAddressHandover = {
    name = "PCFGoodsAddressHandover",
    semanticId = "irdi:0173-1#02-ABI498#001",
    description = "Structure to be reused for denoting addresses.",
    versionIdentifier = "IDTA 02023-0-9",
    fields = {
      AasField {
        name = "Street",
        semanticId = "irdi:0173-1#02-ABH956#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Myroad"},
        description = "Street indication of the place of transfer of goods."
      },
      AasField {
        name = "HouseNumber",
        semanticId = "irdi:0173-1#02-ABH957#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1a"},
        description = "Number for identification or differentiation of individual houses of a street."
      },
      AasField {
        name = "ZipCode",
        semanticId = "irdi:0173-1#02-ABH958#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"12345"},
        description = "Zip code of the goods transfer address."
      },
      AasField {
        name = "CityTown",
        semanticId = "irdi:0173-1#02-ABH959#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Mytown"},
        description = "Indication of the city or town of the transfer of goods."
      },
      AasField {
        name = "Country",
        semanticId = "irdi:0173-1#02-AAO259#005",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Mycountry"},
        description = "Country where the product is transmitted."
      }
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
