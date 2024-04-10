project IDTA_02007_SoftwareNameplate {

  version v1.0;

  import AASDataTypes;
  import IDTA_02002_ContactInformations with (IDTA_02002_ContactInformations.version == v1.0);

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasSubmodelType SoftwareNameplate = {
    name = "SoftwareNameplate",
    semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0",
    description = "Submodel containing the nameplate information for software Asset and associated product classificatons.",
    versionIdentifier = "IDTA 02007-1-0",
    fixedName = true,
    fields = {
      AasField {
        name = "SoftwareNameplate_Type",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplateType",
        type = refBy(SoftwareNameplate_Type),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "SMC defining type-related properties of a software Asset."
      },
      AasField {
        name = "SoftwareNameplate_Instance",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplateInstance",
        type = refBy(SoftwareNameplate_Instance),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "SMC defining instance-related properties of a software Asset."
      }
    }
  };

  AasSubmodelElementCollectionType SoftwareNameplate_Type = {
    name = "SoftwareNameplate_Type",
    semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplateType",
    description = "SMC containing the nameplate information for a type of a software Asset.",
    versionIdentifier = "IDTA 02007-1-0",
    fixedName = true,
    fields = {
      AasField {
        name = "URIOfTheProduct",
        semanticId = "irdi:0173-1#02-AAY811#001",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"ZVEI.I40.ITinAutomation.Dem oSW_123456"},
        description = "Unique global identification of the product using a universal resource identifier (URI)."
      },
      AasField {
        name = "ManufacturerName",
        semanticId = "irdi:0173-1#02-AAO677#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"ZVEI AK IT in Automation"},
        description = "Legally valid designation of the natural or judicial person which is directly responsible for the design, production, packaging and labeling of a product in respect to its being brought into circulation."
      },
      AasField {
        name = "ManufacturerProductDesignation",
        semanticId = "irdi:0173-1#02-AAW338#001",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"My Software Package for Demonstration"},
        description = "The name of the product, provided by the manufacturer."
      },
      AasField {
        name = "ManufacturerProductDescription",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateType/ManufacturerProductDescription",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"A first software installation to be used for demo purpose only."},
        description = "Description of the product, it's technical features and implementation if needed (long text)."
      },
      AasField {
        name = "ManufacturerProductFamily",
        semanticId = "irdi:0173-1#02-AAU731#001",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Demo Products for IT in Automation"},
        description = "2nd level of a 3 level manufacturer specific product hierarchy."
      },
      AasField {
        name = "ManufacturerProductType",
        semanticId = "irdi:0173-1#02-AAO057#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"DP-AKIT-A"},
        description = "Characteristic to differentiate between different products of a product family or special variants."
      },
      AasField {
        name = "SoftwareType",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateType/SoftwareType",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"PLC Runtime"},
        description = "The type of the software (category, e.g. Runtime, Application, Firmeware, Driver, etc.)."
      },
      AasField {
        name = "Version",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateType/Version",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"0.9.1.0"},
        description = "The complete version information consisting of Major Version, Minor Version, Revision and Build Number."
      },
      AasField {
        name = "VersionName",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateType/VersionName",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"R2021 beta"},
        description = "The name this particular version is given."
      },
      AasField {
        name = "VersionInfo",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateType/VersionInfo",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Please do not install in productive environments!"},
        description = "Provides a textual description of most relevant characteristics of the version of the software."
      },
      AasField {
        name = "ReleaseDate",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateType/ReleaseDate",
        type = refBy(DateTimeType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"20220207"},
        description = "The moment in time, when this version of the software was made publicly available."
      },
      AasField {
        name = "ReleaseNotes",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateType/ReleaseNotes",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"This release requires special configuration."},
        description = "Contains information about this release."
      },
      AasField {
        name = "BuildDate",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateType/BuildDate",
        type = refBy(DateTimeType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"20201119"},
        description = "The moment in time, when this particular build of software was created."
      },
      AasField {
        name = "InstallationURI",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateType/InstallationURI",
        type = refBy(AasAnyURIType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"https://tud.de/inf/pk/demo- sw/download/DemoFirmware","_09.zip"},
        description = "Indicates the resource, where the software is being provided by the manufacturer."
      },
      AasField {
        name = "InstallationFile",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateType/InstallationFile",
        type = refBy(AasBlobType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Contains the installation code as BLOB."
      },
      AasField {
        name = "InstallerType",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateType/InstallerType",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"MSI"},
        description = "Indicates the type of installation package."
      },
      AasField {
        name = "InstallationChecksum",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateType/InstallationChecksum",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0x2783"},
        description = "Provides the checksum for the software available at InstallationURI."
      }
    }
  };

  AasSubmodelElementCollectionType SoftwareNameplate_Instance = {
    name = "SoftwareNameplate_Instance",
    semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplateInstance",
    description = "SMC containing the nameplate information for an instance of a software Asset.",
    versionIdentifier = "IDTA 02007-1-0",
    fixedName = true,
    fields = {
      AasField {
        name = "SerialNumber",
        semanticId = "irdi:0173-1#02-AAM556#002",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"123456"},
        description = "Unique combination of numbers and letters used to identify the software instance."
      },
      AasField {
        name = "InstanceName",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateInstance/InstanceName",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"My Software Instance"},
        description = "The name of the software instance."
      },
      AasField {
        name = "InstalledVersion",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateInstance/InstalledVersion",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0.9.1.0"},
        description = "The version information of the installed instance, consisting of Major Version, Minor Version, Revision and Build Number indicates the actual version of the instance."
      },
      AasField {
        name = "InstallationDate",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateInstance/InstallationDate",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"20201119T09:30:20"},
        description = "Date of Installation."
      },
      AasField {
        name = "InstallationPath",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateInstance/InstallationPath",
        type = refBy(AasAnyURIType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"C:\\Windows\\Program Files\\Demo\\Firmware"},
        description = "Indicates the path to the installed instance of the software."
      },
      AasField {
        name = "InstallationSource",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateInstance/InstallationSource",
        type = refBy(AasAnyURIType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"https://tud.de/inf/pk/installatio n/firmware/src"},
        description = "Indicates the path to the installation files used in this instance of the software."
      },
      AasField {
        name = "InstalledOnArchitecture",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateInstance/InstalledOnArchitecture",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"x86-32"},
        description = "Indicates the processor architecture this instance is installed on."
      },
      AasField {
        name = "InstalledOnOS",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateInstance/InstalledOnOS",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Windows 10"},
        description = "Indicates the operating system this instance is installed on."
      },
      AasField {
        name = "InstalledOnHost",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateInstance/InstalledOnHost",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"IPC_42"},
        description = "Indicates the host system in case of a virtual environment."
      },
      AasField {
        name = "InstalledModules",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateInstance/InstalledModules",
        type = refBy(InstalledModules),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Collection of installed modules."
      },
      AasField {
        name = "ConfigurationPaths",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateInstance/ConfigurationPaths",
        type = refBy(ConfigurationPaths),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Indicates the path to the configuration information."
      },
      AasField {
        name = "SLAInformation",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateInstance/SLAInformation",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Service level GOLD USER."},
        description = "Indicates the actual service level agreements."
      },
      AasField {
        name = "Contact",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/ContactInformation",
        type = refBy(ContactInformation),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Collection for general contact data."
      },
      AasField {
        name = "InventoryTag",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateInstance/InventoryTag",
        type = refBy(StringListType),
        minimumInstances = 0,
        examples = {"TU3-88D5"},
        description = "Specifies an information used for inventory of the software."
      }
    }
  };

  AasSubmodelElementCollectionType InstalledModules = {
    name = "InstalledModules",
    semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateInstance/InstalledModules",
    description = "Contains a list of installed modules of the software instance.",
    versionIdentifier = "IDTA 02007-1-0",
    fields = {
      AasField {
        name = "InstalledModule",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateInstance/InstalledModule",
        type = refBy(StringListType),
        minimumInstances = 1,
        examples = {"main"},
        description = "The name of a particular module installed."
      }
    }
  };

  AasSubmodelElementCollectionType ConfigurationPaths = {
    name = "ConfigurationPaths",
    semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateInstance/ConfigurationPaths",
    description = "Contains a list of configuration entries of the software instance.",
    versionIdentifier = "IDTA 02007-1-0",
    fields = {
      AasField {
        name = "ConfigurationPath",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateInstance/ConfigurationPath",
        type = refBy(ConfigurationPath),
        minimumInstances = 1,
        description = "Contains a single configuration entry."
      }
    }
  };

  AasSubmodelElementCollectionType ConfigurationPath = {
    name = "ConfigurationPath",
    semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateInstance/ConfigurationPath",
    description = "Contains a single configuration entry of the software instance.",
    versionIdentifier = "IDTA 02007-1-0",
    fields = {
      AasField {
        name = "ConfigurationURI",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateInstance/ConfigurationURI",
        type = refBy(AasAnyURIType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"C:\\Users\\mw30\\Documents\\Z VEI\\AKITinAutomation\\20210 113"},
        description = "Indicates the path to the configuration."
      },
      AasField {
        name = "ConfigurationType",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateInstance/ConfigurationType",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"initial configuration"},
        description = "Indicates the type of configuration (e.g. general configuration, user configuration)."
      }
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
