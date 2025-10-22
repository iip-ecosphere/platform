project IDTA_02010_ServiceRequestNotification {

  version v1.0;

  import AASDataTypes;
  import IDTA_02002_ContactInformations with (IDTA_02002_ContactInformations.version == v1.0);

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasEnumType Priority = {
    name = "Priority",
    description = "Rating of the primacy of the message, which results from the urgency or importance definition.",
    versionIdentifier = "IDTA 02010-1-0",
    semanticId = "irdi:0173-1#02-ABI773#001",
    literals = {
      AasEnumLiteral {
        name = "Low",
        semanticId = "irdi:0173-1#07-ACA025#001"
      },
      AasEnumLiteral {
        name = "Medium",
        semanticId = "irdi:0173-1#07-ACA026#001"
      },
      AasEnumLiteral {
        name = "High",
        semanticId = "irdi:0173-1#07-ACA027#001"
      },
      AasEnumLiteral {
        name = "Very high",
        semanticId = "irdi:0173-1#07-ACA028#001"
      }
    }
  };

  AasEnumType Status = {
    name = "Status",
    description = "Current processing status of the notification within the workflow definition.",
    versionIdentifier = "IDTA 02010-1-0",
    semanticId = "irdi:0173-1#02-ABH938#002",
    literals = {
      AasEnumLiteral {
        name = "Message was sent by issuer",
        identifier = "sent",
        value = "Message was sent by issuer",
        semanticId = "irdi:0173-1#07-ACA040#001"
      },
      AasEnumLiteral {
        name = "Message was viewed by recipient",
        identifier = "received",
        value = "Message was viewed by recipient",
        semanticId = "irdi:0173-1#07-ACA039#001"
      },
      AasEnumLiteral {
        name = "Message is in progress",
        identifier = "in progress",
        value = "Message is in progress",
        semanticId = "irdi:0173-1#07-ABZ567#002"
      },
      AasEnumLiteral {
        name = "Message is completed",
        identifier = "completed",
        value = "Message is completed",
        semanticId = "irdi:0173-1#07-ABZ565#002"
      },
      AasEnumLiteral {
        name = "Message was cancelled rejected or aborted",
        identifier = "stopped",
        value = "Message was cancelled, rejected or aborted",
        semanticId = "irdi:0173-1#07-ABZ568#002"
      }
    }
  };

  AasSubmodelType ServiceRequestNotification = {
    name = "ServiceRequestNotification",
    semanticId = "irdi:0173-1#01-AHX443#001",
    description = "This Submodel template aims to standardize the description of a Service Request Notification that can be used to create a Service Request Notification for industrial assets or the asset creates it by itself.",
    versionIdentifier = "IDTA 02010-1-0",
    fields = {
      AasField {
        name = "NumberOfServiceRequestNotifications",
        semanticId = "irdi:0173-1#02-ABI761#001",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2"},
        description = "Number Service Request Notification."
      },
      AasField {
        name = "ServiceRequestNotification",
        semanticId = "irdi:0173-1#01-AHX444#001",
        counting = true,
        type = refBy(ServiceRequestNotification_2),
        minimumInstances = 1,
        description = "Collection of information with which notifications are recorded and managed in the service and customer service area definition."
      }
    }
  };

  AasSubmodelElementCollectionType ServiceRequestNotification_2 = {
    name = "ServiceRequestNotification_2",
    semanticId = "irdi:0173-1#01-AHX444#001",
    description = "Collection of information with which notifications are recorded and managed in the service and customer service area.",
    versionIdentifier = "IDTA 02010-1-0",
    fields = {
      AasField {
        name = "ReportedBy",
        semanticId = "irdi:0173-1#01-AHX448#001",
        type = refBy(ReportedBy),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Collection of one or more natural persons or assets that creates or publicizes the message definition."
      },
      AasField {
        name = "ServiceRequestNotificationId",
        semanticId = "irdi:0173-1#02-ABI772#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"200013454"},
        description = "Number assigned by an entity to the notification in order to uniquely identify and reference it in the further process flow definition."
      },
      AasField {
        name = "Priority",
        semanticId = "irdi:0173-1#02-ABI773#001",
        type = refBy(Priority),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"High"},
        description = "Rating of the primacy of the message, which results from the urgency or importance definition."
      },
      AasField {
        name = "Status",
        semanticId = "irdi:0173-1#02-ABH938#002",
        type = refBy(Status),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"sent"},
        description = "Current processing status of the notification within the workflow definition."
      },
      AasField {
        name = "RelatedAsset",
        semanticId = "irdi:0173-1#02-ABI774#001",
        type = refBy(AasGenericEntityType),
        minimumInstances = 1,
        examples = {"https://example.com/ids/ass et/2143"},
        description = "The object affected by the notification activity definition."
      },
      AasField {
        name = "ShortText",
        semanticId = "irdi:0173-1#02-ABI762#001",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Operating noise @en Laufgeräusch @de"},
        description = "Brief textual description of the subject matter definition."
      },
      AasField {
        name = "ServiceType",
        semanticId = "irdi:0173-1#02-ABI763#001",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Inspection"},
        description = "Predefined classification of a requested service definition."
      },
      AasField {
        name = "DetailedInformation",
        semanticId = "irdi:0173-1#01-AHX445#001",
        type = refBy(DetailedInformation),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Collection of in-depth information describing the situation concerned in the service and customer support area definition."
      },
      AasField {
        name = "OnsiteContact",
        semanticId = "irdi:0173-1#01-AHX449#001",
        type = refBy(OnsiteContact),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Collection of one or more natural persons located in the same or close locality to the asset definition."
      }
    }
  };

  AasSubmodelElementCollectionType ReportedBy = {
    name = "ReportedBy",
    semanticId = "irdi:0173-1#01-AHX448#001",
    description = "Collection of one or more natural persons or assets that creates or publicizes the message.",
    versionIdentifier = "IDTA 02010-1-0",
    fields = {
      AasField {
        name = "CustomerNumber",
        semanticId = "irdi:0173-1#02-ABI769#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1000111"},
        description = "An alphanumeric key that identifies a customer definition."
      },
      AasField {
        name = "SenderSystem",
        semanticId = "irdi:0173-1#02-ABI770#001",
        type = refBy(AasGenericEntityType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"https://example.com/ids/asset /2143"},
        description = "System, that creates or makes known the notification definition."
      },
      AasField {
        name = "NumberOfContacts",
        semanticId = "irdi:0173-1#02-AAO203#004",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1"},
        description = "Number of contacts."
      },
      AasField {
        name = "ContactInformation",
        semanticId = "irdi:0173-1#01-ADR448#007",
        type = refBy(ContactInformation),
        minimumInstances = 1,
        description = "The SMC ContactInformation contains information on how to contact the manufacturer or an authorised service provider, e.g. when a maintenance service is required."
      }
    }
  };

  AasSubmodelElementCollectionType DetailedInformation = {
    name = "DetailedInformation",
    semanticId = "irdi:0173-1#01-AHX445#001",
    description = "Collection of in-depth information describing the situation concerned in the service and customer support area.",
    versionIdentifier = "IDTA 02010-1-0",
    fields = {
      AasField {
        name = "LongText",
        semanticId = "irdi:0173-1#02-ABI764#001",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Operating noise from the inner of the pump @en","Laufgeräusch aus dem inneren der Pumpe @de"},
        description = "Textual detailed description of the subject matter definition."
      },
      AasField {
        name = "StartOfFault",
        semanticId = "irdi:0173-1#02-ABI765#001",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Point in time at which the regarded situation happened or started definition."
      },
      AasField {
        name = "ErrorCode",
        semanticId = "irdi:0173-1#02-ABI766#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"E x 23c3"},
        description = "technical key of the manufacturer for malfunction description definition."
      },
      AasField {
        name = "AttachedMedia",
        semanticId = "irdi:0173-1#01-AHX446#001",
        type = refBy(AttachedMedia),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Collection of file attachments that complement the notification and present additional information for processing the notification definition."
      }
    }
  };

  AasSubmodelElementCollectionType OnsiteContact = {
    name = "OnsiteContact",
    semanticId = "irdi:0173-1#01-AHX449#001",
    description = "Collection of one or more natural persons located in the same or close locality to the asset.",
    versionIdentifier = "IDTA 02010-1-0",
    fields = {
      AasField {
        name = "PartnerNumber",
        semanticId = "irdi:0173-1#02-ABI771#001",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1000111"},
        description = "An alphanumeric key that identifies a business partner definition."
      },
      AasField {
        name = "NumberOfContacts",
        semanticId = "irdi:0173-1#02-AAO203#004",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1"},
        description = "Number of contacts."
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

  AasSubmodelElementCollectionType AttachedMedia = {
    name = "AttachedMedia",
    semanticId = "irdi:0173-1#01-AHX446#001",
    description = "Collection of file attachments that complement the Service Request Notification and present additional information for processing the Service Request Notification.",
    versionIdentifier = "IDTA 02010-1-0",
    fields = {
      AasField {
        name = "NumberOfMedia",
        semanticId = "irdi:0173-1#02-ABI767#001",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2"},
        description = "Number of Media."
      },
      AasField {
        name = "Media",
        counting = true,
        type = refBy(Media),
        minimumInstances = 1,
        description = "File attached to the notification definition."
      }
    }
  };

  AasSubmodelElementCollectionType Media = {
    name = "Media",
    semanticId = "irdi:0173-1#01-AHX447#001",
    description = "File attached to the Service Request Notification.",
    versionIdentifier = "IDTA 02010-1-0",
    fields = {
      AasField {
        name = "DigitalFile",
        semanticId = "irdi:0173-1#02-ABK126#001",
        type = refBy(AasFileResourceType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Photo.jpeg"},
        description = "computer resource for recording data in a computer storage device, primarily identified by its file name definition."
      },
      AasField {
        name = "Comment",
        semanticId = "irdi:0173-1#02-ABI768#001",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Picture of the pump nameplate"},
        description = "Explanations that refer to the attached file and relate to its use or origin definition."
      }
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
