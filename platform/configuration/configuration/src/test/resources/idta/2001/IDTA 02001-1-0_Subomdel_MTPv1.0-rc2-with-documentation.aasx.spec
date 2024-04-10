project IDTA_02001_ModuleTypePackage {

  version v1.0;

  import AASDataTypes;

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasSubmodelType ModuleTypePackage = {
    name = "ModuleTypePackage",
    semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/MTPSubmodel",
    fields = {
      AasField {
        name = "MTPFile",
        semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/MTPSUCLib/ModuleTypePackage",
        type = refBy(AasFileResourceType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"/aasx/Example.aml"},
        description = "ModuleTypePackage file included as a zipped package with ending “.mtp”."
      },
      AasField {
        name = "DocumentationReferences",
        semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/MTPReferences",
        type = refBy(DocumentationReferences),
        minimumInstances = 0,
        description = "Collection containing references to documentation documents which are associated with TagNames within the MTP file."
      },
      AasField {
        name = "BOMReferences",
        semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/MTPReferences",
        type = refBy(BOMReferences),
        minimumInstances = 0,
        description = "Collection containing references to documentation documents which are associated with TagNames within the MTP file."
      }
    }
  };

  AasSubmodelElementCollectionType DocumentationReferences = {
    name = "DocumentationReferences",
    semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/MTPReferences",
    description = "Collection containing references to documentation documents which are associated with TagNames within the MTP file.",
    fields = {
      AasField {
        name = "M0013_Datasheet",
        semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/MTPReference",
        type = refBy(AasRelationType),
        minimumInstances = 1,
        description = "Reference between (first) an element within the MTP file and (second) an element within another submodel (e.g. a documentation element within a documentation submodel)."
      }
    }
  };

  AasSubmodelElementCollectionType BOMReferences = {
    name = "BOMReferences",
    semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/MTPReferences",
    description = "Collection containing references to documentation documents which are associated with TagNames within the MTP file.",
    fields = {
    }
  };

  AasSubmodelType AssetIdentification = {
    name = "AssetIdentification",
    semanticId = "iri:www.vendor.com/ids/sm/5324_9041_1002_7612",
    fields = {
      AasField {
        name = "Manufacturer",
        semanticId = "irdi:0173-1#02-AAO677#002",
        type = refBy(StringType),
        examples = {"vendor.com"}
      }
    }
  };

  AasSubmodelType ProcessEquipmentAssembly = {
    name = "ProcessEquipmentAssembly",
    semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/PEASubmodel",
    fields = {
      AasField {
        name = "MTPFile",
        semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/MTPSUCLib/ModuleTypePackage",
        type = refBy(AasFileResourceType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"/aasx/Example.aml"},
        description = "ModuleTypePackage file included as a zipped package with ending “.mtp”."
      },
      AasField {
        name = "SourceList",
        semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/MTPSUCLib/CommunicationSet/SourceList",
        type = refBy(SourceList),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "DisplayName",
        semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/PEASubmodel/DisplayName",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Operator-specific module name@en"}
      },
      AasField {
        name = "Description",
        semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/PEASubmodel/Description",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Operator-specific description@en"},
        description = "Operator-specific module description."
      },
      AasField {
        name = "DocumentationReferences",
        semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/MTPReferences",
        type = refBy(DocumentationReferences),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Collection containing references to documentation documents which are associated with TagNames within the MTP file."
      }
    }
  };

  AasSubmodelElementCollectionType SourceList = {
    name = "SourceList",
    semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/MTPSUCLib/CommunicationSet/SourceList",
    fields = {
      AasField {
        name = "FreelanceOPCUA",
        semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/MTPCommunicationSUCLib/ServerAssembly/OPCUAServer",
        type = refBy(FreelanceOPCUA),
        minimumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType FreelanceOPCUA = {
    name = "FreelanceOPCUA",
    semanticId = "iri:https://admin-shell.io/vdi/2658/1/0/MTPCommunicationSUCLib/ServerAssembly/OPCUAServer",
    fields = {
      AasField {
        name = "DiscoveryUrl01",
        semanticId = "iri:https://admin-shell.io/idta/opcua-server-datasheet/1/0/discovery-url",
        type = refBy(StringListType),
        minimumInstances = 1,
        examples = {"opc.tcp://localhost:4800/BP11"}
      },
      AasField {
        name = "ApplicationUri01",
        semanticId = "iri:https://admin-shell.io/idta/opcua-server-datasheet/1/0/application-uri",
        type = refBy(StringListType),
        minimumInstances = 0,
        examples = {"urn:org.com:PEA1:UA Server"}
      }
    }
  };

  AasSubmodelType Documentation = {
    name = "Documentation",
    semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/Documentation",
    fields = {
      AasField {
        name = "Document01",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/Document",
        type = refBy(Document01),
        description = "Feste und geordnete Menge von für die Verwendung durch Personen bestimmte Informationen, die verwaltet und als Einheit zwischen Benutzern und System ausgetauscht werden kann."
      },
      AasField {
        name = "Document02",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/Document",
        type = refBy(Document02),
        description = "Feste und geordnete Menge von für die Verwendung durch Personen bestimmte Informationen, die verwaltet und als Einheit zwischen Benutzern und System ausgetauscht werden kann."
      },
      AasField {
        name = "Document03",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/Document",
        type = refBy(Document03),
        description = "Feste und geordnete Menge von für die Verwendung durch Personen bestimmte Informationen, die verwaltet und als Einheit zwischen Benutzern und System ausgetauscht werden kann."
      },
      AasField {
        name = "Document04",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/Document",
        type = refBy(Document04),
        description = "Feste und geordnete Menge von für die Verwendung durch Personen bestimmte Informationen, die verwaltet und als Einheit zwischen Benutzern und System ausgetauscht werden kann."
      },
      AasField {
        name = "Document05",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/Document",
        type = refBy(Document05),
        description = "Feste und geordnete Menge von für die Verwendung durch Personen bestimmte Informationen, die verwaltet und als Einheit zwischen Benutzern und System ausgetauscht werden kann."
      },
      AasField {
        name = "Document06",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/Document",
        type = refBy(Document06),
        description = "Feste und geordnete Menge von für die Verwendung durch Personen bestimmte Informationen, die verwaltet und als Einheit zwischen Benutzern und System ausgetauscht werden kann."
      },
      AasField {
        name = "Document07",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/Document",
        type = refBy(Document07),
        description = "Feste und geordnete Menge von für die Verwendung durch Personen bestimmte Informationen, die verwaltet und als Einheit zwischen Benutzern und System ausgetauscht werden kann."
      },
      AasField {
        name = "Document08",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/Document",
        type = refBy(Document08),
        description = "Feste und geordnete Menge von für die Verwendung durch Personen bestimmte Informationen, die verwaltet und als Einheit zwischen Benutzern und System ausgetauscht werden kann."
      },
      AasField {
        name = "Document09",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/Document",
        type = refBy(Document09),
        description = "Feste und geordnete Menge von für die Verwendung durch Personen bestimmte Informationen, die verwaltet und als Einheit zwischen Benutzern und System ausgetauscht werden kann."
      }
    }
  };

  AasSubmodelElementCollectionType Document01 = {
    name = "Document01",
    semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/Document",
    description = "Feste und geordnete Menge von für die Verwendung durch Personen bestimmte Informationen, die verwaltet und als Einheit zwischen Benutzern und System ausgetauscht werden kann.",
    fields = {
      AasField {
        name = "DocumentId",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentId/Id",
        type = refBy(StringType),
        examples = {"64879470"},
        description = "die eigentliche Identifikationsnummer."
      },
      AasField {
        name = "IsPrimaryDocumentId",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentId/isPrimary",
        type = refBy(BooleanType),
        examples = {"true"},
        description = "is the primary document id of the document."
      },
      AasField {
        name = "DocumentClassId",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentClassification/ClassId",
        type = refBy(StringType),
        examples = {"02-02"},
        description = "Eindeutige ID der Klasse in einer Klassifikation."
      },
      AasField {
        name = "DocumentClassName",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentClassification/ClassName",
        type = refBy(StringType),
        examples = {"Drawings, plans"},
        description = "Liste von sprachabhängigen Namen zur ClassId."
      },
      AasField {
        name = "DocumentClassificationSystem",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentClassification/ClassificationSystem",
        type = refBy(StringType),
        examples = {"VDI2770:2018"},
        description = "Eindeutige Kennung für ein Klassifikationssystem. Für Klassifikationen nach VDI 2770 muss 'VDI2770:2018' verwenden werden."
      },
      AasField {
        name = "DocumentVersion01",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentVersion",
        type = refBy(DocumentVersion01),
        description = "Zu jedem Dokument muss eine Menge von mindestens einer Dokumentenversion existieren. Es können auch mehrere Dokumentenversionen ausgeliefert werden."
      }
    }
  };

  AasSubmodelElementCollectionType DocumentVersion01 = {
    name = "DocumentVersion01",
    semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentVersion",
    description = "Zu jedem Dokument muss eine Menge von mindestens einer Dokumentenversion existieren. Es können auch mehrere Dokumentenversionen ausgeliefert werden.",
    fields = {
      AasField {
        name = "Language01",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentVersion/Language",
        type = refBy(StringType),
        description = "Eine Liste der im Dokument verwendeten Sprachen."
      },
      AasField {
        name = "DocumentVersionId",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentVersion/DocumentVersionId",
        type = refBy(StringType),
        examples = {"V1.2"},
        description = "Verschiedene Versionen eines Dokuments müssen eindeutig identifizierbar sein. Die DocumentVersionId stellt eine innerhalb einer Domäne eindeutige Versionsidentifikationsnummer dar."
      },
      AasField {
        name = "Title",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/Description/Title",
        type = refBy(AasMultiLangStringType),
        examples = {"Deutsche Übersetzung von: Overview picture@DE"},
        description = "Sprachabhängiger Titel des Dokuments."
      },
      AasField {
        name = "Summary",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentDescription/Summary",
        type = refBy(AasMultiLangStringType),
        examples = {"Zusammenfassung von: Overview picture@DE"},
        description = "Sprachabhängige, aussagekräftige Zusammenfassung des Dokumenteninhalts."
      },
      AasField {
        name = "KeyWords",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentDescription/KeyWords",
        type = refBy(AasMultiLangStringType),
        examples = {"Stichwörter für: Overview picture@DE"}
      },
      AasField {
        name = "SetDate",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/LifeCycleStatus/SetDate",
        type = refBy(DateTimeType),
        examples = {"2020-02-06"},
        description = "Datum und Uhrzeit, an dem der Status festgelegt wurde. Es muss das Datumsformat „YYYY-MM-dd“ verwendet werden (Y = Jahr, M = Monat, d = Tag, siehe ISO 8601)."
      },
      AasField {
        name = "StatusValue",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/LifeCycleStatus/StatusValue",
        type = refBy(StringType),
        examples = {"Released"},
        description = "Jede Dokumentenversion stellt einen Zeitpunkt im Dokumentenlebenszyklus dar. Dieser Statuswert bezieht sich auf die Meilensteine im Dokumentenlebenszyklus. Für die Anwendung dieser Richtlinie sind die beiden folgenden Status zu verwenden: InReview (in Prüfung), Released (freigegeben)."
      },
      AasField {
        name = "Role",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/Party/Role",
        type = refBy(StringType),
        examples = {"Author"},
        description = "Festlegung einer Rolle für die Organisation gemäß der folgenden Auswahlliste. Author (Autor), Customer (Kunde), Supplier (Zulieferer, Anbieter), Manufacturer (Hersteller), Responsible (Verantwortlicher)."
      },
      AasField {
        name = "OrganizationName",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/Organization/OrganizationName",
        type = refBy(StringType),
        examples = {"Example company"},
        description = "Die gebräuchliche Bezeichnung für die Organisation."
      },
      AasField {
        name = "OrganizationOfficialName",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/Organization/OrganizationOfficialName",
        type = refBy(StringType),
        examples = {"Example company Ltd."},
        description = "Der offizielle Namen der Organisation."
      },
      AasField {
        name = "DigitalFile",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/StoredDocumentRepresentation/DigitalFile",
        type = refBy(AasFileResourceType),
        examples = {"/aasx/documentation/praxis-projekte.jpg"},
        description = "Eine Datei, die die DocumentVersion repräsentiert. Neben der obligatorischen PDF/A Datei können weitere Dateien angegeben werden."
      }
    }
  };

  AasSubmodelElementCollectionType Document02 = {
    name = "Document02",
    semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/Document",
    description = "Feste und geordnete Menge von für die Verwendung durch Personen bestimmte Informationen, die verwaltet und als Einheit zwischen Benutzern und System ausgetauscht werden kann.",
    fields = {
      AasField {
        name = "DocumentId",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentId/Id",
        type = refBy(StringType),
        examples = {"47044325"},
        description = "die eigentliche Identifikationsnummer."
      },
      AasField {
        name = "IsPrimaryDocumentId",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentId/isPrimary",
        type = refBy(BooleanType),
        examples = {"true"},
        description = "is the primary document id of the document."
      },
      AasField {
        name = "DocumentClassId",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentClassification/ClassId",
        type = refBy(StringType),
        examples = {"02-01"},
        description = "Eindeutige ID der Klasse in einer Klassifikation."
      },
      AasField {
        name = "DocumentClassName",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentClassification/ClassName",
        type = refBy(StringType),
        examples = {"Technical specification"},
        description = "Liste von sprachabhängigen Namen zur ClassId."
      },
      AasField {
        name = "DocumentClassificationSystem",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentClassification/ClassificationSystem",
        type = refBy(StringType),
        examples = {"VDI2770:2018"},
        description = "Eindeutige Kennung für ein Klassifikationssystem. Für Klassifikationen nach VDI 2770 muss 'VDI2770:2018' verwenden werden."
      },
      AasField {
        name = "DocumentVersion01",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentVersion",
        type = refBy(DocumentVersion01),
        description = "Zu jedem Dokument muss eine Menge von mindestens einer Dokumentenversion existieren. Es können auch mehrere Dokumentenversionen ausgeliefert werden."
      }
    }
  };

  AasSubmodelElementCollectionType Document03 = {
    name = "Document03",
    semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/Document",
    description = "Feste und geordnete Menge von für die Verwendung durch Personen bestimmte Informationen, die verwaltet und als Einheit zwischen Benutzern und System ausgetauscht werden kann.",
    fields = {
      AasField {
        name = "DocumentId",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentId/Id",
        type = refBy(StringType),
        examples = {"20745743"},
        description = "die eigentliche Identifikationsnummer."
      },
      AasField {
        name = "IsPrimaryDocumentId",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentId/isPrimary",
        type = refBy(BooleanType),
        examples = {"true"},
        description = "is the primary document id of the document."
      },
      AasField {
        name = "DocumentClassId",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentClassification/ClassId",
        type = refBy(StringType),
        examples = {"02-01"},
        description = "Eindeutige ID der Klasse in einer Klassifikation."
      },
      AasField {
        name = "DocumentClassName",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentClassification/ClassName",
        type = refBy(StringType),
        examples = {"Technical specification"},
        description = "Liste von sprachabhängigen Namen zur ClassId."
      },
      AasField {
        name = "DocumentClassificationSystem",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentClassification/ClassificationSystem",
        type = refBy(StringType),
        examples = {"VDI2770:2018"},
        description = "Eindeutige Kennung für ein Klassifikationssystem. Für Klassifikationen nach VDI 2770 muss 'VDI2770:2018' verwenden werden."
      },
      AasField {
        name = "DocumentVersion01",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentVersion",
        type = refBy(DocumentVersion01),
        description = "Zu jedem Dokument muss eine Menge von mindestens einer Dokumentenversion existieren. Es können auch mehrere Dokumentenversionen ausgeliefert werden."
      }
    }
  };

  AasSubmodelElementCollectionType Document04 = {
    name = "Document04",
    semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/Document",
    description = "Feste und geordnete Menge von für die Verwendung durch Personen bestimmte Informationen, die verwaltet und als Einheit zwischen Benutzern und System ausgetauscht werden kann.",
    fields = {
      AasField {
        name = "DocumentId",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentId/Id",
        type = refBy(StringType),
        examples = {"52493967"},
        description = "die eigentliche Identifikationsnummer."
      },
      AasField {
        name = "IsPrimaryDocumentId",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentId/isPrimary",
        type = refBy(BooleanType),
        examples = {"true"},
        description = "is the primary document id of the document."
      },
      AasField {
        name = "DocumentClassId",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentClassification/ClassId",
        type = refBy(StringType),
        examples = {"02-01"},
        description = "Eindeutige ID der Klasse in einer Klassifikation."
      },
      AasField {
        name = "DocumentClassName",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentClassification/ClassName",
        type = refBy(StringType),
        examples = {"Technical specification"},
        description = "Liste von sprachabhängigen Namen zur ClassId."
      },
      AasField {
        name = "DocumentClassificationSystem",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentClassification/ClassificationSystem",
        type = refBy(StringType),
        examples = {"VDI2770:2018"},
        description = "Eindeutige Kennung für ein Klassifikationssystem. Für Klassifikationen nach VDI 2770 muss 'VDI2770:2018' verwenden werden."
      },
      AasField {
        name = "DocumentVersion01",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentVersion",
        type = refBy(DocumentVersion01),
        description = "Zu jedem Dokument muss eine Menge von mindestens einer Dokumentenversion existieren. Es können auch mehrere Dokumentenversionen ausgeliefert werden."
      }
    }
  };

  AasSubmodelElementCollectionType Document05 = {
    name = "Document05",
    semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/Document",
    description = "Feste und geordnete Menge von für die Verwendung durch Personen bestimmte Informationen, die verwaltet und als Einheit zwischen Benutzern und System ausgetauscht werden kann.",
    fields = {
      AasField {
        name = "DocumentId",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentId/Id",
        type = refBy(StringType),
        examples = {"2683661"},
        description = "die eigentliche Identifikationsnummer."
      },
      AasField {
        name = "IsPrimaryDocumentId",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentId/isPrimary",
        type = refBy(BooleanType),
        examples = {"true"},
        description = "is the primary document id of the document."
      },
      AasField {
        name = "DocumentClassId",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentClassification/ClassId",
        type = refBy(StringType),
        examples = {"03-04"},
        description = "Eindeutige ID der Klasse in einer Klassifikation."
      },
      AasField {
        name = "DocumentClassName",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentClassification/ClassName",
        type = refBy(StringType),
        examples = {"Maintenance, Inspection"},
        description = "Liste von sprachabhängigen Namen zur ClassId."
      },
      AasField {
        name = "DocumentClassificationSystem",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentClassification/ClassificationSystem",
        type = refBy(StringType),
        examples = {"VDI2770:2018"},
        description = "Eindeutige Kennung für ein Klassifikationssystem. Für Klassifikationen nach VDI 2770 muss 'VDI2770:2018' verwenden werden."
      },
      AasField {
        name = "DocumentVersion01",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentVersion",
        type = refBy(DocumentVersion01),
        description = "Zu jedem Dokument muss eine Menge von mindestens einer Dokumentenversion existieren. Es können auch mehrere Dokumentenversionen ausgeliefert werden."
      }
    }
  };

  AasSubmodelElementCollectionType Document06 = {
    name = "Document06",
    semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/Document",
    description = "Feste und geordnete Menge von für die Verwendung durch Personen bestimmte Informationen, die verwaltet und als Einheit zwischen Benutzern und System ausgetauscht werden kann.",
    fields = {
      AasField {
        name = "DocumentId",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentId/Id",
        type = refBy(StringType),
        examples = {"24152954"},
        description = "die eigentliche Identifikationsnummer."
      },
      AasField {
        name = "IsPrimaryDocumentId",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentId/isPrimary",
        type = refBy(BooleanType),
        examples = {"true"},
        description = "is the primary document id of the document."
      },
      AasField {
        name = "DocumentClassId",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentClassification/ClassId",
        type = refBy(StringType),
        examples = {"03-04"},
        description = "Eindeutige ID der Klasse in einer Klassifikation."
      },
      AasField {
        name = "DocumentClassName",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentClassification/ClassName",
        type = refBy(StringType),
        examples = {"Maintenance, Inspection"},
        description = "Liste von sprachabhängigen Namen zur ClassId."
      },
      AasField {
        name = "DocumentClassificationSystem",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentClassification/ClassificationSystem",
        type = refBy(StringType),
        examples = {"VDI2770:2018"},
        description = "Eindeutige Kennung für ein Klassifikationssystem. Für Klassifikationen nach VDI 2770 muss 'VDI2770:2018' verwenden werden."
      },
      AasField {
        name = "DocumentVersion01",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentVersion",
        type = refBy(DocumentVersion01),
        description = "Zu jedem Dokument muss eine Menge von mindestens einer Dokumentenversion existieren. Es können auch mehrere Dokumentenversionen ausgeliefert werden."
      }
    }
  };

  AasSubmodelElementCollectionType Document07 = {
    name = "Document07",
    semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/Document",
    description = "Feste und geordnete Menge von für die Verwendung durch Personen bestimmte Informationen, die verwaltet und als Einheit zwischen Benutzern und System ausgetauscht werden kann.",
    fields = {
      AasField {
        name = "DocumentId",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentId/Id",
        type = refBy(StringType),
        examples = {"16049999"},
        description = "die eigentliche Identifikationsnummer."
      },
      AasField {
        name = "IsPrimaryDocumentId",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentId/isPrimary",
        type = refBy(BooleanType),
        examples = {"true"},
        description = "is the primary document id of the document."
      },
      AasField {
        name = "DocumentClassId",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentClassification/ClassId",
        type = refBy(StringType),
        examples = {"03-02"},
        description = "Eindeutige ID der Klasse in einer Klassifikation."
      },
      AasField {
        name = "DocumentClassName",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentClassification/ClassName",
        type = refBy(StringType),
        examples = {"Operation"},
        description = "Liste von sprachabhängigen Namen zur ClassId."
      },
      AasField {
        name = "DocumentClassificationSystem",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentClassification/ClassificationSystem",
        type = refBy(StringType),
        examples = {"VDI2770:2018"},
        description = "Eindeutige Kennung für ein Klassifikationssystem. Für Klassifikationen nach VDI 2770 muss 'VDI2770:2018' verwenden werden."
      },
      AasField {
        name = "DocumentVersion01",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentVersion",
        type = refBy(DocumentVersion01),
        description = "Zu jedem Dokument muss eine Menge von mindestens einer Dokumentenversion existieren. Es können auch mehrere Dokumentenversionen ausgeliefert werden."
      }
    }
  };

  AasSubmodelElementCollectionType Document08 = {
    name = "Document08",
    semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/Document",
    description = "Feste und geordnete Menge von für die Verwendung durch Personen bestimmte Informationen, die verwaltet und als Einheit zwischen Benutzern und System ausgetauscht werden kann.",
    fields = {
      AasField {
        name = "DocumentId",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentId/Id",
        type = refBy(StringType),
        examples = {"10232270"},
        description = "die eigentliche Identifikationsnummer."
      },
      AasField {
        name = "IsPrimaryDocumentId",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentId/isPrimary",
        type = refBy(BooleanType),
        examples = {"true"},
        description = "is the primary document id of the document."
      },
      AasField {
        name = "DocumentClassId",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentClassification/ClassId",
        type = refBy(StringType),
        examples = {"03-04"},
        description = "Eindeutige ID der Klasse in einer Klassifikation."
      },
      AasField {
        name = "DocumentClassName",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentClassification/ClassName",
        type = refBy(StringType),
        examples = {"Maintenance, Inspection"},
        description = "Liste von sprachabhängigen Namen zur ClassId."
      },
      AasField {
        name = "DocumentClassificationSystem",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentClassification/ClassificationSystem",
        type = refBy(StringType),
        examples = {"VDI2770:2018"},
        description = "Eindeutige Kennung für ein Klassifikationssystem. Für Klassifikationen nach VDI 2770 muss 'VDI2770:2018' verwenden werden."
      },
      AasField {
        name = "DocumentVersion01",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentVersion",
        type = refBy(DocumentVersion01),
        description = "Zu jedem Dokument muss eine Menge von mindestens einer Dokumentenversion existieren. Es können auch mehrere Dokumentenversionen ausgeliefert werden."
      }
    }
  };

  AasSubmodelElementCollectionType Document09 = {
    name = "Document09",
    semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/Document",
    description = "Feste und geordnete Menge von für die Verwendung durch Personen bestimmte Informationen, die verwaltet und als Einheit zwischen Benutzern und System ausgetauscht werden kann.",
    fields = {
      AasField {
        name = "DocumentId",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentId/Id",
        type = refBy(StringType),
        examples = {"24981566"},
        description = "die eigentliche Identifikationsnummer."
      },
      AasField {
        name = "IsPrimaryDocumentId",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentId/isPrimary",
        type = refBy(BooleanType),
        examples = {"true"},
        description = "is the primary document id of the document."
      },
      AasField {
        name = "DocumentClassId",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentClassification/ClassId",
        type = refBy(StringType),
        examples = {"03-04"},
        description = "Eindeutige ID der Klasse in einer Klassifikation."
      },
      AasField {
        name = "DocumentClassName",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentClassification/ClassName",
        type = refBy(StringType),
        examples = {"Maintenance, Inspection"},
        description = "Liste von sprachabhängigen Namen zur ClassId."
      },
      AasField {
        name = "DocumentClassificationSystem",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentClassification/ClassificationSystem",
        type = refBy(StringType),
        examples = {"VDI2770:2018"},
        description = "Eindeutige Kennung für ein Klassifikationssystem. Für Klassifikationen nach VDI 2770 muss 'VDI2770:2018' verwenden werden."
      },
      AasField {
        name = "DocumentVersion01",
        semanticId = "iri:http://admin-shell.io/vdi/2770/1/0/DocumentVersion",
        type = refBy(DocumentVersion01),
        description = "Zu jedem Dokument muss eine Menge von mindestens einer Dokumentenversion existieren. Es können auch mehrere Dokumentenversionen ausgeliefert werden."
      }
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
