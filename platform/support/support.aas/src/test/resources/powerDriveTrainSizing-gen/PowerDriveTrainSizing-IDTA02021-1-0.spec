AAS PowerDriveTrainSizingExample
 ASSET ci INSTANCE
 SUBMODEL PowerDriveTrainSizing (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/1/0)
  SMC ApplicationRequirements (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/ApplicationRequirements/1/0)
   SMC Environmental (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/EnvironmentalRequirements/1/0)
    RANGE AmbientTemperatureController (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/AmbientTemperatureController/1/0) min 1 max 2
    RANGE AmbientTemperatureMotor (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/AmbientTemperatureMotor/1/0) min 1 max 2
    PROPERTY Atex2Dust = false (semanticId: irdi:0173-1#02-AAR866#004)
    PROPERTY Atex2Gas = true (semanticId: irdi:0173-1#02-AAZ614#003)
    PROPERTY InstallationAltitude = 1200 m (semanticId: irdi:0173-1#02-AAZ614#003)
   SMC MotionPattern (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MotionPattern/1/0)
    PROPERTY MotionPatternName = Conveyor Motion Pattern 1 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MotionPatternName/1/0)
    SMC MotionPatternSections (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MotionPatternSections/1/0)
     SMC LinearSection01 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LinearMotionPatternSection/1/0)
      PROPERTY CompensationForce = 1 N (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/CompensationForce/1/0)
      PROPERTY CounterMass = 1 kg (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/CounterMass/1/0)
      PROPERTY FrictionCoefficient = 0.05 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FrictionCoefficient/1/0)
      PROPERTY FrictionForce = 652 N (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FrictionForce/1/0)
      PROPERTY LoadMass = 1 kg (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LoadMass/1/0)
      PROPERTY LoadSideForce = 0 N (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LoadSideForce/1/0)
      SMC MetadataLinearMotionFile (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MetadataRotativeMotionFile/1/0)
       PROPERTY FrictionForce = 652 N (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FrictionForce/1/0)
       PROPERTY LinearAcceleration = 1 m/s² (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LinearAcceleration/1/0)
       PROPERTY LinearJerk = 1 m/?³ (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LinearJerk/1/0)
       PROPERTY LinearVelocity = 1 m/s (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LinearVelocity/1/0)
       PROPERTY LoadSideForce = 0 N (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LoadSideForce/1/0)
       PROPERTY Position = 1 m (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/Position/1/0)
       PROPERTY Time = 1 s (semanticId: iri:https://admin-shell.io/idta/TimeSeries/RelativePointInTime/1/1)
      FILE MotionSectionFile (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MotionSectionFile/1/0) length 0
     SMC RotativeSection01 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/RotativeMotionPatternSection/1/0)
      PROPERTY AxialForce = 1 N (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/AxialForce/1/0)
      PROPERTY FrictionTorque = 1 Nm (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FrictionTorque/1/0)
      PROPERTY LeverArmAxialForce = 0.1 m (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmAxialForce/1/0)
      PROPERTY LeverArmRadialForce = 1 m (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmRadialForce/1/0)
      PROPERTY LoadTorque = 1 Nm (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LoadTorque/1/0)
      SMC MetadataRotativeMotionFile (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MetadataRotativeMotionFile/1/0)
       PROPERTY AngularAcceleration = 1 rad/s² (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/AngularAcceleration/1/0)
       PROPERTY AngularJerk = 1 rad/?³ (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/AngularJerk/1/0)
       PROPERTY AngularPosition = 1 rad (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/AngularPosition/1/0)
       PROPERTY AngularVelocity = 1 rad/s (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/AngularVelocity/1/0)
       PROPERTY AxialForce = 1 N (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/AxialForce/1/0)
       PROPERTY FrictionTorque = 1 Nm (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FrictionTorque/1/0)
       PROPERTY LoadTorque = 1 Nm (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LoadTorque/1/0)
       PROPERTY RadialForce = 1 N (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/RadialForce/1/0)
       PROPERTY Time = 1 s (semanticId: iri:https://admin-shell.io/idta/TimeSeries/RelativePointInTime/1/1)
      PROPERTY MomentOfInertiaOfLoad = 1 kg m² (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MomentOfInertiaOfLoad/1/0)
      FILE MotionSectionFile (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MotionSectionFile/1/0) length 0
      PROPERTY RadialForce = 1 N (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/RadialForce/1/0)
   SMC OverallSystemRequirements (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/OverallSystemRequirements/1/0)
    PROPERTY BrakePresent = true (semanticId: irdi:0173-1#02-BAE085#007)
    PROPERTY CertificateApproval = CE (semanticId: irdi:0173-1#02-BAB392#018)
    PROPERTY CoolingType = Air-air heat exchanger (semanticId: irdi:0173-1#02-BAE122#007)
    PROPERTY DcLinkCoupling = false (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/DcLinkCoupling/1/0)
    PROPERTY MainsConnection = 230 VAC (semanticId: irdi:0173-1#02-ABF822#003)
    PROPERTY MinSwitchingFrequency = 100 Hz (semanticId: irdi:0173-1#02-AAN329#003)
    PROPERTY MountingType = geflanscht (semanticId: irdi:0173-1#02-AAH167#006)
    PROPERTY ProtectionType = IP67 (semanticId: irdi:0173-1#02-BAG342#007)
    PROPERTY SafetyIntegrityLevel = SIL2 (semanticId: irdi:0173-1#02-ABH715#002)
   SMC UsageProfile (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/UsageProfile/1/0)
    PROPERTY CyclesPerMinute = 12 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/CyclesPerMinute/1/0)
    PROPERTY OperatingDaysPerYear = 191.0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/OperatingDaysPerYear/1/0)
    PROPERTY OperatingHoursPerDay = 6.0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/OperatingHoursPerDay/1/0)
  SMC SizingProjectInformation (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/SizingProjectInformation/1/0)
   PROPERTY AmlDriveConfigVersion = V1.0.0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/AmlDriveConfigVersion/1/0)
   PROPERTY ClientName = exaMPLe GmbH (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/ClientName/1/0)
   SMC ContactInformation01 (semanticId: iri:https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/ContactInformation)
    MLP AcademicTitle (semanticId: irdi:0173-1#02-AAO209#003)
    PROPERTY AddressOfAdditionalLink = TEST (semanticId: irdi:0173-1#02-AAQ326#002)
    MLP CityTown (semanticId: irdi:0173-1#02-AAO132#002)
    MLP Company (semanticId: irdi:0173-1#02-AAW001#001)
    MLP Department (semanticId: irdi:0173-1#02-AAO127#003)
    SMC Email (semanticId: irdi:0173-1#02-AAQ836#005)
     PROPERTY EmailAddress = email@muster-ag.de (semanticId: irdi:0173-1#02-AAO198#002)
     MLP PublicKey (semanticId: irdi:0173-1#02-AAO200#002)
     PROPERTY TypeOfEmailAddress = office (semanticId: irdi:0173-1#02-AAO199#003)
     MLP TypeOfPublicKey (semanticId: irdi:0173-1#02-AAO201#002)
    SMC Fax (semanticId: irdi:0173-1#02-AAQ834#005)
     MLP FaxNumber (semanticId: irdi:0173-1#02-AAO195#002)
     PROPERTY TypeOfFaxNumber = office (semanticId: irdi:0173-1#02-AAO196#003)
    MLP FirstName (semanticId: irdi:0173-1#02-AAO206#002)
    MLP FurtherDetailsOfContact (semanticId: irdi:0173-1#02-AAO210#002)
    SMC IPCommunication01 (semanticId: iri:https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/ContactInformation/IPCommunication/)
     PROPERTY AddressOfAdditionalLink = TEST (semanticId: irdi:0173-1#02-AAQ326#002)
     MLP AvailableTime (semanticId: iri:https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/ContactInformation/AvailableTime/)
     PROPERTY TypeOfCommunication = Chat Video call (semanticId: iri:https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/ContactInformation/IPCommunication/TypeOfCommunication)
    PROPERTY Language01 = de (semanticId: iri:https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/ContactInformation/Language)
    MLP MiddleNames (semanticId: irdi:0173-1#02-AAO207#002)
    MLP NameOfContact (semanticId: irdi:0173-1#02-AAO205#002)
    MLP NationalCode (semanticId: irdi:0173-1#02-AAO134#002)
    MLP POBox (semanticId: irdi:0173-1#02-AAO130#002)
    SMC Phone (semanticId: iri:https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/ContactInformation/Phone)
     MLP AvailableTime (semanticId: iri:https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/ContactInformation/AvailableTime/)
     MLP TelephoneNumber (semanticId: irdi:0173-1#02-AAO136#002)
     PROPERTY TypeOfTelephone = office (semanticId: irdi:0173-1#02-AAO137#003)
    PROPERTY RoleOfContactPerson = technical contact (semanticId: irdi:0173-1#02-AAO204#003)
    MLP StateCounty (semanticId: irdi:0173-1#02-AAO133#002)
    MLP Street (semanticId: irdi:0173-1#02-AAO128#002)
    PROPERTY TimeZone = Z-12:00 (semanticId: iri:https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/ContactInformation/TimeZone)
    MLP Title (semanticId: irdi:0173-1#02-AAO208#003)
    MLP ZipCodeOfPOBox (semanticId: irdi:0173-1#02-AAO131#002)
    MLP Zipcode (semanticId: irdi:0173-1#02-AAO129#002)
   PROPERTY DateChanged = 2024/01/01 00:01:00 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/DateChanged/1/0)
   PROPERTY DateCreated = 2024/01/01 00:01:00 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/DateCreated/1/0)
   PROPERTY SizingProjectAxisReference = Achse1 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/SizingProjectAxisReference/1/0)
   MLP SizingProjectDescription (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/SizingProjectDescription)
   FILE SizingProjectLink (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/SizingProjectLink/1/0) length 0
   PROPERTY SizingProjectName = Example Machine Conveyor (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/SizingProjectName/1/0)
   PROPERTY SizingToolName = exaMPLe Sizer (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/SizingToolName/1/0)
  SMC SizingResult (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/SizingResult/1/0)
   ENTITY MainComponent01 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MainComponent/1/0)
    PROPERTY AveragePowerLosses = 60.0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/AveragePowerLosses/1/0)
    PROPERTY AverageRegenerativePowerDcLink = 20.0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/AverageRegenerativePowerDcLink/1/0)
    PROPERTY AverageRegenerativePowerMains = 20.0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/AverageFeedInPowerMains/1/0)
    PROPERTY CalculatedServiceLife = 120000 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/CalculatedServiceLife/1/0)
    PROPERTY ContinuousCurrent = 16.0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/ContinuousCurrent/1/0)
    PROPERTY EffectiveUtilization = 55 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/EffectiveUtilization/1/0)
    PROPERTY EnergyConsumtionPerCycle = 43.0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/EnergyConsumtionPerCycle/1/0)
    PROPERTY FrequencyAtMaxSpeed = 1200 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FrequencyAtMaxSpeed/1/0)
    PROPERTY MainComponentType01 = Motor (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MainComponentType/1/0)
    PROPERTY ManufacturerArticleNumber = A123-456 (semanticId: irdi:0173-1#02-AAO676#003)
    PROPERTY ManufacturerName = Example Company (semanticId: irdi:0173-1#02-AAO677#002)
    PROPERTY ManufacturerOrderCode = EEA-EX-200-S/47-Q3 (semanticId: irdi:0173-1#02-AAO227#002)
    MLP ManufacturerProductDesignation (semanticId: irdi:0173-1#02-AAW338#001)
    PROPERTY MassInertiaRatio = 0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MassInertiaRatio/1/0)
    PROPERTY MaxCurrentUtilization = 16.0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MaxCurrentUtilization/1/0)
    PROPERTY MaxCurrentUtilizationPercentage = 78.0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MaxCurrentUtilizationPercentage/1/0)
    PROPERTY MaxRegenerativePowerDcLink = 35.0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MaxRegenerativePowerDcLink/1/0)
    PROPERTY MaxRegenerativePowerMains = 35.0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MaxFeedInPowerMains/1/0)
    PROPERTY MaxRotationSpeedUtilization = 1200 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MaxRotationSpeedUtilization/1/0)
    PROPERTY MaxRotationSpeedUtilizationPercentage = 20 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MaxRotationSpeedUtilizationPercentage/1/0)
    PROPERTY MaxThermalUtilization = 70.0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MaxThermalUtilization/1/0)
    PROPERTY MaxThermalUtilizationPercentage = 80.0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MaxThermalUtilizationPercentage/1/0)
    PROPERTY MaxTorqueUtilization = 500 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MaxTorqueUtilization/1/0)
    PROPERTY MaxTorqueUtilizationPercentage = 78 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MaxTorqueUtilizationPercentage/1/0)
    PROPERTY PowerInMotorOperation = 1200 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/PowerInMotorOperation/1/0)
    PROPERTY PowerInRegenerativeOperation = 1200 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/PowerInRegenerativeOperation/1/0)
    PROPERTY RmsOfMotorTorque = 1200 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/RmsOfMotorTorque/1/0)
    PROPERTY RmsOfPower = 35.0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/RmsOfPower/1/0)
   SMC Messages (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/Messages/1/0)
    SMC Message01 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/Message/1/0)
     PROPERTY CriticalityOfMessage = Warning (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/CriticalityOfMessage/1/0)
     MLP MessageText (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MessageText/1/0)
   ENTITY OtherComponent01 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/OtherComponent/1/0)
    PROPERTY BulkCount = 3 (semanticId: iri:https://admin-shell.io/idta/HierarchicalStructures/BulkCount/1/0)
    PROPERTY EnergyConsumtionPerCycle = 43.0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/EnergyConsumtionPerCycle/1/0)
    PROPERTY ManufacturerArticleNumber = A123-456 (semanticId: irdi:0173-1#02-AAO676#003)
    PROPERTY ManufacturerName = xample Company (semanticId: irdi:0173-1#02-AAO677#002)
    PROPERTY ManufacturerOrderCode = EEA-EX-200-S/47-Q3 (semanticId: irdi:0173-1#02-AAO227#002)
    MLP ManufacturerProductDesignation (semanticId: irdi:0173-1#02-AAW338#001)
    PROPERTY QuantityOfParts = 3 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/QuantityOfParts/1/0)
   ENTITY OverallSystem (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/OverallSystem/1/0)
    PROPERTY CurrentForEmergencyStop = 16.0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/CurrentForEmergencyStop/1/0)
    PROPERTY DecelerationForEmergencyStop = 120.0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/DecelerationForEmergencyStop/1/0)
    PROPERTY DisplacementDuringEmergencyStop = 0.0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/DisplacementDuringEmergencyStop/1/0)
    PROPERTY EnergyConsumtionPerCycle = 43.0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/EnergyConsumtionPerCycle/1/0)
    PROPERTY ExternalMomentOfInertia = 341.0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/ExternalMomentOfInertia/1/0)
    PROPERTY InternalMomentOfIntertia = 341.0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InternalMomentOfInertia/1/0)
    PROPERTY ManufacturerArticleNumber = A123-456 (semanticId: irdi:0173-1#02-AAO676#003)
    PROPERTY ManufacturerName = xample Company (semanticId: irdi:0173-1#02-AAO677#002)
    PROPERTY ManufacturerOrderCode = EEA-EX-200-S/47-Q3 (semanticId: irdi:0173-1#02-AAO227#002)
    MLP ManufacturerProductDesignation (semanticId: irdi:0173-1#02-AAW338#001)
    PROPERTY MassInertiaRatio = 0.0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MassInertiaRatio/1/0)
   MLP TextStatement (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/TextStatement/1/0)
  SMC TransformationMechanism (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/TransformationMechanism/1/0)
   ENTITY BeltConveyor (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/BeltConveyor/1/0)
    PROPERTY Efficiency = 0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/Efficiency/1/0)
    PROPERTY FeedConstant = 40 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FeedConstant/1/0)
    PROPERTY FrictionCoefficient = 0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FrictionCoefficient/1/0)
    PROPERTY InclinationAngle = 5 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InclinationAngle/1/0)
    PROPERTY InertiaMotorSide = 3 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InertiaMotorSide/1/0)
    PROPERTY LeverArmAxialForce = 2 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmAxialForce/1/0)
    PROPERTY LeverArmRadialForce = 1 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmRadialForce/1/0)
    PROPERTY NoLoadTorque = 1 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/NoLoadTorque/1/0)
   ENTITY BeltDrive (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/BeltDrive/1/0)
    PROPERTY Efficiency = 0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/Efficiency/1/0)
    PROPERTY FeedConstant = 40 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FeedConstant/1/0)
    PROPERTY FrictionCoefficient = 0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FrictionCoefficient/1/0)
    PROPERTY InclinationAngle = 5 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InclinationAngle/1/0)
    PROPERTY InertiaMotorSide = 3 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InertiaMotorSide/1/0)
    PROPERTY LeverArmAxialForce = 2 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmAxialForce/1/0)
    PROPERTY LeverArmRadialForce = 1 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmRadialForce/1/0)
    PROPERTY NoLoadTorque = 1 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/NoLoadTorque/1/0)
   ENTITY ChainConveyor (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/ChainConveyor/1/0)
    PROPERTY Efficiency = 0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/Efficiency/1/0)
    PROPERTY FeedConstant = 40 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FeedConstant/1/0)
    PROPERTY FrictionCoefficient = 0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FrictionCoefficient/1/0)
    PROPERTY InclinationAngle = 5 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InclinationAngle/1/0)
    PROPERTY InertiaMotorSide = 3 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InertiaMotorSide/1/0)
    PROPERTY LeverArmAxialForce = 2 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmAxialForce/1/0)
    PROPERTY LeverArmRadialForce = 1 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmRadialForce/1/0)
    PROPERTY NoLoadTorque = 1 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/NoLoadTorque/1/0)
   ENTITY Fan (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/Fan/1/0)
    PROPERTY Efficiency = 0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/Efficiency/1/0)
    PROPERTY InclinationAngle = 5 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InclinationAngle/1/0)
    PROPERTY InertiaMotorSide = 3 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InertiaMotorSide/1/0)
    PROPERTY LeverArmAxialForce = 2 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmAxialForce/1/0)
    PROPERTY LeverArmRadialForce = 1 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmRadialForce/1/0)
    PROPERTY NoLoadTorque = 1 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/NoLoadTorque/1/0)
   SMC LinearApplication
   ENTITY Pump (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/Pump/1/0)
    PROPERTY Efficiency = 0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/Efficiency/1/0)
    PROPERTY InclinationAngle = 5 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InclinationAngle/1/0)
    PROPERTY InertiaMotorSide = 3 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InertiaMotorSide/1/0)
    PROPERTY LeverArmAxialForce = 2 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmAxialForce/1/0)
    PROPERTY LeverArmRadialForce = 1 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmRadialForce/1/0)
    PROPERTY NoLoadTorque = 1 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/NoLoadTorque/1/0)
   ENTITY RackDrive (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/RackDrive/1/0)
    PROPERTY DiameterPinion = 0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/DiameterPinion/1/0)
    PROPERTY Efficiency = 0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/Efficiency/1/0)
    PROPERTY FeedConstant = 40 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FeedConstant/1/0)
    PROPERTY FrictionCoefficient = 0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FrictionCoefficient/1/0)
    PROPERTY HelixAngle = 14 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/HelixAngle/1/0)
    PROPERTY InclinationAngle = 5 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InclinationAngle/1/0)
    PROPERTY InertiaMotorSide = 3 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InertiaMotorSide/1/0)
    PROPERTY LeverArmAxialForce = 2 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmAxialForce/1/0)
    PROPERTY LeverArmRadialForce = 1 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmRadialForce/1/0)
    PROPERTY MovingPart = Rack (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/MovingPart/1/0)
    PROPERTY NoLoadTorque = 1 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/NoLoadTorque/1/0)
   ENTITY RollerConveyor (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/RollerConveyor/1/0)
    PROPERTY Efficiency = 0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/Efficiency/1/0)
    PROPERTY FeedConstant = 40 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FeedConstant/1/0)
    PROPERTY FrictionCoefficient = 0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FrictionCoefficient/1/0)
    PROPERTY InclinationAngle = 5 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InclinationAngle/1/0)
    PROPERTY InertiaMotorSide = 3 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InertiaMotorSide/1/0)
    PROPERTY LeverArmAxialForce = 2 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmAxialForce/1/0)
    PROPERTY LeverArmRadialForce = 1 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmRadialForce/1/0)
    PROPERTY NoLoadTorque = 1 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/NoLoadTorque/1/0)
   ENTITY RotaryTable (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/RotraryTable/1/0)
    PROPERTY CentroidAngle = 5 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/CentroidAngle/1/0)
    PROPERTY Efficiency = 0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/Efficiency/1/0)
    PROPERTY InclinationAngle = 5 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InclinationAngle/1/0)
    PROPERTY InertiaMotorSide = 3 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InertiaMotorSide/1/0)
    PROPERTY LeverArmAxialForce = 2 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmAxialForce/1/0)
    PROPERTY LeverArmRadialForce = 1 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmRadialForce/1/0)
    PROPERTY NoLoadTorque = 1 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/NoLoadTorque/1/0)
    PROPERTY StaticEccentricity = 5 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/StaticEccentricity/1/0)
   SMC RotativeApplication
   ENTITY SpindleDrive (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/SpindleDrive/1/0)
    PROPERTY Efficiency = 0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/Efficiency/1/0)
    PROPERTY FeedConstant = 40 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FeedConstant/1/0)
    PROPERTY FrictionCoefficient = 0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FrictionCoefficient/1/0)
    PROPERTY InclinationAngle = 5 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InclinationAngle/1/0)
    PROPERTY InertiaMotorSide = 3 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InertiaMotorSide/1/0)
    PROPERTY LeverArmAxialForce = 2 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmAxialForce/1/0)
    PROPERTY LeverArmRadialForce = 1 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmRadialForce/1/0)
    PROPERTY NoLoadTorque = 1 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/NoLoadTorque/1/0)
   ENTITY TravelingDrive (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/TravelingDrive/1/0)
    PROPERTY Efficiency = 0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/Efficiency/1/0)
    PROPERTY FeedConstant = 40 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FeedConstant/1/0)
    PROPERTY FrictionCoefficient = 0 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/FrictionCoefficient/1/0)
    PROPERTY InclinationAngle = 5 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InclinationAngle/1/0)
    PROPERTY InertiaMotorSide = 3 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/InertiaMotorSide/1/0)
    PROPERTY LeverArmAxialForce = 2 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmAxialForce/1/0)
    PROPERTY LeverArmRadialForce = 1 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/LeverArmRadialForce/1/0)
    PROPERTY NoLoadTorque = 1 (semanticId: iri:https://admin-shell.io/idta/PowerDriveTrainSizing/NoLoadTorque/1/0)