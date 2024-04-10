project IDTA_02016_ControlComponentInstance {

  import AASDataTypes;

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasSubmodelType CiControlComponentInstance = {
    name = "CiControlComponentInstance",
    idShort = "ControlComponentInstance",
    semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Instance/1/0",
    description = "A ControlComponentInstance Submodel.",
    fields = {
      AasField {
        name = "Type",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Instance/Type/1/0",
        type = refBy(AasReferenceType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Reference between the component instance and its respective ControlComponentType Submodel."
      },
      AasField {
        name = "Endpoints",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Instance/Endpoints/1/0",
        type = refBy(Endpoints),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Collection of references to control endpoints supported by the instance of the component."
      },
      AasField {
        name = "Skills",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skills/1/0",
        type = refBy(Skills),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Collection of skills offered by the component type."
      }
    }
  };

  AasSubmodelElementCollectionType Endpoints = {
    name = "CiEndpoints",
    idShort = "Endpoints",
    semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Instance/Endpoints/1/0",
    description = "Collection of references to control endpoints supported by the instance of the component.",
    fields = {
      AasField {
        name = "Endpoint",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Instance/Endpoint/1/0",
        type = refBy(AasReferenceType),
        minimumInstances = 0,
        description = "A reference to a control endpoint."
      }
    }
  };

  AasSubmodelElementCollectionType Skills = {
    name = "CiSkills",
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
    name = "CiSkill",
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
    name = "CiModes",
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
    name = "CiParameters",
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
    name = "CiParameter",
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
    name = "CiValues",
    idShort = "Values",
    semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/Parameter/Values/1/0",
    description = "Collection of properties of the accepted values that the parameter may take.",
    fields = {
    }
  };

  AasSubmodelElementCollectionType Errors = {
    name = "CiErrors",
    idShort = "Errors",
    semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/Errors/1/0",
    description = "Collection of references to the error codes of the component that may be triggered by this skill.",
    fields = {
    }
  };

  AasSubmodelElementCollectionType Uses = {
    name = "CiUses",
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
