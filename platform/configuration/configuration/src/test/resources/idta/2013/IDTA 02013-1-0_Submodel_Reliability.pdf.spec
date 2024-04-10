project IDTA_02013_Reliability {

  version v1.0;

  import AASDataTypes;

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasEnumType TypeOfVoltage = {
    name = "RyTypeOfVoltage",
    description = "classification of a power supply according to the time behaviour of the voltage.",
    versionIdentifier = "IDTA 02013-1-0",
    literals = {
      AasEnumLiteral {
        name = "AC",
        value = "AC",
        semanticId = "irdi:0112/2///61987#ABL837#001"
      },
      AasEnumLiteral {
        name = "DC",
        value = "DC",
        semanticId = "irdi:0112/2///61987#ABL838#001"
      },
      AasEnumLiteral {
        name = "others",
        value = "others",
        semanticId = "irdi:0112/2///61987#ABI407#004"
      }
    }
  };

  AasSubmodelType RyReliability = {
    name = "RyReliability",
    idShort = "Reliability",
    semanticId = "irdi:0112/2///62683#ACC008#001",
    description = "The Submodel defines a reliability data model for devices to be used by engineering tools for the design of safety related control systems according to IEC 62061, IEC 61508-2 or ISO 13849-1 or for dependability analysis of electrotechnical systems. This Submodel is used to facilitate the exchange between computers of data characterizing safety relevant devices in particular. The data models described in this document is based on the definition in the IEC/CDD 62683-1 DB.",
    versionIdentifier = "IDTA 02013-1-0",
    fields = {
      AasField {
        name = "NumberOfReliabilitySets",
        semanticId = "irdi:0112/2///62683#ACE006#001",
        type = refBy(IntegerType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"1"},
        description = "cardinality property for the number of sets of characteristics of a product for use in reliability assessment."
      },
      AasField {
        name = "OperatingConditionsOfReliabilityCharacteristics",
        semanticId = "irdi:0112/2///62683#ACG071#001",
        type = refBy(OperatingConditionsOfReliabilityCharacteristics),
        minimumInstances = 0,
        description = "operating condition limits for which the reliability characteristics are valid."
      },
      AasField {
        name = "ReliabilityCharacteristics",
        semanticId = "irdi:0112/2///62683#ACG080#001",
        type = refBy(ReliabilityCharacteristics),
        minimumInstances = 0,
        description = "characteristics of a subsystem or a subsystem element intended for evaluating its ability to perform as required, without failure, for a given time interval, under given conditions."
      }
    }
  };

  AasSubmodelElementCollectionType OperatingConditionsOfReliabilityCharacteristics = {
    name = "RyOperatingConditionsOfReliabilityCharacteristics",
    idShort = "OperatingConditionsOfReliabilityCharacteristics",
    semanticId = "irdi:0112/2///62683#ACG071#001",
    description = "This SubmodelElementCollection contains information on operating condition limits for which the reliability characteristics are valid.",
    versionIdentifier = "IDTA 02013-1-0",
    fields = {
      AasField {
        name = "TypeOfVoltage",
        semanticId = "irdi:0112/2///61987#ABA969#007",
        type = refBy(TypeOfVoltage),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"DC"},
        description = "classification of a power supply according to the time behaviour of the voltage."
      },
      AasField {
        name = "RatedVoltage",
        semanticId = "irdi:0112/2///61987#ABA588#004",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"24 [V]"},
        description = "operating voltage of the device as defined by the manufacturer and to which certain device properties are referenced."
      },
      AasField {
        name = "MinimumRatedVoltage",
        semanticId = "irdi:0112/2///61987#ABD461#004",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"15 [V]"},
        description = "lowest operating voltage of the device as defined by the manufacturer."
      },
      AasField {
        name = "MaximumRatedVoltage",
        semanticId = "irdi:0112/2///61987#ABD462#004",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"30 [V]"},
        description = "highest operating voltage of the device as defined by the manufacturer."
      },
      AasField {
        name = "RatedOperationalCurrent",
        semanticId = "iri:https://admin-shell.io/idta/Reliabliity/RatedOperationalCurrent/1/0",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"300 [mA]"},
        description = "current combined with a rated operational voltage intended to be switched by the device under specified conditions."
      },
      AasField {
        name = "OtherOperatingConditions",
        semanticId = "irdi:0112/2///62683#ACE070#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Duty in number of operations per hour, 50% of normal current"},
        description = "other limits of operation related to functional safety characteristics."
      },
      AasField {
        name = "UsefulLifeInNumberOfOperations",
        semanticId = "irdi:0112/2///62683#ACE055#001",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"50,000"},
        description = "under given conditions, the number of operations for which the failure rate becomes unacceptable."
      },
      AasField {
        name = "UsefulLifeInTimeInterval",
        semanticId = "irdi:0112/2///62683#ACE054#001",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"10 [y]"},
        description = "under given conditions, the time interval beginning at a given instant of time, and ending when the failure rate becomes unacceptable."
      }
    }
  };

  AasSubmodelElementCollectionType ReliabilityCharacteristics = {
    name = "RyReliabilityCharacteristics",
    idShort = "ReliabilityCharacteristics",
    semanticId = "irdi:0112/2///62683#ACG080#001",
    description = "This SubmodelElementCollection contains information on characteristics of a subsystem or a subsystem element intended for evaluating its ability to perform as required, without failure, for a given time interval, under given conditions.",
    versionIdentifier = "IDTA 02013-1-0",
    fields = {
      AasField {
        name = "MTTF",
        semanticId = "irdi:0112/2///62683#ACE061#001",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"[y]"},
        description = "mean operating time to failure: expectation of the operating time to failure."
      },
      AasField {
        name = "MTBF",
        semanticId = "irdi:0112/2///62683#ACE062#001",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"[y]"},
        description = "mean operating time between failure: expectation of the duration of the operating time between failures."
      },
      AasField {
        name = "B10",
        semanticId = "iri:https://admin-shell.io/idta/Reliabliit/B10/1/0",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"500000"},
        description = "mean number of cycles until 10% of the components fail."
      }
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
