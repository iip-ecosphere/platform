project IDTA_02045_AssetLocation {

  version v1.0;

  import AASDataTypes;

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasSubmodelType AssetLocation = {
    name = "AssetLocation",
    semanticId = "iri:https://admin-shell.io/idta/smt/assetlocation/1/0",
    description = "Submodel for tracking & tracing of the location of an asset.",
    versionIdentifier = "IDTA 02045-1-0",
    fields = {
      AasField {
        name = "Addresses",
        semanticId = "iri:https://admin-shell.io/idta/sml/addresses/1/0",
        type = refBy(Addresses),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "orderRelevant: true typeValueListElement: SubmodelElementCollection semanticIdListElement: 0173-1#01-ADR442#007 List with postal addresses where an object has been located."
      },
      AasField {
        name = "CoordinateSystems",
        semanticId = "iri:https://admin-shell.io/idta/sml/coordinatesystems/1/0",
        type = refBy(CoordinateSystems),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "orderRelevant: false typeValueListElement: SubmodelElementCollection semanticIdListElement: https://admin- shell.io/idta/smc/coordinatesystemsrecord/1/0 List with information about different coordinate systems that have been used to determine the location of an asset."
      },
      AasField {
        name = "VisitedAreas",
        semanticId = "iri:https://admin-shell.io/idta/sml/visitedareas/1/0",
        type = refBy(VisitedAreas),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "orderRelevant: true typeValueListElement: SubmodelElementCollection semanticIdListElement: https://admin- shell.io/idta/smc/visitedareasrecord/1/0 List with areas (e.g., sites, buildings, field warehouses) where an asset has been located or is located."
      },
      AasField {
        name = "AreaRelations",
        semanticId = "iri:https://admin-shell.io/idta/sml/arearelations/1/0",
        type = refBy(AasGenericSubmodelElementCollection),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "orderRelevant: false typeValueListElement: RelationshipElement semanticIdListElement: https://admin- shell.io/idta/rel/islocatedin/1/0."
      },
      AasField {
        name = "AssetTraces",
        semanticId = "iri:https://admin-shell.io/idta/smc/assettraces/1/0",
        type = refBy(AssetTraces),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Collection of localization event records for sites, areas, fences and locations."
      },
      AasField {
        name = "AssetLocatingInformation",
        semanticId = "iri:https://admin-shell.io/idta/sml/assetlocatinginformation/1/0",
        type = refBy(AssetLocatingInformation),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Collection with additional information concerning the localization of an asset."
      }
    }
  };

  AasSubmodelElementListType Addresses = {
    name = "Addresses",
    semanticId = "iri:https://admin-shell.io/idta/sml/addresses/1/0",
    description = "List with postal addresses where an object has been located.",
    versionIdentifier = "IDTA 02045-1-0",
    fields = {
      AasField {
        name = "Adress",
        semanticId = "irdi:0173-1#01-ADR442#007",
        type = refBy(StringListType),
        minimumInstances = 0,
        examples = {"-- SubmodelElementCollection"},
        description = "Order not relevant."
      }
    }
  };

  AasSubmodelElementCollectionType Address = {
    name = "Address",
    semanticId = "irdi:0173-1#01-ADR442#007",
    description = "Postal addresses where an object has been located.",
    versionIdentifier = "IDTA 02045-1-0",
    fields = {
      AasField {
        name = "AddressLine1",
        semanticId = "irdi:0173-1#02-AAO124#004",
        type = refBy(StringListType),
        minimumInstances = 0,
        description = "description: address line 1."
      },
      AasField {
        name = "AddressLine2",
        semanticId = "irdi:0173-1#02-AAO125#004",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "description: address line 2."
      },
      AasField {
        name = "AddressLine3",
        semanticId = "irdi:0173-1#02-AAO126#004",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "description: address line 3."
      },
      AasField {
        name = "AddressOfAdditionalLink",
        semanticId = "irdi:0173-1#02-AAQ326#003",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "description: address of additional link."
      },
      AasField {
        name = "AddressRemarks",
        semanticId = "irdi:0173-1#02-AAO202#004",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "description: address remarks."
      },
      AasField {
        name = "NationalCode",
        semanticId = "irdi:0173-1#02-AAO134#003",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "description: national code."
      },
      AasField {
        name = "StateCounty",
        semanticId = "irdi:0173-1#02-AAO133#003",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "description: state/county."
      },
      AasField {
        name = "CityTown",
        semanticId = "irdi:0173-1#02-AAO132#003",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "description: city/town."
      },
      AasField {
        name = "Street",
        semanticId = "irdi:0173-1#02-AAO128#003",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "description: street."
      }
    }
  };

  AasSubmodelElementListType CoordinateSystems = {
    name = "CoordinateSystems",
    semanticId = "iri:https://admin-shell.io/idta/sml/coordinatesystems/1/0",
    description = "List with information about different coordinate systems that have been used to determine the location of an asset.",
    versionIdentifier = "IDTA 02045-1-0",
    fields = {
      AasField {
        name = "CoordinateSystemsRecord",
        semanticId = "iri:https://admin-shell.io/idta/smc/coordinatesystemsrecord/1/0",
        type = refBy(StringListType),
        minimumInstances = 0,
        examples = {"-- SubmodelElementCollection"},
        description = "Order not relevant."
      }
    }
  };

  AasSubmodelElementCollectionType CoordinateSystemsRecord = {
    name = "CoordinateSystemsRecord",
    semanticId = "iri:https://admin-shell.io/idta/smc/coordinatesystemsrecord/1/0",
    description = "Coordinate reference system (CRS) record.",
    versionIdentifier = "IDTA 02045-1-0",
    fields = {
      AasField {
        name = "CoordinateSystemName",
        semanticId = "iri:https://admin-shell.io/idta/prop/coordinatesystemname/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "coordinate system name."
      },
      AasField {
        name = "CoordinateSystemId",
        semanticId = "iri:https://admin-shell.io/idta/prop/coordinatesystemid/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Identification of a coordinate system."
      },
      AasField {
        name = "CoordinateSystemType",
        semanticId = "iri:https://admin-shell.io/idta/prop/coordinatesystemtype/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"EPSG:4326 LOCAL"},
        description = "Type of a coordinate system with the allowed enumeration values 'EPSG:4326' or 'LOCAL'."
      },
      AasField {
        name = "ElevationReference",
        semanticId = "iri:https://admin-shell.io/idta/prop/elevationreference/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Reference of the elevation information in a coordinate system; with the allowed enumeration values 'SEALEVEL' or 'LOCAL'."
      },
      AasField {
        name = "SeaLevelOfBaseHeight",
        semanticId = "iri:https://admin-shell.io/idta/prop/sealevelofbaseheight/1/0",
        type = refBy(FloatType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"105.50 m"},
        description = "Sea level of the base height of a coordinate system; normally the base height is at the origin of the coordinate system with Z=0,00 m."
      },
      AasField {
        name = "GroundControlPoints",
        semanticId = "iri:https://admin-shell.io/idta/sml/groundcontrolpoints/1/0",
        type = refBy(GroundControlPoints),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "An array containing a mapping between geographic coordinates (longitude, latitude) in WGS84 (EPSG:4326) and relative coordinates (x,y)."
      }
    }
  };

  AasSubmodelElementListType GroundControlPoints = {
    name = "GroundControlPoints",
    semanticId = "iri:https://admin-shell.io/idta/sml/groundcontrolpoints/1/0",
    description = "Arrays containing a mapping between geographic coordinates (longitude, latitude) in WGS84 (EPSG:4326) and relative coordinates (x,y).",
    versionIdentifier = "IDTA 02045-1-0",
    fields = {
      AasField {
        name = "GroundControlPointsEntry",
        semanticId = "iri:https://admin-shell.io/idta/smc/groundcontrolpointsentry/1/0",
        type = refBy(StringListType),
        minimumInstances = 0,
        examples = {"-- SubmodelElementCollection"},
        description = "Order not relevant."
      }
    }
  };

  AasSubmodelElementCollectionType GroundControlPointsEntry = {
    name = "GroundControlPointsEntry",
    semanticId = "iri:https://admin-shell.io/idta/smc/groundcontrolpointsentry/1/0",
    description = "An array containing a mapping between geographic coordinates (longitude, latitude) in WGS84 (EPSG:4326) and relative coordinates (x,y).",
    versionIdentifier = "IDTA 02045-1-0",
    fields = {
      AasField {
        name = "GeographicCoordinates",
        semanticId = "irdi:0173-1#02-ABH934#002",
        type = refBy(GeographicCoordinates),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Indication of the position of a point on the earth's surface."
      },
      AasField {
        name = "RelativeCoordinates",
        semanticId = "irdi:0173-1#02-ABG741#001",
        type = refBy(RelativeCoordinates),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "defined value of the location related to the zero point of the coordinate system."
      }
    }
  };

  AasSubmodelElementCollectionType GeographicCoordinates = {
    name = "GeographicCoordinates",
    semanticId = "irdi:0173-1#02-ABH934#002",
    description = "Indication of the position of a point on the earth's surface.",
    versionIdentifier = "IDTA 02045-1-0",
    fields = {
      AasField {
        name = "Longitude",
        semanticId = "irdi:0173-1#02-ABH961#002",
        type = refBy(DoubleType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"13.413215"},
        description = "Geographic longitude, also called longitude (Latin longitudo, English longitude, international abbreviation long or LON), describes one of the two coordinates of a location on the earth's surface, namely its position east or west of a defined (arbitrarily determined) north-south line, the prime meridian."
      },
      AasField {
        name = "Latitude",
        semanticId = "irdi:0173-1#02-ABH960#002",
        type = refBy(DoubleType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"52.521918"},
        description = "Latitude (B), also called geodetic latitude or latitude (Latin latitudo, English latitude, international abbreviation Lat. or LAT), is the northerly or southerly distance of a point on the earth's surface from the equator, given in angular measure in the unit of measurement degrees."
      }
    }
  };

  AasSubmodelElementCollectionType RelativeCoordinates = {
    name = "RelativeCoordinates",
    semanticId = "irdi:0173-1#02-ABG741#001",
    description = "defined value of the location related to the zero point of the coordinate system.",
    versionIdentifier = "IDTA 02045-1-0",
    fields = {
      AasField {
        name = "X",
        semanticId = "iri:https://admin-shell.io/idta/prop/x/1/0",
        type = refBy(FloatType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"115.10 m"},
        description = "X-coordinate value within a coordinate system."
      },
      AasField {
        name = "Y",
        semanticId = "iri:https://admin-shell.io/idta/prop/y/1/0",
        type = refBy(FloatType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"45.00 m"},
        description = "Y-coordinate value within a coordinate system."
      }
    }
  };

  AasSubmodelElementListType VisitedAreas = {
    name = "VisitedAreas",
    semanticId = "iri:https://admin-shell.io/idta/sml/visitedareas/1/0",
    description = "List with areas (e.g., sites, buildings, field warehouses) where an asset has been located or is located.",
    versionIdentifier = "IDTA 02045-1-0",
    fields = {
      AasField {
        name = "VisitedAreasRecord",
        semanticId = "iri:https://admin-shell.io/idta/smc/visitedareasrecord/1/0",
        type = refBy(StringListType),
        minimumInstances = 0,
        examples = {"-- SubmodelElementCollection"},
        description = "Order not relevant."
      }
    }
  };

  AasSubmodelElementCollectionType VisitedAreasRecord = {
    name = "VisitedAreasRecord",
    semanticId = "iri:https://admin-shell.io/idta/smc/visitedareasrecord/1/0",
    description = "Areas (e.g., site, building, field warehouse) where an asset has been located or is located.",
    versionIdentifier = "IDTA 02045-1-0",
    fields = {
      AasField {
        name = "CoordinateSystemOfArea",
        semanticId = "iri:https://admin-shell.io/idta/ref/coordinatesystemreference/1/0",
        type = refBy(AasReferenceType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Reference to a local coordinate reference system for an area."
      },
      AasField {
        name = "AreaRegionCoordinates",
        semanticId = "iri:https://admin-shell.io/idta/sml/regioncoordinates/1/0",
        type = refBy(AreaRegionCoordinates),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Coordinates forming a polygon that describes the area within the coordinate reference system of the area."
      },
      AasField {
        name = "AddressReferences",
        semanticId = "iri:https://admin-shell.io/idta/sml/addressreferences/1/0",
        type = refBy(AasGenericSubmodelElementCollection),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "List with references to addresses for the area (area addresses)."
      },
      AasField {
        name = "KindOfArea",
        semanticId = "iri:https://admin-shell.io/idta/prop/kindofarea/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"BUILDING"},
        description = "Kind of the area, the enumeration 'AREA_NOT_SPECIFIED', 'BUILDING' and 'SITE' should be used."
      },
      AasField {
        name = "AreaName",
        semanticId = "iri:https://admin-shell.io/idta/prop/areaname/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Außenlager Signale"},
        description = "Name of the area or building."
      },
      AasField {
        name = "AreaId",
        semanticId = "iri:https://admin-shell.io/idta/prop/areaid/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"ALSig"},
        description = "Identification of an area."
      },
      AasField {
        name = "AreaDesciption",
        semanticId = "iri:https://admin-shell.io/idta/mlp/areadescription/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Einbruchgesicherter Bereich@de"},
        description = "Description of an area."
      },
      AasField {
        name = "AreaLayout",
        semanticId = "iri:https://admin-shell.io/idta/file/arealayout/1/0",
        type = refBy(AasFileResourceType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "File with a layout (map) of the area (e.g., hall plan)."
      },
      AasField {
        name = "BuildingLevel",
        semanticId = "irdi:0173-1#02-ABJ094#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1.5 EG"},
        description = "Number/designation of the floor."
      }
    }
  };

  AasSubmodelElementListType AreaRegionCoordinates = {
    name = "AreaRegionCoordinates",
    semanticId = "iri:https://admin-shell.io/idta/sml/regioncoordinates/1/0",
    description = "Coordinates forming a polygon that describe the region of the area within the coordinate reference system of the area.",
    versionIdentifier = "IDTA 02045-1-0",
    fields = {
      AasField {
        name = "RegionCoordinateEntry",
        semanticId = "iri:https://admin-shell.io/idta/smc/regioncoordinateentry/1/0",
        type = refBy(StringListType),
        minimumInstances = 0,
        examples = {"-- SubmodelElementCollection"},
        description = "Order not relevant."
      }
    }
  };

  AasSubmodelElementCollectionType RegionCoordinateEntry = {
    name = "RegionCoordinateEntry",
    semanticId = "iri:https://admin-shell.io/idta/smc/regioncoordinateentry/1/0",
    description = "One coordinate of coordinates forming a polygon that describes the region of the area within the coordinate reference system of the area.",
    versionIdentifier = "IDTA 02045-1-0",
    fields = {
      AasField {
        name = "X",
        semanticId = "iri:https://admin-shell.io/idta/prop/x/1/0",
        type = refBy(FloatType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"115.10 m"},
        description = "X-coordinate."
      },
      AasField {
        name = "Y",
        semanticId = "iri:https://admin-shell.io/idta/prop/y/1/0",
        type = refBy(FloatType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"45.00 m"},
        description = "Y-coordinate."
      }
    }
  };

  AasSubmodelElementCollectionType AssetTraces = {
    name = "AssetTraces",
    semanticId = "iri:https://admin-shell.io/idta/smc/assettraces/1/0",
    description = "Collection of localization event records for sites, areas, fences and locations.",
    versionIdentifier = "IDTA 02045-1-0",
    fields = {
      AasField {
        name = "AreaEventTimeSeriesData",
        semanticId = "iri:https://admin-shell.io/idta/ref/eventtimeseriesdata/1/0",
        type = refBy(AasReferenceType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Reference to an AAS time series data Submodel instance of the same AAS with AreaRecords."
      },
      AasField {
        name = "LocationEventTimeSeriesData",
        semanticId = "iri:https://admin-shell.io/idta/ref/eventtimeseriesdata/1/0",
        type = refBy(AasReferenceType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Reference to an AAS time series data Submodel instance of the same AAS with LocationRecords."
      },
      AasField {
        name = "AreaRecords",
        semanticId = "iri:https://admin-shell.io/idta/sml/arearecords/1/0",
        type = refBy(AreaRecords),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "orderRelevant: true typeValueListElement: SubmodelElementCollection semanticIdListElement: https://admin- shell.io/idta/smc/arearecordsrecord/1/0 List with records for area localization events."
      },
      AasField {
        name = "LocationRecords",
        semanticId = "iri:https://admin-shell.io/idta/sml/locationrecords/1/0",
        type = refBy(LocationRecords),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "orderRelevant: true typeValueListElement: SubmodelElementCollection semanticIdListElement: https://admin- shell.io/idta/smc/locationrecordsrecord/1/0 List with records for location (position) localization events."
      }
    }
  };

  AasSubmodelElementListType AreaRecords = {
    name = "AreaRecords",
    semanticId = "iri:https://admin-shell.io/idta/sml/arearecords/1/0",
    description = "List with records for area localization events.",
    versionIdentifier = "IDTA 02045-1-0",
    fields = {
      AasField {
        name = "AreaRecordsRecord",
        semanticId = "iri:https://admin-shell.io/idta/smc/arearecordsrecord/1/0",
        type = refBy(StringListType),
        minimumInstances = 0,
        examples = {"-- SubmodelElementCollection"},
        description = "Order relevant."
      }
    }
  };

  AasSubmodelElementCollectionType AreaRecordsRecord = {
    name = "AreaRecordsRecord",
    semanticId = "iri:https://admin-shell.io/idta/smc/arearecordsrecord/1/0",
    description = "Record of an area localization event.",
    versionIdentifier = "IDTA 02045-1-0",
    fields = {
      AasField {
        name = "AreaRef",
        semanticId = "iri:https://admin-shell.io/idta/ref/areareference/1/0",
        type = refBy(AasReferenceType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Reference to the area where the event has been recorded for."
      },
      AasField {
        name = "Time",
        semanticId = "irdi:0173-1#02-ABF198#002",
        type = refBy(DateTimeType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Time when the event occurred."
      },
      AasField {
        name = "EventId",
        semanticId = "iri:https://admin-shell.io/idta/prop/eventid/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Identification of an event."
      },
      AasField {
        name = "ProviderId",
        semanticId = "iri:https://admin-shell.io/idta/prop/providerid/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Identification of the location provider which triggered the event."
      },
      AasField {
        name = "EventType",
        semanticId = "iri:https://admin-shell.io/idta/prop/eventtype/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"REGION_ENTRY REGION_EXIT"},
        description = "Type of an event that is triggered when an asset is located at a localization fence."
      }
    }
  };

  AasSubmodelElementListType LocationRecords = {
    name = "LocationRecords",
    semanticId = "iri:https://admin-shell.io/idta/sml/locationrecords/1/0",
    description = "List with records for location (position) localization events.",
    versionIdentifier = "IDTA 02045-1-0",
    fields = {
      AasField {
        name = "LocationRecordsRecord",
        semanticId = "iri:https://admin-shell.io/idta/smc/locationrecordsrecord/1/0",
        type = refBy(StringListType),
        minimumInstances = 0,
        examples = {"-- SubmodelElementCollection"},
        description = "Order relevant."
      }
    }
  };

  AasSubmodelElementCollectionType LocationRecordsRecord = {
    name = "LocationRecordsRecord",
    semanticId = "iri:https://admin-shell.io/idta/smc/locationrecordsrecord/1/0",
    description = "Record of a location (position) localization event.",
    versionIdentifier = "IDTA 02045-1-0",
    fields = {
      AasField {
        name = "CoordinateSystemReference",
        semanticId = "iri:https://admin-shell.io/idta/ref/coordinatesystemreference/1/0",
        type = refBy(AasReferenceType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Reference to a coordinate reference system for the position."
      },
      AasField {
        name = "Position",
        semanticId = "irdi:0173-1#02-ABI783#001",
        type = refBy(Position),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Position of the asset."
      },
      AasField {
        name = "ProviderId",
        semanticId = "iri:https://admin-shell.io/idta/prop/providerid/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Identification of the location provider which triggered the event."
      },
      AasField {
        name = "ProviderType",
        semanticId = "iri:https://admin-shell.io/idta/prop/providertype/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Type of the location information provider, e.g. 'UWB tag'."
      },
      AasField {
        name = "Accuracy",
        semanticId = "iri:https://admin-shell.io/idta/prop/accuracy/1/0",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0.1 m"},
        description = "The horizontal accuracy of the position data."
      },
      AasField {
        name = "MagneticHeading",
        semanticId = "iri:https://admin-shell.io/idta/prop/magneticheading/1/0",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"30°"},
        description = "The magnetic heading direction of the Asset."
      },
      AasField {
        name = "TrueHeading",
        semanticId = "iri:https://admin-shell.io/idta/prop/trueheading/1/0",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"31°"},
        description = "The corrected magnetic heading direction of the Asset."
      },
      AasField {
        name = "HeadingAccuracy",
        semanticId = "iri:https://admin-shell.io/idta/prop/headingaccuracy/1/0",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2°"},
        description = "The maximum deviation between the reported magnetic heading and the true heading."
      },
      AasField {
        name = "Time",
        semanticId = "irdi:0173-1#02-ABF198#002",
        type = refBy(DateTimeType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Time when the event occurred."
      },
      AasField {
        name = "TransmissionTime",
        semanticId = "iri:https://admin-shell.io/idta/prop/transmissiontime/1/0",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Time (timestamp) when the location information has been updated."
      },
      AasField {
        name = "LocationDescription",
        semanticId = "iri:https://admin-shell.io/idta/mlp/locationdescription/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Location description (meta information for the position), it is recommended to refer to the origin of the CRS."
      },
      AasField {
        name = "Speed",
        semanticId = "irdi:0173-1#02-AAV544#004",
        type = refBy(FloatType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0.1 m/s"},
        description = "Operating speed."
      },
      AasField {
        name = "Course",
        semanticId = "iri:https://admin-shell.io/idta/prop/course/1/0",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"45°"},
        description = "The current course ('compass direction') where the asset is heading to."
      },
      AasField {
        name = "ReferencePointId",
        semanticId = "iri:https://admin-shell.io/idta/prop/referencepointid/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"X23"},
        description = "Identificator of a reference point at the Asset for which the position has been submitted."
      }
    }
  };

  AasSubmodelElementCollectionType Position = {
    name = "Position",
    semanticId = "irdi:0173-1#02-ABI783#001",
    description = "Position of an asset.",
    versionIdentifier = "IDTA 02045-1-0",
    fields = {
      AasField {
        name = "X",
        semanticId = "iri:https://admin-shell.io/idta/prop/x/1/0",
        type = refBy(FloatType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"103.234 m"},
        description = "X-coordinate value within a coordinate system."
      },
      AasField {
        name = "Y",
        semanticId = "iri:https://admin-shell.io/idta/prop/y/1/0",
        type = refBy(FloatType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"103.234 m"},
        description = "Y-coordinate value within a coordinate system."
      },
      AasField {
        name = "Z",
        semanticId = "iri:https://admin-shell.io/idta/prop/z/1/0",
        type = refBy(FloatType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"103.234 m"},
        description = "Z-coordinate (height) value within a coordinate system."
      }
    }
  };

  AasSubmodelElementCollectionType AssetLocatingInformation = {
    name = "AssetLocatingInformation",
    semanticId = "iri:https://admin-shell.io/idta/sml/assetlocatinginformation/1/0",
    description = "Collection with additional information concerning the localization of an asset.",
    versionIdentifier = "IDTA 02045-1-0",
    fields = {
      AasField {
        name = "Localizable",
        semanticId = "iri:https://admin-shell.io/idta/prop/localizable/1/0",
        type = refBy(BooleanType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Information whether the position can be currently updated with the correct position."
      },
      AasField {
        name = "RealtimeCapabilityOfAAS",
        semanticId = "iri:https://admin-shell.io/idta/mlp/realtimecapabilityofaas/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Description of the extend and conditions for real time applications of the AAS."
      },
      AasField {
        name = "RealtimeLocationSourceType",
        semanticId = "iri:https://admin-shell.io/idta/prop/realtimelocationsourcetype/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Type or name of the source that delivers real time information for the asset's location, e.g., OMLOX."
      },
      AasField {
        name = "RealtimeLocationSource",
        semanticId = "iri:https://admin-shell.io/idta/mlp/realtimelocationsource/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Information regarding a source for real time location data, e.g., URL and API documentation for DeepHub."
      }
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
