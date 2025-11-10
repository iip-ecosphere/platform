project IDTA_02005_SimulationModels {

  version v1.0;

  import AASDataTypes;
  import IDTA_02002_ContactInformations with (IDTA_02002_ContactInformations.version == v1.0);

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasSubmodelType SimulationModels = {
    name = "SimulationModels",
    semanticId = "iri:https://admin-shell.io/idta/SimulationModels/SimulationModels/1/0",
    description = "The Submodel may provide one or more simulation models, a service to generate a specific model, or access to an open or specific query.",
    versionIdentifier = "IDTA 02005-1-0",
    fields = {
      AasField {
        name = "SimulationModel",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/SimulationModel/1/0",
        type = refBy(SimulationModel),
        minimumInstances = 0,
        description = "Feature collection to provide or request simulation models. Models can be described by objective and content."
      }
    }
  };

  AasSubmodelElementCollectionType SimulationModel = {
    name = "SimulationModel",
    semanticId = "iri:https://admin-shell.io/idta/SimulationModels/SimulationModel/1/0",
    description = "Feature collection to provide or request simulation models. Models can be described by objective and content.",
    versionIdentifier = "IDTA 02005-1-0",
    fields = {
      AasField {
        name = "Summary",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/Summary/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Summary of the contents of the simulation model in text form."
      },
      AasField {
        name = "SimPurpose",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/SimPurpose/1/0",
        type = refBy(SimPurpose),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "This characteristic describes the simulation purpose or suitability for different simulation goals."
      },
      AasField {
        name = "TypeOfModel",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/TypeOfModel/1/0",
        type = refBy(StringListType),
        minimumInstances = 0,
        description = "List of modeling approaches used for the model."
      },
      AasField {
        name = "ScopeOfModel",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/ScopeOfModel/1/0",
        type = refBy(StringListType),
        minimumInstances = 1,
        description = "List of basic physical characteristics which are represented by the model."
      },
      AasField {
        name = "LicenseModel",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/LicenseModel/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "If a simulation model usage will be charged and how it will be charged."
      },
      AasField {
        name = "EngineeringDomain",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/EngineeringDomain/1/0",
        type = refBy(StringListType),
        minimumInstances = 0,
        description = "List of engineering disciplines supported or mapped with the model."
      },
      AasField {
        name = "Environment",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/Environment/1/0",
        type = refBy(Environment),
        minimumInstances = 0,
        description = "Information about prerequisite environments or dependencies of underlying components on the target system."
      },
      AasField {
        name = "RefSimDocumentation",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/RefSimDocumentation/1/0",
        type = refBy(AasFileResourceType),
        minimumInstances = 0,
        description = "Simulation Documentation Documentation of example simulations of the model can be supplied. This includes a solver setup and sample circuit and sample results. e.g. zip file, PDF, html, ... -."
      },
      AasField {
        name = "ModelFile",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/ModelFile/1/0",
        type = refBy(ModelFile),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Providing versions of the simulation model and with characteristics to distinguish them."
      },
      AasField {
        name = "ParamMethod",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/ParamMethod/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Indicates whether the model must be parameterized and if so, which method is required."
      },
      AasField {
        name = "ParamFile",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/ParamFile/1/0",
        type = refBy(AasFileResourceType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "File for parameterization of the model. As parameter file or parameter documentation (e.g. pdf)."
      },
      AasField {
        name = "InitStateMethod",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/InitStateMethod/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Describes the state variables of the simulation model that must be initialized to start the simulation. For initial value problems, these quantities describe the system state at the start of the simulation. In this case, the system is in a state of equilibrium. Alternatively, a simulation model may include a method to determine consistent initial values at this step, e.g., at an operating point."
      },
      AasField {
        name = "InitStateFile",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/InitStateFile/1/0",
        type = refBy(AasFileResourceType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "File for parameterizing the initial states of the model. As parameter file or parameter documentation (e.g. pdf)."
      },
      AasField {
        name = "DefaultSimTime",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/DefaultSimTime/1/0",
        type = refBy(FloatType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Predefined simulation period in seconds."
      },
      AasField {
        name = "SimModManufacturerInformation",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/SimModManufacturerInformation/1/0",
        type = refBy(SimModManufacturerInformation),
        minimumInstances = 0,
        description = "Provide access to simulation support service provided by the distributor via mail or phone."
      },
      AasField {
        name = "Ports",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/Ports/1/0",
        type = refBy(Ports),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Interfaces of the model. This includes inputs, outputs as well as acausal connections (e.g. mechanical connections). In addition, it is specified here whether the model provides binary interfaces (e.g. for visualization)."
      }
    }
  };

  AasSubmodelElementCollectionType SimPurpose = {
    name = "SimPurpose",
    semanticId = "iri:https://admin-shell.io/idta/SimulationModels/SimPurpose/1/0",
    description = "This characteristic describes the simulation purpose or suitability for different simulation goals.",
    versionIdentifier = "IDTA 02005-1-0",
    fields = {
      AasField {
        name = "PosSimPurpose",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/PosSimPurpose/1/0",
        type = refBy(StringListType),
        minimumInstances = 1,
        description = "List of simulation purposes for which the model is intended."
      },
      AasField {
        name = "NegSimPurpose",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/NegSimPurpose/1/0",
        type = refBy(StringListType),
        minimumInstances = 0,
        description = "List of simulation purposes for which the model is explicitly not suitable."
      }
    }
  };

  AasSubmodelElementCollectionType Environment = {
    name = "Environment",
    semanticId = "iri:https://admin-shell.io/idta/SimulationModels/Environment/1/0",
    description = "Information about prerequisite environments or dependencies of underlying components on the target system.",
    versionIdentifier = "IDTA 02005-1-0",
    fields = {
      AasField {
        name = "OperatingSystem",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/OperatingSystem/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Name of the operating system including version and architecture (e.g. Windows 10 64bit)."
      },
      AasField {
        name = "ToolEnvironment",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/ToolEnvironment/1/0",
        type = refBy(StringListType),
        minimumInstances = 0,
        description = "List with required simulation tools, interpreters, model libraries or runtime libraries. In each case the exact designation of the software producer is given as free text."
      },
      AasField {
        name = "DependencyEnvironment",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/DependencyEnvironment/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Description of dependencies to associated hardware and software."
      },
      AasField {
        name = "VisualizationInformation",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/VisualizationInformation/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Ability to use a visualization. This can be integrated in a model or the model offers capabilities for connection. The connection can be described in more detail under Ports."
      },
      AasField {
        name = "SimulationTool",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/SimulationTool/1/0",
        type = refBy(SimulationTool),
        minimumInstances = 1,
        description = "Properties of the model with regarding to concrete simulation tools."
      }
    }
  };

  AasSubmodelElementCollectionType SimulationTool = {
    name = "SimulationTool",
    semanticId = "iri:https://admin-shell.io/idta/SimulationModels/simulationTool/1/0",
    description = "Properties of the model with regarding to concrete simulation tools.",
    versionIdentifier = "IDTA 02005-1-0",
    fields = {
      AasField {
        name = "SimToolName",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/SimToolName/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Name of the simulation tool including version."
      },
      AasField {
        name = "DependencySimTool",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/DependencySimTool/1/0",
        type = refBy(StringListType),
        minimumInstances = 0,
        description = "Dependencies of Simulation Tools."
      },
      AasField {
        name = "Compiler",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/compiler/1/0",
        type = refBy(StringListType),
        minimumInstances = 0,
        description = "Name of necessary compiler including version."
      },
      AasField {
        name = "SolverAndTolerances",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/SolverAndTolerances/1/0",
        type = refBy(SolverAndTolerances),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Useful settings of the simulation environment. Includes e.g. solver settings."
      }
    }
  };

  AasSubmodelElementCollectionType SolverAndTolerances = {
    name = "SolverAndTolerances",
    semanticId = "iri:https://admin-shell.io/idta/SimulationModels/SolverAndTolerances/1/0",
    description = "Useful settings of the simulation environment. Includes e.g. solver settings.",
    versionIdentifier = "IDTA 02005-1-0",
    fields = {
      AasField {
        name = "StepSizeControlNeeded",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/StepSizeControlNeeded/1/0",
        type = refBy(BooleanType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Solver with step size control recommended."
      },
      AasField {
        name = "FixedStepSize",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/FixedStepSize/1/0",
        type = refBy(FloatType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Fixed integration step size, if there is no adaptive step size."
      },
      AasField {
        name = "StiffSolverNeeded",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/StiffSolverNeeded/1/0",
        type = refBy(BooleanType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Stiff solver needed."
      },
      AasField {
        name = "SolverIncluded",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/SolverIncluded/1/0",
        type = refBy(BooleanType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Solver is integrated in the model (e.g. FMU for co- simulation)."
      },
      AasField {
        name = "TestedToolSolverAlgorithm",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/TestedToolSolverAlgorithm/1/0",
        type = refBy(TestedToolSolverAlgorithm),
        minimumInstances = 0,
        description = "List of validated tool-solver combinations."
      }
    }
  };

  AasSubmodelElementCollectionType TestedToolSolverAlgorithm = {
    name = "TestedToolSolverAlgorithm",
    semanticId = "iri:https://admin-shell.io/idta/SimulationModels/TestedToolSolverAlgorithm/1/0",
    description = "List of validated tool-solver combinations.",
    versionIdentifier = "IDTA 02005-1-0",
    fields = {
      AasField {
        name = "SolverAlgorithm",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/SolverAlgorithm/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "validated solver."
      },
      AasField {
        name = "ToolSolverFurtherDescription",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/ToolSolverFurtherDescription/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Further tool- and solver-specific information."
      },
      AasField {
        name = "Tolerance",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/tolerance/1/0",
        type = refBy(FloatType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "(relative) tolerance for theadaptive step size."
      }
    }
  };

  AasSubmodelElementCollectionType ModelFile = {
    name = "ModelFile",
    semanticId = "iri:https://admin-shell.io/idta/SimulationModels/ModelFile/1/0",
    description = "Providing versions of the simulation model and with characteristics to distinguish them.",
    versionIdentifier = "IDTA 02005-1-0",
    fields = {
      AasField {
        name = "ModelFileType",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/ModelFileType/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Designation of the exchange format of the model. E.G.: FMI 1.0, Co-Simulation, Platform / Source - Code. FMI 2.0.2, Model Exchange, Source - Code, S-function, Version 2, 64bit, mex - Format / or C-Code, Modelica 3, encoded, VHDL."
      },
      AasField {
        name = "ModelFileVersion",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/ModelFileVersion/1/0",
        type = refBy(ModelFileVersion),
        minimumInstances = 1,
        description = "Provision of a version of the simulation model with information to distinguish the versions. The versions are primarily intended for bug fixes without content changes."
      }
    }
  };

  AasSubmodelElementCollectionType ModelFileVersion = {
    name = "ModelFileVersion",
    semanticId = "iri:https://admin-shell.io/idta/SimulationModels/ModelFileVersion/1/0",
    description = "Provision of a version of the simulation model with information to distinguish the versions. The versions are primarily intended for bug fixes without content changes.",
    versionIdentifier = "IDTA 02005-1-0",
    fields = {
      AasField {
        name = "ModelVersionId",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/ModelVersionId/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Version number of the model from the vendor."
      },
      AasField {
        name = "ModelPreviewImage",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/ModelPreviewImage/1/0",
        type = refBy(AasFileResourceType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Image file to represent the model in user interfaces, e.g. in a search."
      },
      AasField {
        name = "DigitalFile",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/DigitalFile/1/0",
        type = refBy(AasFileResourceType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Deployment of the model file."
      },
      AasField {
        name = "ModelFileReleaseNotesTxt",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/ModelFileReleaseNotesTxt/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "contains information about this release."
      },
      AasField {
        name = "ModelFileReleaseNotesFile",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/ModelFileReleaseNotesFile/1/0",
        type = refBy(AasFileResourceType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "release notes link or file."
      }
    }
  };

  AasSubmodelElementCollectionType SimModManufacturerInformation = {
    name = "SimModManufacturerInformation",
    semanticId = "iri:https://admin-shell.io/idta/SimulationModels/SimModManufacturerInformation/1/0",
    description = "Provide access to simulation support service provided by the distributor via mail or phone.",
    versionIdentifier = "IDTA 02005-1-0",
    fields = {
      AasField {
        name = "Company",
        semanticId = "irdi:0173-1#02-AAW001#001",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "name of the company."
      },
      AasField {
        name = "Language",
        semanticId = "irdi:0173-1#02-AAO895#003",
        type = refBy(StringListType),
        minimumInstances = 1,
        description = "available language."
      },
      AasField {
        name = "Email",
        semanticId = "irdi:0173-1#02-AAQ836#005",
        type = refBy(Email),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "E-mail address and encryption method."
      },
      AasField {
        name = "Phone",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/ContactInformation/Phone",
        type = refBy(Phone),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Phone number including type."
      }
    }
  };

  AasSubmodelElementCollectionType Ports = {
    name = "Ports",
    semanticId = "iri:https://admin-shell.io/idta/SimulationModels/Ports/1/0",
    description = "Interfaces of the model. This includes inputs, outputs as well as acausal connections (e.g. mechanical connections). In addition, it is specified here whether the model provides binary interfaces (e.g. for visualization).",
    versionIdentifier = "IDTA 02005-1-0",
    fields = {
      AasField {
        name = "PortsConnector",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/PortsConnector/1/0",
        type = refBy(PortsConnector),
        minimumInstances = 0,
        description = "List of ports of the model. These include a name, a description, a list of variables, and a list of ports."
      },
      AasField {
        name = "BinaryConnector",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/BinaryConnector/1/0",
        type = refBy(BinaryConnector),
        minimumInstances = 0,
        description = "Binary interfaces (binaryType) based on the FMI 3.0 standard (https://fmi-standard.org/docs/3.0-dev/#definition- of-types). At this point the name (e.g. 'Binary interface visualization') and the description (e.g. 'Interface for binary transfer of visualization information') are specified."
      }
    }
  };

  AasSubmodelElementCollectionType PortsConnector = {
    name = "PortsConnector",
    semanticId = "iri:https://admin-shell.io/idta/SimulationModels/PortsConnector/1/0",
    description = "List of ports of the model. These include a name, a description, a list of variables, and a list of ports.",
    versionIdentifier = "IDTA 02005-1-0",
    fields = {
      AasField {
        name = "PortConnectorName",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/PortConnectorName/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Name of the Connector Port."
      },
      AasField {
        name = "PortConDescription",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/PortConDescription/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Description of the Connector Port."
      },
      AasField {
        name = "Variable",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/Variable/1/0",
        type = refBy(Variable),
        minimumInstances = 0,
        description = "List of variables of the port."
      }
    }
  };

  AasSubmodelElementCollectionType Variable = {
    name = "Variable",
    semanticId = "iri:https://admin-shell.io/idta/SimulationModels/Variable/1/0",
    description = "-.",
    versionIdentifier = "IDTA 02005-1-0",
    fields = {
      AasField {
        name = "VariableName",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/VariableName/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Name of the variable."
      },
      AasField {
        name = "Range",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/Range/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Range of values for the variable (e.g. [min, max], [min, max[, ]min, max], ]min, max[, {val1, val2, ...}). -."
      },
      AasField {
        name = "VariableType",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/VariableType/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Type of the variable (e.g. Real, Integer, Boolean, String or Enum)."
      },
      AasField {
        name = "VariableDescription",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/VariableDescription/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Description of the variable."
      },
      AasField {
        name = "UnitList",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/UnitList/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "The most common units can be selected here. .. If 'others' is selected, a free text can be entered."
      },
      AasField {
        name = "UnitDescription",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/UnitDescription/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Text field for missing units of the list."
      },
      AasField {
        name = "VariableCausality",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/VariableCausality/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "The causality of the variable: input to inputs, output to ouputs, acausal connections (e.g. mechanical connection) do not have causality."
      },
      AasField {
        name = "VariablePrefix",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/VariablePrefix/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Prefix for acausal variable. Potential variables are set equal when connecting (no prefix). “flow” variables are connected according to Kirchhoff's law, i.e. the sum of the variables equals zero. The bi-directional flow of matter is described by the prefix 'stream' (e.g. for enthalpy)."
      }
    }
  };

  AasSubmodelElementCollectionType BinaryConnector = {
    name = "BinaryConnector",
    semanticId = "iri:https://admin-shell.io/idta/SimulationModels/BinaryConnector/1/0",
    description = "Binary interfaces (binaryType) based on the FMI 3.0 standard (https://fmi-standard.org/docs/3.0- dev/#definition-of-types). At this point the name (e.g. 'Binary interface visualization') and the description (e.g. 'Interface for binary transfer of visualization information') are specified.",
    versionIdentifier = "IDTA 02005-1-0",
    fields = {
      AasField {
        name = "BinaryConName",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/BinaryConnectorName/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Binary interface name."
      },
      AasField {
        name = "BinaryConDescription",
        semanticId = "iri:https://admin-shell.io/idta/SimulationModels/BinaryConDescription/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Binary interface description."
      }
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
