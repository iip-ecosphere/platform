project IDTA_02013_Reliability {

  version v1.0;

  import AASDataTypes;

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasSubmodelType RyReliability = {
    name = "RyReliability",
    idShort = "Reliability",
    semanticId = "iri:https://admin-shell.io/idta/iec62683/1/0/Reliability",
    fields = {
      AasField {
        name = "NumberOfReliabilitySets",
        semanticId = "irdi:0112/2///62683#ACE006#001",
        type = refBy(IntegerType),
        description = "number of reliability sets of characteristics."
      },
      AasField {
        name = "OperatingConditionsOfReliabilityCharacteristics",
        semanticId = "irdi:0112/2///62683#ACG071#001",
        type = refBy(OperatingConditionsOfReliabilityCharacteristics),
        description = "operating conditions of reliability characteristics."
      },
      AasField {
        name = "ReliabilityCharacteristics",
        semanticId = "irdi:0112/2///62683#ACG080#001",
        type = refBy(ReliabilityCharacteristics),
        description = "Reliability characteristics."
      }
    }
  };

  AasSubmodelElementCollectionType OperatingConditionsOfReliabilityCharacteristics = {
    name = "RyOperatingConditionsOfReliabilityCharacteristics",
    idShort = "OperatingConditionsOfReliabilityCharacteristics",
    semanticId = "irdi:0112/2///62683#ACG071#001",
    description = "operating conditions of reliability characteristics.",
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

  AasSubmodelElementCollectionType ReliabilityCharacteristics = {
    name = "RyReliabilityCharacteristics",
    idShort = "ReliabilityCharacteristics",
    semanticId = "irdi:0112/2///62683#ACG080#001",
    description = "Reliability characteristics.",
    fields = {
      AasField {
        name = "MTTF",
        semanticId = "irdi:0112/2///62683#ACE061#001",
        type = refBy(IntegerType),
        description = "mean operating time to failure."
      },
      AasField {
        name = "MTBF",
        semanticId = "irdi:0112/2///62683#ACE062#001",
        type = refBy(IntegerType),
        description = "mean operating time between failure."
      },
      AasField {
        name = "B10",
        semanticId = "iri:https://admin-shell.io/idta/Reliability/B10/1/0",
        type = refBy(IntegerType),
        description = "mean number of cycles until 10% of the components fail."
      }
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
