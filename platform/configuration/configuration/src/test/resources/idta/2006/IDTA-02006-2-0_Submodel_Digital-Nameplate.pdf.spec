project IDTA_02006_Nameplate {

  version v2.0;

  import AASDataTypes;
  import IDTA_02002_ContactInformations with (IDTA_02002_ContactInformations.version == v1.0);

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasSubmodelType Nameplate = {
    name = "Nameplate",
    semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate",
    description = "Contains the nameplate information attached to the product.",
    versionIdentifier = "IDTA 02006-2-0",
    fixedName = true,
    fields = {
      AasField {
        name = "URIOfTheProduct",
        semanticId = "irdi:0173-1#02-AAY811#001",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"https://www.domain- abc.com/Model-Nr- 1234/Serial-Nr-5678"},
        description = "unique global identification of the product using an universal resource identifier (URI)."
      },
      AasField {
        name = "ManufacturerName",
        semanticId = "irdi:0173-1#02-AAO677#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Muster AG @DE"},
        description = "legally valid designation of the natural or judicial person which is directly responsible for the design, production, packaging and labeling of a product in respect to its being brought into circulation."
      },
      AasField {
        name = "ManufacturerProductDesignation",
        semanticId = "irdi:0173-1#02-AAW338#001",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"ABC-123 @EN Industrieroboter @DE"},
        description = "Short description of the product (short text)."
      },
      AasField {
        name = "ContactInformation",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/ContactInformation",
        type = refBy(ContactInformation),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "The SMC “ContactInformation” contains information on how to contact the manufacturer or an authorised service provider, e.g. when a maintenance service is required."
      },
      AasField {
        name = "ManufacturerProductRoot",
        semanticId = "irdi:0173-1#02-AAU732#001",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"flow meter@EN"},
        description = "Top level of a 3 level manufacturer specific product hierarchy."
      },
      AasField {
        name = "ManufacturerProductFamily",
        semanticId = "irdi:0173-1#02-AAU731#001",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Type ABC@EN"},
        description = "2nd level of a 3 level manufacturer specific product hierarchy."
      },
      AasField {
        name = "ManufacturerProductType",
        semanticId = "irdi:0173-1#02-AAO057#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"FM-ABC-1234@EN"},
        description = "Characteristic to differentiate between different products of a product family or special variants."
      },
      AasField {
        name = "OrderCodeOfManufacturer",
        semanticId = "irdi:0173-1#02-AAO227#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"FMABC1234@EN"},
        description = "By manufactures issued unique combination of numbers and letters used to identify the device for ordering."
      },
      AasField {
        name = "ProductArticleNumberOfManufacturer",
        semanticId = "irdi:0173-1#02-AAO676#003",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"FM11-ABC22- 123456@EN"},
        description = "unique product identifier of the manufacturer."
      },
      AasField {
        name = "SerialNumber",
        semanticId = "irdi:0173-1#02-AAM556#002",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"12345678"},
        description = "unique combination of numbers and letters used to identify the device once it has been manufactured."
      },
      AasField {
        name = "YearOfConstruction",
        semanticId = "irdi:0173-1#02-AAP906#001",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"2020"},
        description = "Year as completion date of object."
      },
      AasField {
        name = "DateOfManufacture",
        semanticId = "irdi:0173-1#02-AAR972#002",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2021-01-01"},
        description = "Date from which the production and / or development process is completed or from which a service is provided completely."
      },
      AasField {
        name = "HardwareVersion",
        semanticId = "irdi:0173-1#02-AAN270#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1.0.0@EN"},
        description = "Version of the hardware supplied with the device."
      },
      AasField {
        name = "FirmwareVersion",
        semanticId = "irdi:0173-1#02-AAM985#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1.0@EN"},
        description = "Version of the firmware supplied with the device."
      },
      AasField {
        name = "SoftwareVersion",
        semanticId = "irdi:0173-1#02-AAM737#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1.0.0@EN"},
        description = "Version of the software used by the device."
      },
      AasField {
        name = "CountryOfOrigin",
        semanticId = "irdi:0173-1#02-AAO259#004",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"DE"},
        description = "Country where the product was manufactured."
      },
      AasField {
        name = "CompanyLogo",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/CompanyLogo",
        type = refBy(AasFileResourceType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "A graphic mark used to represent a company, an organisation or a product."
      },
      AasField {
        name = "Markings",
        semanticId = "irdi:0173-1#01-AGZ673#001",
        type = refBy(Markings),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Collection of product markings."
      },
      AasField {
        name = "AssetSpecificProperties",
        semanticId = "irdi:0173-1#01-AGZ672#001",
        type = refBy(AssetSpecificProperties),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Group of properties that are listed on the asset's nameplate and are grouped based on guidelines."
      }
    }
  };

  AasSubmodelElementCollectionType Markings = {
    name = "Markings",
    semanticId = "irdi:0173-1#01-AGZ673#001",
    description = "Collection of product markings Note: CE marking is declared as mandatory according to EU Machine Directive 2006/42/EC.",
    versionIdentifier = "IDTA 02006-2-0",
    allowDuplicates = true,
    fields = {
      AasField {
        name = "Marking",
        semanticId = "irdi:0173-1#01-AHD206#001",
        counting = true,
        type = refBy(Marking),
        minimumInstances = 1,
        description = "contains information about the marking labelled on the device."
      }
    }
  };

  AasSubmodelElementCollectionType Marking = {
    name = "Marking",
    semanticId = "irdi:0173-1#01-AHD206#001",
    multiSemanticIds = true,
    description = "contains information about the marking labelled on the device Note: see also [IRDI] 0112/2///61987#ABH515#003 Certificate or approval.",
    versionIdentifier = "IDTA 02006-2-0",
    allowDuplicates = true,
    fields = {
      AasField {
        name = "MarkingName",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/MarkingName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"valueId with ECLASS enumeration IRDI is preferable, e.g. [IRDI] 0173-1#07- DAA603#004 for CE. If","no IRDI available, string value can also be accepted.","Samples for valueId from ECLASS are listed in Annex B"},
        description = "common name of the marking."
      },
      AasField {
        name = "DesignationOfCertificateOrApproval",
        semanticId = "irdi:0112/2///61987#ABH783#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"KEMA99IECEX1105/12 8"},
        description = "alphanumeric character sequence identifying a certificate or approval."
      },
      AasField {
        name = "IssueDate",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/IssueDate",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2021-01-01"},
        description = "Date, at which the specified certificate is issued."
      },
      AasField {
        name = "ExpiryDate",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExpiryDate",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2021-01-01"},
        description = "Date, at which the specified certificate expires."
      },
      AasField {
        name = "MarkingFile",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/MarkingFile",
        type = refBy(AasFileResourceType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"/aasx/Nameplate/marki ng_ce.png"},
        description = "conformity symbol of the marking."
      },
      AasField {
        name = "MarkingAdditionalText",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/MarkingAdditionalText",
        counting = true,
        type = refBy(StringListType),
        minimumInstances = 0,
        examples = {"0044"},
        description = "where applicable, additional information on the marking in plain text, e.g. the ID-number of the notified body involved in the conformity process."
      },
      AasField {
        name = "ExplosionSafeties",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExplosionSafeties",
        type = refBy(ExplosionSafeties),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Collection of explosion safefy specifications See separate clause."
      }
    }
  };

  AasSubmodelElementCollectionType ExplosionSafeties = {
    name = "ExplosionSafeties",
    semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExplosionSafeties",
    description = "Collection of explosion safefy specifications.",
    versionIdentifier = "IDTA 02006-2-0",
    allowDuplicates = true,
    fields = {
      AasField {
        name = "ExplosionSafety",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExplosionSafeties/ExplosionSafety",
        counting = true,
        type = refBy(ExplosionSafety),
        minimumInstances = 1,
        description = "contains information related to explosion safety according to device nameplate See separate clause."
      }
    }
  };

  AasSubmodelElementCollectionType ExplosionSafety = {
    name = "ExplosionSafety",
    semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExplosionSafeties/ExplosionSafety",
    description = "contains information related to explosion safety according to device nameplate.",
    versionIdentifier = "IDTA 02006-2-0",
    allowDuplicates = true,
    fields = {
      AasField {
        name = "DesignationOfCertificateOrApproval",
        semanticId = "irdi:0112/2///61987#ABH783#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"KEMA99IECEX1105/1 28"},
        description = "alphanumeric character sequence identifying a certificate or approval."
      },
      AasField {
        name = "TypeOfApproval",
        semanticId = "irdi:0173-1#02-AAM812#003",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"ATEX@DE"},
        description = "( [IRDI] 0112/2///61987#ABA231#008 type of hazardous area approval) classification according to the standard or directive to which the approval applies."
      },
      AasField {
        name = "ApprovalAgencyTestingAgency",
        semanticId = "irdi:0173-1#02-AAM632#001",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"PTB@DE"},
        description = "( [IRDI] 0112/2///61987#ABA634#004 approval agency/testing agency) certificates and approvals pertaining to general usage and compliance with constructional standards and directives."
      },
      AasField {
        name = "TypeOfProtection",
        semanticId = "irdi:0173-1#02-AAQ325#003",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"db NI; NIFW","Ex db eb ia Ex db; Ex eb"},
        description = "( [IRDI] 0112/2///61987#ABA589#002 type of protection (Ex)) classification of an explosion protection according to the specific measures applied to avoid ignition of a surrounding explosive atmosphere."
      },
      AasField {
        name = "RatedInsulationVoltage",
        semanticId = "irdi:0173-1#02-AAN532#003",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"250 Unit: V"},
        description = "from the manufacturer for the capital assets limited isolation with given(indicated) operating conditions."
      },
      AasField {
        name = "InstructionsControlDrawing",
        semanticId = "irdi:0112/2///61987#ABO102#001filenameofcontrol/referencedrawing",
        type = refBy(AasReferenceType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "designation used to uniquely identify a control/reference drawing stored in a file system."
      },
      AasField {
        name = "SpecificConditionsForUse",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExplosionSafeties/ExplosionSafety/SpecificConditionsForUse",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"X"},
        description = "Note: X if any, otherwise no entry."
      },
      AasField {
        name = "IncompleteDevice",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExplosionSafeties/ExplosionSafety/IncompleteDevice",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"U"},
        description = "U if any, otherwise no entry."
      },
      AasField {
        name = "AmbientConditions",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExplosionSafeties/ExplosionSafety/AmbientConditions",
        type = refBy(AmbientConditions),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Contains properties which are related to the ambient conditions of the device."
      },
      AasField {
        name = "ProcessConditions",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExplosionSafeties/ExplosionSafety/ProcessConditions",
        type = refBy(ProcessConditions),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Contains properties which are related to the process conditions of the device."
      },
      AasField {
        name = "ExternalElectricalCircuit",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExplosionSafeties/ExplosionSafety/ExternalElectricalCircuit",
        counting = true,
        type = refBy(ExternalElectricalCircuit),
        minimumInstances = 0,
        description = "specifies the parameters of external electrical circuits."
      }
    }
  };

  AasSubmodelElementCollectionType AmbientConditions = {
    name = "AmbientConditions",
    semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExplosionSafeties/ExplosionSafety/AmbientConditions",
    description = "Contains properties which are related to the ambient conditions of the device. If the device is mounted in the process boundary, ambient and process conditions are provided separately.",
    versionIdentifier = "IDTA 02006-2-0",
    fields = {
      AasField {
        name = "DeviceCategory",
        semanticId = "irdi:0173-1#02-AAK297#004",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2G"},
        description = "( [IRDI] 0112/2///61987#ABA467#002 equipment/device category) category of device in accordance with directive 94/9/EC."
      },
      AasField {
        name = "EquipmentProtectionLevel",
        semanticId = "irdi:0173-1#02-AAM668#001",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Gb@DE"},
        description = "( [IRDI] 0112/2///61987#ABA464#005 equipment protection level) part of a hazardous area classification system indicating the likelihood of the existence of a classified hazard."
      },
      AasField {
        name = "RegionalSpecificMarking",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExplosionSafeties/ExplosionSafety/RegionalSpecificMarking",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Class I, Division 2"},
        description = "Marking used only in specific regions, e.g. North America: class/divisions, EAC: “1” or NEC: “AIS”."
      },
      AasField {
        name = "TypeOfProtection",
        semanticId = "irdi:0173-1#02-AAQ325#003",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"db NI; NIFW","Ex db eb ia Ex db; Ex eb"},
        description = "( [IRDI] 0112/2///61987#ABA589#002 type of protection (Ex)) classification of an explosion protection according to the specific measures applied to avoid ignition of a surrounding explosive atmosphere."
      },
      AasField {
        name = "ExplosionGroup",
        semanticId = "irdi:0173-1#02-AAT372#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"IIC IIIB A,B,C,D"},
        description = "( [IRDI] 0112/2///61987#ABA961#007 permitted gas group/explosion group) classification of dangerous gaseous substances based on their ability to cause an explosion."
      },
      AasField {
        name = "MinimumAmbientTemperature",
        semanticId = "irdi:0173-1#02-AAZ952#001",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"-40","Unit: ºC"},
        description = "( [IRDI] 0112/2///61987#ABA621#007 minimum ambient temperature) lower limit of the temperature range of the surrounding space in which the component, the pipework or the system can be operated."
      },
      AasField {
        name = "MaxAmbientTemperature",
        semanticId = "irdi:0173-1#02-BAA039#010",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"120 Unit: ºC"},
        description = "( [IRDI] 0112/2///61987#ABA623#007 maximum ambient temperature) upper limit of the temperature range of the surrounding space in which the component, the pipework or the system can be operated."
      },
      AasField {
        name = "MaxSurfaceTemperatureForDustProof",
        semanticId = "irdi:0173-1#02-AAM666#005",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"100 Unit: ºC"},
        description = "( [IRDI] 0112/2///61987#ABB159#004 maximum surface temperature for dust-proof) maximum permissible surface temperature of a device used in an explosion hazardous area with combustible dust."
      },
      AasField {
        name = "TemperatureClass",
        semanticId = "irdi:0173-1#02-AAO371#004",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"T6 T5"},
        description = "( [IRDI] 0112/2///61987#ABA593#002 temperature class) classification system of electrical apparatus, based on its maximum surface temperature, related to the specific explosive atmosphere for which it is intended to be used."
      }
    }
  };

  AasSubmodelElementCollectionType ProcessConditions = {
    name = "ProcessConditions",
    semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExplosionSafeties/ExplosionSafety/ProcessConditions",
    description = "Contains properties are related to the process conditions of the device. Note: If the device is mounted in the process boundary, ambient and process conditions are provided separately.",
    versionIdentifier = "IDTA 02006-2-0",
    fields = {
      AasField {
        name = "DeviceCategory",
        semanticId = "irdi:0173-1#02-AAK297#004",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1G"},
        description = "Note: Equipment category according to the ATEX system."
      },
      AasField {
        name = "EquipmentProtectionLevel",
        semanticId = "irdi:0173-1#02-AAM668#001",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Ga@DE"},
        description = "( [IRDI] 0112/2///61987#ABA464#005 equipment protection level) part of a hazardous area classification system indicating the likelihood of the existence of a classified hazard."
      },
      AasField {
        name = "RegionalSpecificMarking",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExplosionSafeties/ExplosionSafety/RegionalSpecificMarking",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"IS NI;AIS"},
        description = "Marking used only in specific regions, e.g. North America: class/divisions, EAC: “1” or NEC: “AIS”."
      },
      AasField {
        name = "TypeOfProtection",
        semanticId = "irdi:0173-1#02-AAQ325#003",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"ia"},
        description = "( [IRDI] 0112/2///61987#ABA589#002 type of protection (Ex)) classification of an explosion protection according to the specific measures applied to avoid ignition of a surrounding explosive atmosphere."
      },
      AasField {
        name = "ExplosionGroup",
        semanticId = "irdi:0173-1#02-AAT372#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"IIC A,B,C,D"},
        description = "( [IRDI] 0112/2///61987#ABA961#007 permitted gas group/explosion group) classification of dangerous gaseous substances based on their ability to cause an explosion."
      },
      AasField {
        name = "LowerLimitingValueOfProcessTemperature",
        semanticId = "irdi:0173-1#02-AAN309#004",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"-40","Unit: ºC"},
        description = "lowest temperature to which the wetted parts of the equipment can be subjected without permanent impairment of operating characteristics."
      },
      AasField {
        name = "UpperLimitingValueOfProcessTemperature",
        semanticId = "irdi:0173-1#02-AAN307#004",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"120 Unit: ºC"},
        description = "highest temperature to which the wetted parts of the device may be subjected without permanent impairment of operating characteristics."
      },
      AasField {
        name = "MaxSurfaceTemperatureForDustProof",
        semanticId = "irdi:0173-1#02-AAM666#005",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"85 Unit: ºC"},
        description = "( [IRDI] 0112/2///61987#ABB159#004 maximum surface temperature for dust-proof) maximum permissible surface temperature of a device used in an explosion hazardous area with combustible dust."
      },
      AasField {
        name = "TemperatureClass",
        semanticId = "irdi:0173-1#02-AAO371#004",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"T4"},
        description = "( [IRDI] 0112/2///61987#ABA593#002 temperature class) classification system of electrical apparatus, based on its maximum surface temperature, related to the specific explosive atmosphere for which it is intended to be used."
      }
    }
  };

  AasSubmodelElementCollectionType ExternalElectricalCircuit = {
    name = "ExternalElectricalCircuit",
    semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExplosionSafeties/ExplosionSafety/ExternalElectricalCircuit",
    description = "specifies the parameters of external  electrical circuits. Note: If several external circuits can be connected to the device, this block shall provide a cardinality with the number of circuits Note: If for one external IS circuit several sets of safety parameters are provided (e.g. for several material groups), each set is specified in a separate block as a separate circuit.",
    versionIdentifier = "IDTA 02006-2-0",
    fields = {
      AasField {
        name = "DesignationOfElectricalTerminal",
        semanticId = "irdi:0112/2///61987#ABB147#004",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"+/- 1/2","26(+)/27(-)"},
        description = "alphanumeric character sequence identifying an electrical terminal."
      },
      AasField {
        name = "TypeOfProtection",
        semanticId = "irdi:0173-1#02-AAQ325#003",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"db NI; NIFW","Ex db eb ia Ex db; Ex eb"},
        description = "( [IRDI] 0112/2///61987#ABA589#002 type of protection (Ex)) classification of an explosion protection according to the specific measures applied to avoid ignition of a surrounding explosive atmosphere."
      },
      AasField {
        name = "EquipmentProtectionLevel",
        semanticId = "irdi:0173-1#02-AAM668#001",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Ga@DE"},
        description = "( [IRDI] 0112/2///61987#ABA464#005 equipment protection level) part of a hazardous area classification system indicating the likelihood of the existence of a classified hazard."
      },
      AasField {
        name = "ExplosionGroup",
        semanticId = "irdi:0173-1#02-AAT372#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"IIC"},
        description = "( [IRDI] 0112/2///61987#ABA961#007 permitted gas group/explosion group) classification of dangerous gaseous substances based on their ability to cause an explosion."
      },
      AasField {
        name = "Characteristics",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExplosionSafeties/ExplosionSafety/ExternalElectricalCircuit/Characteristics",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"linear"},
        description = "Characteristic of the intrinsically safe circuit."
      },
      AasField {
        name = "Fisco",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Marking/ExplosionSafeties/ExplosionSafety/ExternalElectric alCircuit/Fisco FISCO certified intrinsically safe fieldbus circuit (IEC 60079- 11)."
      },
      AasField {
        name = "TwoWISE",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExplosionSafeties/ExplosionSafety/ExternalElectricalCircuit/TwoWISE",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "2-WISE certified intrinsically safe circuit (IEC 60079-47)."
      },
      AasField {
        name = "SafetyRelatedPropertiesForPassiveBehaviour",
        semanticId = "irdi:0173-1#02-AAQ380#006",
        type = refBy(SafetyRelatedPropertiesForPassiveBehaviour),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "( [IRDI] 0112/2///61987#ABC586#001 Safety related properties for passive behaviour) properties characterizing the safety related parameters of a loop-powered, intrinsically safe input or output circuit."
      },
      AasField {
        name = "SafetyRelatedPropertiesForActiveBehaviour",
        semanticId = "irdi:0173-1#02-AAQ381#006",
        type = refBy(SafetyRelatedPropertiesForActiveBehaviour),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "( [IRDI] 0112/2///61987#ABC585#001 Safety related properties for active behaviour) properties characterizing the safety related parameters of an intrinsically safe circuit."
      }
    }
  };

  AasSubmodelElementCollectionType SafetyRelatedPropertiesForPassiveBehaviour = {
    name = "SafetyRelatedPropertiesForPassiveBehaviour",
    semanticId = "irdi:0173-1#02-AAQ380#006",
    multiSemanticIds = true,
    description = "properties characterizing the safety related parameters of a loop-powered, intrinsically safe input or output circuit Note: IS-parameters for passive circuits, if relevant (e.g. 2 wire field devices, valves).",
    versionIdentifier = "IDTA 02006-2-0",
    fields = {
      AasField {
        name = "MaxInputPower",
        semanticId = "irdi:0173-1#02-AAQ372#003",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1250 Unit: mW"},
        description = "( [IRDI] 0112/2///61987#ABA981#001 maximum input power (Pi)) maximum power that can be applied to the connection facilities of the apparatus without invalidating the type of protection."
      },
      AasField {
        name = "MaxInputVoltage",
        semanticId = "irdi:0173-1#02-AAM638#003",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"30 Unit: V"},
        description = "( [IRDI] 0112/2///61987#ABA982#001 maximum input voltage (Ui)) maximum voltage (peak a.c. or d.c.) that can be applied to the connection facilities of the apparatus without invalidating the type of protection."
      },
      AasField {
        name = "MaxInputCurrent",
        semanticId = "irdi:0173-1#02-AAM642#004",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"100 Unit: mA"},
        description = "( [IRDI] 0112/2///61987#ABA983#001 maximum input current (Ii)) maximum current (peak a.c. or d.c) that can be applied to the connection facilities of the apparatus without invalidating the type of protection."
      },
      AasField {
        name = "MaxInternalCapacitance",
        semanticId = "irdi:0173-1#02-AAM640#004",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0 Unit: µF"},
        description = "( [IRDI] 0112/2///61987#ABA984#001 maximum internal capacitance (Ci)) maximum equivalent internal capacitance of the apparatus which is considered as appearing across the connection facilities."
      },
      AasField {
        name = "MaxInternalInductance",
        semanticId = "irdi:0173-1#02-AAM639#003",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0 Unit: mH"},
        description = "( [IRDI] 0112/2///61987#ABA985#001 maximum internal inductance (Li)) maximum equivalent internal inductance of the apparatus which is considered as appearing across the connection facilities."
      }
    }
  };

  AasSubmodelElementCollectionType SafetyRelatedPropertiesForActiveBehaviour = {
    name = "SafetyRelatedPropertiesForActiveBehaviour",
    semanticId = "irdi:0173-1#02-AAQ381#006",
    multiSemanticIds = true,
    description = "properties characterizing the safety related parameters of an intrinsically safe circuit Note: IS-parameters for active circuits, if relevant (e.g. power supply, IS-barriers).",
    versionIdentifier = "IDTA 02006-2-0",
    fields = {
      AasField {
        name = "MaxOutputPower",
        semanticId = "irdi:0173-1#02-AAQ371#003",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"960 Unit: mW"},
        description = "( [IRDI] 0112/2///61987#ABA987#001 maximum output power (Po)) maximum electrical power that can be taken from the apparatus."
      },
      AasField {
        name = "MaxOutputVoltage",
        semanticId = "irdi:0173-1#02-AAM635#003",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"15.7 Unit: V"},
        description = "( [IRDI] 0112/2///61987#ABA989#001 maximum output voltage (Uo)) maximum voltage (peak a.c. or d.c.) that can occur at the connection facilities of the apparatus at any applied voltage up to the maximum voltage."
      },
      AasField {
        name = "MaxOutputCurrent",
        semanticId = "irdi:0173-1#02-AAM641#004",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"245 Unit: mA"},
        description = "( [IRDI] 0112/2///61987#ABA988#001maximum output current (Io)) maximum current (peak a.c. or d.c.) in the apparatus that can be taken from the connection facilities of the apparatus."
      },
      AasField {
        name = "MaxExternalCapacitance",
        semanticId = "irdi:0173-1#02-AAM637#004",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2878 Unit: µF"},
        description = "( [IRDI] 0112/2///61987#ABA990#001 maximum external capacitance (Co)) maximum capacitance that can be connected to the connection facilities of the apparatus without invalidating the type of protection."
      },
      AasField {
        name = "MaxExternalInductance",
        semanticId = "irdi:0173-1#02-AAM636#003",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2.9 Unit: mH"},
        description = "( [IRDI] 0112/2///61987#ABA991#001 maximum external inductance (Lo)) maximum value of inductance that can be connected to the connection facilities of the apparatus without invalidating the type of protection."
      },
      AasField {
        name = "MaxExternalInductanceResistanceRatio",
        semanticId = "irdi:0173-1#02-AAM634#003",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Unit: mH/Q"},
        description = "( [IRDI] 0112/2///61987#ABB145#001 maximum external inductance/resistance ratio (Lo/Ro)) maximum value of ratio of inductance (Lo) to resistance (Ro) of any external circuit that can be connected to the connection facilities of the electrical apparatus without invalidating intrinsic safety."
      }
    }
  };

  AasSubmodelElementCollectionType AssetSpecificProperties = {
    name = "AssetSpecificProperties",
    semanticId = "irdi:0173-1#01-AGZ672#001",
    description = "Group of properties that are listed on the asset's nameplate and are grouped based on guidelines.",
    versionIdentifier = "IDTA 02006-2-0",
    fields = {
      AasField {
        name = "GuidelineSpecificProperties",
        semanticId = "irdi:0173-1#01-AHD205#001",
        counting = true,
        type = refBy(GuidelineSpecificProperties),
        minimumInstances = 1,
        description = "Asset specific nameplate information required by guideline, stipulation or legislation. See separate clause."
      },
      AasField {
        name = "arbitrary",
        displayName = "{arbitrary}",
        isGeneric = true,
        type = refBy(StringListType),
        minimumInstances = 1,
        description = "Properties which are not required by any legislations but provided due to best practice."
      }
    }
  };

  AasSubmodelElementCollectionType GuidelineSpecificProperties = {
    name = "GuidelineSpecificProperties",
    semanticId = "irdi:0173-1#01-AHD205#001",
    description = "Asset specific nameplate information required by guideline, stipulation or legislation.",
    versionIdentifier = "IDTA 02006-2-0",
    fields = {
      AasField {
        name = "GuidelineForConformityDeclaration",
        semanticId = "irdi:0173-1#02-AAO856#002",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "guideline, stipulation or legislation used for determining conformity."
      },
      AasField {
        name = "arbitrary",
        displayName = "{arbitrary}",
        isGeneric = true,
        type = refBy(StringListType),
        minimumInstances = 1,
        description = "= {arbitrary, representing information required by further standards}."
      }
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
