project IDTA_02002_ContactInformations {

  import AASDataTypes;

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasEnumType RoleOfContactPerson = {
    name = "RoleOfContactPerson",
    description = "function of a contact person in a process.",
    isOpen = true,
    semanticId = "irdi:0173-1#02-AAO204#003",
    literals = {
      AasEnumLiteral {
        name = "administrativ contact",
        description = "administrativ contact.",
        semanticId = "irdi:0173-1#07-AAS927#001"
      },
      AasEnumLiteral {
        name = "commercial contact",
        description = "commercial contact.",
        semanticId = "irdi:0173-1#07-AAS928#001"
      },
      AasEnumLiteral {
        name = "other contact",
        description = "other contact.",
        semanticId = "irdi:0173-1#07-AAS929#001"
      },
      AasEnumLiteral {
        name = "hazardous goods contact",
        description = "hazardous goods contact.",
        semanticId = "irdi:0173-1#07-AAS930#001"
      },
      AasEnumLiteral {
        name = "technical contact",
        description = "technical contact.",
        semanticId = "irdi:0173-1#07-AAS931#001"
      }
    }
  };

  AasEnumType TypeOfTelephone = {
    name = "TypeOfTelephone",
    description = "characterization of a telephone according to its location or usage.",
    semanticId = "irdi:0173-1#02-AAO137#003",
    literals = {
      AasEnumLiteral {
        name = "office",
        description = "office.",
        semanticId = "irdi:0173-1#07-AAS754#001"
      },
      AasEnumLiteral {
        name = "office mobile",
        description = "office mobile.",
        semanticId = "irdi:0173-1#07-AAS755#001"
      },
      AasEnumLiteral {
        name = "secretary",
        description = "secretary.",
        semanticId = "irdi:0173-1#07-AAS756#001"
      },
      AasEnumLiteral {
        name = "substitute",
        description = "substitute.",
        semanticId = "irdi:0173-1#07-AAS757#001"
      },
      AasEnumLiteral {
        name = "home",
        description = "home.",
        semanticId = "irdi:0173-1#07-AAS758#001"
      },
      AasEnumLiteral {
        name = "private mobile",
        description = "private mobile.",
        semanticId = "irdi:0173-1#07-AAS759#001"
      }
    }
  };

  AasEnumType TypeOfFaxNumber = {
    name = "TypeOfFaxNumber",
    description = "characterization of the fax according its location or usage.",
    semanticId = "irdi:0173-1#02-AAO196#003",
    literals = {
      AasEnumLiteral {
        name = "office",
        description = "office.",
        semanticId = "irdi:0173-1#07-AAS754#001"
      },
      AasEnumLiteral {
        name = "secretary",
        description = "secretary.",
        semanticId = "irdi:0173-1#07-AAS756#001"
      },
      AasEnumLiteral {
        name = "home",
        description = "home.",
        semanticId = "irdi:0173-1#07-AAS758#001"
      }
    }
  };

  AasEnumType TypeOfEmailAddress = {
    name = "TypeOfEmailAddress",
    description = "characterization of an e-mail address according to its location or usage.",
    semanticId = "irdi:0173-1#02-AAO199#003",
    literals = {
      AasEnumLiteral {
        name = "office",
        description = "office.",
        semanticId = "irdi:0173-1#07-AAS754#001"
      },
      AasEnumLiteral {
        name = "secretary",
        description = "secretary.",
        semanticId = "irdi:0173-1#07-AAS756#001"
      },
      AasEnumLiteral {
        name = "substitute",
        description = "substitute.",
        semanticId = "irdi:0173-1#07-AAS757#001"
      },
      AasEnumLiteral {
        name = "home",
        description = "home.",
        semanticId = "irdi:0173-1#07-AAS758#001"
      }
    }
  };

  AasSubmodelType ContactInformations = {
    name = "ContactInformations",
    semanticId = "iri:https://admin-shell.io/zvei/nameplate/1/0/ContactInformations",
    description = "The Submodel ContactInformations is the collection for various contact information.",
    fields = {
      AasField {
        name = "ContactInformation",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/ContactInformation",
        type = refBy(ContactInformation),
        minimumInstances = 1,
        description = "The SMC “ContactInformation” contains information on how to contact the manufacturer or an authorised service provider, e.g. when a maintenance service is required."
      }
    }
  };

  AasSubmodelElementCollectionType ContactInformation = {
    name = "ContactInformation",
    semanticId = "iri:https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/ContactInformation",
    description = "The SMC “ContactInformation” contains information on how to contact the manufacturer or an authorised service provider, e.g. when a maintenance service is required.",
    allowDuplicates = true,
    fields = {
      AasField {
        name = "RoleOfContactPerson",
        semanticId = "irdi:0173-1#02-AAO204#003",
        type = refBy(RoleOfContactPerson),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0173-1#07-AAS931#001"},
        description = "function of a contact person in a process."
      },
      AasField {
        name = "NationalCode",
        semanticId = "irdi:0173-1#02-AAO134#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"DE@en"},
        description = "code of a country Note: country codes defined accord. to ISO 3166-1. Recommendation: property declaration as MLP is required by its semantic definition. As the property value is language independent, users are recommended to provide maximal 1 string in any language of the users choice."
      },
      AasField {
        name = "Language",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/ContactInformation/Language",
        type = refBy(StringListType),
        minimumInstances = 0,
        examples = {"de"},
        description = "Available language Note: language codes defined accord. to ISO 639-1. Note: as per ECLASS definition, Expression and representation of thoughts, information, feelings, ideas through characters."
      },
      AasField {
        name = "TimeZone",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/ContactInformation/TimeZone",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Z"},
        description = "offsets from Coordinated Universal Time (UTC) Note: notation accord. to ISO 8601 Note: for time in UTC the zone designator Z is to be used."
      },
      AasField {
        name = "CityTown",
        semanticId = "irdi:0173-1#02-AAO132#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Musterstadt@de"},
        description = "town or city."
      },
      AasField {
        name = "Company",
        semanticId = "irdi:0173-1#02-AAW001#001",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"ABC Company@en"},
        description = "name of the company."
      },
      AasField {
        name = "Department",
        semanticId = "irdi:0173-1#02-AAO127#003",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Vertrieb@de"},
        description = "administrative section within an organisation where a business partner is located."
      },
      AasField {
        name = "Phone",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/ContactInformation/Phone",
        type = refBy(Phone),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Phone number including type."
      },
      AasField {
        name = "Fax",
        semanticId = "irdi:0173-1#02-AAQ834#005",
        type = refBy(Fax),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Fax number including type."
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
        name = "IPCommunication",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/",
        counting = true,
        type = refBy(IPCommunication),
        minimumInstances = 0,
        description = "ContactInformation/IPCommunication IP-based communication channels, e.g. chat or video call."
      },
      AasField {
        name = "Street",
        semanticId = "irdi:0173-1#02-AAO128#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Musterstraße 1@de"},
        description = "street name and house number."
      },
      AasField {
        name = "Zipcode",
        semanticId = "irdi:0173-1#02-AAO129#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"12345@de"},
        description = "ZIP code of address Recommendation: property declaration as MLP is required by its semantic definition. As the property value is language independent, users are recommended to provide maximal 1 string in any language of the users choice."
      },
      AasField {
        name = "POBox",
        semanticId = "irdi:0173-1#02-AAO130#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"PF 1234@en"},
        description = "P.O. box number."
      },
      AasField {
        name = "ZipCodeOfPOBox",
        semanticId = "irdi:0173-1#02-AAO131#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"12345@en"},
        description = "ZIP code of P.O. box address Recommendation: property declaration as MLP is required by its semantic definition. As the property value is language independent, users are recommended to provide maximal 1 string in any language of the users choice."
      },
      AasField {
        name = "StateCounty",
        semanticId = "irdi:0173-1#02-AAO133#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Muster-Bundesland@de"},
        description = "federal state a part of a state."
      },
      AasField {
        name = "NameOfContact",
        semanticId = "irdi:0173-1#02-AAO205#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "surname of a contact person."
      },
      AasField {
        name = "FirstName",
        semanticId = "irdi:0173-1#02-AAO206#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "first name of a contact person."
      },
      AasField {
        name = "MiddleNames",
        semanticId = "irdi:0173-1#02-AAO207#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "middle names of contact person."
      },
      AasField {
        name = "Title",
        semanticId = "irdi:0173-1#02-AAO208#003",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "common, formal, religious, or other title preceding a contact person's name."
      },
      AasField {
        name = "AcademicTitle",
        semanticId = "irdi:0173-1#02-AAO209#003",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "academic title preceding a contact person's name."
      },
      AasField {
        name = "FurtherDetailsOfContact",
        semanticId = "irdi:0173-1#02-AAO210#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "additional information of the contact person."
      },
      AasField {
        name = "AddressOfAdditionalLink",
        semanticId = "irdi:0173-1#02-AAQ326#002",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "web site address where information about the product or contact is given."
      }
    }
  };

  AasSubmodelElementCollectionType Phone = {
    name = "Phone",
    semanticId = "iri:https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/ContactInformation/Phone",
    description = "Phone number including type.",
    fields = {
      AasField {
        name = "TelephoneNumber",
        semanticId = "irdi:0173-1#02-AAO136#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"+491234567890@en"},
        description = "complete telephone number to be called to reach a business partner Recommendation: property declaration as MLP is required by its semantic definition. As the property value is language independent, users are recommended to provide maximal 1 string in any language of the users choice."
      },
      AasField {
        name = "TypeOfTelephone",
        semanticId = "irdi:0173-1#02-AAO137#003",
        type = refBy(TypeOfTelephone),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0173-1#07-AAS754#001"},
        description = "characterization of a telephone according to its location or usage."
      },
      AasField {
        name = "AvailableTime",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/ContactInformation/AvailableTime/",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Montag – Freitag 08:00 bis 16:00@de"},
        description = "Specification of the available time window."
      }
    }
  };

  AasSubmodelElementCollectionType Fax = {
    name = "Fax",
    semanticId = "irdi:0173-1#02-AAQ834#005",
    description = "Fax number including type.",
    fields = {
      AasField {
        name = "FaxNumber",
        semanticId = "irdi:0173-1#02-AAO195#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"+491234567890@en"},
        description = "complete telephone number to be called to reach a business partner's fax machine Recommendation: property declaration as MLP is required by its semantic definition. As the property value is language independent, users are recommended to provide maximal 1 string in any language of the users choice."
      },
      AasField {
        name = "TypeOfFaxNumber",
        semanticId = "irdi:0173-1#02-AAO196#003",
        type = refBy(TypeOfFaxNumber),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0173-1#07-AAS754#001"},
        description = "characterization of the fax according its location or usage."
      }
    }
  };

  AasSubmodelElementCollectionType Email = {
    name = "Email",
    semanticId = "irdi:0173-1#02-AAQ836#005",
    description = "E-mail address and encryption method.",
    fields = {
      AasField {
        name = "EmailAddress",
        semanticId = "irdi:0173-1#02-AAO198#002",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"email@muster-ag.de"},
        description = "electronic mail address of a business partner."
      },
      AasField {
        name = "PublicKey",
        semanticId = "irdi:0173-1#02-AAO200#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "public part of an unsymmetrical key pair to sign or encrypt text or messages Recommendation: property declaration as MLP is required by its semantic definition. As the property value is language independent, users are recommended to provide maximal 1 string in any language of the users choice."
      },
      AasField {
        name = "TypeOfEmailAddress",
        semanticId = "irdi:0173-1#02-AAO199#003",
        type = refBy(TypeOfEmailAddress),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"0173-1#07-AAS754#001"},
        description = "characterization of an e-mail address according to its location or usage."
      },
      AasField {
        name = "TypeOfPublicKey",
        semanticId = "irdi:0173-1#02-AAO201#002",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "characterization of a public key according to its encryption process."
      }
    }
  };

  AasSubmodelElementCollectionType IPCommunication = {
    name = "IPCommunication",
    semanticId = "iri:https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/",
    description = "ContactInformation/IPCommunication IP-based communication channels, e.g. chat or video call.",
    fields = {
      AasField {
        name = "AddressOfAdditionalLink",
        semanticId = "irdi:0173-1#02-AAQ326#002",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "web site address where information about the product or contact is given."
      },
      AasField {
        name = "TypeOfCommunication",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/1/0/ ContactInformations/ContactInformation/IPCommunication/TypeOfCommunication",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Chat"},
        description = "characterization of an IP-based communication channel."
      },
      AasField {
        name = "AvailableTime",
        semanticId = "iri:https://admin-shell.io/zvei/nameplate/1/0/ContactInformations/ContactInformation/AvailableTime/",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Montag – Freitag 08:00 bis 16:00@de"},
        description = "Specification of the available time window."
      }
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
