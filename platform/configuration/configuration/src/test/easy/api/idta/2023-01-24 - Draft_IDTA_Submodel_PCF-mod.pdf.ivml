project IDTA_0000_CarbonFootprint {

  import AASDataTypes;

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasEnumType PCFCalculationMethod = {
    name = "DraftPCFCalculationMethod",
    description = "Standard, method for determining the greenhouse gas emissions of a product.",
    semanticId = "irdi:0173-1#02-ABG854#001",
    literals = {
      AasEnumLiteral {
        name = "EN 15804",
        value = "1",
        semanticId = "0173-1#07-ABU223#001"
      },
      AasEnumLiteral {
        name = "GHG Protocol",
        value = "2",
        semanticId = "0173-1#07-ABU221#001"
      },
      AasEnumLiteral {
        name = "IEC TS 63058",
        value = "3",
        semanticId = "0173-1#07-ABU222#001"
      },
      AasEnumLiteral {
        name = "ISO 14040",
        value = "4",
        semanticId = "0173-1#07-ABV505#001"
      },
      AasEnumLiteral {
        name = "ISO 14044",
        value = "5",
        semanticId = "0173-1#07-ABV506#001"
      },
      AasEnumLiteral {
        name = "ISO 14067",
        value = "6",
        semanticId = "0173-1#07-ABU218#001"
      }
    }
  };

  AasEnumType PCFReferenceValueForCalculation = {
    name = "DraftPCFReferenceValueForCalculation",
    description = "Quantity unit of the product to which the PCF information on the CO2 footprint refers.",
    semanticId = "irdi:0173-1#02-ABG856#001",
    literals = {
      AasEnumLiteral {
        name = "g",
        value = "1",
        semanticId = "0173-1#07-ABZ596#001"
      },
      AasEnumLiteral {
        name = "kg",
        value = "2",
        semanticId = "0173-1#07-ABZ597#001"
      },
      AasEnumLiteral {
        name = "t",
        value = "3",
        semanticId = "0173-1#07-ABZ598#001"
      },
      AasEnumLiteral {
        name = "ml",
        value = "4",
        semanticId = "0173-1#07-ABZ599#001"
      },
      AasEnumLiteral {
        name = "l",
        value = "5",
        semanticId = "0173-1#07-ABZ600#001"
      },
      AasEnumLiteral {
        name = "cbm",
        value = "6",
        semanticId = "0173-1#07-ABZ601#001"
      },
      AasEnumLiteral {
        name = "qm",
        value = "7",
        semanticId = "0173-1#07-ABZ602#001"
      }
    }
  };

  AasEnumType PCFLiveCyclePhase = {
    name = "DraftPCFLiveCyclePhase",
    description = "Life cycle stages of the product according to the quantification requirements of the standard to which the PCF carbon footprint statement refers.",
    semanticId = "irdi:0173-1#02-ABG858#001",
    literals = {
      AasEnumLiteral {
        name = "A1 - raw material supply and upstream production",
        identifier = "A1",
        value = "1",
        semanticId = "0173-1#07-ABU208#001"
      },
      AasEnumLiteral {
        name = "A2 - cradle-to-gate transport to factory",
        identifier = "A2",
        value = "2",
        semanticId = "0173-1#07-ABU209#001"
      },
      AasEnumLiteral {
        name = "A3 - production",
        identifier = "A3",
        value = "3",
        semanticId = "0173-1#07-ABU210#001"
      },
      AasEnumLiteral {
        name = "A4 - transport to final destination",
        identifier = "A4",
        value = "4",
        semanticId = "0173-1#07-ABU211#001"
      },
      AasEnumLiteral {
        name = "B1 - usage phase",
        identifier = "B1",
        value = "5",
        semanticId = "0173-1#07-ABU212#001"
      },
      AasEnumLiteral {
        name = "B2 - maintenance",
        identifier = "B2",
        value = "6",
        semanticId = "0173-1#07-ABV498#001"
      },
      AasEnumLiteral {
        name = "B3 - repair",
        identifier = "B3",
        value = "15",
        semanticId = "0173-1#07-ABV497#001"
      },
      AasEnumLiteral {
        name = "B5 - update upgrade refurbishing",
        identifier = "B5",
        value = "7",
        semanticId = "0173-1#07-ABV499#001"
      },
      AasEnumLiteral {
        name = "B6 - usage energy consumption",
        identifier = "B6",
        value = "8",
        semanticId = "0173-1#07-ABV500#001"
      },
      AasEnumLiteral {
        name = "B7 - usage water consumption",
        identifier = "B7",
        value = "9",
        semanticId = "0173-1#07-ABV501#001"
      },
      AasEnumLiteral {
        name = "C1 - reassembly",
        identifier = "C1",
        value = "10",
        semanticId = "0173-1#07-ABV502#001"
      },
      AasEnumLiteral {
        name = "C2 - transport to recycler",
        identifier = "C2",
        value = "11",
        semanticId = "0173-1#07-ABU213#001"
      },
      AasEnumLiteral {
        name = "C3 - recycling waste treatment",
        identifier = "C3",
        value = "12",
        semanticId = "0173-1#07-ABV503#001"
      },
      AasEnumLiteral {
        name = "C4 - landfill",
        identifier = "C4",
        value = "13",
        semanticId = "0173-1#07-ABV504#001"
      },
      AasEnumLiteral {
        name = "D - reuse",
        identifier = "D",
        value = "14",
        semanticId = "0173-1#07-ABU214#001"
      },
      AasEnumLiteral {
        name = "A1-A3",
        identifier = "A1_A3",
        value = "16",
        semanticId = "0173-1#07-ABZ789#001"
      }
    }
  };

  AasEnumType TCFCalculationMethod = {
    name = "DraftTCFCalculationMethod",
    description = "Standard, method for determining the greenhouse gas emissions for the transport of a product.",
    semanticId = "irdi:0173-1#02-ABG859#001",
    literals = {
      AasEnumLiteral {
        name = "EN 16258",
        value = "1",
        semanticId = "0173-1#07-ABU224#001"
      }
    }
  };

  AasEnumType TCFReferenceValueForCalculation = {
    name = "DraftTCFReferenceValueForCalculation",
    description = "Amount of product to which the TCF carbon footprint statement relates.",
    semanticId = "irdi:0173-1#02-ABG861#001",
    literals = {
      AasEnumLiteral {
        name = "g",
        value = "1",
        semanticId = "0173-1#07-ABZ596#001"
      },
      AasEnumLiteral {
        name = "kg",
        value = "2",
        semanticId = "0173-1#07-ABZ597#001"
      },
      AasEnumLiteral {
        name = "t",
        value = "3",
        semanticId = "0173-1#07-ABZ598#001"
      },
      AasEnumLiteral {
        name = "ml",
        value = "4",
        semanticId = "0173-1#07-ABZ599#001"
      },
      AasEnumLiteral {
        name = "l",
        value = "5",
        semanticId = "0173-1#07-ABZ600#001"
      },
      AasEnumLiteral {
        name = "cbm",
        value = "6",
        semanticId = "0173-1#07-ABZ601#001"
      },
      AasEnumLiteral {
        name = "qm",
        value = "7",
        semanticId = "0173-1#07-ABZ602#001"
      },
      AasEnumLiteral {
        name = "piece",
        value = "8",
        semanticId = "0173-1#07-ABZ603#001"
      }
    }
  };

  AasEnumType TCFProcessesForGreenhouseGasEmissionInATransportService = {
    name = "DraftTCFProcessesForGreenhouseGasEmissionInATransportService",
    description = "Processes in a transport service to determine the sum of all direct or indirect greenhouse gas emissions from fuel supply and vehicle operation.",
    semanticId = "irdi:0173-1#02-ABG863#001",
    literals = {
      AasEnumLiteral {
        name = "WTT - Well-to-Tank",
        identifier = "WTT",
        value = "1",
        semanticId = "0173-1#07-ABU216#001"
      },
      AasEnumLiteral {
        name = "TTW - Tank-to-Wheel",
        identifier = "TTW",
        value = "2",
        semanticId = "0173-1#07-ABU215#001"
      },
      AasEnumLiteral {
        name = "WTW - Well-to-Wheel",
        identifier = "WTW",
        value = "3",
        semanticId = "0173-1#07-ABU217#001"
      }
    }
  };

  AasSubmodelType DraftCarbonFootprint = {
    name = "DraftCarbonFootprint",
    idShort = "CarbonFootprint",
    semanticId = "irdi:0173-1#01-AHE712#001",
    description = "The Submodel provides the means to access the Carbon Footprint of the asset.",
    fields = {
      AasField {
        name = "ProductCarbonFootprint",
        semanticId = "irdi:0173-1#01-AHE716#001",
        counting = true,
        type = refBy(ProductCarbonFootprint),
        minimumInstances = 0,
        description = "Balance of greenhouse gas emissions along the entire life cycle of a product in a defined application and in relation to a defined unit of use."
      },
      AasField {
        name = "TransportCarbonFootprint",
        semanticId = "irdi:0173-1#01-AHE717#001",
        counting = true,
        type = refBy(TransportCarbonFootprint),
        minimumInstances = 0,
        description = "Balance of greenhouse gas emissions generated by a transport service of a product."
      }
    }
  };

  AasSubmodelElementCollectionType ProductCarbonFootprint = {
    name = "DraftProductCarbonFootprint",
    idShort = "ProductCarbonFootprint",
    semanticId = "irdi:0173-1#01-AHE716#001",
    description = "Balance of greenhouse gas emissions along the entire life cycle of a product in a defined application and in relation to a defined unit of use.",
    fields = {
      AasField {
        name = "PCFCalculationMethod",
        semanticId = "irdi:0173-1#02-ABG854#001",
        type = refBy(PCFCalculationMethod),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"String “ISO 14067”"},
        description = "Standard, method for determining the greenhouse gas emissions of a product."
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
        examples = {"piece"},
        description = "Quantity unit of the product to which the PCF information on the CO2 footprint refers."
      },
      AasField {
        name = "PCFQuantityOfMeasureForCalculation",
        semanticId = "irdi:0173-1#02-ABG857#001",
        type = refBy(DoubleType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"5.0"},
        description = "Quantity of the product to which the PCF information on the CO2 footprint refers."
      },
      AasField {
        name = "PCFLiveCyclePhase",
        semanticId = "irdi:0173-1#02-ABG858#001",
        type = refBy(PCFLiveCyclePhase),
        minimumInstances = 1,
        examples = {"String “C4 - landfill”"},
        description = "Life cycle stages of the product according to the quantification requirements of the standard to which the PCF carbon footprint statement refers."
      },
      AasField {
        name = "PCFGoodsAddressHandover",
        semanticId = "irdi:0173-1#02-ABI497#001",
        type = refBy(AasGenericSubmodelElementCollection),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Indicates the place of hand-over of the goods (use structure defined in 2.5 SMC Address)."
      }
    }
  };

  AasSubmodelElementCollectionType TransportCarbonFootprint = {
    name = "DraftTransportCarbonFootprint",
    idShort = "TransportCarbonFootprint",
    semanticId = "irdi:0173-1#01-AHE717#001",
    description = "Balance of greenhouse gas emissions generated by a transport service of a product.",
    fields = {
      AasField {
        name = "TCFCalculationMethod",
        semanticId = "irdi:0173-1#02-ABG859#001",
        type = refBy(TCFCalculationMethod),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"String “EN 16258”"},
        description = "Standard, method for determining the greenhouse gas emissions for the transport of a product."
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
        semanticId = "irdi:0173-1#02-ABG861#001",
        type = refBy(TCFReferenceValueForCalculation),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"piece"},
        description = "Amount of product to which the TCF carbon footprint statement relates."
      },
      AasField {
        name = "TCFQuantityOfMeasureForCalculation",
        semanticId = "irdi:0173-1#02-ABG862#001",
        type = refBy(DoubleType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Quantity of the product to which the TCF information on the CO2 footprint refers."
      },
      AasField {
        name = "TCFProcessesForGreenhouseGasEmissionInATransportService",
        semanticId = "irdi:0173-1#02-ABG863#001",
        type = refBy(TCFProcessesForGreenhouseGasEmissionInATransportService),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"String “WTT - Well-to- Tank”"},
        description = "Processes in a transport service to determine the sum of all direct or indirect greenhouse gas emissions from fuel supply and vehicle operation."
      },
      AasField {
        name = "TCFGoodsTransportAddressTakeover",
        semanticId = "irdi:0173-1#02-ABI499#001",
        type = refBy(TCFGoodsTransportAddressTakeover),
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
      }
    }
  };

  AasSubmodelElementCollectionType TCFGoodsTransportAddressTakeover = {
    name = "DraftTCFGoodsTransportAddressTakeover",
    idShort = "TCFGoodsTransportAddressTakeover",
    semanticId = "irdi:0173-1#02-ABI499#001",
    description = "Structure to be reused for denoting addresses.",
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
      },
      AasField {
        name = "Latitude",
        semanticId = "irdi:0173-1#02-ABH960#001",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"40.757"},
        description = "Latitude (B), also called geodetic latitude or latitude (Latin latitudo, English latitude, international abbreviation Lat. or LAT), is the northerly or southerly distance of a point on the earth's surface from the  equator, given in angular measure in the unit of measurement degrees."
      },
      AasField {
        name = "Longitude",
        semanticId = "irdi:0173-1#02-ABH961#001",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"-73.986"},
        description = "Geographic longitude, also called longitude (Latin longitudo, English longitude, international abbreviation long or LON), describes one of the two coordinates of a location on the earth's surface, namely its position east or west of a defined (arbitrarily determined) north-south line, the prime meridian."
      }
    }
  };

  AasSubmodelElementCollectionType TCFGoodsTransportAddressHandover = {
    name = "DraftTCFGoodsTransportAddressHandover",
    idShort = "TCFGoodsTransportAddressHandover",
    semanticId = "irdi:0173-1#02-ABI498#001",
    description = "Structure to be reused for denoting addresses.",
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
      },
      AasField {
        name = "Latitude",
        semanticId = "irdi:0173-1#02-ABH960#001",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"40.757"},
        description = "Latitude (B), also called geodetic latitude or latitude (Latin latitudo, English latitude, international abbreviation Lat. or LAT), is the northerly or southerly distance of a point on the earth's surface from the  equator, given in angular measure in the unit of measurement degrees."
      },
      AasField {
        name = "Longitude",
        semanticId = "irdi:0173-1#02-ABH961#001",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"-73.986"},
        description = "Geographic longitude, also called longitude (Latin longitudo, English longitude, international abbreviation long or LON), describes one of the two coordinates of a location on the earth's surface, namely its position east or west of a defined (arbitrarily determined) north-south line, the prime meridian."
      }
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
