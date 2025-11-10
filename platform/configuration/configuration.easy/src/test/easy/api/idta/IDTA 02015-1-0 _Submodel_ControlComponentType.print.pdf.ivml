project IDTA_02015_ControlComponentType {

  version v1.0;

  import AASDataTypes;

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasSubmodelType CtControlComponentType = {
    name = "CtControlComponentType",
    idShort = "ControlComponentType",
    semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Type/1/0",
    description = "Contains the type information of a control component.",
    versionIdentifier = "IDTA 02015-1-0",
    fixedName = true,
    fields = {
      AasField {
        name = "Interfaces",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Type/Interfaces/1/0",
        type = refBy(Interfaces),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Collection of references to control interfaces supported by the component type, e.g. to elements of the Interface Metadata SMC of the Asset Interface Description Submodel (IDTA 02017), the MTP Submodel (IDTA 02001) or OPC UA Server Datasheet Submodel (IDTA 02009)."
      },
      AasField {
        name = "Skills",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skills/1/0",
        type = refBy(AasGenericSubmodelElementCollection),
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
    description = "Collection of interface references.",
    versionIdentifier = "IDTA 02015-1-0",
    fields = {
      AasField {
        name = "Interface",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Type/Interface/1/0",
        counting = true,
        type = refBy(AasReferenceType),
        minimumInstances = 0,
        description = "A reference to a control interface supported by the component type, e.g. to elements of the Interface Metadata SMC of the Asset Interface Description Submodel (IDTA 02017), the MTP Submodel (IDTA 02001) or OPC UA Server Datasheet Submodel (IDTA 02009)."
      }
    }
  };

  AasSubmodelElementCollectionType Errors = {
    name = "CtErrors",
    idShort = "Errors",
    semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Type/Errors/1/0",
    description = "Collection of error codes related to the component type.",
    versionIdentifier = "IDTA 02015-1-0",
    fields = {
      AasField {
        name = "ErrorCode",
        semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Type/ErrorCode/1/0",
        counting = true,
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        examples = {"en-us","Pressure Loss"},
        description = "Semantic description of the error code in multiple languages. The IdShort represents the actual error code. The naming scheme ErrorCode{00} does not have to be followed."
      }
    }
  };

  AasSubmodelElementCollectionType Skill = {
    name = "CtSkill",
    idShort = "Skill",
    semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/1/0",
    description = "Contains the basic information to call (request the execution of) a skill, e.g. its signature.",
    versionIdentifier = "IDTA 02015-1-0",
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
        type = refBy(Errors),
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
    name = "CtModes",
    idShort = "Modes",
    semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/Modes/1/0",
    description = "Collection of operation, operating, operational or execution modes (depending on the standard), in which the skill is available/allowed to execute.",
    versionIdentifier = "IDTA 02015-1-0",
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
    name = "CtParameter",
    idShort = "Parameter",
    semanticId = "iri:https://admin-shell.io/idta/ControlComponent/Skill/Parameter/1/0",
    description = "Parameter used for the configuration of the skill.",
    versionIdentifier = "IDTA 02015-1-0",
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
