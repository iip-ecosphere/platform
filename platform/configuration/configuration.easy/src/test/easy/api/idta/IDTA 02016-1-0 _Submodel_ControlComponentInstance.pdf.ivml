project IDTA_02016_ControlComponentInstance {

  version v1.0;

  import AASDataTypes;

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasSubmodelType CiControlComponentInstance = {
    name = "CiControlComponentInstance",
    idShort = "ControlComponentInstance",
    semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Instance/1/0",
    description = "Contains the instance information of a control component.",
    versionIdentifier = "IDTA 02016-1-0",
    fixedName = true,
    fields = {
      AasField {
        name = "Endpoints",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Instance/Endpoints/1/0",
        type = refBy(Endpoints),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Collection of references to control endpoints supported by the instance of the component, e.g., to the Endpoint Metadata SMC of the Asset Interface Description Submodel (IDTA 02017), the MTP submodel (IDTA 02001) or OPC UA Server Datasheet submodel (IDTA 02009)."
      },
      AasField {
        name = "Skills",
        semanticId = "iri:https://admin-shell.io/idta/controlcomponent/Skills/1/0",
        type = refBy(AasGenericSubmodelElementCollection),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Collection of skills offered by the component instance."
      },
      AasField {
        name = "Type",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Instance/Type/1/0",
        type = refBy(AasReferenceType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Reference between the component instance and its respective ControlComponentType Submodel."
      }
    }
  };

  AasSubmodelElementCollectionType Endpoints = {
    name = "CiEndpoints",
    idShort = "Endpoints",
    semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Instance/Endpoints/1/0",
    description = "Collection of endpoint references.",
    versionIdentifier = "IDTA 02016-1-0",
    fields = {
      AasField {
        name = "Endpoint",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Instance/Endpoint/1/0",
        counting = true,
        type = refBy(AasReferenceType),
        minimumInstances = 0,
        description = "A reference to a control endpoint."
      }
    }
  };

  AasSubmodelElementCollectionType Skill = {
    name = "CiSkill",
    idShort = "Skill",
    semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/1/0",
    description = "Contains the basic information to call (request the execution of) a skill, e.g. its signature.",
    versionIdentifier = "IDTA 02016-1-0",
    fields = {
      AasField {
        name = "Name",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/Name/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"PICK"},
        description = "Name used to select the skill via its interface."
      },
      AasField {
        name = "DisplayName",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/DisplayName/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"en-us","Pick"},
        description = "Name to display the skill, e.g. in an HMI or GUI."
      },
      AasField {
        name = "Disabled",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/Disabled/1/0",
        type = refBy(BooleanType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"False"},
        description = "Boolean variable that defines if the skill is (currently) disabled, e.g. not licensed, tested, suitable, …."
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
        type = refBy(AasGenericSubmodelElementCollection),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Collection of parameters used for the configuration of the skill."
      },
      AasField {
        name = "Errors",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/Errors/1/0",
        type = refBy(AasGenericSubmodelElementCollection),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Collection of references to the error codes of the component that may be triggered by this skill."
      },
      AasField {
        name = "Uses",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/Uses/1/0",
        type = refBy(AasGenericSubmodelElementCollection),
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
    versionIdentifier = "IDTA 02016-1-0",
    fields = {
      AasField {
        name = "Mode",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/Mode/1/0",
        counting = true,
        type = refBy(StringListType),
        minimumInstances = 1,
        examples = {"AUTO","SEMIAUTO","MANUAL","SIMULATE"},
        description = "Name of the operation, operating, operational or execution modes (depending on the standard), in which the skill is available/allowed to execute."
      }
    }
  };

  AasSubmodelElementCollectionType Parameter = {
    name = "CiParameter",
    idShort = "Parameter",
    semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/Parameter/1/0",
    description = "Parameter used for the configuration of the skill.",
    versionIdentifier = "IDTA 02016-1-0",
    fields = {
      AasField {
        name = "Name",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/Parameter/Name/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Position"},
        description = "Name of the parameter."
      },
      AasField {
        name = "Direction",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/Parameter/Direction/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"In"},
        description = "Indicates whether the parameter is an input (In) or an output (Out) of the skill. This also determines, whether the skill will read (In) or write (Out) the value. Hence, an InOut parameter can be set from outside and can also be changed from skill itself."
      },
      AasField {
        name = "Type",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/Parameter/Type/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Integer"},
        description = "Data type as string used to interpret the parameter. Because the technology for implementing a CC is intenionally left open for the vendor, it is not possible to reference a specific type set. Especially the XML data type set or AAS-specific subsets are not sufficicent. Example: a skill could use a custom data type (IEC 61131 / OPC UA Structure, a Class  in Java, C#, ...) as a parameter, e.g., a struct containing three float variables representing a 3D position."
      },
      AasField {
        name = "Values",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/Parameter/Values/1/0",
        type = refBy(AasGenericSubmodelElementCollection),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Collection of properties of the accepted values that the parameter may take. Each entry of the collection may contain a semantic description of the meaning of the parameter value."
      }
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
