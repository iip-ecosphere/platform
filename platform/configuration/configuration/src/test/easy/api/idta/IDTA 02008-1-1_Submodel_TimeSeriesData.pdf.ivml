project IDTA_02008_TimeSeries {

  version v1.1;

  import AASDataTypes;

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasEnumType State = {
    name = "State",
    description = "State of the time series related to its progress.",
    versionIdentifier = "IDTA 02008-1-1",
    literals = {
      AasEnumLiteral {
        name = "in progress",
        semanticId = "iri:https://admin- shell.io/idta/TimeSeries/Segment/State/InProgress/1/1"
      },
      AasEnumLiteral {
        name = "completed",
        semanticId = "iri:https://admin- shell.io/idta/TimeSeries/Segment/State/Completed/1/1"
      }
    }
  };

  AasSubmodelType TimeSeries = {
    name = "TimeSeries",
    semanticId = "iri:https://admin-shell.io/idta/TimeSeries/1/1",
    description = "Contains time series data and references to time series data to discover and semantically describe them along the asset lifecycle.",
    versionIdentifier = "IDTA 02008-1-1",
    fixedName = true,
    fields = {
      AasField {
        name = "Metadata",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Metadata/1/1",
        type = refBy(Metadata),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "A set of data describing and providing information about the time series."
      },
      AasField {
        name = "Segments",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segments/1/1",
        type = refBy(Segments),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Contains segments of a time series allowDuplicates = true."
      }
    }
  };

  AasSubmodelElementCollectionType Metadata = {
    name = "Metadata",
    semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Metadata/1/1",
    description = "A set of data describing and providing information about the time series.",
    versionIdentifier = "IDTA 02008-1-1",
    fixedName = true,
    fields = {
      AasField {
        name = "Name",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Metadata/Name/1/1",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"IO-Link Sensor Data@en","IO-Link Sensordaten@de"},
        description = "Meaningful name for labeling."
      },
      AasField {
        name = "Description",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Metadata/Description/1/1",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"IO-Link process data of the sensor@en","IO-Link Prozessdaten des Sensors@de"},
        description = "Short description of the time series."
      },
      AasField {
        name = "Record",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Record/1/1",
        type = refBy(Record),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "A time series record is unique by its ID within the time series and contains the timestamps and variable values referenced to the ID ordered = true allowDuplicated = true."
      }
    }
  };

  AasSubmodelElementCollectionType Record = {
    name = "Record",
    semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Record/1/1",
    description = "A time series record is unique by its ID within the time series and contains the timestamps and variable values referenced to the ID. Similar to a row in a table.",
    versionIdentifier = "IDTA 02008-1-1",
    fields = {
      AasField {
        name = "Time",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/UtcTime/1/1",
        multiSemanticIds = true,
        counting = true,
        type = refBy(StringListType),
        minimumInstances = 1,
        description = "Time."
      },
      AasField {
        name = "Variable",
        displayName = "{Variable}",
        isGeneric = true,
        type = refBy(StringListType),
        minimumInstances = 0,
        description = "A suitable semantic ID should be used to select the appropriate variable definitions."
      }
    }
  };

  AasSubmodelElementCollectionType Segments = {
    name = "Segments",
    semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segments/1/1",
    description = "Contains segments of a time series.",
    versionIdentifier = "IDTA 02008-1-1",
    fixedName = true,
    fields = {
      AasField {
        name = "ExternalSegment",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segments/ExternalSegment/1/1",
        counting = true,
        type = refBy(ExternalSegment),
        minimumInstances = 0,
        description = "Reference to a file of data points in sequential order over a period of time."
      },
      AasField {
        name = "LinkedSegment",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segments/LinkedSegment/1/1",
        counting = true,
        type = refBy(LinkedSegment),
        minimumInstances = 0,
        description = "Reference to an endpoint of data points in sequential order over a period of time."
      },
      AasField {
        name = "InternalSegment",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segments/InternalSegment/1/1",
        counting = true,
        type = refBy(InternalSegment),
        minimumInstances = 0,
        description = "Grouped sequence of data points in sequential order over a specified period of time."
      }
    }
  };

  AasSubmodelElementCollectionType InternalSegment = {
    name = "InternalSegment",
    semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segments/InternalSegment/1/1",
    description = "Grouped sequence of data points in successive order over a specified period of time.",
    versionIdentifier = "IDTA 02008-1-1",
    fields = {
      AasField {
        name = "Name",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segment/Name/1/1",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Regular operation@en","Normalbetrieb@de"},
        description = "Meaningful name for labeling."
      },
      AasField {
        name = "Description",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segment/Description/1/1",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"IO-Link process data of the sensor during a normal motion@en","IO-Link Prozessdaten des Sensors während einer Normalfahrt@de"},
        description = "Short description of the time series segment."
      },
      AasField {
        name = "RecordCount",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segment/RecordCount/1/1",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"13134"},
        description = "Indicates how many records are present in a segment."
      },
      AasField {
        name = "StartTime",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segment/StartTime/1/1",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2020-09-19T14:40:38.318"},
        description = "Contains the first recorded timestamp of the time series segment or its start time if it is a qualitative time series. Time format and scale corresponds to that of the time series."
      },
      AasField {
        name = "EndTime",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segment/EndTime/1/1",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2020-09-19T14:40:38.318"},
        description = "Contains the last recorded timestamp of the time series segment or its end. Time format and scale corresponds to that of the time series."
      },
      AasField {
        name = "Duration",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segment/Duration/1/1",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"P3DT4H59M","(according to ISO 8601)","or in case of other seamantic id","[long]","14 [s]"},
        description = "Period covered by the segment, represented according to ISO 8601 by the format P[n]Y[n]M[n]DT[n]H[n]M[n]S."
      },
      AasField {
        name = "SamplingInterval",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segment/SamplingInterval/1/1",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1 s"},
        description = "The time period between two time series records (Length of cycle)."
      },
      AasField {
        name = "SamplingRate",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segment/SamplingRate/1/1",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"3200 Hz"},
        description = "Defines the number of samples per second for a regular time series in Hz."
      },
      AasField {
        name = "State",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segment/State/1/1",
        type = refBy(State),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"completed"},
        description = "State of the time series related to its progress."
      },
      AasField {
        name = "LastUpdate",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segment/LastUpdate/1/1",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2020-09-19T14:40:38.318"},
        description = "Time of the last chance."
      },
      AasField {
        name = "Records",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Records/1/1",
        type = refBy(Records),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Group of data points in successive order over a specified period of time ordered = true allowDuplicates = false."
      }
    }
  };

  AasSubmodelElementCollectionType ExternalSegment = {
    name = "ExternalSegment",
    semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segments/ExternalSegment/1/1",
    description = "Reference to a file of data points in sequential order over a period of time.",
    versionIdentifier = "IDTA 02008-1-1",
    fields = {
      AasField {
        name = "Name",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segment/Name/1/1",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Regular operation@en","Normalbetrieb@de"},
        description = "Meaningful name for labeling."
      },
      AasField {
        name = "Description",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segment/Description/1/1",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"IO-Link process data of the sensor during a normal motion@en","IO-Link Prozessdaten des Sensors während einer Normalfahrt@de"},
        description = "Short description of the time series segment."
      },
      AasField {
        name = "RecordCount",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segment/RecordCount/1/1",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"13134"},
        description = "Indicates how many records are present in a segment."
      },
      AasField {
        name = "StartTime",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segment/StartTime/1/1",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2020-09-19T14:40:38.318"},
        description = "Contains the first recorded timestamp of the time series segment or its start time if it is a qualitative time series. Time format and scale corresponds to that of the time series."
      },
      AasField {
        name = "EndTime",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segment/EndTime/1/1",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2020-09-19T14:40:38.318"},
        description = "Contains the last recorded timestamp of the time series segment or its end. Time format and scale corresponds to that of the time series."
      },
      AasField {
        name = "Duration",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segment/Duration/1/1",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"P3DT4H59M","(according to ISO 8601)","or in case of other seamantic id","[long]","14 [s]"},
        description = "Period covered by the segment, represented according to ISO 8601 by the format P[n]Y[n]M[n]DT[n]H[n]M[n]S."
      },
      AasField {
        name = "SamplingInterval",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segment/SamplingInterval/1/1",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1 s"},
        description = "The time period between two time series records (Length of cycle)."
      },
      AasField {
        name = "SamplingRate",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segment/SamplingRate/1/1",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"3200 Hz"},
        description = "Defines the number of samples per second for a regular time series in Hz."
      },
      AasField {
        name = "State",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segment/State/1/1",
        type = refBy(State),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"completed"},
        description = "State of the time series related to its progress."
      },
      AasField {
        name = "LastUpdate",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segment/LastUpdate/1/1",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2020-09-19T14:40:38.318"},
        description = "Time of the last chance."
      },
      AasField {
        name = "File",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/File/1/1",
        type = refBy(AasFileResourceType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Sequence of data points in sequential order over a period of time within a paged data file."
      },
      AasField {
        name = "Blob",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Blob/1/1",
        type = refBy(AasBlobType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Sequence of data points in sequential order over a period of time within a BLOB."
      }
    }
  };

  AasSubmodelElementCollectionType LinkedSegment = {
    name = "LinkedSegment",
    semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segments/LinkedSegment/1/1",
    description = "Reference to an endpoint of data points in sequential order over a period of time.",
    versionIdentifier = "IDTA 02008-1-1",
    fields = {
      AasField {
        name = "Name",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segment/Name/1/1",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Regular operation@en","Normalbetrieb@de"},
        description = "Meaningful name for labeling."
      },
      AasField {
        name = "Description",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segment/Description/1/1",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"IO-Link process data of the sensor during a normal motion@en","IO-Link Prozessdaten des Sensors während einer Normalfahrt@de"},
        description = "Short description of the time series segment."
      },
      AasField {
        name = "RecordCount",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segment/RecordCount/1/1",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"13134"},
        description = "Indicates how many records are present in a segment."
      },
      AasField {
        name = "StartTime",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segment/StartTime/1/1",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2020-09-19T14:40:38.318"},
        description = "Contains the first recorded timestamp of the time series segment or its start time if it is a qualitative time series. Time format and scale corresponds to that of the time series."
      },
      AasField {
        name = "EndTime",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segment/EndTime/1/1",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2020-09-19T14:40:38.318"},
        description = "Contains the last recorded timestamp of the time series segment or its end. Time format and scale corresponds to that of the time series."
      },
      AasField {
        name = "Duration",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segment/Duration/1/1",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"P3DT4H59M","(According to ISO 8601)","or in case of other seamantic id","[long]","14 [s]"},
        description = "Period covered by the segment, represented according to ISO 8601 by the format P[n]Y[n]M[n]DT[n]H[n]M[n]S."
      },
      AasField {
        name = "SamplingInterval",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segment/SamplingInterval/1/1",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1 s"},
        description = "The time period between two time series records (Length of cycle)."
      },
      AasField {
        name = "SamplingRate",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segment/SamplingRate/1/1",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"3200 Hz"},
        description = "Defines the number of samples per second for a regular time series in Hz."
      },
      AasField {
        name = "State",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segment/State/1/1",
        type = refBy(State),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"completed"},
        description = "State of the time series related to its progress."
      },
      AasField {
        name = "LastUpdate",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Segment/LastUpdate/1/1",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2020-09-19T14:40:38.318"},
        description = "Time of the last chance."
      },
      AasField {
        name = "Endpoint",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Endpoint/1/1",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Specifies a location of a resource on an API server through which time series can be requested."
      },
      AasField {
        name = "Query",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Query/1/1",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "generic query component to read time series data from an API."
      }
    }
  };

  AasSubmodelElementCollectionType Records = {
    name = "Records",
    semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Records/1/1",
    description = "Group of data points in successive order over a specified period of time.",
    versionIdentifier = "IDTA 02008-1-1",
    fixedName = true,
    fields = {
      AasField {
        name = "Record",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/Record/1/1",
        type = refBy(Record),
        minimumInstances = 0,
        description = "Time series record."
      }
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
