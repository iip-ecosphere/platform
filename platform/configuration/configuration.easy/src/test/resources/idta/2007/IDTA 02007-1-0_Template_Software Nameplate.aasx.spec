project IDTA_02007_SoftwareNameplate {

  version v0.15;

  import AASDataTypes;
  import IDTA_02002_ContactInformations with (IDTA_02002_ContactInformations.version == v1.0);

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasSubmodelType SoftwareNameplate = {
    name = "SoftwareNameplate",
    semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0",
    description = "Contains the nameplate information attached to the software product.",
    fields = {
      AasField {
        name = "SoftwareNameplateType",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateType",
        type = refBy(SoftwareNameplateType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "This collection contains the type related information."
      },
      AasField {
        name = "SoftwareNameplateInstance",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateInstance",
        type = refBy(SoftwareNameplateInstance),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "This collection contains the type related information."
      }
    }
  };

  AasSubmodelElementCollectionType SoftwareNameplateType = {
    name = "SoftwareNameplateType",
    semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateType",
    description = "This collection contains the type related information.",
    fields = {
      AasField {
        name = "URIOfTheProduct",
        semanticId = "irdi:0173-1#02-AAY811#001",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "unique global identification of the product using an universal resource identifier (URI)."
      },
      AasField {
        name = "ManufacturerName",
        semanticId = "irdi:0173-1#02-AAO677#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "legally valid designation of the natural or judicial person which is directly responsible for the design, production, packaging and labeling of a product in respect to its being brought into circulation."
      },
      AasField {
        name = "ManufacturerProductDesignation",
        semanticId = "irdi:0173-1#02-AAW338#001",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Short description of the product (short text)."
      },
      AasField {
        name = "ManufacturerProductDescription",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateType/ManufacturerProductDescription",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "A description of the software product, provided by the manufacturer."
      },
      AasField {
        name = "ManufacturerProductFamily",
        semanticId = "irdi:0173-1#02-AAU731#001",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "2nd level of a 3 level manufacturer specific product hierarchy."
      },
      AasField {
        name = "ManufacturerProductType",
        semanticId = "irdi:0173-1#02-AAO057#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Characteristic to differentiate between different products of a product family or special variants."
      },
      AasField {
        name = "SoftwareType",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateType/SoftwareType",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"PLC Runtime"},
        description = "type of the software."
      },
      AasField {
        name = "Version",
        semanticId = "irdi:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateType/Version",
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
        examples = {"R2021 beta@en"},
        description = "the name this particular version is given."
      },
      AasField {
        name = "VersionInfo",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateType/VersionInfo",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Please do not install in productive environments!@en"},
        description = "Provides a textual description of most relevant characteristics of the version of the software."
      },
      AasField {
        name = "ReleaseDate",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateType/ReleaseDate",
        type = refBy(DateTimeType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"20220207"},
        description = "The moment in time, when this particular build of software was released."
      },
      AasField {
        name = "ReleaseNotes",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateType/ReleaseNotes",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"This release requires special configuration.@en"},
        description = "Provides a textual description of most relevant characteristics of the version of the software."
      },
      AasField {
        name = "ReleaseInformation",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateType/ReleaseInformation",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1
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
        examples = {"https://tud.de/inf/pk/demo-sw/download/DemoFirmware_09.zip"},
        description = "Indicates the resource, where the software is being provided by the manufacturer."
      },
      AasField {
        name = "InstallationFile",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateType/InstallationFile",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "InstallerType",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateType/InstallerType",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1"},
        description = "Indicates the type of installation package."
      },
      AasField {
        name = "InstallationChecksum",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateType/InstallationChecksum",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0x2783"},
        description = "provides the checksum for the software available at InstallationURI."
      }
    }
  };

  AasSubmodelElementCollectionType SoftwareNameplateInstance = {
    name = "SoftwareNameplateInstance",
    semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateInstance",
    description = "This collection contains the type related information.",
    fields = {
      AasField {
        name = "SerialNumber",
        semanticId = "irdi:0173-1#02-AAM556#002",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"123456"},
        description = "unique combination of numbers and letters used to identify the device once it has been manufactured."
      },
      AasField {
        name = "InventoryTag",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateInstance/InventoryTag",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"TU3-88D5"},
        description = "contains inventory information of the user."
      },
      AasField {
        name = "InstanceName",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateInstance/InstanceName",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"My Software Instance"},
        description = "Der Name der konkreten Instanz."
      },
      AasField {
        name = "InstalledVersion",
        semanticId = "irdi:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateInstance/InstalledVersion",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0.9.1.0"},
        description = "indicates the actual version of the instance."
      },
      AasField {
        name = "InstallationDate",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateInstance/InstallationDate",
        type = refBy(DateTimeType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"20201119"},
        description = "containes the time of installation of this instance."
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
        examples = {"https://tud.de/inf/pk/installation/firmware/src"},
        description = "Indicates the path to the installation files used in this instance of the software."
      },
      AasField {
        name = "InstalledOnArchitecture",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateInstance/InstalledOnArchitecture",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"x64"},
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
        examples = {"Feldgerät"},
        description = "Indicates the host system in case of a virtual environment."
      },
      AasField {
        name = "InstalledModules",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateInstance/InstalledModules",
        type = refBy(InstalledModules),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Indicates which modules of a software have been installed."
      },
      AasField {
        name = "ConfigurationPaths",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateInstance/ConfigurationPaths",
        type = refBy(ConfigurationPaths),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "collection of configuration path entries."
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
        description = "The SMC “ContactInformation” contains information on how to contact the manufacturer or an authorised service provider, e.g. when a maintenance service is required."
      }
    }
  };

  AasSubmodelElementCollectionType InstalledModules = {
    name = "InstalledModules",
    semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateInstance/InstalledModules",
    description = "Indicates which modules of a software have been installed.",
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
    description = "collection of configuration path entries.",
    fields = {
      AasField {
        name = "ConfigurationPath",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateInstance/ConfigurationPath",
        type = refBy(ConfigurationPath),
        minimumInstances = 1,
        description = "Indicates the path to the configuration."
      }
    }
  };

  AasSubmodelElementCollectionType ConfigurationPath = {
    name = "ConfigurationPath",
    semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateInstance/ConfigurationPath",
    description = "Indicates the path to the configuration.",
    fields = {
      AasField {
        name = "ConfigurationURI",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateInstance/ConfigurationPath",
        type = refBy(AasAnyURIType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"C:\\Users\\mw30\\Documents\\ZVEI\\AKITinAutomation\\20210113"},
        description = "Indicates the path to the configuration."
      },
      AasField {
        name = "ConfigurationType",
        semanticId = "iri:https://admin-shell.io/idta/SoftwareNameplate/1/0/SoftwareNameplate/SoftwareNameplateInstance/ConfigurationType",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1"},
        description = "Indicates the type of configuration (e.g. general configuration, user configuration)."
      }
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
