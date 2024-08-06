project IDTA_02046_WorkstationWorkerMatchingData {

  version v1.0;

  import AASDataTypes;

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasEnumType DemandKind = {
    name = "DemandKind",
    description = "Kind of the demand, defined by the.",
    versionIdentifier = "IDTA 02046-1-0",
    semanticId = "iri:https://admin-shell.io/idta/prop/demandkind/1/0",
    literals = {
      AasEnumLiteral {
        name = "ad-hoc"
      },
      AasEnumLiteral {
        name = "general"
      },
      AasEnumLiteral {
        name = "orderDepending"
      }
    }
  };

  AasSubmodelType WorkstationWorkerMatchingData = {
    name = "WorkstationWorkerMatchingData",
    semanticId = "iri:https://admin-shell.io/idta/sm/workstationworkermatchingdata/1/0",
    description = "Submodel containing workstation data in order to match and deploy workers to workstations.",
    versionIdentifier = "IDTA 02046-1-0",
    fields = {
      AasField {
        name = "GeneralWorkstationData",
        semanticId = "iri:https://admin-shell.io/idta/smc/generalworkstationdata/1/0",
        type = refBy(GeneralWorkstationData),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "General workstation data, which are relevant for worker deployment control and deployment planning."
      },
      AasField {
        name = "ErgonomicWorkstationProfile",
        semanticId = "iri:https://admin-shell.io/idta/smc/ergonomicworkstationprofile/1/0",
        type = refBy(ErgonomicWorkstationProfile),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Ergonomic characteristics of the workstation which might influence the worker deployment."
      },
      AasField {
        name = "WorkstationConfigurationRecords",
        semanticId = "iri:https://admin-shell.io/idta/sml/workstationconfigurationrecords/1/0",
        type = refBy(WorkstationConfigurationRecords),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "List with worker specific configuration options of a workstation."
      },
      AasField {
        name = "PlannedQualificationDemand",
        semanticId = "iri:https://admin-shell.io/idta/sml/plannedqualificationdemand/1/0",
        type = refBy(PlannedQualificationDemand),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Production plan depending planned qualification demand at a workstation."
      },
      AasField {
        name = "PlannedSkillDemand",
        semanticId = "iri:https://admin-shell.io/idta/sml/plannedskilldemand/1/0",
        type = refBy(PlannedSkillDemand),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Production plan depending planned skill demand at a workstation."
      }
    }
  };

  AasSubmodelElementCollectionType GeneralWorkstationData = {
    name = "GeneralWorkstationData",
    semanticId = "iri:https://admin-shell.io/idta/smc/generalworkstationdata/1/0",
    description = "General workstation data, which are relevant for worker deployment control and deployment planning.",
    versionIdentifier = "IDTA 02046-1-0",
    fields = {
      AasField {
        name = "WorkstationInformation",
        semanticId = "iri:https://admin-shell.io/idta/smc/workstationinformation/1/0",
        type = refBy(WorkstationInformation),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "General information about the workstation in respect of worker deployment."
      },
      AasField {
        name = "GeneralQualificationDemand",
        semanticId = "iri:https://admin-shell.io/idta/sml/generalqualificationdemand/1/0",
        type = refBy(GeneralQualificationDemand),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Ergonomic characteristics of the workstation which might influence the worker deployment."
      },
      AasField {
        name = "GeneralSkillDemand",
        semanticId = "iri:https://admin-shell.io/idta/sml/generalskilldemand/1/0",
        type = refBy(GeneralSkillDemand),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Worker skills that are required to work at the workstation."
      }
    }
  };

  AasSubmodelElementCollectionType WorkstationInformation = {
    name = "WorkstationInformation",
    semanticId = "iri:https://admin-shell.io/idta/smc/workstationinformation/1/0",
    description = "General information about the workstation in respect of worker deployment.",
    versionIdentifier = "IDTA 02046-1-0",
    fields = {
      AasField {
        name = "WorkstationName",
        semanticId = "iri:https://admin-shell.io/idta/prop/workstationname/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"milling machine"},
        description = "Name of a workstation according to IEC 62264 defined “work unit”."
      },
      AasField {
        name = "WorkstationId",
        semanticId = "iri:https://admin-shell.io/idta/prop/workstationid/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"mil0123"},
        description = "Identification of the workstation."
      },
      AasField {
        name = "OrgName",
        semanticId = "iri:https://admin-shell.io/idta/mlp/orgname/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"mechanical workshop@en","Mechanische Werkstatt@de"},
        description = "Organizational name."
      },
      AasField {
        name = "TypeOfWorkstation",
        semanticId = "iri:https://admin-shell.io/idta/mlp/typeofworkstation/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"testing station@en"},
        description = "Type of the workstation, e.g, cable assembly station."
      },
      AasField {
        name = "WorkerAssistanceInformation",
        semanticId = "iri:https://admin-shell.io/idta/mlp/workerassistanceeinformation/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"digital step-by-step instructions with final visual quality control, that is supported by computer vision@en"},
        description = "Information about the kind and degree of implemented worker assistance at the workstation."
      },
      AasField {
        name = "RequiredPersonalSafetyEquipment",
        semanticId = "iri:https://admin-shell.io/idta/mlp/requiredpersonalsafetyequipment/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Safety helmet@en"},
        description = "Required or recommended personal safety equipment and gear at a workstation, e.g., noise protection."
      },
      AasField {
        name = "NecessaryPersonalTools",
        semanticId = "iri:https://admin-https://admin-shell.io/idta/mlp/necessarypersonaltools/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"caliper gauge@en"},
        description = "Necessary personal tools to be brought with by the worker to the workstation."
      },
      AasField {
        name = "PersonalDataProcessing",
        semanticId = "iri:https://admin-shell.io/idta/mlp/personaldataprocessing/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"employee ID is recorded when starting a work order@en"},
        description = "Description how personal data are captured and processed at the workstation."
      },
      AasField {
        name = "LocationDescription",
        semanticId = "iri:https://admin-shell.io/idta/mlp/locationdescription/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Hall A, Bay 1@en"},
        description = "Description of the location of the workstation."
      },
      AasField {
        name = "Directions",
        semanticId = "iri:https://admin-shell.io/idta/mlp/directions/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"From the entry move straight forward 100m@en"},
        description = "Descriptions how to get from certain locations to a workstation."
      }
    }
  };

  AasSubmodelElementListType GeneralQualificationDemand = {
    name = "GeneralQualificationDemand",
    semanticId = "iri:https://admin-shell.io/idta/sml/generalqualificationdemand/1/0",
    description = "Worker qualifications that are required to work at the workstation.",
    versionIdentifier = "IDTA 02046-1-0",
    fields = {
      AasField {
        name = "GeneralQualificationDemandRecord",
        semanticId = "iri:https://admin-shell.io/idta/smc/generalqualificationdemandrecord/1/0",
        type = refBy(StringListType),
        minimumInstances = 0,
        examples = {"-- SubmodelElementCollection"},
        description = "Order not relevant."
      }
    }
  };

  AasSubmodelElementCollectionType GeneralQualificationDemandRecord = {
    name = "GeneralQualificationDemandRecord",
    semanticId = "iri:https://admin-shell.io/idta/smc/generalqualificationdemandrecord/1/0",
    description = "Worker qualification that is required to work at the workstation.",
    versionIdentifier = "IDTA 02046-1-0",
    fields = {
      AasField {
        name = "QualificationClassificationId",
        semanticId = "iri:https://admin-shell.io/idta/prop/qualificationclassificationid/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Identification of the classification system where the qualification is classified."
      },
      AasField {
        name = "QualificationId",
        semanticId = "iri:https://admin-shell.io/idta/prop/qualificationid/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Indentification of the qualification."
      },
      AasField {
        name = "ExceptionRules",
        semanticId = "iri:https://admin-shell.io/idta/mlp/exceptionrules/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Exceptions rules that define possible deviations when the required qualification or skill is not available."
      }
    }
  };

  AasSubmodelElementListType GeneralSkillDemand = {
    name = "GeneralSkillDemand",
    semanticId = "iri:https://admin-shell.io/idta/sml/generalskilldemand/1/0",
    description = "Worker skills that are required in order to work at the workstation.",
    versionIdentifier = "IDTA 02046-1-0",
    fields = {
      AasField {
        name = "GeneralSkillDemandRecord",
        semanticId = "iri:https://admin-shell.io/idta/smc/generalskilldemandrecord/1/0",
        type = refBy(StringListType),
        minimumInstances = 0,
        examples = {"-- SubmodelElementCollection"},
        description = "Order not relevant."
      }
    }
  };

  AasSubmodelElementCollectionType GeneralSkillDemandRecord = {
    name = "GeneralSkillDemandRecord",
    semanticId = "iri:https://admin-shell.io/idta/smc/generalskilldemandrecord/1/0",
    description = "Worker skill that is required in order to work at the workstation.",
    versionIdentifier = "IDTA 02046-1-0",
    fields = {
      AasField {
        name = "SkillClassificationId",
        semanticId = "iri:https://admin-shell.io/idta/prop/skillclassificationid/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Identification of the classification system where the skill is classified."
      },
      AasField {
        name = "SkillLevelClassificationId",
        semanticId = "iri:https://admin-shell.io/idta/prop/skilllevelclassificationid/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "definition: Identification of the classification system where the skill level is classified, e.g., EQR level 1-8."
      },
      AasField {
        name = "SkillLevelId",
        semanticId = "iri:https://admin-shell.io/idta/prop/skilllevelid/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Identification of the skill level for a worker."
      },
      AasField {
        name = "SkillId",
        semanticId = "iri:https://admin-shell.io/idta/prop/skillid/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Identification of the skill for a worker."
      },
      AasField {
        name = "ExceptionRules",
        semanticId = "iri:https://admin-shell.io/idta/mlp/exceptionrules/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Exceptions rules that define possible deviations when the required qualification or skill is not available."
      }
    }
  };

  AasSubmodelElementCollectionType ErgonomicWorkstationProfile = {
    name = "ErgonomicWorkstationProfile",
    semanticId = "iri:https://admin-shell.io/idta/smc/ergonomicworkstationprofile/1/0",
    description = "Ergonomic characteristics of the workstation which might influence the worker deployment.",
    versionIdentifier = "IDTA 02046-1-0",
    fields = {
      AasField {
        name = "MaxLiftingWeight",
        semanticId = "iri:https://admin-shell.io/idta/prop/maxliftingweight/1/0",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"16 kg"},
        description = "Maximum weight the worker must lift at the workstation."
      },
      AasField {
        name = "MinWorkerHeight",
        semanticId = "iri:https://admin-shell.io/idta/prop/minworkerheight/1/0",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"170 cm"},
        description = "Minimal height of the worker to perform all operations at the workstation."
      },
      AasField {
        name = "AllowedPersonalLimitations",
        semanticId = "iri:https://admin-shell.io/idta/sml/allowedpersonallimitations/1/0",
        type = refBy(AllowedPersonalLimitations),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "List of all personal limitations that are accepted for working at the workstation, e.g., special measures have been implemented."
      }
    }
  };

  AasSubmodelElementListType AllowedPersonalLimitations = {
    name = "AllowedPersonalLimitations",
    semanticId = "iri:https://admin-shell.io/idta/sml/allowedpersonallimitations/1/0",
    description = "Personal limitations that are accepted for working at the workstation, e.g., special measures have been implemented.",
    versionIdentifier = "IDTA 02046-1-0",
    fields = {
      AasField {
        name = "AllowedPersonalLimitationsRecord",
        semanticId = "iri:https://admin-shell.io/idta/smc/allowedpersonallimitationsrecord/1/0",
        type = refBy(StringListType),
        minimumInstances = 0,
        examples = {"-- SubmodelElementCollection"},
        description = "Order not relevant."
      }
    }
  };

  AasSubmodelElementCollectionType AllowedPersonalLimitationsRecord = {
    name = "AllowedPersonalLimitationsRecord",
    semanticId = "iri:https://admin-shell.io/idta/sml/allowedpersonallimitationsrecord/1/0",
    description = "Personal limitation that is accepted for working at the workstation, e.g., special measures have been implemented.",
    versionIdentifier = "IDTA 02046-1-0",
    fields = {
      AasField {
        name = "LimitationClassificationId",
        semanticId = "iri:https://admin-shell.io/idta/prop/limitationclassificationid/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Identification of a classification system for worker limitations."
      },
      AasField {
        name = "PersonalLimitationId",
        semanticId = "iri:https://admin-shell.io/idta/prop/personallimitationid/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "and definition: Identification of a personal limitation an employee has."
      }
    }
  };

  AasSubmodelElementListType WorkstationConfigurationRecords = {
    name = "WorkstationConfigurationRecords",
    semanticId = "iri:https://admin-shell.io/idta/sml/workstationconfigurationrecords/1/0",
    description = "Worker specific configuration options of a workstation.",
    versionIdentifier = "IDTA 02046-1-0",
    fields = {
      AasField {
        name = "WorkstationConfigurationRecord",
        semanticId = "iri:https://admin-shell.io/idta/smc/workstationconfigurationrecord/1/0",
        type = refBy(StringListType),
        minimumInstances = 0,
        examples = {"-- SubmodelElementCollection"},
        description = "Order not relevant."
      }
    }
  };

  AasSubmodelElementCollectionType WorkstationConfigurationRecord = {
    name = "WorkstationConfigurationRecord",
    semanticId = "iri:https://admin-shell.io/idta/sml/workstationconfigurationrecord/1/0",
    description = "Worker specific configuration options of a workstation.",
    versionIdentifier = "IDTA 02046-1-0",
    fields = {
      AasField {
        name = "WorkerId",
        semanticId = "iri:https://admin-shell.io/idta/prop/workerid/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Identification of a worker (employee)."
      },
      AasField {
        name = "AccessStart",
        semanticId = "iri:https://admin-shell.io/idta/prop/accessstart/1/0",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Start (date and time) from when a worker is allowed to be deployed at a workstation."
      },
      AasField {
        name = "AccessEnd",
        semanticId = "iri:https://admin-shell.io/idta/prop/accessend/1/0",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "End (date and time) until when a worker is allowed to be deployed at a workstation."
      },
      AasField {
        name = "PreferredHeight",
        semanticId = "iri:https://admin-shell.io/idta/prop/preferredhight/1/0",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"720 mm"},
        description = "Preferred height of the working table of a workstation measured from floor level, where the worker stands."
      },
      AasField {
        name = "ProprietaryConfigurations",
        semanticId = "iri:https://admin-shell.io/idta/sml/proprietaryconfigurations/1/0",
        type = refBy(ProprietaryConfigurations),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "definition: A list to that proprietary worker-depending configurations can be added."
      }
    }
  };

  AasSubmodelElementListType ProprietaryConfigurations = {
    name = "ProprietaryConfigurations",
    semanticId = "iri:https://admin-shell.io/idta/sml/proprietaryconfigurations/1/0",
    description = "Proprietary worker-depending configurations.",
    versionIdentifier = "IDTA 02046-1-0",
    fields = {
      AasField {
        name = "ProprietaryConfigurationsRecord",
        semanticId = "iri:https://admin-shell.io/idta/smc/proprietaryconfigurationsrecord/1/0",
        type = refBy(StringListType),
        minimumInstances = 0,
        examples = {"-- SubmodelElementCollection"},
        description = "Order not relevant."
      }
    }
  };

  AasSubmodelElementCollectionType ProprietaryConfigurationsRecord = {
    name = "ProprietaryConfigurationsRecord",
    semanticId = "iri:https://admin-shell.io/idta/smc/proprietaryconfigurationsrecord/1/0",
    description = "Proprietary worker-depending configuration.",
    versionIdentifier = "IDTA 02046-1-0",
    fields = {
      AasField {
        name = "ConfigurationName",
        semanticId = "iri:https://admin-shell.io/idta/prop/configurationname/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"DashboardDesign"},
        description = "Name of a worker-dependend configuration of a workstation."
      },
      AasField {
        name = "ConfigurationValue",
        semanticId = "iri:https://admin-shell.io/idta/prop/configurationvalue/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Classic"},
        description = "Configuration value for a worker-depending workstation configuration."
      }
    }
  };

  AasSubmodelElementListType PlannedQualificationDemand = {
    name = "PlannedQualificationDemand",
    semanticId = "iri:https://admin-shell.io/idta/sml/plannedqualificationdemand/1/0",
    description = "Production plan depending planned qualification demand at a workstation.",
    versionIdentifier = "IDTA 02046-1-0",
    fields = {
      AasField {
        name = "PlannedQualificationDemandRecord",
        semanticId = "iri:https://admin-shell.io/idta/smc/plannedqualificationdemandrecord/1/0",
        type = refBy(StringListType),
        minimumInstances = 0,
        examples = {"-- SubmodelElementCollection"},
        description = "Order not relevant."
      }
    }
  };

  AasSubmodelElementCollectionType PlannedQualificationDemandRecord = {
    name = "PlannedQualificationDemandRecord",
    semanticId = "iri:https://admin-shell.io/idta/sml/plannedqualificationdemandrecord/1/0",
    description = "Production plan depending planned qualification demand at a workstation.",
    versionIdentifier = "IDTA 02046-1-0",
    fields = {
      AasField {
        name = "IntervalDescription",
        semanticId = "iri:https://admin-shell.io/idta/mlp/intervaldescription/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Description of a production planning interval and planning granularity (hour, day, ...), e.g., defined in an APS software."
      },
      AasField {
        name = "IntervalStart",
        semanticId = "iri:https://admin-shell.io/idta/prop/intervalstart/1/0",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Begin (timestamp) of a planning interval for qualification demand depending on the planning horizon."
      },
      AasField {
        name = "IntervalEnd",
        semanticId = "iri:https://admin-shell.io/idta/prop/intervalend/1/0",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "End (timestamp) of a planning interval for qualification demand depending on the planning horizon."
      },
      AasField {
        name = "QualificationDemandRecords",
        semanticId = "iri:https://admin-shell.io/idta/sml/qualificationdemandrecords/1/0",
        type = refBy(QualificationDemandRecords),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Worker qualifications that are required in order to work at the workstation."
      }
    }
  };

  AasSubmodelElementListType QualificationDemandRecords = {
    name = "QualificationDemandRecords",
    semanticId = "iri:https://admin-shell.io/idta/sml/qualificationdemandrecords/1/0",
    description = "Worker qualifications that are required in order to work at the workstation.",
    versionIdentifier = "IDTA 02046-1-0",
    fields = {
      AasField {
        name = "QualificationDemandRecord",
        semanticId = "iri:https://admin-shell.io/idta/smc/qualificationdemandrecord/1/0",
        type = refBy(StringListType),
        minimumInstances = 0,
        examples = {"-- SubmodelElementCollection"},
        description = "Order not relevant."
      }
    }
  };

  AasSubmodelElementCollectionType QualificationDemandRecord = {
    name = "QualificationDemandRecord",
    semanticId = "iri:https://admin-shell.io/idta/smc/qualificationdemandrecord/1/0",
    description = "Worker qualification that is required to work at the workstation.",
    versionIdentifier = "IDTA 02046-1-0",
    fields = {
      AasField {
        name = "QualificationClassificationId",
        semanticId = "iri:https://admin-shell.io/idta/prop/qualificationclassificationid/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Identification of the classification system where the qualification is classified."
      },
      AasField {
        name = "QualificationId",
        semanticId = "iri:https://admin-shell.io/idta/prop/qualificationid/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Indentification of the qualification."
      },
      AasField {
        name = "ExceptionRules",
        semanticId = "iri:https://admin-shell.io/idta/mlp/exceptionrules/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Exceptions rules that define possible deviations when the required qualification or skill is not available."
      },
      AasField {
        name = "DemandKind",
        semanticId = "iri:https://admin-shell.io/idta/prop/demandkind/1/0",
        type = refBy(DemandKind),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"ad-hoc, general, orderDepending"},
        description = "Kind of the demand, defined by the."
      },
      AasField {
        name = "StartTime",
        semanticId = "iri:https://admin-shell.io/idta/prop/starttime/1/0",
        type = refBy(DateTimeType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Point in time where a process with a certain resource demand starts."
      },
      AasField {
        name = "EndTime",
        semanticId = "iri:https://admin-shell.io/idta/prop/endtime/1/0",
        type = refBy(DateTimeType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Point in time where a process with a certain resource demand ends."
      },
      AasField {
        name = "ProcessTime",
        semanticId = "iri:https://admin-shell.io/idta/prop/processtime/1/0",
        type = refBy(IntegerType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"120 min"},
        description = "Manual work time that is planned for a production process."
      }
    }
  };

  AasSubmodelElementListType PlannedSkillDemand = {
    name = "PlannedSkillDemand",
    semanticId = "iri:https://admin-shell.io/idta/sml/plannedskilldemand/1/0",
    description = "Production plan depending planned skill demand at a workstation.",
    versionIdentifier = "IDTA 02046-1-0",
    fields = {
      AasField {
        name = "PlannedSkillDemandRecord",
        semanticId = "iri:https://admin-shell.io/idta/smc/plannedskilldemandrecord/1/0",
        type = refBy(StringListType),
        minimumInstances = 0,
        examples = {"-- SubmodelElementCollection"},
        description = "Order not relevant."
      }
    }
  };

  AasSubmodelElementCollectionType PlannedSkillDemandRecord = {
    name = "PlannedSkillDemandRecord",
    semanticId = "iri:https://admin-shell.io/idta/sml/plannedskilldemandrecord/1/0",
    description = "Production plan depending planned skill demand at a workstation.",
    versionIdentifier = "IDTA 02046-1-0",
    fields = {
      AasField {
        name = "IntervalDescription",
        semanticId = "iri:https://admin-shell.io/idta/mlp/intervaldescription/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Description of a production planning interval and planning granularity (hour, day, ...), e.g., defined in an APS software."
      },
      AasField {
        name = "IntervalStart",
        semanticId = "iri:https://admin-shell.io/idta/prop/intervalstart/1/0",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Begin (timestamp) of a planning interval for qualification demand depending on the planning horizon."
      },
      AasField {
        name = "IntervalEnd",
        semanticId = "iri:https://admin-shell.io/idta/prop/intervalend/1/0",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "End (timestamp) of a planning interval for qualification demand depending on the planning horizon."
      },
      AasField {
        name = "SkillDemandRecords",
        semanticId = "iri:https://admin-shell.io/idta/sml/skilldemandrecords/1/0",
        type = refBy(SkillDemandRecords),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Worker skills that are required in order to work at the workstation."
      }
    }
  };

  AasSubmodelElementListType SkillDemandRecords = {
    name = "SkillDemandRecords",
    semanticId = "iri:https://admin-shell.io/idta/sml/skilldemandrecords/1/0",
    description = "Worker skills that are required in order to work at the workstation.",
    versionIdentifier = "IDTA 02046-1-0",
    fields = {
      AasField {
        name = "SkillDemandRecord",
        semanticId = "iri:https://admin-shell.io/idta/smc/skilldemandrecord/1/0",
        type = refBy(StringListType),
        minimumInstances = 0,
        examples = {"-- SubmodelElementCollection"},
        description = "Order not relevant."
      }
    }
  };

  AasSubmodelElementCollectionType SkillDemandRecord = {
    name = "SkillDemandRecord",
    semanticId = "iri:https://admin-shell.io/idta/smc/skilldemandrecord/1/0",
    description = "Worker skill that is required in order to work at the workstation.",
    versionIdentifier = "IDTA 02046-1-0",
    fields = {
      AasField {
        name = "SkillClassificationId",
        semanticId = "iri:https://admin-shell.io/idta/prop/skillclassificationid/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "and definition: Identification of the classification system where the skill is classified."
      },
      AasField {
        name = "SkillLevelClassificationId",
        semanticId = "iri:https://admin-shell.io/idta/prop/skilllevelclassificationid/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Identification of the classification system where the skill level is classified, e.g., EQR level 1-8."
      },
      AasField {
        name = "SkillLevelId",
        semanticId = "iri:https://admin-shell.io/idta/prop/skilllevelid/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Identification of the skill level for a worker."
      },
      AasField {
        name = "SkillId",
        semanticId = "iri:https://admin-shell.io/idta/prop/skillid/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Identification of the skill for a worker."
      },
      AasField {
        name = "ExceptionRules",
        semanticId = "iri:https://admin-shell.io/idta/mlp/exceptionrules/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Exceptions rules that define possible deviations when the required qualification or skill is not available."
      },
      AasField {
        name = "DemandKind",
        semanticId = "iri:https://admin-shell.io/idta/prop/demandkind/1/0",
        type = refBy(DemandKind),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"ad-hoc, general, orderDepending"},
        description = "Kind of the demand, defined by the."
      },
      AasField {
        name = "StartTime",
        semanticId = "iri:https://admin-shell.io/idta/prop/starttime/1/0",
        type = refBy(DateTimeType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Point in time where a process with a certain resource demand starts."
      },
      AasField {
        name = "EndTime",
        semanticId = "iri:https://admin-shell.io/idta/prop/endtime/1/0",
        type = refBy(DateTimeType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Point in time where a process with a certain resource demand ends."
      },
      AasField {
        name = "ProcessTime",
        semanticId = "iri:https://admin-shell.io/idta/prop/processtime/1/0",
        type = refBy(IntegerType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"120 min"},
        description = "Manual work time that is planned for a production process."
      }
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
