project IDTA_02021_PowerDriveTrainSizing {

  version v1.0;

  import AASDataTypes;
  import IDTA_02002_ContactInformations with (IDTA_02002_ContactInformations.version == v1.0);

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasEnumType MovingPart = {
    name = "MovingPart",
    description = "The part that acts in a linear motion along the teeth.",
    versionIdentifier = "IDTA 02021-1-0",
    semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MovingPart/1/0",
    literals = {
      AasEnumLiteral {
        name = "Rack",
        semanticId = "Rack"
      },
      AasEnumLiteral {
        name = "Pinion",
        semanticId = "Pinion"
      }
    }
  };

  AasEnumType MainComponentType = {
    name = "MainComponentType",
    description = "The kind of main component in the drive train, such as a motor, gearbox or drive controller.",
    versionIdentifier = "IDTA 02021-1-0",
    semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MainComponentType/1/0",
    literals = {
      AasEnumLiteral {
        name = "Drive",
        semanticId = "Drive"
      },
      AasEnumLiteral {
        name = "Motor",
        semanticId = "Motor"
      },
      AasEnumLiteral {
        name = "Gearbox",
        semanticId = "Gearbox"
      },
      AasEnumLiteral {
        name = "LinearMotor",
        semanticId = "LinearMotor"
      }
    }
  };

  AasEnumType CriticalityOfMessage = {
    name = "CriticalityOfMessage",
    description = "marker for the criticality of the message with respect to the sizing result. E. g. trough symbol, color or code.",
    versionIdentifier = "IDTA 02021-1-0",
    semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/CriticalityOfMessage/1/0",
    literals = {
      AasEnumLiteral {
        name = "Information",
        semanticId = "Information"
      },
      AasEnumLiteral {
        name = "Warning",
        semanticId = "Warning"
      },
      AasEnumLiteral {
        name = "Error",
        semanticId = "Error"
      }
    }
  };

  AasSubmodelType PowerDriveTrainSizing = {
    name = "PowerDriveTrainSizing",
    semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/1/0",
    description = "Submodel containing customer specifications for motion and load profile, limitations and requirements of an industrial motion application.",
    versionIdentifier = "IDTA 02021-1-0",
    fields = {
      AasField {
        name = "SizingProjectInformation",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/SizingProjectInformation/1/0",
        type = refBy(SizingProjectInformation),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Descriptive information that characterizes and contextualizes the project itself."
      },
      AasField {
        name = "ApplicationRequirements",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/ApplicationRequirements/1/0",
        type = refBy(ApplicationRequirements),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Technical and environmental requirements from the customer which have to be fulfilled by the entire power drive train and its components."
      },
      AasField {
        name = "TransformationMechanism",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/TransformationMechanism/1/0",
        type = refBy(TransformationMechanism),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Devices used to convert one form of input energy or motion into another form. This can include the conversion of rotary motion to linear motion, the amplification of torque, or other mechanical conversions."
      },
      AasField {
        name = "SizingResult",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/SizingResult/1/0",
        type = refBy(SizingResult),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Insights resulting from the process of designing a drive system. This can include selecting the right components, calculating torques, speeds, power requirements, sizing motors, gearboxes and other elements."
      }
    }
  };

  AasSubmodelElementCollectionType SizingProjectInformation = {
    name = "SizingProjectInformation",
    semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/SizingProjectInformation/1/0",
    description = "Descriptive information that characterizes and contextualizes the project itself.",
    versionIdentifier = "IDTA 02021-1-0",
    fields = {
      AasField {
        name = "ClientName",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/ClientName/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"exaMPLe GmbH"},
        description = "Designation for a person or organization for which the s project was created."
      },
      AasField {
        name = "SizingProjectName",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/SizingProjectName/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Example Machine Conveyor"},
        description = "Meaningful name for labeling of the sizing project."
      },
      AasField {
        name = "SizingProjectDescription",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/SizingProjectDescription",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"@de: Transportband für neue Beispielmaschine mit Zahnriemen"},
        description = "Short description of the project, power drive train or application (short text) in common language."
      },
      AasField {
        name = "SizingProjectLink",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/SizingProjectLink/1/0",
        type = refBy(AasFileResourceType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"…/sizingproject1.aml"},
        description = "Resource for storing data of a sizing application, identified primarily by its file name."
      },
      AasField {
        name = "SizingToolName",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/SizingToolName/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"exaMPLe Sizer"},
        description = "Name or title of a computer program or application software. This name identifies the specific software with the help of which the sizing was performed."
      },
      AasField {
        name = "DateCreated",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/DateCreated/1/0",
        type = refBy(DateTimeType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"2002-05-30T09:30:10Z"},
        description = "The exact date and time that this file was created or generated on a computer system. This date indicates when the file was first created or captured on the system."
      },
      AasField {
        name = "DateChanged",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/DateChanged/1/0",
        type = refBy(DateTimeType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"2002-05-30T09:30:10Z"},
        description = "Exact date and time when the file was last edited, modified, or updated. This date indicates when the last modifications were made to the file."
      },
      AasField {
        name = "SizingProjectAxisReference",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/SizingProjectAxisReference/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Achse1"},
        description = "Unique identification number or identifier assigned to a specific axis within the design project."
      },
      AasField {
        name = "ContactInformation",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/ContactInformation",
        type = refBy(ContactInformation),
        minimumInstances = 1,
        description = "Note – collection for various contact information."
      },
      AasField {
        name = "AmlDriveConfigVersion",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/AmlDriveConfigVersion/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"V1.0.0"},
        description = "Used version of AML Application Reccomendation Drive Configurations with the help of which the sizing was performed."
      }
    }
  };

  AasSubmodelElementCollectionType ApplicationRequirements = {
    name = "ApplicationRequirements",
    semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/ApplicationRequirements/1/0",
    description = "Technical and environmental requirements from the customer which have to be fulfilled by the entire power drive train and its components.",
    versionIdentifier = "IDTA 02021-1-0",
    fields = {
      AasField {
        name = "MotionPattern",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MotionPattern/1/0",
        type = refBy(MotionPattern),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "The motion profile represents trajectories moving along different points."
      },
      AasField {
        name = "Environmental",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/EnvironmentalRequirements/1/0",
        type = refBy(Environmental),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Conditions and prerequisites necessary for the proper functioning, installation or use of the system."
      },
      AasField {
        name = "OverallSystemRequirements",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/OverallSystemRequirements/1/0",
        type = refBy(OverallSystemRequirements),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "basic criteria and specifications that apply to the installation, configuration, and operation of the system. These requirements typically include minimum specifications for hardware, software, security requirements, and other basic aspects necessary for the application to function smoothly."
      },
      AasField {
        name = "UsageProfile",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/UsageProfile/1/0",
        type = refBy(UsageProfile),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Characteristic behavior patterns with which the system or facility is used over the course of a day, week, or year."
      }
    }
  };

  AasSubmodelElementCollectionType MotionPattern = {
    name = "MotionPattern",
    semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MotionPattern/1/0",
    description = "The motion profile represents trajectories moving along different points.",
    versionIdentifier = "IDTA 02021-1-0",
    fields = {
      AasField {
        name = "MotionPatternName",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MotionPatternName/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Conveyor Motion Pattern 1"},
        description = "Designation that is assigned to a specific movement pattern or movement sequence."
      },
      AasField {
        name = "MotionPatternSections",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MotionPatternSections/1/0",
        type = refBy(MotionPatternSections),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Part of a motion profile, each of which represents a specific motion characteristic."
      }
    }
  };

  AasSubmodelElementCollectionType MotionPatternSections = {
    name = "MotionPatternSections",
    semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MotionPatternSections/1/0",
    description = "Part of a motion profile, each of which represents a specific motion characteristic.",
    versionIdentifier = "IDTA 02021-1-0",
    fields = {
      AasField {
        name = "RotativeSection",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/RotativeMotionPatternSection/1/0",
        type = refBy(RotativeSection),
        minimumInstances = 0,
        description = "Part of a motion profile that concerns a rotating motion."
      },
      AasField {
        name = "LinearSection",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LinearMotionPatternSection/1/0",
        type = refBy(LinearSection),
        minimumInstances = 0,
        description = "Part of a motion profile that concerns a linear motion."
      }
    }
  };

  AasSubmodelElementCollectionType RotativeSection = {
    name = "RotativeSection",
    semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/RotativeMotionPatternSection/1/0",
    description = "Part of a motion profile that concerns a rotating motion.",
    versionIdentifier = "IDTA 02021-1-0",
    fields = {
      AasField {
        name = "FrictionTorque",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FrictionTorque/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1 Nm"},
        description = "the restraining torque that acts between two objects in contact with each other."
      },
      AasField {
        name = "LeverArmAxialForce",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmAxialForce/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0.1 m"},
        description = "distance between the point of axial force application and the axis of rotation."
      },
      AasField {
        name = "AxialForce",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/AxialForce/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1 N"},
        description = "outer force on the rotating object parallel to the rotation axis."
      },
      AasField {
        name = "LeverArmRadialForce",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmRadialForce/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1 m"},
        description = "distance between the point of application of the radial force and the flange, referred to the flange coordinate system."
      },
      AasField {
        name = "RadialForce",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/RadialForce/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1 N"},
        description = "outer force on the rotating object orthogonal to the rotation axis."
      },
      AasField {
        name = "MomentOfInertiaOfLoad",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MomentOfInertiaOfLoad/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1 kg m²"},
        description = "Attempt of the object to be moved to maintain its state of motion."
      },
      AasField {
        name = "LoadTorque",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LoadTorque/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1 Nm"},
        description = "torque acting on the rotating mass from the outside. Positive values act in the direction of increasing angular positions in the angular position system."
      },
      AasField {
        name = "MetadataRotativeMotionFile",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MetadataRotativeMotionFile/1/0",
        type = refBy(MetadataRotativeMotionFile),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "descriptive attributes that are included in a CSV file in addition to the actual time series data. This metadata forms the columns of the CSV and provides information about the content of the time series data, the units of the measured quantities and other relevant details."
      },
      AasField {
        name = "MotionSectionFile",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MotionSectionFile/1/0",
        type = refBy(AasFileResourceType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"…/MotionSection1.csv"},
        description = "Sequence of data points in sequential order over a period of time within a paged data file."
      }
    }
  };

  AasSubmodelElementCollectionType MetadataRotativeMotionFile = {
    name = "MetadataRotativeMotionFile",
    semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MetadataRotativeMotionFile/1/0",
    description = "descriptive attributes that are included in a CSV file in addition to the actual time series data. This metadata forms the columns of the CSV and provides information about the content of the time series data, the units of the measured quantities and other relevant details.",
    versionIdentifier = "IDTA 02021-1-0",
    fields = {
      AasField {
        name = "Time",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/RelativePointInTime/1/1",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"1 s"},
        description = "Defined time within a time range, measured from the start time of the time range."
      },
      AasField {
        name = "AngularPosition",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/AngularPosition/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"1 rad"},
        description = "rotation angle relative to the origin of the angular position system."
      },
      AasField {
        name = "AngularVelocity",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/AngularVelocity/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"1 rad/s"},
        description = "temporal rate of change of the angular position in the angular position system. Positive values describe an angular change with increasing angular position."
      },
      AasField {
        name = "AngularAcceleration",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/AngularAcceleration/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"1 rad/s²"},
        description = "temporal rate of change in the angular velocity in the angular position system. Positive values describe a change in angular velocity with increasing angular position."
      },
      AasField {
        name = "AngularJerk",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/AngularJerk/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1 rad/?³"},
        description = "instantaneous time rate of change of angular acceleration of a object."
      },
      AasField {
        name = "FrictionTorque",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FrictionTorque/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1 Nm"},
        description = "the restraining torque that acts between two objects in contact with each other."
      },
      AasField {
        name = "AxialForce",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/AxialForce/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1 N"},
        description = "outer force on the rotating object parallel to the rotation axis."
      },
      AasField {
        name = "RadialForce",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/RadialForce/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1 N"},
        description = "outer force on the rotating object orthogonal to the rotation axis."
      },
      AasField {
        name = "LoadTorque",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LoadTorque/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1 Nm"},
        description = "torque acting on the rotating mass from the outside. Positive values act in the direction of increasing angular positions in the angular position system."
      }
    }
  };

  AasSubmodelElementCollectionType LinearSection = {
    name = "LinearSection",
    semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LinearMotionPatternSection/1/0",
    description = "Part of a motion profile that concerns a linear motion.",
    versionIdentifier = "IDTA 02021-1-0",
    fields = {
      AasField {
        name = "FrictionCoefficient",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FrictionCoefficient/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0.05"},
        description = "Ratio between the frictional force and the contact force between two objects."
      },
      AasField {
        name = "FrictionForce",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FrictionForce/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"652 N"},
        description = "The restraining force that acts between two objects in contact."
      },
      AasField {
        name = "CompensationForce",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/CompensationForce/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1 N"},
        description = "Force to compensate permanently acting forces."
      },
      AasField {
        name = "LoadMass",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LoadMass/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1 kg"},
        description = "mass of the object to be moved."
      },
      AasField {
        name = "LoadSideForce",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LoadSideForce/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0 N"},
        description = "force acting on the moving mass from the outside."
      },
      AasField {
        name = "CounterMass",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/CounterMass/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1 kg"},
        description = "mass whose weight force compensates permanently acting forces."
      },
      AasField {
        name = "MetadataLinearMotionFile",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MetadataLinearMotionFile/1/0",
        type = refBy(MetadataLinearMotionFile),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "descriptive attributes that are included in a CSV file in addition to the actual time series data. This metadata forms the columns of the CSV and provides information about the content of the time series data, the units of the measured quantities and other relevant details."
      },
      AasField {
        name = "MotionSectionFile",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MotionSectionFile/1/0",
        type = refBy(AasFileResourceType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"…/MotionSection1.csv"},
        description = "Sequence of data points in sequential order over a period of time within a paged data file."
      }
    }
  };

  AasSubmodelElementCollectionType MetadataLinearMotionFile = {
    name = "MetadataLinearMotionFile",
    semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MetadataRotativeMotionFile/1/0",
    description = "descriptive attributes that are included in a CSV file in addition to the actual time series data. This metadata forms the columns of the CSV and provides information about the content of the time series data, the units of the measured quantities and other relevant details.",
    versionIdentifier = "IDTA 02021-1-0",
    fields = {
      AasField {
        name = "Time",
        semanticId = "iri:https://admin-shell.io/idta/TimeSeries/RelativePointInTime/1/1",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"1 s"},
        description = "Defined time within a time range, measured from the start time of the time range."
      },
      AasField {
        name = "Position",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/Position/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"1 m"},
        description = "defined value of the location related to the zero point of the coordinate system."
      },
      AasField {
        name = "LinearVelocity",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LinearVelocity/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"1 m/s"},
        description = "temporal rate of change of position in the position coordinate system. Positive values describe a change in position with increasing position values."
      },
      AasField {
        name = "LinearAcceleration",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LinearAcceleration/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"1 m/s²"},
        description = "temporal rate of change of velocity in the position coordinate system. Positive values describe a change in velocity with increasing velocity values."
      },
      AasField {
        name = "LinearJerk",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LinearJerk/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1 m/?³"},
        description = "largest temporal rate of change of the acceleration."
      },
      AasField {
        name = "FrictionForce",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FrictionForce/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"652 N"},
        description = "The restraining force that acts between two objects in contact."
      },
      AasField {
        name = "LoadSideForce",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LoadSideForce/1/0",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0 N"},
        description = "force acting on the moving mass from the outside."
      }
    }
  };

  AasSubmodelElementCollectionType Environmental = {
    name = "Environmental",
    semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/EnvironmentalRequirements/1/0",
    description = "Conditions and prerequisites necessary for the proper functioning, installation or use of the system.",
    versionIdentifier = "IDTA 02021-1-0",
    fields = {
      AasField {
        name = "InstallationAltitude",
        semanticId = "irdi:0173-1#02-AAZ614#003",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1200 m"},
        description = "Altitude above sea level on which a device or installation is installed."
      },
      AasField {
        name = "Atex2Gas",
        semanticId = "irdi:0173-1#02-AAZ614#003",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"true"},
        description = "Information whether device is tested and approved according to ATEX II / gas."
      },
      AasField {
        name = "Atex2Dust",
        semanticId = "irdi:0173-1#02-AAR866#004",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"false"},
        description = "Information whether device is tested and approved according to ATEX II / dust."
      },
      AasField {
        name = "AmbientTemperatureMotor",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/AmbientTemperatureMotor/1/0",
        type = refBy(AasRangeType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"-10 – 60 °C"},
        description = "Temperature in the outer area of the motor and gear during operation."
      },
      AasField {
        name = "AmbientTemperatureController",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/AmbientTemperatureController/1/0",
        type = refBy(AasRangeType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"-10 – 60 °C"},
        description = "Temperature in the outer area of the drive during operation."
      }
    }
  };

  AasSubmodelElementCollectionType OverallSystemRequirements = {
    name = "OverallSystemRequirements",
    semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/OverallSystemRequirements/1/0",
    description = "basic criteria and specifications that apply to the installation, configuration, and operation of the system. These requirements typically include minimum specifications for hardware, software, security requirements, and other basic aspects necessary for the application to function smoothly.",
    versionIdentifier = "IDTA 02021-1-0",
    fields = {
      AasField {
        name = "DcLinkCoupling",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/DcLinkCoupling/1/0",
        type = refBy(BooleanType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"false"},
        description = "Statement whether there is a connection between the DC links or not."
      },
      AasField {
        name = "BrakePresent",
        semanticId = "irdi:0173-1#02-BAE085#007",
        type = refBy(BooleanType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"true"},
        description = "Brake present."
      },
      AasField {
        name = "MainsConnection",
        semanticId = "irdi:0173-1#02-ABF822#003",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"230 VAC"},
        description = "Line side link converter."
      },
      AasField {
        name = "MountingType",
        semanticId = "irdi:0173-1#02-AAH167#006",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"geflanscht","with value ID","0173-1#07-AAX411#002"},
        description = "Type of mounting."
      },
      AasField {
        name = "MinSwitchingFrequency",
        semanticId = "irdi:0173-1#02-AAN329#003",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"100 Hz"},
        description = "lowest switching frequency for which the device is designed to operate."
      },
      AasField {
        name = "CoolingType",
        semanticId = "irdi:0173-1#02-BAE122#007",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Air-air heat exchanger"},
        description = "Summary of various types of cooling, for use as search criteria that limit a selection."
      },
      AasField {
        name = "ProtectionType",
        semanticId = "irdi:0173-1#02-BAG342#007",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"IP67"},
        description = "Summary of the various IP protection degrees to achieve a limited selected for search features."
      },
      AasField {
        name = "CertificateApproval",
        semanticId = "irdi:0173-1#02-BAB392#018",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"CE"},
        description = "certification of class properties through certification, with which it is confirmed that the product a) corresponds to the significant technical specifications and b) is subjected to a factory-internal production control as well as c) external monitoring, or a permit to market or exploit a product, a process or service for the given purpose or under given conditions."
      },
      AasField {
        name = "SafetyIntegrityLevel",
        semanticId = "irdi:0173-1#02-ABH715#002",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"SIL2"},
        description = "safety integrity level (SIL) according to IEC 61508."
      }
    }
  };

  AasSubmodelElementCollectionType UsageProfile = {
    name = "UsageProfile",
    semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/UsageProfile/1/0",
    description = "Characteristic behavior patterns with which the system or facility is used over the course of a day, week, or year.",
    versionIdentifier = "IDTA 02021-1-0",
    fields = {
      AasField {
        name = "CyclesPerMinute",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/CyclesPerMinute/1/0",
        type = refBy(IntegerType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"12"},
        description = "Number of complete repetitions of the motion profile in one minute."
      },
      AasField {
        name = "OperatingHoursPerDay",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/OperatingHoursPerDay/1/0",
        type = refBy(DoubleType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"6 h"},
        description = "Hours during which the plant, in particular the drive system, is in operation during a single day."
      },
      AasField {
        name = "OperatingDaysPerYear",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/OperatingDaysPerYear/1/0",
        type = refBy(DoubleType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"191"},
        description = "Days during which the plant, in particular the drive system, is in operation during a year."
      }
    }
  };

  AasSubmodelElementCollectionType TransformationMechanism = {
    name = "TransformationMechanism",
    semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/TransformationMechanism/1/0",
    description = "Devices used to convert one form of input energy or motion into another form. This can include the conversion of rotary motion to linear motion, the amplification of torque, or other mechanical conversions. All application mechanisms are listed in the submodel template - note that only one application mechanism can be selected in the design project instance.",
    versionIdentifier = "IDTA 02021-1-0",
    fields = {
      AasField {
        name = "Fan",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/Fan/1/0",
        type = refBy(Fan),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Fan."
      },
      AasField {
        name = "Pump",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/Pump/1/0",
        type = refBy(Pump),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Pump."
      },
      AasField {
        name = "RotaryTable",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/RotaryTable/1/0",
        type = refBy(RotaryTable),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Rotary table."
      },
      AasField {
        name = "ChainConveyor",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/ChainConveyor/1/0",
        type = refBy(ChainConveyor),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Chain conveyor."
      },
      AasField {
        name = "BeltConveyor",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/BeltConveyor/1/0",
        type = refBy(BeltConveyor),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Belt conveyor."
      },
      AasField {
        name = "RollerConveyor",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/RollerConveyor/1/0",
        type = refBy(RollerConveyor),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Roller conveyor."
      },
      AasField {
        name = "BeltDrive",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/BeltDrive/1/0",
        type = refBy(BeltDrive),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Belt Drive."
      },
      AasField {
        name = "TravelingDrive",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/TravelingDrive/1/0",
        type = refBy(TravelingDrive),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Traveling drive."
      },
      AasField {
        name = "RackDrive",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/RollerConveyor/1/0",
        type = refBy(RackDrive),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Rack drive."
      },
      AasField {
        name = "SpindleDrive",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/SpindleDrive/1/0",
        type = refBy(SpindleDrive),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Spindle drive."
      },
      AasField {
        name = "RotativeApplication",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/GenericRotativeApplication/1/0",
        type = refBy(AasGenericSubmodelElementCollection),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "technical device that transforms movement of components in a machine or system into rotational movements."
      },
      AasField {
        name = "LinearApplication",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/GenericLinearApplication/1/0",
        type = refBy(AasGenericSubmodelElementCollection),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "technical device that transforms rotational motion of components in a machine or system into linear motion."
      }
    }
  };

  AasEntityType Fan = {
    name = "Fan",
    semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/Fan/1/0",
    description = "technical device that transforms movement of components in a machine or system into rotational movements.",
    versionIdentifier = "IDTA 02021-1-0",
    fields = {
      AasField {
        name = "Efficiency",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/Efficiency/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0,98"},
        description = "Ratio between the actual useful power or work performed by the device and the energy or power supplied. It expresses how efficiently the device converts the energy into the desired work, taking into account losses and friction."
      },
      AasField {
        name = "InertiaMotorSide",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InertiaMotorSide/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"3 kgm²"},
        description = "additional mass or inertia added on the side of a drive system or motor. This additional mass can be caused by various factors, such as the own rotor inertia (including pinion), heavier gears or mechanical loads added to the drive system."
      },
      AasField {
        name = "LeverArmAxialForce",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmAxialForce/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2 m"},
        description = "distance between the point of axial force application and the axis of rotation."
      },
      AasField {
        name = "LeverArmRadialForce",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmRadialForce/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1,1 m"},
        description = "distance between the point of application of the radial force and the flange, referred to the flange coordinate system."
      },
      AasField {
        name = "NoLoadTorque",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/NoLoadTorque/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1,1 Nm"},
        description = "The minimum torque required to set the application in motion without overcoming an external load."
      },
      AasField {
        name = "InclinationAngle",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InclinationAngle/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"5 rad"},
        description = "Angle at which a linear application is inclined with respect to the horizontal."
      }
    }
  };

  AasEntityType Pump = {
    name = "Pump",
    semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/Pump/1/0",
    description = "technical device that transforms movement of components in a machine or system into rotational movements.",
    versionIdentifier = "IDTA 02021-1-0",
    fields = {
      AasField {
        name = "Efficiency",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/Efficiency/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0,98"},
        description = "Ratio between the actual useful power or work performed by the device and the energy or power supplied. It expresses how efficiently the device converts the energy into the desired work, taking into account losses and friction."
      },
      AasField {
        name = "InertiaMotorSide",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InertiaMotorSide/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"3 kgm²"},
        description = "additional mass or inertia added on the side of a drive system or motor. This additional mass can be caused by various factors, such as the own rotor inertia (including pinion), heavier gears or mechanical loads added to the drive system."
      },
      AasField {
        name = "LeverArmAxialForce",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmAxialForce/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2 m"},
        description = "distance between the point of axial force application and the axis of rotation."
      },
      AasField {
        name = "LeverArmRadialForce",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmRadialForce/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1,1 m"},
        description = "distance between the point of application of the radial force and the flange, referred to the flange coordinate system."
      },
      AasField {
        name = "NoLoadTorque",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/NoLoadTorque/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1,1 Nm"},
        description = "The minimum torque required to set the application in motion without overcoming an external load."
      },
      AasField {
        name = "InclinationAngle",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InclinationAngle/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"5 rad"},
        description = "Angle at which a linear application is inclined with respect to the horizontal."
      }
    }
  };

  AasEntityType RotaryTable = {
    name = "RotaryTable",
    semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/RotraryTable/1/0",
    description = "technical device that transforms movement of components in a machine or system into rotational movements.",
    versionIdentifier = "IDTA 02021-1-0",
    fields = {
      AasField {
        name = "Efficiency",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/Efficiency/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0,98"},
        description = "Ratio between the actual useful power or work performed by the device and the energy or power supplied. It expresses how efficiently the device converts the energy into the desired work, taking into account losses and friction."
      },
      AasField {
        name = "InertiaMotorSide",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InertiaMotorSide/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"3 kgm²"},
        description = "additional mass or inertia added on the side of a drive system or motor. This additional mass can be caused by various factors, such as the own rotor inertia (including pinion), heavier gears or mechanical loads added to the drive system."
      },
      AasField {
        name = "LeverArmAxialForce",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmAxialForce/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2 m"},
        description = "distance between the point of axial force application and the axis of rotation."
      },
      AasField {
        name = "LeverArmRadialForce",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmRadialForce/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1,1 m"},
        description = "distance between the point of application of the radial force and the flange, referred to the flange coordinate system."
      },
      AasField {
        name = "NoLoadTorque",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/NoLoadTorque/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1,1 Nm"},
        description = "The minimum torque required to set the application in motion without overcoming an external load."
      },
      AasField {
        name = "InclinationAngle",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InclinationAngle/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"5 rad"},
        description = "Angle at which a linear application is inclined with respect to the horizontal."
      },
      AasField {
        name = "StaticEccentricity",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/StaticEccentricity/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"5 m"},
        description = "Distance between the table's center of rotation and the point bearing the payload. Static, not variable via the motion profile."
      },
      AasField {
        name = "CentroidAngle",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/CentroidAngle/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"5 rad"},
        description = "The center angle refers to the angle describing the center of a circle. In the context of a rotary table, the center angle refers to the angle by which a rotary table has been rotated relative to a reference position. It is used to determine the exact position of the table. (Positive angle to start movement)."
      }
    }
  };

  AasEntityType ChainConveyor = {
    name = "ChainConveyor",
    semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/ChainConveyor/1/0",
    description = "technical device that transforms rotational motion of components in a machine or system into linear motion.",
    versionIdentifier = "IDTA 02021-1-0",
    fields = {
      AasField {
        name = "Efficiency",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/Efficiency/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0,98"},
        description = "Ratio between the actual useful power or work performed by the device and the energy or power supplied. It expresses how efficiently the device converts the energy into the desired work, taking into account losses and friction."
      },
      AasField {
        name = "InertiaMotorSide",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InertiaMotorSide/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"3 kgm²"},
        description = "additional mass or inertia added on the side of a drive system or motor. This additional mass can be caused by various factors, such as the own rotor inertia (including pinion), heavier gears or mechanical loads added to the drive system."
      },
      AasField {
        name = "LeverArmAxialForce",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmAxialForce/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2 m"},
        description = "distance between the point of axial force application and the axis of rotation."
      },
      AasField {
        name = "LeverArmRadialForce",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmRadialForce/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1,1 m"},
        description = "distance between the point of application of the radial force and the flange, referred to the flange coordinate system."
      },
      AasField {
        name = "NoLoadTorque",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/NoLoadTorque/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1,1 Nm"},
        description = "The minimum torque required to set the application in motion without overcoming an external load."
      },
      AasField {
        name = "InclinationAngle",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InclinationAngle/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"5 rad"},
        description = "Angle at which a linear application is inclined with respect to the horizontal."
      },
      AasField {
        name = "FrictionCoefficient",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FrictionCoefficient/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0,02"},
        description = "Ratio between the frictional force and the contact force between two objects."
      },
      AasField {
        name = "FeedConstant",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FeedConstant/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"40 m/rev"},
        description = "Parameter in technical systems that describes the relationship between a single rotary motion and the resulting linear motion or feed motion."
      }
    }
  };

  AasEntityType BeltConveyor = {
    name = "BeltConveyor",
    semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/BeltConveyor/1/0",
    description = "technical device that transforms rotational motion of components in a machine or system into linear motion.",
    versionIdentifier = "IDTA 02021-1-0",
    fields = {
      AasField {
        name = "Efficiency",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/Efficiency/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0,98"},
        description = "Ratio between the actual useful power or work performed by the device and the energy or power supplied. It expresses how efficiently the device converts the energy into the desired work, taking into account losses and friction."
      },
      AasField {
        name = "InertiaMotorSide",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InertiaMotorSide/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"3 kgm²"},
        description = "additional mass or inertia added on the side of a drive system or motor. This additional mass can be caused by various factors, such as the own rotor inertia (including pinion), heavier gears or mechanical loads added to the drive system."
      },
      AasField {
        name = "LeverArmAxialForce",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmAxialForce/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2 m"},
        description = "distance between the point of axial force application and the axis of rotation."
      },
      AasField {
        name = "LeverArmRadialForce",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmRadialForce/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1,1 m"},
        description = "distance between the point of application of the radial force and the flange, referred to the flange coordinate system."
      },
      AasField {
        name = "NoLoadTorque",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/NoLoadTorque/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1,1 Nm"},
        description = "The minimum torque required to set the application in motion without overcoming an external load."
      },
      AasField {
        name = "InclinationAngle",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InclinationAngle/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"5 rad"},
        description = "Angle at which a linear application is inclined with respect to the horizontal."
      },
      AasField {
        name = "FrictionCoefficient",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FrictionCoefficient/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0,02"},
        description = "Ratio between the frictional force and the contact force between two objects."
      },
      AasField {
        name = "FeedConstant",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FeedConstant/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"40 m/rev"},
        description = "Parameter in technical systems that describes the relationship between a single rotary motion and the resulting linear motion or feed motion."
      }
    }
  };

  AasEntityType RollerConveyor = {
    name = "RollerConveyor",
    semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/RollerConveyor/1/0",
    description = "technical device that transforms rotational motion of components in a machine or system into linear motion.",
    versionIdentifier = "IDTA 02021-1-0",
    fields = {
      AasField {
        name = "Efficiency",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/Efficiency/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0,98"},
        description = "Ratio between the actual useful power or work performed by the device and the energy or power supplied. It expresses how efficiently the device converts the energy into the desired work, taking into account losses and friction."
      },
      AasField {
        name = "InertiaMotorSide",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InertiaMotorSide/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"3 kgm²"},
        description = "additional mass or inertia added on the side of a drive system or motor. This additional mass can be caused by various factors, such as the own rotor inertia (including pinion), heavier gears or mechanical loads added to the drive system."
      },
      AasField {
        name = "LeverArmAxialForce",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmAxialForce/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2 m"},
        description = "distance between the point of axial force application and the axis of rotation."
      },
      AasField {
        name = "LeverArmRadialForce",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmRadialForce/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1,1 m"},
        description = "distance between the point of application of the radial force and the flange, referred to the flange coordinate system."
      },
      AasField {
        name = "NoLoadTorque",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/NoLoadTorque/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1,1 Nm"},
        description = "The minimum torque required to set the application in motion without overcoming an external load."
      },
      AasField {
        name = "InclinationAngle",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InclinationAngle/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"5 rad"},
        description = "Angle at which a linear application is inclined with respect to the horizontal."
      },
      AasField {
        name = "FrictionCoefficient",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FrictionCoefficient/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0,02"},
        description = "Ratio between the frictional force and the contact force between two objects."
      },
      AasField {
        name = "FeedConstant",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FeedConstant/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"40 m/rev"},
        description = "Parameter in technical systems that describes the relationship between a single rotary motion and the resulting linear motion or feed motion."
      }
    }
  };

  AasEntityType BeltDrive = {
    name = "BeltDrive",
    semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/BeltDrive/1/0",
    description = "technical device that transforms rotational motion of components in a machine or system into linear motion.",
    versionIdentifier = "IDTA 02021-1-0",
    fields = {
      AasField {
        name = "Efficiency",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/Efficiency/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0,98"},
        description = "Ratio between the actual useful power or work performed by the device and the energy or power supplied. It expresses how efficiently the device converts the energy into the desired work, taking into account losses and friction."
      },
      AasField {
        name = "InertiaMotorSide",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InertiaMotorSide/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"3 kgm²"},
        description = "additional mass or inertia added on the side of a drive system or motor. This additional mass can be caused by various factors, such as the own rotor inertia (including pinion), heavier gears or mechanical loads added to the drive system."
      },
      AasField {
        name = "LeverArmAxialForce",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmAxialForce/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2 m"},
        description = "distance between the point of axial force application and the axis of rotation."
      },
      AasField {
        name = "LeverArmRadialForce",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmRadialForce/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1,1 m"},
        description = "distance between the point of application of the radial force and the flange, referred to the flange coordinate system."
      },
      AasField {
        name = "NoLoadTorque",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/NoLoadTorque/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1,1 Nm"},
        description = "The minimum torque required to set the application in motion without overcoming an external load."
      },
      AasField {
        name = "InclinationAngle",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InclinationAngle/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"5 rad"},
        description = "Angle at which a linear application is inclined with respect to the horizontal."
      },
      AasField {
        name = "FrictionCoefficient",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FrictionCoefficient/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0,02"},
        description = "Ratio between the frictional force and the contact force between two objects."
      },
      AasField {
        name = "FeedConstant",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FeedConstant/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"40 m/rev"},
        description = "Parameter in technical systems that describes the relationship between a single rotary motion and the resulting linear motion or feed motion."
      }
    }
  };

  AasEntityType TravelingDrive = {
    name = "TravelingDrive",
    semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/TravelingDrive/1/0",
    description = "technical device that transforms rotational motion of components in a machine or system into linear motion.",
    versionIdentifier = "IDTA 02021-1-0",
    fields = {
      AasField {
        name = "Efficiency",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/Efficiency/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0,98"},
        description = "Ratio between the actual useful power or work performed by the device and the energy or power supplied. It expresses how efficiently the device converts the energy into the desired work, taking into account losses and friction."
      },
      AasField {
        name = "InertiaMotorSide",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InertiaMotorSide/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"3 kgm²"},
        description = "additional mass or inertia added on the side of a drive system or motor. This additional mass can be caused by various factors, such as the own rotor inertia (including pinion), heavier gears or mechanical loads added to the drive system."
      },
      AasField {
        name = "LeverArmAxialForce",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmAxialForce/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2 m"},
        description = "distance between the point of axial force application and the axis of rotation."
      },
      AasField {
        name = "LeverArmRadialForce",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmRadialForce/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1,1 m"},
        description = "distance between the point of application of the radial force and the flange, referred to the flange coordinate system."
      },
      AasField {
        name = "NoLoadTorque",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/NoLoadTorque/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1,1 Nm"},
        description = "The minimum torque required to set the application in motion without overcoming an external load."
      },
      AasField {
        name = "InclinationAngle",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InclinationAngle/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"5 rad"},
        description = "Angle at which a linear application is inclined with respect to the horizontal."
      },
      AasField {
        name = "FrictionCoefficient",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FrictionCoefficient/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0,02"},
        description = "Ratio between the frictional force and the contact force between two objects."
      },
      AasField {
        name = "FeedConstant",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FeedConstant/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"40 m/rev"},
        description = "Parameter in technical systems that describes the relationship between a single rotary motion and the resulting linear motion or feed motion."
      }
    }
  };

  AasEntityType RackDrive = {
    name = "RackDrive",
    semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/RackDrive/1/0",
    description = "technical device that transforms rotational motion of components in a machine or system into linear motion.",
    versionIdentifier = "IDTA 02021-1-0",
    fields = {
      AasField {
        name = "Efficiency",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/Efficiency/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0,98"},
        description = "Ratio between the actual useful power or work performed by the device and the energy or power supplied. It expresses how efficiently the device converts the energy into the desired work, taking into account losses and friction."
      },
      AasField {
        name = "InertiaMotorSide",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InertiaMotorSide/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"3 kgm²"},
        description = "additional mass or inertia added on the side of a drive system or motor. This additional mass can be caused by various factors, such as the own rotor inertia (including pinion), heavier gears or mechanical loads added to the drive system."
      },
      AasField {
        name = "LeverArmAxialForce",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmAxialForce/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2 m"},
        description = "distance between the point of axial force application and the axis of rotation."
      },
      AasField {
        name = "LeverArmRadialForce",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmRadialForce/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1,1 m"},
        description = "distance between the point of application of the radial force and the flange, referred to the flange coordinate system."
      },
      AasField {
        name = "NoLoadTorque",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/NoLoadTorque/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1,1 Nm"},
        description = "The minimum torque required to set the application in motion without overcoming an external load."
      },
      AasField {
        name = "InclinationAngle",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InclinationAngle/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"5 rad"},
        description = "Angle at which a linear application is inclined with respect to the horizontal."
      },
      AasField {
        name = "FrictionCoefficient",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FrictionCoefficient/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0,02"},
        description = "Ratio between the frictional force and the contact force between two objects."
      },
      AasField {
        name = "FeedConstant",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FeedConstant/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"40 m/rev"},
        description = "Parameter in technical systems that describes the relationship between a single rotary motion and the resulting linear motion or feed motion."
      },
      AasField {
        name = "DiameterPinion",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/DiameterPinion/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0.14 m"},
        description = "Distance from one edge to the opposite edge of the tooth wheel, measured through the center."
      },
      AasField {
        name = "HelixAngle",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/HelixAngle/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"14 rad"},
        description = "The helix angle determines the direction of the teeth. The helix angle is measured between the axis and the tooth. If the helix angle = 0°, the teeth are straight. If the helix angle > 0°, the teeth are helical."
      },
      AasField {
        name = "MovingPart",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MovingPart/1/0",
        type = refBy(MovingPart),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0.14 m"},
        description = "The part that acts in a linear motion along the teeth."
      }
    }
  };

  AasEntityType SpindleDrive = {
    name = "SpindleDrive",
    semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/SpindleDrive/1/0",
    description = "technical device that transforms rotational motion of components in a machine or system into linear motion.",
    versionIdentifier = "IDTA 02021-1-0",
    fields = {
      AasField {
        name = "Efficiency",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/Efficiency/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0,98"},
        description = "Ratio between the actual useful power or work performed by the device and the energy or power supplied. It expresses how efficiently the device converts the energy into the desired work, taking into account losses and friction."
      },
      AasField {
        name = "InertiaMotorSide",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InertiaMotorSide/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"3 kgm²"},
        description = "additional mass or inertia added on the side of a drive system or motor. This additional mass can be caused by various factors, such as the own rotor inertia (including pinion), heavier gears or mechanical loads added to the drive system."
      },
      AasField {
        name = "LeverArmAxialForce",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmAxialForce/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2 m"},
        description = "distance between the point of axial force application and the axis of rotation."
      },
      AasField {
        name = "LeverArmRadialForce",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmRadialForce/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1,1 m"},
        description = "distance between the point of application of the radial force and the flange, referred to the flange coordinate system."
      },
      AasField {
        name = "NoLoadTorque",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/NoLoadTorque/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1,1 Nm"},
        description = "The minimum torque required to set the application in motion without overcoming an external load."
      },
      AasField {
        name = "InclinationAngle",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InclinationAngle/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"5 rad"},
        description = "Angle at which a linear application is inclined with respect to the horizontal."
      },
      AasField {
        name = "FrictionCoefficient",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FrictionCoefficient/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0,02"},
        description = "Ratio between the frictional force and the contact force between two objects."
      },
      AasField {
        name = "FeedConstant",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FeedConstant/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"40 m/rev"},
        description = "Parameter in technical systems that describes the relationship between a single rotary motion and the resulting linear motion or feed motion."
      }
    }
  };

  AasSubmodelElementCollectionType SizingResult = {
    name = "SizingResult",
    semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/SizingResult/1/0",
    description = "Insights resulting from the process of designing a drive system. This can include selecting the right components, calculating torques, speeds, power requirements, sizing motors, gearboxes and other elements.",
    versionIdentifier = "IDTA 02021-1-0",
    fields = {
      AasField {
        name = "OverallSystem",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/OverallSystem/1/0",
        type = refBy(OverallSystem),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Drive unit created in the design that consists of all components or parts that work together in the drive design to perform the drive task."
      },
      AasField {
        name = "MainComponent",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MainComponent/1/0",
        type = refBy(MainComponent),
        minimumInstances = 0,
        description = "Key component of an electric axis, such as drive controller, motor or gearbox, which significantly influences and controls the functionality and performance of the entire system."
      },
      AasField {
        name = "OtherComponents",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/OtherComponent/1/0",
        type = refBy(OtherComponent),
        minimumInstances = 0,
        description = "Additional parts or elements identified in the design that are present in a system, device or installation in addition to the main components."
      },
      AasField {
        name = "Messages",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/Messages/1/0",
        type = refBy(Messages),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Messages and notes on the interpretation and its results."
      },
      AasField {
        name = "TextStatement",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/TextStatement/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Declaration of the design service provider in text form, e.g. scope of validity of the statements, areas of application, conditions of use of the sizing result."
      }
    }
  };

  AasEntityType OverallSystem = {
    name = "OverallSystem",
    semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/OverallSystem/1/0",
    description = "Drive unit created in the design that consists of all components or parts that work together in the drive design to perform the drive task.",
    versionIdentifier = "IDTA 02021-1-0",
    fields = {
      AasField {
        name = "ManufacturerName",
        semanticId = "irdi:0173-1#02-AAO677#002",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"xample Company"},
        description = "Legally valid designation of the natural or judicial body which is directly responsible for the design, production, packaging and labeling of a product in respect to its being brought into the market."
      },
      AasField {
        name = "ManufacturerArticleNumber",
        semanticId = "irdi:0173-1#02-AAO676#003",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"A123-456"},
        description = "unique product identifier of the manufacturer."
      },
      AasField {
        name = "ManufacturerProductDesignation",
        semanticId = "irdi:0173-1#02-AAW338#001",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"X-Axis for turning machine 123@en","X-Achse für Drehmaschine 123@de"},
        description = "Product designation as given by the manufacturer. Short description of the product, product group or function (short text) in common language."
      },
      AasField {
        name = "ManufacturerOrderCode",
        semanticId = "irdi:0173-1#02-AAO227#002",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"EEA-EX-200-S/47-Q3"},
        description = "By manufactures issued unique combination of numbers and letters used to identify the device for ordering."
      },
      AasField {
        name = "ExternalMomentOfInertia",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/ExternalMomentOfInertia/1/0",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"341 kg m2"},
        description = "Moment of inertia of the parts outside the drive. It describes the resistance of these parts to changes in their rotational speed and influences the energy required to accelerate or decelerate the system."
      },
      AasField {
        name = "InternalMomentOfIntertia",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InternalMomentOfInertia/1/0",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"341 kg m2"},
        description = "Moment of inertia of the moving parts within the drive, such as a motor or gearbox. It describes the resistance of these parts to changes in their rotational speed and affects the energy required to accelerate or decelerate the system."
      },
      AasField {
        name = "MassInertiaRatio",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MassInertiaRatio/1/0",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0,2"},
        description = "The mass inertia ratio ? is the ratio of external mass inertia (application side) to internal mass inertia (motor and gearbox side). It is an important parameter for the controllability of an application. The more different the mass moments of inertia are and the larger ? becomes, the less precisely dynamic processes can be controlled."
      },
      AasField {
        name = "DecelerationForEmergencyStop",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/DecelerationForEmergencyStop/1/0",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"120 rpm/s"},
        description = "Rate (in revolutions per minute per second) at which the speed of a rotating system is reduced when an emergency stop is activated. This parameter is important to describe the rate at which the system is brought to a stop after an emergency stop is triggered to ensure the safety of people and equipment."
      },
      AasField {
        name = "CurrentForEmergencyStop",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/CurrentForEmergencyStop/1/0",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"16 A"},
        description = "electrical current required for the operation of an emergency stop system."
      },
      AasField {
        name = "DisplacementDuringEmergencyStop",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/DisplacementDuringEmergencyStop/1/0",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0,3 rad"},
        description = "Displacement of mechanical components or parts in a machine or plant caused by the triggering of an emergency stop signal."
      },
      AasField {
        name = "EnergyConsumtionPerCycle",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/EnergyConsumtionPerCycle/1/0",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"43 kWh"},
        description = "Energy consumed during a single operating cycle of a system, machine or device."
      }
    }
  };

  AasEntityType MainComponent = {
    name = "MainComponent",
    semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MainComponent/1/0",
    description = "Key component of an electric axis, such as drive controller, motor or gearbox, which significantly influences and controls the functionality and performance of the entire system.",
    versionIdentifier = "IDTA 02021-1-0",
    fields = {
      AasField {
        name = "MainComponentType",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MainComponentType/1/0",
        type = refBy(MainComponentType),
        minimumInstances = 1,
        examples = {"Motor"},
        description = "The kind of main component in the drive train, such as a motor, gearbox or drive controller."
      },
      AasField {
        name = "ManufacturerName",
        semanticId = "irdi:0173-1#02-AAO677#002",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Example Company"},
        description = "Legally valid designation of the natural or judicial body which is directly responsible for the design, production, packaging and labeling of a product in respect to its being brought into the market."
      },
      AasField {
        name = "ManufacturerArticleNumber",
        semanticId = "irdi:0173-1#02-AAO676#003",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"A123-456"},
        description = "unique product identifier of the manufacturer."
      },
      AasField {
        name = "ManufacturerProductDesignation",
        semanticId = "irdi:0173-1#02-AAW338#001",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"X-Axis for turning machine 123@en","X-Achse für Drehmaschine 123@de"},
        description = "Product designation as given by the manufacturer. Short description of the product, product group or function (short text) in common language."
      },
      AasField {
        name = "ManufacturerOrderCode",
        semanticId = "irdi:0173-1#02-AAO227#002",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"EEA-EX-200-S/47-Q3"},
        description = "By manufactures issued unique combination of numbers and letters used to identify the device for ordering."
      },
      AasField {
        name = "MaxCurrentUtilizationPercentage",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MaxCurrentUtilizationPercentage/1/0",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"78 %"},
        description = "The maximum calculated effective peak current within the motion profile based on the technical specification of the component."
      },
      AasField {
        name = "MaxCurrentUtilization",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MaxCurrentUtilization/1/0",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"16 A"},
        description = "The maximum calculated effective peak current within the motion profile."
      },
      AasField {
        name = "MaxThermalUtilizationPercentage",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MaxThermalUtilizationPercentage/1/0",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"80 %"},
        description = "The calculated maximum thermal utilization of the component based on its specification."
      },
      AasField {
        name = "MaxThermalUtilization",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MaxThermalUtilization/1/0",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"70 °C"},
        description = "The calculated maximum thermal utilization of the component."
      },
      AasField {
        name = "AveragePowerLosses",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/AveragePowerLosses/1/0",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"60 W"},
        description = "The average electrical power loss in watts for the component within the motion profile."
      },
      AasField {
        name = "AverageRegenerativePowerDcLink",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/AverageRegenerativePowerDcLink/1/0",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"20 W"},
        description = "The power averaged over the motion profile that flows back to the system DC link."
      },
      AasField {
        name = "MaxRegenerativePowerDcLink",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MaxRegenerativePowerDcLink/1/0",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"35 W"},
        description = "The power maximum over the motion profile that flows back to the system DC link."
      },
      AasField {
        name = "AverageRegenerativePowerMains",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/AverageFeedInPowerMains/1/0",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"20 W"},
        description = "The average power fed into the power grid over the motion profile."
      },
      AasField {
        name = "MaxRegenerativePowerMains",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MaxFeedInPowerMains/1/0",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"35 W"},
        description = "The maximum power fed into the power grid within the motion profile."
      },
      AasField {
        name = "ContinuousCurrent",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/ContinuousCurrent/1/0",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"16 A"},
        description = "Maximum permissible continuous current that the drive controller can supply to the connected electric motor without causing overload or damage."
      },
      AasField {
        name = "RmsOfPower",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/RmsOfPower/1/0",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"35 W"},
        description = "Average of the power over the motion profile, taking into account the fluctuations."
      },
      AasField {
        name = "MaxTorqueUtilizationPercentage",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MaxTorqueUtilizationPercentage/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"78 %"},
        description = "The maximum torque utilization of the component calculated during the motion profile based on its permissible specification."
      },
      AasField {
        name = "MaxTorqueUtilization",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MaxTorqueUtilization/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"500 Nm"},
        description = "The maximum torque utilization of the component calculated during the motion profile."
      },
      AasField {
        name = "MaxRotationSpeedUtilizationPercentage",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MaxRotationSpeedUtilizationPercentage/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"20 %"},
        description = "The maximum speed utilization of the component calculated during the motion profile based on its permissible specification."
      },
      AasField {
        name = "MaxRotationSpeedUtilization",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MaxRotationSpeedUtilization/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1200 rpm"},
        description = "The maximum speed utilization of the component calculated during the motion profile."
      },
      AasField {
        name = "EffectiveUtilization",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/EffectiveUtilization/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"55 %"},
        description = "Degree to which the motor is actually used to perform mechanical work compared to its maximum capacity. It is the ratio between the power actually produced and the maximum possible power of the engine."
      },
      AasField {
        name = "CalculatedServiceLife",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/CalculatedServiceLife/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"120000 h"},
        description = "components or complete failure under the operating conditions which were calculated in the Sizing."
      },
      AasField {
        name = "MassInertiaRatio",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MassInertiaRatio/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0,2"},
        description = "The mass inertia ratio ? is the ratio of external mass inertia (application side) to internal mass inertia (motor and gearbox side). It is an important parameter for the controllability of an application. The more different the mass moments of inertia are and the larger ? becomes, the less precisely dynamic processes can be controlled."
      },
      AasField {
        name = "FrequencyAtMaxSpeed",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FrequencyAtMaxSpeed/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1200 Hz"},
        description = "Specifies how many times the shaft of the motor rotates completely per second when the motor is operated at its maximum output."
      },
      AasField {
        name = "PowerInRegenerativeOperation",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/PowerInRegenerativeOperation/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1200 W"},
        description = "electrical energy generated by a motor over the motion profile when it operates as a generator and converts mechanical energy into electrical energy."
      },
      AasField {
        name = "PowerInMotorOperation",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/PowerInMotorOperation/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1200 W"},
        description = "electrical energy used by a motor over the motion profile when it converts electrical energy into mechanical energy."
      },
      AasField {
        name = "RmsOfMotorTorque",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/RmsOfMotorTorque/1/0",
        type = refBy(LongType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1200 Hz"},
        description = "average value of the torque of a motor over the motion profile. This value takes into account both positive and negative fluctuations of the torque during a complete cycle and thus gives a more stable idea of the average torque output of the motor."
      },
      AasField {
        name = "EnergyConsumtionPerCycle",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/EnergyConsumtionPerCycle/1/0",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"43 kWh"},
        description = "Energy consumed during a single operating cycle of a system, machine or device."
      }
    }
  };

  AasEntityType OtherComponent = {
    name = "OtherComponent",
    semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/OtherComponent/1/0",
    description = "Additional parts or elements identified in the design that are present in a system, device or installation in addition to the main components.",
    versionIdentifier = "IDTA 02021-1-0",
    fields = {
      AasField {
        name = "ManufacturerName",
        semanticId = "irdi:0173-1#02-AAO677#002",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"xample Company"},
        description = "Legally valid designation of the natural or judicial body which is directly responsible for the design, production, packaging and labeling of a product in respect to its being brought into the market."
      },
      AasField {
        name = "ManufacturerArticleNumber",
        semanticId = "irdi:0173-1#02-AAO676#003",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"A123-456"},
        description = "unique product identifier of the manufacturer."
      },
      AasField {
        name = "ManufacturerProductDesignation",
        semanticId = "irdi:0173-1#02-AAW338#001",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"X-Axis for turning machine 123@en","X-Achse für Drehmaschine 123@de"},
        description = "Product designation as given by the manufacturer. Short description of the product, product group or function (short text) in common language."
      },
      AasField {
        name = "ManufacturerOrderCode",
        semanticId = "irdi:0173-1#02-AAO227#002",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"EEA-EX-200-S/47-Q3"},
        description = "By manufactures issued unique combination of numbers and letters used to identify the device for ordering."
      },
      AasField {
        name = "QuantityOfParts",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/QuantityOfParts/1/0",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"3"},
        description = "Total number of separate components or parts that make up a particular item, product, or system."
      },
      AasField {
        name = "BulkCount",
        semanticId = "iri:https://admin-shell.io/idta/HierarchicalStructures/BulkCount/1/0",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"3"},
        description = "countable set of identical elements or products present in a group."
      },
      AasField {
        name = "EnergyConsumtionPerCycle",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/EnergyConsumtionPerCycle/1/0",
        type = refBy(DoubleType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"43 kWh"},
        description = "Energy consumed during a single operating cycle of a system, machine or device."
      }
    }
  };

  AasSubmodelElementCollectionType Messages = {
    name = "Messages",
    semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/Messages/1/0",
    description = "Messages and notes on the interpretation and its results.",
    versionIdentifier = "IDTA 02021-1-0",
    fields = {
      AasField {
        name = "Message",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/Message/1/0",
        type = refBy(Message),
        minimumInstances = 0,
        description = "Message or information transmitted by the interpretation tool."
      }
    }
  };

  AasSubmodelElementCollectionType Message = {
    name = "Message",
    semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/Message/1/0",
    description = "Message or information transmitted by the sizing tool.",
    versionIdentifier = "IDTA 02021-1-0",
    fields = {
      AasField {
        name = "CriticalityOfMessage",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/CriticalityOfMessage/1/0",
        type = refBy(CriticalityOfMessage),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Warning"},
        description = "marker for the criticality of the message with respect to the sizing result. E. g. trough symbol, color or code."
      },
      AasField {
        name = "MessageText",
        semanticId = "iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MessageText/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"„Dichtigkeit Motor- Adapterplatte ist nicht gewährleistet. Zur Abdichtung kann ein Dichtblech verwendet werden.“@de","„Sealing between motor and adapter plate is not guaranteed. A sealing plate can be used for sealing.“@en"},
        description = "Content of a message transmitted in written form. The content is specific to the sizing tool and manufacturer."
      }
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
