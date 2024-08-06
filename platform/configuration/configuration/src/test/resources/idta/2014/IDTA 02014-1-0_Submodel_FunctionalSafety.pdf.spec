project IDTA_02014_FunctionalSafety {

  version v1.0;

  import AASDataTypes;

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasEnumType TypeOfVoltage = {
    name = "FsTypeOfVoltage",
    description = "classification of a power supply according to the time behaviour of the voltage.",
    versionIdentifier = "IDTA 02014-1-0",
    semanticId = "irdi:0112/2///61987#ABA969#007",
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

  AasEnumType TypeOfInterlockingDevice = {
    name = "FsTypeOfInterlockingDevice",
    description = "classification of device which prevent the hazardous operation of machine, depending on the technology of their actuating means and their output system.",
    versionIdentifier = "IDTA 02014-1-0",
    semanticId = "irdi:0112/2///62683#ACE053#001",
    literals = {
      AasEnumLiteral {
        name = "TYPE1",
        value = "type 1",
        semanticId = "irdi:0112/2///62683#ACH673#001"
      },
      AasEnumLiteral {
        name = "TYPE2",
        value = "type 2",
        semanticId = "irdi:0112/2///62683#ACH674#001"
      },
      AasEnumLiteral {
        name = "TYPE3",
        value = "type 3",
        semanticId = "irdi:0112/2///62683#ACH675#001"
      },
      AasEnumLiteral {
        name = "TYPE4",
        value = "type 4",
        semanticId = "irdi:0112/2///62683#ACH676#001"
      }
    }
  };

  AasEnumType FunctionalSafetyDeviceType = {
    name = "FsFunctionalSafetyDeviceType",
    description = "classification of device depending on their safety related characteristics and their capability as subsystem or subsystem element.",
    versionIdentifier = "IDTA 02014-1-0",
    semanticId = "irdi:0112/2///62683#ACE071#001",
    literals = {
      AasEnumLiteral {
        name = "SUBST",
        value = "safety subsystem",
        semanticId = "irdi:0112/2///62683#ACH687#001"
      },
      AasEnumLiteral {
        name = "ELECTROEL",
        value = "electronic element",
        semanticId = "irdi:0112/2///62683#ACH688#001"
      },
      AasEnumLiteral {
        name = "ELECMECEL",
        value = "electromechanical element",
        semanticId = "irdi:0112/2///62683#ACH689#001"
      },
      AasEnumLiteral {
        name = "INTSUBST",
        value = "inherently safe subsystem",
        semanticId = "irdi:0112/2///62683#ACH690#001"
      }
    }
  };

  AasEnumType SIL = {
    name = "FsSIL",
    description = "safety integrity level: discrete level (one out of a possible three) for describing the capability to perform a safety function where safety integrity level three has the highest level of safety integrity and safety integrity level one has the lowest.",
    versionIdentifier = "IDTA 02014-1-0",
    semanticId = "irdi:0112/2///62683#ACE051#001",
    literals = {
      AasEnumLiteral {
        name = "SIL1",
        value = "SIL 1",
        semanticId = "irdi:0112/2///62683#ACH670#001"
      },
      AasEnumLiteral {
        name = "SIL2",
        value = "SIL 2",
        semanticId = "irdi:0112/2///62683#ACH671#001"
      },
      AasEnumLiteral {
        name = "SIL3",
        value = "SIL 3",
        semanticId = "irdi:0112/2///62683#ACH672#001"
      }
    }
  };

  AasEnumType PL = {
    name = "FsPL",
    description = "performance level: discrete level used to specify the ability of safety-related parts of control systems to perform a safety function under foreseeable conditions.",
    versionIdentifier = "IDTA 02014-1-0",
    semanticId = "irdi:0112/2///62683#ACE060#001",
    literals = {
      AasEnumLiteral {
        name = "PLA",
        value = "PL a",
        semanticId = "irdi:0112/2///62683#ACH677#001"
      },
      AasEnumLiteral {
        name = "PLB",
        value = "PL b",
        semanticId = "irdi:0112/2///62683#ACH678#001"
      },
      AasEnumLiteral {
        name = "PLC",
        value = "PL c",
        semanticId = "irdi:0112/2///62683#ACH679#001"
      },
      AasEnumLiteral {
        name = "PLD",
        value = "PL d",
        semanticId = "irdi:0112/2///62683#ACH680#001"
      },
      AasEnumLiteral {
        name = "PLE",
        value = "PL e",
        semanticId = "irdi:0112/2///62683#ACH681#001"
      }
    }
  };

  AasEnumType Category = {
    name = "FsCategory",
    description = "classification of the safety-related parts of a control system in respect of their resistance to faults and their subsequent behaviour in the fault condition, and which is achieved by the structural arrangement of the parts, fault detection and/or by their reliability.",
    versionIdentifier = "IDTA 02014-1-0",
    semanticId = "irdi:0112/2///62683#ACE063#001",
    literals = {
      AasEnumLiteral {
        name = "CATB",
        value = "category B",
        semanticId = "irdi:0112/2///62683#ACH682#001"
      },
      AasEnumLiteral {
        name = "CAT1",
        value = "category 1",
        semanticId = "irdi:0112/2///62683#ACH683#001"
      },
      AasEnumLiteral {
        name = "CAT2",
        value = "category 2",
        semanticId = "irdi:0112/2///62683#ACH684#001"
      },
      AasEnumLiteral {
        name = "CAT3",
        value = "category 3",
        semanticId = "irdi:0112/2///62683#ACH685#001"
      },
      AasEnumLiteral {
        name = "CAT4",
        value = "category 4",
        semanticId = "irdi:0112/2///62683#ACH686#001"
      }
    }
  };

  AasSubmodelType FsFunctionalSafety = {
    name = "FsFunctionalSafety",
    idShort = "FunctionalSafety",
    semanticId = "irdi:0112/2///62683#ACC007#001",
    description = "The Submodel defines a functional safety data model for devices to be used by engineering tools for the design of safety related control systems according to IEC 62061, IEC 61508-2 or ISO 13849-1 or for dependability analysis of electrotechnical systems. This Submodel is used to facilitate the exchange between computers of data characterizing safety relevant devices in particular. The data models described in this document is based on the definition in the IEC/CDD 62683-1 DB.",
    versionIdentifier = "IDTA 02014-1-0",
    fields = {
      AasField {
        name = "NumberOfFunctionalSafetySets",
        semanticId = "irdi:0112/2///62683#ACE005#001",
        type = refBy(IntegerType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"1"},
        description = "cardinality property for the number of sets of characteristics of a product for use in functional safety assessment."
      },
      AasField {
        name = "OperatingConditionsOfFunctionalSafetyCharacteristics",
        semanticId = "irdi:0112/2///62683#ACG057#001",
        type = refBy(OperatingConditionsOfFunctionalSafetyCharacteristics),
        minimumInstances = 0,
        description = "operating condition limits for which the functional safety characteristics are valid."
      },
      AasField {
        name = "SafetyDeviceTypes",
        semanticId = "irdi:0112/2///62683#ACG070#001",
        type = refBy(SafetyDeviceTypes),
        minimumInstances = 0,
        description = "selected device type depending on its safety related characteristics and its capability as subsystem or subsystem element."
      }
    }
  };

  AasSubmodelElementCollectionType OperatingConditionsOfFunctionalSafetyCharacteristics = {
    name = "FsOperatingConditionsOfFunctionalSafetyCharacteristics",
    idShort = "OperatingConditionsOfFunctionalSafetyCharacteristics",
    semanticId = "irdi:0112/2///62683#ACG057#001",
    description = "This SubmodelElementCollection contains information on operating condition limits for which the functional safety characteristics are valid.",
    versionIdentifier = "IDTA 02014-1-0",
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
        semanticId = "iri:https://admin-shell.io/idta/FunctionalSafety/RatedOperationalCurrent/1/0",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"300 [mA]"},
        description = "current combined with a rated operational voltage intended to be switched by the device under specified conditions."
      },
      AasField {
        name = "TypeOfInterlockingDevice",
        semanticId = "irdi:0112/2///62683#ACE053#001",
        type = refBy(TypeOfInterlockingDevice),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"TYPE1"},
        description = "classification of device which prevent the hazardous operation of machine, depending on the technology of their actuating means and their output system."
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

  AasSubmodelElementCollectionType SafetyDeviceTypes = {
    name = "FsSafetyDeviceTypes",
    idShort = "SafetyDeviceTypes",
    semanticId = "irdi:0112/2///62683#ACG070#001",
    description = "This SubmodelElementCollection contains information on the selected device type depending on its safety related characteristics and its capability as subsystem or subsystem element.",
    versionIdentifier = "IDTA 02014-1-0",
    fields = {
      AasField {
        name = "FunctionalSafetyDeviceType",
        semanticId = "irdi:0112/2///62683#ACE071#001",
        type = refBy(FunctionalSafetyDeviceType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"INTSUBST"},
        description = "classification of device depending on their safety related characteristics and their capability as subsystem or subsystem element."
      },
      AasField {
        name = "SafetySubsystem",
        semanticId = "irdi:0112/2///62683#ACG065#001",
        type = refBy(SafetySubsystem),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "entity of the top-level architectural design of a safety-related system where a dangerous failure of the subsystem results in dangerous failure of a safety function."
      },
      AasField {
        name = "ElectronicElement",
        semanticId = "irdi:0112/2///62683#ACG066#001",
        type = refBy(ElectronicElement),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "selected device type depending on its safety related characteristics and its capability as subsystem or subsystem element."
      },
      AasField {
        name = "ElectromechanicalElement",
        semanticId = "irdi:0112/2///62683#ACG067#001",
        type = refBy(ElectromechanicalElement),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "electromechanical element subject to wearing provided with functional safety characteristics."
      },
      AasField {
        name = "InherentlySafeSubsystem",
        semanticId = "irdi:0112/2///62683#ACG069#001",
        type = refBy(InherentlySafeSubsystem),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "subsystem without dangerous failure mode."
      }
    }
  };

  AasSubmodelElementCollectionType SafetySubsystem = {
    name = "FsSafetySubsystem",
    idShort = "SafetySubsystem",
    semanticId = "irdi:0112/2///62683#ACG065#001",
    description = "This SubmodelElementCollection contains information on safety subsystems, the entity of the top-level architectural design of a safety-related system where a dangerous failure of the subsystem results in dangerous failure of a safety function.",
    versionIdentifier = "IDTA 02014-1-0",
    fields = {
      AasField {
        name = "SIL",
        semanticId = "irdi:0112/2///62683#ACE051#001",
        type = refBy(SIL),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"SIL1"},
        description = "safety integrity level: discrete level (one out of a possible three) for describing the capability to perform a safety function where safety integrity level three has the highest level of safety integrity and safety integrity level one has the lowest."
      },
      AasField {
        name = "PFH",
        semanticId = "irdi:0112/2///62683#ACE052#001",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1.0 x 10-8 [1/h]"},
        description = "probability of dangerous failure within one hour: average frequency of dangerous failure of an SCS to perform a specified safety function over a given period of time Note 1: Both terms PFH and PFHD correspond to the probability of dangerous failures per hour. Note 2: The term “average probability of dangerous failure per hour” is not used in this edition anymore but the acronym PFH has been retained but when it is used it means “average frequency of dangerous failure [h]'."
      },
      AasField {
        name = "ProofTestInterval",
        semanticId = "irdi:0112/2///62683#ACE058#001",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1.0 [y]"},
        description = "time interval between test performed to detect dangerous hidden failures in a safety-related system NOTE The assumption is made that the useful lifetime corresponds to the mission time and proof test interval."
      },
      AasField {
        name = "PL",
        semanticId = "irdi:0112/2///62683#ACE060#001",
        type = refBy(PL),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"PLA"},
        description = "performance level: discrete level used to specify the ability of safety-related parts of control systems to perform a safety function under foreseeable conditions."
      },
      AasField {
        name = "Category",
        semanticId = "irdi:0112/2///62683#ACE063#001",
        type = refBy(Category),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"CAT1"},
        description = "classification of the safety-related parts of a control system in respect of their resistance to faults and their subsequent behaviour in the fault condition, and which is achieved by the structural arrangement of the parts, fault detection and/or by their reliability."
      }
    }
  };

  AasSubmodelElementCollectionType ElectronicElement = {
    name = "FsElectronicElement",
    idShort = "ElectronicElement",
    semanticId = "irdi:0112/2///62683#ACG066#001",
    description = "This SubmodelElementCollection contains information on electronic elements, elements of electronic technology non evaluated according to a functional safety standard, provided with reliability data and which needs to be integrated specifically into a subsystem.",
    versionIdentifier = "IDTA 02014-1-0",
    fields = {
      AasField {
        name = "MTTFD",
        semanticId = "irdi:0112/2///62683#ACE057#001",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"10 [y]"},
        description = "mean time to dangerous failure: expectation of the mean time to dangerous failure."
      },
      AasField {
        name = "RDF",
        semanticId = "iri:https://admin-shell.io/idta/FunctionalSafety/RDF/1/0",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"50 [%]"},
        description = "ratio of the overall failure rate of a device that can lead to a dangerous failure of the safety function."
      },
      AasField {
        name = "ProofTestInterval",
        semanticId = "irdi:0112/2///62683#ACE058#001",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1.0 [y]"},
        description = "time interval between test performed to detect dangerous hidden failures in a safety-related system NOTE The assumption is made that the useful lifetime corresponds to the mission time and proof test interval."
      }
    }
  };

  AasSubmodelElementCollectionType ElectromechanicalElement = {
    name = "FsElectromechanicalElement",
    idShort = "ElectromechanicalElement",
    semanticId = "irdi:0112/2///62683#ACG067#001",
    description = "This SubmodelElementCollection contains information on electromechanical elements, electromechanical elements subject to wearing provided with functional safety characteristics.",
    versionIdentifier = "IDTA 02014-1-0",
    fields = {
      AasField {
        name = "B10D",
        semanticId = "irdi:0112/2///62683#ACE056#001",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2.0 x 10"},
        description = "number of operations until ten percent dangerous failure: mean number of operating cycles at which ten percent of the components fail dangerously."
      },
      AasField {
        name = "ProofTestInterval",
        semanticId = "irdi:0112/2///62683#ACE058#001",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1.0 [y]"},
        description = "time interval between test performed to detect dangerous hidden failures in a safety-related system NOTE The assumption is made that the useful lifetime corresponds to the mission time and proof test interval."
      }
    }
  };

  AasSubmodelElementCollectionType InherentlySafeSubsystem = {
    name = "FsInherentlySafeSubsystem",
    idShort = "InherentlySafeSubsystem",
    semanticId = "irdi:0112/2///62683#ACG069#001",
    description = "This SubmodelElementCollection contains information on inherently safe subsystems, subsystem without dangerous failure mode.",
    versionIdentifier = "IDTA 02014-1-0",
    fields = {
      AasField {
        name = "SIL",
        semanticId = "irdi:0112/2///62683#ACE051#001",
        type = refBy(SIL),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"SIL1"},
        description = "safety integrity level: discrete level (one out of a possible three) for describing the capability to perform a safety function where safety integrity level three has the highest level of safety integrity and safety integrity level one has the lowest."
      },
      AasField {
        name = "ProofTestInterval",
        semanticId = "irdi:0112/2///62683#ACE058#001",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1.0 [y]"},
        description = "time interval between test performed to detect dangerous hidden failures in a safety-related system NOTE The assumption is made that the useful lifetime corresponds to the mission time and proof test interval."
      },
      AasField {
        name = "PL",
        semanticId = "irdi:0112/2///62683#ACE060#001",
        type = refBy(PL),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"PLA"},
        description = "performance level: discrete level used to specify the ability of safety-related parts of control systems to perform a safety function under foreseeable conditions."
      },
      AasField {
        name = "Category",
        semanticId = "irdi:0112/2///62683#ACE063#001",
        type = refBy(Category),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"CAT1"},
        description = "classification of the safety-related parts of a control system in respect of their resistance to faults and their subsequent behaviour in the fault condition, and which is achieved by the structural arrangement of the parts, fault detection and/or by their reliability."
      }
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
