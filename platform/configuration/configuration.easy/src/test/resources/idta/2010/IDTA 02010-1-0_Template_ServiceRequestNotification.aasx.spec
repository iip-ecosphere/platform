project IDTA_02010_ServiceRequestNotification {

  version v1.0;

  import AASDataTypes;
  import IDTA_02002_ContactInformations with (IDTA_02002_ContactInformations.version == v1.0);

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasSubmodelType ServiceRequestNotification = {
    name = "ServiceRequestNotification",
    semanticId = "irdi:0173-1#01-AHX443#001",
    description = "This submodel aims to standardize the description of a service request notification that can be used to create a service request notification for industrial assets or the asset creates it by itself.",
    fields = {
      AasField {
        name = "NumberOfServiceRequestNotifications",
        semanticId = "irdi:0173-1#02-ABI761#001",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "ServiceRequestNotification",
        semanticId = "irdi:0173-1#01-AHX444#001",
        type = refBy(ServiceRequestNotification),
        minimumInstances = 0,
        description = "Service request notification."
      }
    }
  };

  AasSubmodelElementCollectionType ReportedBy = {
    name = "ReportedBy",
    semanticId = "irdi:0173-1#01-AHX448#001",
    description = "Reported by.",
    fields = {
      AasField {
        name = "CustomerNumber",
        semanticId = "irdi:0173-1#02-ABI769#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "An alphanumeric key that identifies a customer."
      },
      AasField {
        name = "SenderSystem",
        semanticId = "irdi:0173-1#02-ABI770#001",
        type = refBy(SenderSystem),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Sender system."
      },
      AasField {
        name = "NumberOfContacs",
        semanticId = "irdi:0173-1#02-AAO203#004",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "ContactInformation",
        semanticId = "irdi:0173-1#01-ADR448#007",
        type = refBy(ContactInformation),
        minimumInstances = 1,
        description = "The SMC “ContactInformation” contains information on how to contact the manufacturer or an authorised service provider, e.g. when a maintenance service is required."
      }
    }
  };

  AasEntityType SenderSystem = {
    name = "SenderSystem",
    semanticId = "irdi:0173-1#02-ABI770#001",
    description = "Sender system."
  };

  AasEntityType RelatedAsset = {
    name = "RelatedAsset",
    semanticId = "irdi:0173-1#02-ABI774#001",
    description = "Related Asset."
  };

  AasSubmodelElementCollectionType DetailedInformation = {
    name = "DetailedInformation",
    semanticId = "irdi:0173-1#01-AHX445#001",
    description = "Detailed information of service need.",
    fields = {
      AasField {
        name = "LongText",
        semanticId = "irdi:0173-1#02-ABI764#001",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Textual detailed description of the subject matter."
      },
      AasField {
        name = "StartOfFault",
        semanticId = "irdi:0173-1#02-ABI765#001",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Point in time at which the regarded situation happened or started."
      },
      AasField {
        name = "ErrorCode",
        semanticId = "irdi:0173-1#02-ABI766#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "technical key of the manufacturer for malfunction description."
      },
      AasField {
        name = "AttachedMedia",
        semanticId = "irdi:0173-1#01-AHX446#001",
        type = refBy(AttachedMedia),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Attached media."
      }
    }
  };

  AasSubmodelElementCollectionType AttachedMedia = {
    name = "AttachedMedia",
    semanticId = "irdi:0173-1#01-AHX446#001",
    description = "Attached media.",
    fields = {
      AasField {
        name = "NumberOfMedias",
        semanticId = "irdi:0173-1#02-ABI767#001",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "Media",
        semanticId = "irdi:0173-1#01-AHX447#001",
        type = refBy(Media),
        minimumInstances = 1,
        description = "Media."
      }
    }
  };

  AasSubmodelElementCollectionType Media = {
    name = "Media",
    semanticId = "irdi:0173-1#01-AHX447#001",
    description = "Media.",
    fields = {
      AasField {
        name = "DigitalFile",
        semanticId = "irdi:0173-1#02-ABK126#001",
        type = refBy(AasFileResourceType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "computer resource for recording data in a computer storage device, primarily identified by its file name."
      },
      AasField {
        name = "Comment",
        semanticId = "irdi:0173-1#02-ABI768#001",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Explanations that refer to the attached file and relate to its use or origin."
      }
    }
  };

  AasSubmodelElementCollectionType OnsiteContact = {
    name = "OnsiteContact",
    semanticId = "irdi:0173-1#01-AHX449#001",
    description = "Onsite contact.",
    fields = {
      AasField {
        name = "PartnerNumber",
        semanticId = "irdi:0173-1#02-ABI771#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "An alphanumeric key that identifies a business partner."
      },
      AasField {
        name = "NumberOfContacts",
        semanticId = "irdi:0173-1#02-AAO203#004",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "ContactInformation",
        semanticId = "irdi:0173-1#01-ADR448#007",
        type = refBy(ContactInformation),
        minimumInstances = 1,
        description = "The SMC “ContactInformation” contains information on how to contact the manufacturer or an authorised service provider, e.g. when a maintenance service is required."
      }
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
