project IDTA_02014_FunctionalSafety {

  version v1.0;

  import AASDataTypes;

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasSubmodelType FsFunctionalSafety = {
    name = "FsFunctionalSafety",
    idShort = "FunctionalSafety",
    semanticId = "iri:https://admin-shell.io/idta/iec62683/1/0/FunctionalSafety",
    fields = {
      AasField {
        name = "NumberOfFunctionalSafetySetsOfCharacteristics",
        semanticId = "irdi:0112/2///62683#ACE005#001",
        type = refBy(IntegerType),
        description = "number of functional safety sets of characteristics."
      },
      AasField {
        name = "OperatingConditionsOfFunctionalSafetyCharacteristics",
        semanticId = "irdi:0112/2///62683#ACG057#001",
        type = refBy(OperatingConditionsOfFunctionalSafetyCharacteristics),
        description = "operating conditions of functional safety characteristics."
      },
      AasField {
        name = "SafetyDeviceTypes",
        semanticId = "irdi:0112/2///62683#ACG070#001",
        type = refBy(SafetyDeviceTypes),
        description = "safety device."
      }
    }
  };

  AasSubmodelElementCollectionType OperatingConditionsOfFunctionalSafetyCharacteristics = {
    name = "FsOperatingConditionsOfFunctionalSafetyCharacteristics",
    idShort = "OperatingConditionsOfFunctionalSafetyCharacteristics",
    semanticId = "irdi:0112/2///62683#ACG057#001",
    description = "operating conditions of functional safety characteristics.",
    fields = {
      AasField {
        name = "TypeOfVoltage",
        semanticId = "irdi:0112/2///61987#ABA969#007",
        type = refBy(StringType),
        description = "type of voltage."
      },
      AasField {
        name = "RatedVoltage",
        semanticId = "irdi:0112/2///61987#ABA588#004",
        type = refBy(DoubleType),
        description = "rated voltage."
      },
      AasField {
        name = "MinimumRatedVoltage",
        semanticId = "irdi:0112/2///61987#ABD461#004",
        type = refBy(DoubleType),
        description = "minimum rated voltage."
      },
      AasField {
        name = "MaximumRatedVoltage",
        semanticId = "irdi:0112/2///61987#ABD462#004",
        type = refBy(DoubleType),
        description = "maximum rated voltage."
      },
      AasField {
        name = "RatedOperationalCurrent",
        semanticId = "iri:https://admin-shell.io/idta/FunctionalSafety/RatedOperationalCurrent/1/0",
        type = refBy(DoubleType),
        description = "rated operational current."
      },
      AasField {
        name = "TypeOfInterlockingDevice",
        semanticId = "irdi:0112/2///62683#ACE053#001",
        type = refBy(StringType),
        description = "type of interlocking device."
      },
      AasField {
        name = "OtherOperatingConditions",
        semanticId = "irdi:0112/2///62683#ACE070#001",
        type = refBy(StringType),
        description = "other operating conditions."
      },
      AasField {
        name = "UsefulLifeInNumberOfOperations",
        semanticId = "irdi:0112/2///62683#ACE055#001",
        type = refBy(DoubleType),
        description = "useful life in number of operations."
      },
      AasField {
        name = "UsefulLifeInTimeInterval",
        semanticId = "irdi:0112/2///62683#ACE054#001",
        type = refBy(DoubleType),
        description = "useful life in time interval."
      }
    }
  };

  AasSubmodelElementCollectionType SafetyDeviceTypes = {
    name = "FsSafetyDeviceTypes",
    idShort = "SafetyDeviceTypes",
    semanticId = "irdi:0112/2///62683#ACG070#001",
    description = "safety device.",
    fields = {
      AasField {
        name = "FunctionalSafetyDeviceType",
        semanticId = "irdi:0112/2///62683#ACE071#001",
        type = refBy(StringType),
        description = "functional safety device type."
      },
      AasField {
        name = "SafetySubsystem",
        semanticId = "irdi:0112/2///62683#ACG065#001",
        type = refBy(SafetySubsystem),
        description = "Safety subsystem."
      },
      AasField {
        name = "ElectronicElement",
        semanticId = "irdi:0112/2///62683#ACG066#001",
        type = refBy(ElectronicElement),
        description = "Electronic element."
      },
      AasField {
        name = "ElectromechanicalElement",
        semanticId = "irdi:0112/2///62683#ACG067#001",
        type = refBy(ElectromechanicalElement),
        description = "Electromechanical element."
      },
      AasField {
        name = "InherentlySafeSubsystem",
        semanticId = "irdi:0112/2///62683#ACG069#001",
        type = refBy(InherentlySafeSubsystem),
        description = "Inherently safe subsystem."
      }
    }
  };

  AasSubmodelElementCollectionType SafetySubsystem = {
    name = "FsSafetySubsystem",
    idShort = "SafetySubsystem",
    semanticId = "irdi:0112/2///62683#ACG065#001",
    description = "Safety subsystem.",
    fields = {
      AasField {
        name = "SIL",
        semanticId = "irdi:0112/2///62683#ACE051#001",
        type = refBy(StringType),
        description = "safety integrity level."
      },
      AasField {
        name = "PFH",
        semanticId = "irdi:0112/2///62683#ACE052#001",
        type = refBy(DoubleType),
        description = "probability of dangerous failure within one hour."
      },
      AasField {
        name = "ProofTestInterval",
        semanticId = "irdi:0112/2///62683#ACE058#001",
        type = refBy(DoubleType),
        description = "proof test interval."
      },
      AasField {
        name = "PL",
        semanticId = "irdi:0112/2///62683#ACE060#001",
        type = refBy(StringType),
        description = "performance level."
      },
      AasField {
        name = "Category",
        semanticId = "irdi:0112/2///62683#ACE063#001",
        type = refBy(StringType),
        description = "category."
      }
    }
  };

  AasSubmodelElementCollectionType ElectronicElement = {
    name = "FsElectronicElement",
    idShort = "ElectronicElement",
    semanticId = "irdi:0112/2///62683#ACG066#001",
    description = "Electronic element.",
    fields = {
      AasField {
        name = "MTTFD",
        semanticId = "irdi:0112/2///62683#ACE057#001",
        type = refBy(IntegerType),
        description = "mean time to dangerous failure."
      },
      AasField {
        name = "RDF",
        semanticId = "iri:https://admin-shell.io/idta/FunctionalSafety/RDF/1/0",
        type = refBy(IntegerType),
        description = "ratio of dangerous failure."
      },
      AasField {
        name = "ProofTestInterval",
        semanticId = "irdi:0112/2///62683#ACE058#001",
        type = refBy(DoubleType),
        description = "proof test interval."
      }
    }
  };

  AasSubmodelElementCollectionType ElectromechanicalElement = {
    name = "FsElectromechanicalElement",
    idShort = "ElectromechanicalElement",
    semanticId = "irdi:0112/2///62683#ACG067#001",
    description = "Electromechanical element.",
    fields = {
      AasField {
        name = "B10D",
        semanticId = "irdi:0112/2///62683#ACE056#001",
        type = refBy(DoubleType),
        description = "number of operations until ten percent dangerous failure."
      },
      AasField {
        name = "ProofTestInterval",
        semanticId = "irdi:0112/2///62683#ACE058#001",
        type = refBy(DoubleType),
        description = "proof test interval."
      }
    }
  };

  AasSubmodelElementCollectionType InherentlySafeSubsystem = {
    name = "FsInherentlySafeSubsystem",
    idShort = "InherentlySafeSubsystem",
    semanticId = "irdi:0112/2///62683#ACG069#001",
    description = "Inherently safe subsystem.",
    fields = {
      AasField {
        name = "SIL",
        semanticId = "irdi:0112/2///62683#ACE051#001",
        type = refBy(StringType),
        description = "safety integrity level."
      },
      AasField {
        name = "ProofTestInterval",
        semanticId = "irdi:0112/2///62683#ACE058#001",
        type = refBy(DoubleType),
        description = "proof test interval."
      },
      AasField {
        name = "PL",
        semanticId = "irdi:0112/2///62683#ACE060#001",
        type = refBy(StringType),
        description = "performance level."
      },
      AasField {
        name = "Category",
        semanticId = "irdi:0112/2///62683#ACE063#001",
        type = refBy(StringType),
        description = "category."
      }
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
