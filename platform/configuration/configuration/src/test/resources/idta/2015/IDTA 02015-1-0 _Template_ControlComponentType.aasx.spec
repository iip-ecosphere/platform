project IDTA_02015_ControlComponentType {

  import AASDataTypes;

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasSubmodelType CtControlComponentType = {
    name = "CtControlComponentType",
    idShort = "ControlComponentType",
    semanticId = "iri:https://example.com/ids/sm/5213_1120_8022_9305",
    fields = {
      AasField {
        name = "Interfaces",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Type/Interfaces/1/0",
        type = refBy(Interfaces),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Collection of references to control interfaces supported by the component type, e.g. to elements of the Interface Metadata SMC of the Asset Interface Description submodel, the MTP submodel or OPC UA Server Datasheet submodel."
      },
      AasField {
        name = "Skills",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skills/1/0",
        type = refBy(Skills),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Collection of skills offered by the component type."
      },
      AasField {
        name = "Errors",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Type/Errors/1/0",
        type = refBy(Errors),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Collection of all possible error codes that may appear in components of this type."
      }
    }
  };

  AasSubmodelElementCollectionType Interfaces = {
    name = "CtInterfaces",
    idShort = "Interfaces",
    semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Type/Interfaces/1/0",
    description = "Collection of references to control interfaces supported by the component type, e.g. to elements of the Interface Metadata SMC of the Asset Interface Description submodel, the MTP submodel or OPC UA Server Datasheet submodel.",
    fields = {
      AasField {
        name = "Interface",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Type/Interface/1/0",
        type = refBy(AasReferenceType),
        minimumInstances = 0,
        description = "Reference to a single control interface."
      }
    }
  };

  AasSubmodelElementCollectionType Skills = {
    name = "CtSkills",
    idShort = "Skills",
    semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skills/1/0",
    description = "Collection of skills offered by the component type.",
    fields = {
      AasField {
        name = "Skill",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/1/0",
        type = refBy(Skill),
        description = "Contains the basic information to call (request the execution of) a skill, e.g. its signature."
      }
    }
  };

  AasSubmodelElementCollectionType Skill = {
    name = "CtSkill",
    idShort = "Skill",
    semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/1/0",
    description = "Contains the basic information to call (request the execution of) a skill, e.g. its signature.",
    fields = {
      AasField {
        name = "Name",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/Name/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Name used to select the skill via its interface."
      },
      AasField {
        name = "DisplayName",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/DisplayName/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Name to display the skill, e.g. in an HMI or GUI."
      },
      AasField {
        name = "Disabled",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/Disabled/1/0",
        type = refBy(BooleanType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Boolean property that defines if the skill is (currently) disabled, e.g. not licensed, tested, suitable."
      },
      AasField {
        name = "Modes",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/Modes/1/0",
        type = refBy(Modes),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Collection of operation, operating, operational or execution modes (depending on the standard), in which the skill is available/allowed to execute."
      },
      AasField {
        name = "Parameters",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/Parameters/1/0",
        type = refBy(Parameters),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Collection of parameters used for the configuration of the skill."
      },
      AasField {
        name = "Errors",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/Errors/1/0",
        type = refBy(Errors),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Collection of references to the error codes of the component that may be triggered by this skill."
      },
      AasField {
        name = "Uses",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/Uses/1/0",
        type = refBy(Uses),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Collection of references to other skills, that this skill uses."
      }
    }
  };

  AasSubmodelElementCollectionType Modes = {
    name = "CtModes",
    idShort = "Modes",
    semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/Modes/1/0",
    description = "Collection of operation, operating, operational or execution modes (depending on the standard), in which the skill is available/allowed to execute.",
    fields = {
      AasField {
        name = "Mode",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/Mode/1/0",
        type = refBy(StringListType),
        minimumInstances = 1,
        description = "Name of the operation, operating, operational or execution modes (depending on the standard), in which the skill is available/allowed to execute."
      }
    }
  };

  AasSubmodelElementCollectionType Parameters = {
    name = "CtParameters",
    idShort = "Parameters",
    semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/Parameters/1/0",
    description = "Collection of parameters used for the configuration of the skill.",
    fields = {
      AasField {
        name = "Parameter",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/Parameter/1/0",
        type = refBy(Parameter),
        minimumInstances = 0,
        description = "Parameter used for the configuration of the skill."
      }
    }
  };

  AasSubmodelElementCollectionType Parameter = {
    name = "CtParameter",
    idShort = "Parameter",
    semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/Parameter/1/0",
    description = "Parameter used for the configuration of the skill.",
    fields = {
      AasField {
        name = "Name",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/Parameter/Name/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Name of the parameter."
      },
      AasField {
        name = "Direction",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/Parameter/Direction/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Indicates whether the parameter is an input (In) or an output (Out) of the skill. An InOut parameter can be set from outside and can also be changed from skill itself."
      },
      AasField {
        name = "Type",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/Parameter/Type/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Data type as string used to interpret the parameter."
      },
      AasField {
        name = "Values",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/Parameter/Values/1/0",
        type = refBy(Values),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Collection of properties of the accepted values that the parameter may take."
      }
    }
  };

  AasSubmodelElementCollectionType Values = {
    name = "CtValues",
    idShort = "Values",
    semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/Parameter/Values/1/0",
    description = "Collection of properties of the accepted values that the parameter may take.",
    fields = {
    }
  };

  AasSubmodelElementCollectionType Errors = {
    name = "CtErrors",
    idShort = "Errors",
    semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/Errors/1/0",
    description = "Collection of references to the error codes of the component that may be triggered by this skill.",
    fields = {
    }
  };

  AasSubmodelElementCollectionType Uses = {
    name = "CtUses",
    idShort = "Uses",
    semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/Uses/1/0",
    description = "Collection of references to other skills, that this skill uses.",
    fields = {
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
