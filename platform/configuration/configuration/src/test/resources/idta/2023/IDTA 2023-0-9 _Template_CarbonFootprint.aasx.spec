project IDTA_02023_CarbonFootprint {

  version v0.9;

  import AASDataTypes;

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasSubmodelType CarbonFootprint = {
    name = "CarbonFootprint",
    semanticId = "iri:https://admin-shell.io/idta/CarbonFootprint/CarbonFootprint/0/9",
    description = "The Submodel provides the means to access the Carbon Footprint of the asset.",
    fields = {
      AasField {
        name = "ProductCarbonFootprint",
        semanticId = "iri:https://admin-shell.io/idta/CarbonFootprint/ProductCarbonFootprint/0/9",
        type = refBy(ProductCarbonFootprint),
        minimumInstances = 0,
        description = "Balance of greenhouse gas emissions along the entire life cycle of a product in a defined application and in relation to a defined unit of use."
      },
      AasField {
        name = "TransportCarbonFootprint",
        semanticId = "iri:https://admin-shell.io/idta/CarbonFootprint/TransportCarbonFootprint/0/9",
        type = refBy(TransportCarbonFootprint),
        minimumInstances = 0,
        description = "Balance of greenhouse gas emissions generated by a transport service of a product."
      }
    }
  };

  AasSubmodelElementCollectionType ProductCarbonFootprint = {
    name = "ProductCarbonFootprint",
    semanticId = "iri:https://admin-shell.io/idta/CarbonFootprint/ProductCarbonFootprint/0/9",
    description = "Balance of greenhouse gas emissions along the entire life cycle of a product in a defined application and in relation to a defined unit of use.",
    fields = {
      AasField {
        name = "PCFCalculationMethod",
        semanticId = "irdi:0173-1#02-ABG854#001",
        type = refBy(StringListType),
        minimumInstances = 1,
        description = "Standard, method for determining the greenhouse gas emissions of a product."
      },
      AasField {
        name = "PCFCO2eq",
        semanticId = "irdi:0173-1#02-ABG855#001",
        type = refBy(DoubleType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Sum of all greenhouse gas emissions of a product according to the quantification requirements of the standard."
      },
      AasField {
        name = "PCFReferenceValueForCalculation",
        semanticId = "irdi:0173-1#02-ABG856#001",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Quantity unit of the product to which the PCF information on the CO2 footprint refers."
      },
      AasField {
        name = "PCFQuantityOfMeasureForCalculation",
        semanticId = "irdi:0173-1#02-ABG857#001",
        type = refBy(DoubleType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Quantity of the product to which the PCF information on the CO2 footprint refers."
      },
      AasField {
        name = "PCFLifeCyclePhase",
        semanticId = "irdi:0173-1#02-ABG858#001",
        type = refBy(StringListType),
        minimumInstances = 1,
        description = "Life cycle stages of the product according to the quantification requirements of the standard to which the PCF carbon footprint statement refers."
      },
      AasField {
        name = "ExplanatoryStatement",
        semanticId = "iri:https://admin-shell.io/idta/CarbonFootprint/ExplanatoryStatement/1/0",
        type = refBy(AasFileResourceType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Explanation which is needed or given so that a footprint communication can be properly understood by a purchaser, potential purchaser or user of the product."
      },
      AasField {
        name = "PCFGoodsAddressHandover",
        semanticId = "irdi:0173-1#02-ABI497#001",
        type = refBy(PCFGoodsAddressHandover),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Indicates the place of hand-over of the goods."
      },
      AasField {
        name = "PublicationDate",
        semanticId = "iri:https://admin-shell.io/idta/CarbonFootprint/PublicationDate/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Time at which something was first published or made available."
      },
      AasField {
        name = "ExpirationDate",
        semanticId = "iri:https://admin-shell.io/idta/CarbonFootprint/ExpirationnDate/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Time at which something should no longer be used effectively because it may lose its validity, quality or safety."
      }
    }
  };

  AasSubmodelElementCollectionType PCFGoodsAddressHandover = {
    name = "PCFGoodsAddressHandover",
    semanticId = "irdi:0173-1#02-ABI497#001",
    description = "Indicates the place of hand-over of the goods.",
    fields = {
      AasField {
        name = "Street",
        semanticId = "irdi:0173-1#02-ABH956#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Street indication of the place of transfer of goods."
      },
      AasField {
        name = "HouseNumber",
        semanticId = "irdi:0173-1#02-ABH957#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Number for identification or differentiation of individual houses of a street."
      },
      AasField {
        name = "ZipCode",
        semanticId = "irdi:0173-1#02-ABH958#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Zip code of the goods transfer address."
      },
      AasField {
        name = "CityTown",
        semanticId = "irdi:0173-1#02-ABH959#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Indication of the city or town of the transfer of goods."
      },
      AasField {
        name = "Country",
        semanticId = "irdi:0173-1#02-AAO259#005",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Country where the product is transmitted."
      }
    }
  };

  AasSubmodelElementCollectionType TransportCarbonFootprint = {
    name = "TransportCarbonFootprint",
    semanticId = "iri:https://admin-shell.io/idta/CarbonFootprint/TransportCarbonFootprint/0/9",
    description = "Balance of greenhouse gas emissions generated by a transport service of a product.",
    fields = {
      AasField {
        name = "TCFCalculationMethod",
        semanticId = "irdi:0173-1#02-ABG859#001",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Standard, method for determining the greenhouse gas emissions for the transport of a product."
      },
      AasField {
        name = "TCFCO2eq",
        semanticId = "irdi:0173-1#02-ABG860#001",
        type = refBy(DoubleType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Sum of all greenhouse gas emissions from vehicle operation."
      },
      AasField {
        name = "TCFReferenceValueForCalculation",
        semanticId = "irdi:0173-1#02-ABG861#001",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
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
        type = refBy(StringListType),
        minimumInstances = 1,
        description = "Processes in a transport service to determine the sum of all direct or indirect greenhouse gas emissions from fuel supply and vehicle operation."
      },
      AasField {
        name = "ExplanatoryStatement",
        semanticId = "iri:https://example.com/ids/cd/3291_7022_2032_0718",
        type = refBy(AasFileResourceType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Explanation which is needed or given so that a footprint communication can be properly understood by a purchaser, potential purchaser or user of the product."
      },
      AasField {
        name = "TCFGoodsTransportAddressTakeover",
        semanticId = "irdi:0173-1#02-ABI499#001",
        type = refBy(TCFGoodsTransportAddressTakeover),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Indication of the place of receipt of goods."
      },
      AasField {
        name = "TCFGoodsTransportAddressHandover",
        semanticId = "irdi:0173-1#02-ABI498#001",
        type = refBy(TCFGoodsTransportAddressHandover),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Indicates the hand-over address of the goods transport."
      },
      AasField {
        name = "PublicationDate",
        semanticId = "iri:https://admin-shell.io/idta/CarbonFootprint/PublicationDate/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Time at which something was first published or made available."
      },
      AasField {
        name = "ExpirationDate",
        semanticId = "iri:https://admin-shell.io/idta/CarbonFootprint/ExpirationnDate/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Time at which something should no longer be used effectively because it may lose its validity, quality or safety."
      }
    }
  };

  AasSubmodelElementCollectionType TCFGoodsTransportAddressTakeover = {
    name = "TCFGoodsTransportAddressTakeover",
    semanticId = "irdi:0173-1#02-ABI499#001",
    description = "Indication of the place of receipt of goods.",
    fields = {
      AasField {
        name = "Street",
        semanticId = "irdi:0173-1#02-ABH956#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Street indication of the place of transfer of goods."
      },
      AasField {
        name = "HouseNumber",
        semanticId = "irdi:0173-1#02-ABH957#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Number for identification or differentiation of individual houses of a street."
      },
      AasField {
        name = "ZipCode",
        semanticId = "irdi:0173-1#02-ABH958#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Zip code of the goods transfer address."
      },
      AasField {
        name = "CityTown",
        semanticId = "irdi:0173-1#02-ABH959#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Indication of the city or town of the transfer of goods."
      },
      AasField {
        name = "Country",
        semanticId = "irdi:0173-1#02-AAO259#005",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Country where the product is transmitted."
      }
    }
  };

  AasSubmodelElementCollectionType TCFGoodsTransportAddressHandover = {
    name = "TCFGoodsTransportAddressHandover",
    semanticId = "irdi:0173-1#02-ABI498#001",
    description = "Indicates the hand-over address of the goods transport.",
    fields = {
      AasField {
        name = "Street",
        semanticId = "irdi:0173-1#02-ABH956#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Street indication of the place of transfer of goods."
      },
      AasField {
        name = "HouseNumber",
        semanticId = "irdi:0173-1#02-ABH957#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Number for identification or differentiation of individual houses of a street."
      },
      AasField {
        name = "ZipCode",
        semanticId = "irdi:0173-1#02-ABH958#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Zip code of the goods transfer address."
      },
      AasField {
        name = "CityTown",
        semanticId = "irdi:0173-1#02-ABH959#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Indication of the city or town of the transfer of goods."
      },
      AasField {
        name = "Country",
        semanticId = "irdi:0173-1#02-AAO259#005",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Country where the product is transmitted."
      }
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
