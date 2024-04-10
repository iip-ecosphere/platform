project IDTA_02006_Nameplate {

  import AASDataTypes;
  import IDTA_02002_ContactInformations with (IDTA_02002_ContactInformations.version == v1.0);

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasSubmodelType Nameplate = {
    name = "Nameplate",
    semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate",
    description = "Contains the nameplate information attached to the product.",
    fields = {
      AasField {
        name = "URIOfTheProduct",
        semanticId = "irdi:0173-1#02-AAY811#001",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"https://www.domain-abc.com/Model-Nr-1234/Serial-Nr-5678"},
        description = "unique global identification of the product using an universal resource identifier (URI) Note: see also [IRDI] 0112/2///61987#ABN590#001 URI of product instance."
      },
      AasField {
        name = "ManufacturerName",
        semanticId = "irdi:0173-1#02-AAO677#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Muster AG@de"},
        description = "legally valid designation of the natural or judicial person which is directly responsible for the design, production, packaging and labeling of a product in respect to its being brought into circulation Note: see also [IRDI] 0112/2///61987#ABA565#007 manufacturer Note: mandatory property according to EU Machine Directive 2006/42/EC."
      },
      AasField {
        name = "ManufacturerProductDesignation",
        semanticId = "irdi:0173-1#02-AAW338#001",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"ABC-123@en"},
        description = "Short description of the product (short text) Note: see also [IRDI] 0112/2///61987#ABA567#007 name of product Note: Short designation of the product is meant. Note: mandatory property according to EU Machine Directive 2006/42/EC."
      },
      AasField {
        name = "ContactInformation",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/ContactInformation",
        type = refBy(ContactInformation),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "The SMC “ContactInformation” contains information on how to contact the manufacturer or an authorised service provider, e.g. when a maintenance service is required. Note: physical address is a mandatory property according to EU Machine Directive 2006/42/EC."
      },
      AasField {
        name = "ManufacturerProductRoot",
        semanticId = "irdi:0173-1#02-AAU732#001",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"flow meter@en"},
        description = "Top level of a 3 level manufacturer specific product hierarchy."
      },
      AasField {
        name = "ManufacturerProductFamily",
        semanticId = "irdi:0173-1#02-AAU731#001",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Type ABC@en"},
        description = "2nd level of a 3 level manufacturer specific product hierarchy Note: conditionally mandatory property according to EU Machine Directive 2006/42/EC. One of the two properties must be provided: ManufacturerProductFamily (0173-1#02-AAU731#001) or ManufacturerProductType (0173-1#02-AAO057#002)."
      },
      AasField {
        name = "ManufacturerProductType",
        semanticId = "irdi:0173-1#02-AAO057#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"FM-ABC-1234@en"},
        description = "Characteristic to differentiate between different products of a product family or special variants Note: see also [IRDI] 0112/2///61987#ABA300#006 code of product Note: conditionally mandatory property according to EU Machine Directive 2006/42/EC. One of the two properties must be provided: ManufacturerProductFamily (0173-1#02-AAU731#001) or ManufacturerProductType (0173-1#02-AAO057#002)."
      },
      AasField {
        name = "OrderCodeOfManufacturer",
        semanticId = "irdi:0173-1#02-AAO227#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"FMABC1234@en"},
        description = "By manufactures issued unique combination of numbers and letters used to identify the device for ordering Note: see also [IRDI] 0112/2///61987#ABA950#006 order code of product Note: Recommendation: property declaration as MLP is required by its semantic definition. As the property value is language independent, users are recommended to provide maximal 1 string in any language of the user’s choice."
      },
      AasField {
        name = "ProductArticleNumberOfManufacturer",
        semanticId = "irdi:0173-1#02-AAO676#003",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"FM11-ABC22-123456@en"},
        description = "unique product identifier of the manufacturer Note: see also [IRDI] 0112/2///61987#ABA581#006 article number Note: Recommendation: property declaration as MLP is required by its semantic definition. As the property value is language independent, users are recommended to provide maximal 1 string in any language of the user’s choice."
      },
      AasField {
        name = "SerialNumber",
        semanticId = "irdi:0173-1#02-AAM556#002",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"12345678"},
        description = "unique combination of numbers and letters used to identify the device once it has been manufactured Note: see also [IRDI] 0112/2///61987#ABA951#007 serial number."
      },
      AasField {
        name = "YearOfConstruction",
        semanticId = "irdi:0173-1#02-AAP906#001",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"2022"},
        description = "Year as completion date of object Note: mandatory property according to EU Machine Directive 2006/42/EC."
      },
      AasField {
        name = "DateOfManufacture",
        semanticId = "irdi:0173-1#02-AAR972#002",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2022-01-01"},
        description = "Date from which the production and / or development process is completed or from which a service is provided completely Note: see also [IRDI] 0112/2///61987#ABB757#007 date of manufacture Note: format by lexical representation: CCYY-MM-DD."
      },
      AasField {
        name = "HardwareVersion",
        semanticId = "irdi:0173-1#02-AAN270#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1.0.0@en"},
        description = "Version of the hardware supplied with the device Note: see also [IRDI] 0112/2///61987#ABA926#006 hardware version Note: Recommendation: property declaration as MLP is required by its semantic definition. As the property value is language independent, users are recommended to provide maximal 1 string in any language of the user’s choice."
      },
      AasField {
        name = "FirmwareVersion",
        semanticId = "irdi:0173-1#02-AAM985#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1.0@en"},
        description = "Version of the firmware supplied with the device Note: see also [IRDI] 0112/2///61987#ABA302#004 firmware version Note: Recommendation: property declaration as MLP is required by its semantic definition. As the property value is language independent, users are recommended to provide maximal 1 string in any language of the user’s choice."
      },
      AasField {
        name = "SoftwareVersion",
        semanticId = "irdi:0173-1#02-AAM737#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1.0.0@en"},
        description = "Version of the software used by the device Note: see also [IRDI] 0112/2///61987#ABA601#006 software version Note: Recommendation: property declaration as MLP is required by its semantic definition. As the property value is language independent, users are recommended to provide maximal 1 string in any language of the user’s choice."
      },
      AasField {
        name = "CountryOfOrigin",
        semanticId = "irdi:0173-1#02-AAO259#004",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"DE"},
        description = "Country where the product was manufactured Note: see also [IRDI] 0112/2///61360_4#ADA034#001 country of origin Note: Country codes defined accord. to DIN EN ISO 3166-1 alpha-2 codes."
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
        description = "Collection of product markings Note: CE marking is declared as mandatory according to EU Machine Directive 2006/42/EC."
      },
      AasField {
        name = "AssetSpecificProperties",
        semanticId = "irdi:0173-1#01-AGZ672#001",
        type = refBy(AssetSpecificProperties),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Group of properties that are listed on the asset's nameplate and are grouped based on guidelines Note: defined as “Asset specific nameplate information” per ECLASS."
      }
    }
  };

  AasSubmodelElementCollectionType Markings = {
    name = "Markings",
    semanticId = "irdi:0173-1#01-AGZ673#001",
    description = "Collection of product markings Note: CE marking is declared as mandatory according to EU Machine Directive 2006/42/EC.",
    allowDuplicates = true,
    fields = {
      AasField {
        name = "Marking",
        semanticId = "irdi:0173-1#01-AHD206#001",
        type = refBy(Marking),
        description = "Note: see also [IRDI] 0112/2///61987#ABH515#003 Certificate or approval Note: CE marking is declared as mandatory according to the Blue Guide of the EU-Commission."
      }
    }
  };

  AasSubmodelElementCollectionType Marking = {
    name = "Marking",
    semanticId = "irdi:0173-1#01-AHD206#001",
    description = "Note: see also [IRDI] 0112/2///61987#ABH515#003 Certificate or approval Note: CE marking is declared as mandatory according to the Blue Guide of the EU-Commission.",
    allowDuplicates = true,
    fields = {
      AasField {
        name = "MarkingName",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/MarkingName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"0173-1#07-DAA603#004"},
        description = "common name of the marking Note: see also [IRDI] 0173-1#02-BAB392#015 certificate/approval valueId with ECLASS enumeration IRDI is preferable, e.g. [IRDI] 0173-1#07-DAA603#004 for CE. If no IRDI available, string value can also be accepted. Note: CE marking is declared as mandatory according to Blue Guide of the EU-Commission."
      },
      AasField {
        name = "DesignationOfCertificateOrApproval",
        semanticId = "irdi:0112/2///61987#ABH783#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"KEMA99IECEX1105/128"},
        description = "alphanumeric character sequence identifying a certificate or approval Note: Approval identifier, reference to the certificate number, to be entered without spaces."
      },
      AasField {
        name = "IssueDate",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/IssueDate",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2022-01-01"},
        description = "Date, at which the specified certificate is issued Note: format by lexical representation: CCYY-MM-DD Note: to be specified to the day."
      },
      AasField {
        name = "ExpiryDate",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExpiryDate",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2022-01-01"},
        description = "Date, at which the specified certificate expires Note: see also ([IRDI] 0173-1#02-AAO997#001 Validity date Note: format by lexical representation: CCYY-MM-DD Note: to be specified to the day."
      },
      AasField {
        name = "MarkingFile",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/MarkingFile",
        type = refBy(AasFileResourceType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"/aasx/Nameplate/marking_ce.png"},
        description = "conformity symbol of the marking."
      },
      AasField {
        name = "MarkingAdditionalText",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/MarkingAdditionalText",
        type = refBy(StringListType),
        minimumInstances = 0,
        examples = {"0044"},
        description = "where applicable, additional information on the marking in plain text, e.g. the ID-number of the notified body involved in the conformity process Note: see also [IRDI] 0173-1#02-AAM954#002 details of other certificate."
      },
      AasField {
        name = "ExplosionSafeties",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExplosionSafeties",
        type = refBy(ExplosionSafeties),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Collection of explosion safety specifications."
      }
    }
  };

  AasSubmodelElementCollectionType ExplosionSafeties = {
    name = "ExplosionSafeties",
    semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExplosionSafeties",
    description = "Collection of explosion safety specifications.",
    allowDuplicates = true,
    fields = {
      AasField {
        name = "ExplosionSafety",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExplosionSafeties/ExplosionSafety",
        type = refBy(ExplosionSafety),
        minimumInstances = 1,
        description = "contains information related to explosion safety according to device nameplate."
      }
    }
  };

  AasSubmodelElementCollectionType ExplosionSafety = {
    name = "ExplosionSafety",
    semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExplosionSafeties/ExplosionSafety",
    description = "contains information related to explosion safety according to device nameplate.",
    allowDuplicates = true,
    fields = {
      AasField {
        name = "DesignationOfCertificateOrApproval",
        semanticId = "irdi:0112/2///61987#ABH783#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"KEMA99IECEX1105/128"},
        description = "alphanumeric character sequence identifying a certificate or approval Note: Approval identifier, reference to the certificate number, to be entered without spaces."
      },
      AasField {
        name = "TypeOfApproval",
        semanticId = "irdi:0173-1#02-AAM812#003",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"ATEX@en"},
        description = "classification according to the standard or directive to which the approval applies Note: see also [IRDI] 0112/2///61987#ABA231#008 type of hazardous area approval Note: name of the approval system, e.g. ATEX, IECEX, NEC, EAC, CCC, CEC Note: only values from the enumeration should be used as stated. For additional systems further values can be used. Note: Recommendation: property declaration as MLP is required by its semantic definition. As the property value is language independent, users are recommended to provide maximal 1 string in any language of the user’s choice."
      },
      AasField {
        name = "ApprovalAgencyTestingAgency",
        semanticId = "irdi:0173-1#02-AAM632#001",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"PTB@en"},
        description = "certificates and approvals pertaining to general usage and compliance with constructional standards and directives Note: see also [IRDI] 0112/2///61987#ABA634#004 approval agency/testing agency Note: name of the agency, which has issued the certificate, e.g. PTB, KEMA, CSA, SIRA Note: only values from the enumeration should be used as stated. For additional systems further values can be used. Note: Recommendation: property declaration as MLP is required by its semantic definition. As the property value is language independent, users are recommended to provide maximal 1 string in any language of the user’s choice."
      },
      AasField {
        name = "TypeOfProtection",
        semanticId = "irdi:0173-1#02-AAQ325#003",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"db"},
        description = "classification of an explosion protection according to the specific measures applied to avoid ignition of a surrounding explosive atmosphere Note: see also [IRDI] 0112/2///61987#ABA589#002 type of protection (Ex) Note: ·       Type of protection for the device as listed in the certificate ·       Symbol(s) for the Type of protection. Several types of protection are separated by a semicolon “;” ·       If several TypeOfProtection are listed in the same certificate, for each TypeOfProtection a separate SMC “Explosion Safety” shall be provided."
      },
      AasField {
        name = "RatedInsulationVoltage",
        semanticId = "irdi:0173-1#02-AAN532#003",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"250"},
        description = "from the manufacturer for the capital assets limited isolation with given(indicated) operating conditions Note: Um(eff) Note: Insulation voltage, if specified in the certificate."
      },
      AasField {
        name = "InstructionsControlDrawing",
        semanticId = "irdi:0112/2///61987#ABO102#001",
        type = refBy(AasReferenceType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "designation used to uniquely identify a control/reference drawing stored in a file system Note: Reference to the instruction manual or control drawing."
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
        description = "Contains properties which are related to the ambient conditions of the device. Note: If the device is mounted in the process boundary, ambient and process conditions are provided separately."
      },
      AasField {
        name = "ProcessConditions",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExplosionSafeties/ExplosionSafety/ProcessConditions",
        type = refBy(ProcessConditions),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Contains properties which are related to the process conditions of the device. Note: If the device is mounted in the process boundary, ambient and process conditions are provided separately."
      },
      AasField {
        name = "ExternalElectricalCircuit",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExplosionSafeties/ExplosionSafety/ExternalElectricalCircuit",
        type = refBy(ExternalElectricalCircuit),
        minimumInstances = 0,
        description = "specifies the parameters of external electrical circuits. Note: If several external circuits can be connected to the device, this block shall provide a cardinality with the number of circuits Note: If for one external IS circuit several sets of safety parameters are provided (e.g. for several material groups), each set is specified in a separate block as a separate circuit."
      }
    }
  };

  AasSubmodelElementCollectionType AmbientConditions = {
    name = "AmbientConditions",
    semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExplosionSafeties/ExplosionSafety/AmbientConditions",
    description = "Contains properties which are related to the ambient conditions of the device. Note: If the device is mounted in the process boundary, ambient and process conditions are provided separately.",
    fields = {
      AasField {
        name = "DeviceCategory",
        semanticId = "irdi:0173-1#02-AAK297#004",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2G"},
        description = "category of device in accordance with directive 94/9/EC Note: see also [IRDI] 0112/2///61987#ABA467#002 equipment/device category Note: editorial definiton: Category of device in accordance with directive 2014/34/EU Note: Equipment category according to the ATEX system. According to the current nameplate, also the combination “GD” is permitted Note: The combination “GD” is no longer accepted and was changed in the standards. Currently the marking for “G” and “D” must be provided in a separate marking string. Older devices may still exist with the marking “GD”."
      },
      AasField {
        name = "EquipmentProtectionLevel",
        semanticId = "irdi:0173-1#02-AAM668#001",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Gb@en"},
        description = "part of a hazardous area classification system indicating the likelihood of the existence of a classified hazard Note: see also [IRDI] 0112/2///61987#ABA464#005 equipment protection level Note: editorial definition: Level of protection assigned to equipment based on its likelihood of becoming a source of ignition Note: Equipment protection level according to the IEC standards. According to the current nameplate, also the combination “GD” is permitted Note: The combination “GD” is no longer accepted and was changed in the standards. Currently the marking for “G” and “D” must be provided in a separate marking string. Older devices may still exist with the marking “GD”. Note: Recommendation: property declaration as MLP is required by its semantic definition. As the property value is language independent, users are recommended to provide maximal 1 string in any language of the user’s choice."
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
        examples = {"db"},
        description = "classification of an explosion protection according to the specific measures applied to avoid ignition of a surrounding explosive atmosphere Note: see also [IRDI] 0112/2///61987#ABA589#002 type of protection (Ex) Note: Symbol(s) for the Type of protection. Several types of protection are separated by a semicolon “;”."
      },
      AasField {
        name = "ExplosionGroup",
        semanticId = "irdi:0173-1#02-AAT372#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"IIC"},
        description = "classification of dangerous gaseous substances based on their ability to cause an explosion Note: see also [IRDI] 0112/2///61987#ABA961#007 permitted gas group/explosion group Note: Equipment grouping according to IEC 60079-0 is meant by this property Note: Symbol(s) for the gas group (IIA…IIC) or dust group (IIIA…IIIC)."
      },
      AasField {
        name = "MinimumAmbientTemperature",
        semanticId = "irdi:0173-1#02-AAZ952#001",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"-40"},
        description = "lower limit of the temperature range of the surrounding space in which the component, the pipework or the system can be operated Note: see also [IRDI] 0112/2///61987#ABA621#007 minimum ambient temperature Note: editorial defnition: lower limit of the temperature range of the environment in which the component, the pipework or the system can be operated Note: Rated minimum ambient temperature Note: Positive temperatures are listed without “+” sign. If several temperatures ranges are marked, only the most general range shall be indicated in the template, which is consistent with the specified temperature class or maximum surface temperature. Other temperature ranges and temperature classes/maximum surface temperatures may be listed in the instructions."
      },
      AasField {
        name = "MaxAmbientTemperature",
        semanticId = "irdi:0173-1#02-BAA039#010",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"120"},
        description = "upper limit of the temperature range of the surrounding space in which the component, the pipework or the system can be operated Note: see also [IRDI] 0112/2///61987#ABA623#007 maximum ambient temperature Note: editorial definition: upper limit of the temperature range of the environment in which the component, the pipework or the system can be operated Note: Rated maximum ambient temperature Note: Positive temperatures are listed without “+” sign. If several temperatures ranges are marked, only the most general range shall be indicated in the template, which is consistent with the specified temperature class or maximum surface temperature. Other temperature ranges and temperature classes/maximum surface temperatures may be listed in the instructions."
      },
      AasField {
        name = "MaxSurfaceTemperatureForDustProof",
        semanticId = "irdi:0173-1#02-AAM666#005",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"100"},
        description = "maximum permissible surface temperature of a device used in an explosion hazardous area with combustible dust Note: see also [IRDI] 0112/2///61987#ABB159#004 maximum surface temperature for dust-proof Note: Maximum surface temperature of the device (dust layer ? 5 mm) for specified maximum ambient and maximum process temperature, relevant for Group III only Note: Positive temperatures are listed without “+” sign. If several temperatures ranges are marked, only the most general range shall be indicated in the template, which is consistent with the specified temperature class or maximum surface temperature. Other temperature ranges and temperature classes/maximum surface temperatures may be listed in the instructions."
      },
      AasField {
        name = "TemperatureClass",
        semanticId = "irdi:0173-1#02-AAO371#004",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"T4"},
        description = "classification system of electrical apparatus, based on its maximum surface temperature, related to the specific explosive atmosphere for which it is intended to be used Note: see also [IRDI] 0112/2///61987#ABA593#002 temperature class Note: editorial definition: classification system of electrical apparatus, based on its maximum surface temperature, intended for use in an explosive atmospheres with flammable gas, vapour or mist. Note: Temperature class for specified maximum ambient and maximum process temperature, relevant for Group II only (Further combinations may be provided in the instruction manual)."
      }
    }
  };

  AasSubmodelElementCollectionType ProcessConditions = {
    name = "ProcessConditions",
    semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExplosionSafeties/ExplosionSafety/ProcessConditions",
    description = "Contains properties which are related to the process conditions of the device. Note: If the device is mounted in the process boundary, ambient and process conditions are provided separately.",
    fields = {
      AasField {
        name = "DeviceCategory",
        semanticId = "irdi:0173-1#02-AAK297#004",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1G"},
        description = "category of device in accordance with directive 94/9/EC Note: see also [IRDI] 0112/2///61987#ABA467#002 equipment/device category Note: editorial defnition: Category of device in accordance with directive 2014/34/EU Note: Equipment category according to the ATEX system."
      },
      AasField {
        name = "EquipmentProtectionLevel",
        semanticId = "irdi:0173-1#02-AAM668#001",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Ga@en"},
        description = "part of a hazardous area classification system indicating the likelihood of the existence of a classified hazard Note: see also [IRDI] 0112/2///61987#ABA464#005 equipment protection level Note: editorial defnition: Level of protection assigned to equipment based on its likelihood of becoming a source of ignition Note: Equipment protection level according to the IEC or other standards, e.g. Ga (IEC), Class I/Division 1 (US), Zone (EAC) Note: Recommendation: property declaration as MLP is required by its semantic definition. As the property value is language independent, users are recommended to provide maximal 1 string in any language of the user’s choice."
      },
      AasField {
        name = "RegionalSpecificMarking",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExplosionSafeties/ExplosionSafety/RegionalSpecificMarking",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"IS"},
        description = "Marking used only in specific regions, e.g. North America: class/divisions, EAC: “1” or NEC: “AIS”."
      },
      AasField {
        name = "TypeOfProtection",
        semanticId = "irdi:0173-1#02-AAQ325#003",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"ia"},
        description = "classification of an explosion protection according to the specific measures applied to avoid ignition of a surrounding explosive atmosphere Note: see also [IRDI] 0112/2///61987#ABA589#002 type of protection (Ex) Note: Symbol(s) for the Type of protection. Several types of protection are separated by a semicolon “;”."
      },
      AasField {
        name = "ExplosionGroup",
        semanticId = "irdi:0173-1#02-AAT372#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"IIC"},
        description = "classification of dangerous gaseous substances based on their ability to cause an explosion Note: see also [IRDI] 0112/2///61987#ABA961#007 permitted gas group/explosion group Note: editorial definition: classification of dangerous gaseous substances based on their ability to be ignited Note: Equipment grouping according to IEC 60079-0 is meant by this property Note: Symbol(s) for the gas group (IIA…IIC) or dust group (IIIA…IIIC)."
      },
      AasField {
        name = "LowerLimitingValueOfProcessTemperature",
        semanticId = "irdi:0173-1#02-AAN309#004",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"-40"},
        description = "lowest temperature to which the wetted parts of the equipment can be subjected without permanent impairment of operating characteristics Note: Rated minimum process temperature Note: Positive temperatures are listed without “+” sign. If several temperatures ranges are marked, only the most general range shall be indicated in the template, which is consistent with the specified temperature class or maximum surface temperature. Other temperature ranges and temperature classes/maximum surface temperatures may be listed in the instructions."
      },
      AasField {
        name = "UpperLimitingValueOfProcessTemperature",
        semanticId = "irdi:0173-1#02-AAN307#004",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"120"},
        description = "highest temperature to which the wetted parts of the device may be subjected without permanent impairment of operating characteristics Note: Rated maximum process temperature Note: Positive temperatures are listed without “+” sign. If several temperatures ranges are marked, only the most general range shall be indicated in the template, which is consistent with the specified temperature class or maximum surface temperature. Other temperature ranges and temperature classes/maximum surface temperatures may be listed in the instructions."
      },
      AasField {
        name = "MaxSurfaceTemperatureForDustProof",
        semanticId = "irdi:0173-1#02-AAM666#005",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"85"},
        description = "maximum permissible surface temperature of a device used in an explosion hazardous area with combustible dust Note: see also [IRDI] 0112/2///61987#ABB159#004 maximum surface temperature for dust-proof Note: Maximum surface temperature (dust layer ? 5 mm) for specified maximum ambient and maximum process temperature, relevant for Group III only Note: Positive temperatures are listed without “+” sign. If several temperatures ranges are marked, only the most general range shall be indicated in the template, which is consistent with the specified temperature class or maximum surface temperature. Other temperature ranges and temperature classes/maximum surface temperatures may be listed in the instructions."
      },
      AasField {
        name = "TemperatureClass",
        semanticId = "irdi:0173-1#02-AAO371#004",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"T4"},
        description = "classification system of electrical apparatus, based on its maximum surface temperature, related to the specific explosive atmosphere for which it is intended to be used Note: see also [IRDI] 0112/2///61987#ABA593#002 temperature class Note: editorial definition: classification system of electrical apparatus, based on its maximum surface temperature, intended for use in an explosive atmospheres with flammable gas, vapour or mist. Note: Temperature class for specified maximum ambient and maximum process temperature, relevant for Group II only (Further combinations may be provided in the instruction manual)."
      }
    }
  };

  AasSubmodelElementCollectionType ExternalElectricalCircuit = {
    name = "ExternalElectricalCircuit",
    semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExplosionSafeties/ExplosionSafety/ExternalElectricalCircuit",
    description = "specifies the parameters of external electrical circuits. Note: If several external circuits can be connected to the device, this block shall provide a cardinality with the number of circuits Note: If for one external IS circuit several sets of safety parameters are provided (e.g. for several material groups), each set is specified in a separate block as a separate circuit.",
    fields = {
      AasField {
        name = "DesignationOfElectricalTerminal",
        semanticId = "irdi:0112/2///61987#ABB147#004",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"+/-"},
        description = "alphanumeric character sequence identifying an electrical terminal Note: For each circuit the designation of the terminals shall be specified. If several circuits are provided with the same parameters, their terminal pairs are listed and separated by a semicolon. If several circuits belong to one channel this shall be described in the instructions."
      },
      AasField {
        name = "TypeOfProtection",
        semanticId = "irdi:0173-1#02-AAQ325#003",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"db"},
        description = "classification of an explosion protection according to the specific measures applied to avoid ignition of a surrounding explosive atmosphere Note: see also [IRDI] 0112/2///61987#ABA589#002 type of protection (Ex)) Note: ·       Type of protection for the device as listed in the certificate ·       Symbol(s) for the Type of protection. Several types of protection are separated by a semicolon “;” ·       If several TypeOfProtection are listed in the same certificate, for each TypeOfProtection a separate SMC “Explosion Safety” shall be provided."
      },
      AasField {
        name = "EquipmentProtectionLevel",
        semanticId = "irdi:0173-1#02-AAM668#001",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Ga@en"},
        description = "part of a hazardous area classification system indicating the likelihood of the existence of a classified hazard Note: see also [IRDI] 0112/2///61987#ABA464#005 equipment protection level Note: editorial definition: Level of protection assigned to equipment based on its likelihood of becoming a source of ignition Note: EPL according to IEC standards Note: value should be chosen from an enumeration list with values “Ga, Gb, Gc, Da, Db, Dc, Ma, Mb” Note: Recommendation: property declaration as MLP is required by its semantic definition. As the property value is language independent, users are recommended to provide maximal 1 string in any language of the user’s choice."
      },
      AasField {
        name = "ExplosionGroup",
        semanticId = "irdi:0173-1#02-AAT372#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"IIC"},
        description = "classification of dangerous gaseous substances based on their ability to cause an explosion Note: see also [IRDI] 0112/2///61987#ABA961#007 permitted gas group/explosion group Note: editorial definition: classification of dangerous gaseous substances based on their ability to be ignited Note: Equipment grouping according to IEC 60079-0 is meant by this property Note: Symbol(s) for the gas group (IIA…IIC) or dust group (IIIA…IIIC)."
      },
      AasField {
        name = "Characteristics",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExplosionSafeties/ExplosionSafety/ExternalElectricalCircuit/Characteristics",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"linear"},
        description = "Characteristic of the intrinsically safe circuit Note: linear/ non-linear."
      },
      AasField {
        name = "Fisco",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExplosionSafeties/ExplosionSafety/ExternalElectricalCircuit/Fisco",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"x"},
        description = "FISCO certified intrinsically safe fieldbus circuit (IEC 60079-11) Note: Enter “x” if relevant."
      },
      AasField {
        name = "TwoWISE",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/2/0/Nameplate/Markings/Marking/ExplosionSafeties/ExplosionSafety/ExternalElectricalCircuit/TwoWISE",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"x"},
        description = "2-WISE certified intrinsically safe circuit (IEC 60079-47) Note: Enter “x” if relevant."
      },
      AasField {
        name = "SafetyRelatedPropertiesForPassiveBehaviour",
        semanticId = "irdi:0173-1#02-AAQ380#006",
        type = refBy(SafetyRelatedPropertiesForPassiveBehaviour),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "properties characterizing the safety related parameters of a loop-powered, intrinsically safe input or output circuit Note: see also [IRDI] 0112/2///61987#ABC586#001 Safety related properties for passive behaviour Note: IS-parameters for passive circuits, if relevant (e.g. 2 wire field devices, valves)."
      },
      AasField {
        name = "SafetyRelatedPropertiesForActiveBehaviour",
        semanticId = "irdi:0173-1#02-AAQ381#006",
        type = refBy(SafetyRelatedPropertiesForActiveBehaviour),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "properties characterizing the safety related parameters of an intrinsically safe circuit Note: see also [IRDI] 0112/2///61987#ABC585#001 Safety related properties for active behaviour Note: IS-parameters for active circuits, if relevant (e.g. power supply, IS-barriers)."
      }
    }
  };

  AasSubmodelElementCollectionType SafetyRelatedPropertiesForPassiveBehaviour = {
    name = "SafetyRelatedPropertiesForPassiveBehaviour",
    semanticId = "irdi:0173-1#02-AAQ380#006",
    description = "properties characterizing the safety related parameters of a loop-powered, intrinsically safe input or output circuit Note: see also [IRDI] 0112/2///61987#ABC586#001 Safety related properties for passive behaviour Note: IS-parameters for passive circuits, if relevant (e.g. 2 wire field devices, valves).",
    fields = {
      AasField {
        name = "MaxInputPower",
        semanticId = "irdi:0173-1#02-AAQ372#003",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1250"},
        description = "maximum power that can be applied to the connection facilities of the apparatus without invalidating the type of protection Note: see also [IRDI] 0112/2///61987#ABA981#001 maximum input power (Pi) Note: Limit value for input power."
      },
      AasField {
        name = "MaxInputVoltage",
        semanticId = "irdi:0173-1#02-AAM638#003",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"30"},
        description = "maximum voltage (peak a.c. or d.c.) that can be applied to the connection facilities of the apparatus without invalidating the type of protection Note: see also [IRDI] 0112/2///61987#ABA982#001 maximum input voltage (Ui) Note: Limit value for input voltage."
      },
      AasField {
        name = "MaxInputCurrent",
        semanticId = "irdi:0173-1#02-AAM642#004",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"100"},
        description = "maximum current (peak a.c. or d.c) that can be applied to the connection facilities of the apparatus without invalidating the type of protection Note: see also [IRDI] 0112/2///61987#ABA983#001 maximum input current (Ii) Note: Limit value for input current."
      },
      AasField {
        name = "MaxInternalCapacitance",
        semanticId = "irdi:0173-1#02-AAM640#004",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0"},
        description = "maximum equivalent internal capacitance of the apparatus which is considered as appearing across the connection facilities Note: see also [IRDI] 0112/2///61987#ABA984#001 maximum internal capacitance (Ci) Note: Maximum internal capacitance of the circuit."
      },
      AasField {
        name = "MaxInternalInductance",
        semanticId = "irdi:0173-1#02-AAM639#003",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0"},
        description = "maximum equivalent internal inductance of the apparatus which is considered as appearing across the connection facilities Note: see also [IRDI] 0112/2///61987#ABA985#001 maximum internal inductance (Li) Note: Maximum internal inductance of the circuit."
      }
    }
  };

  AasSubmodelElementCollectionType SafetyRelatedPropertiesForActiveBehaviour = {
    name = "SafetyRelatedPropertiesForActiveBehaviour",
    semanticId = "irdi:0173-1#02-AAQ381#006",
    description = "properties characterizing the safety related parameters of an intrinsically safe circuit Note: see also [IRDI] 0112/2///61987#ABC585#001 Safety related properties for active behaviour Note: IS-parameters for active circuits, if relevant (e.g. power supply, IS-barriers).",
    fields = {
      AasField {
        name = "MaxOutputPower",
        semanticId = "irdi:0173-1#02-AAQ371#003",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"960"},
        description = "maximum electrical power that can be taken from the apparatus Note: see also [IRDI] 0112/2///61987#ABA987#001 maximum output power (Po) Note: Limit value for output power."
      },
      AasField {
        name = "MaxOutputVoltage",
        semanticId = "irdi:0173-1#02-AAM635#003",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"15.7"},
        description = "maximum voltage (peak a.c. or d.c.) that can occur at the connection facilities of the apparatus at any applied voltage up to the maximum voltage Note: see also [IRDI] 0112/2///61987#ABA989#001 maximum output voltage (Uo) Note: Limit value for open circuits output voltage."
      },
      AasField {
        name = "MaxOutputCurrent",
        semanticId = "irdi:0173-1#02-AAM641#004",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"245"},
        description = "maximum current (peak a.c. or d.c.) in the apparatus that can be taken from the connection facilities of the apparatus Note: see also [IRDI] 0112/2///61987#ABA988#001maximum output current (Io) Note: Limit value for closed circuit output current."
      },
      AasField {
        name = "MaxExternalCapacitance",
        semanticId = "irdi:0173-1#02-AAM637#004",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2878"},
        description = "maximum capacitance that can be connected to the connection facilities of the apparatus without invalidating the type of protection Note: see also [IRDI] 0112/2///61987#ABA990#001 maximum external capacitance (Co) Note: Maximum external capacitance to be connected to the circuit."
      },
      AasField {
        name = "MaxExternalInductance",
        semanticId = "irdi:0173-1#02-AAM636#003",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2.9"},
        description = "maximum value of inductance that can be connected to the connection facilities of the apparatus without invalidating the type of protection Note: see also [IRDI] 0112/2///61987#ABA991#001 maximum external inductance (Lo) Note: Maximum external inductance to be connected to the circuit."
      },
      AasField {
        name = "MaxExternalInductanceResistanceRatio",
        semanticId = "irdi:0173-1#02-AAM634#003",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "maximum value of ratio of inductance (Lo) to resistance (Ro) of any external circuit that can be connected to the connection facilities of the electrical apparatus without invalidating intrinsic safety Note: see also [IRDI] 0112/2///61987#ABB145#001 maximum external inductance/resistance ratio (Lo/Ro) Note: External Inductance to Resistance ratio."
      }
    }
  };

  AasSubmodelElementCollectionType AssetSpecificProperties = {
    name = "AssetSpecificProperties",
    semanticId = "irdi:0173-1#01-AGZ672#001",
    description = "Group of properties that are listed on the asset's nameplate and are grouped based on guidelines Note: defined as “Asset specific nameplate information” per ECLASS.",
    allowDuplicates = true,
    fields = {
      AasField {
        name = "GuidelineSpecificProperties",
        semanticId = "irdi:0173-1#01-AHD205#001",
        counting = true,
        type = refBy(GuidelineSpecificProperties),
        minimumInstances = 1,
        description = "Asset specific nameplate information required by guideline, stipulation or legislation."
      },
      AasField {
        name = "arbitrary",
        displayName = "{arbitrary}",
        semanticId = "iri:www.example.com/ids/cd/3325_9020_5022_1964",
        isGeneric = true,
        type = refBy(StringListType),
        minimumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType GuidelineSpecificProperties = {
    name = "GuidelineSpecificProperties",
    semanticId = "irdi:0173-1#01-AHD205#001",
    description = "Asset specific nameplate information required by guideline, stipulation or legislation.",
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
        semanticId = "iri:www.example.com/ids/cd/3325_9020_5022_1074",
        isGeneric = true,
        type = refBy(StringListType),
        minimumInstances = 1
      }
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
