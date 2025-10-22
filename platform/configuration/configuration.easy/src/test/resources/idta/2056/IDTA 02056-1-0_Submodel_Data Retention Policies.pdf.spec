project IDTA_02056_DataRetentionPolicies {

  version v1.0;

  import AASDataTypes;

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasSubmodelType DataRetentionPolicies = {
    name = "DataRetentionPolicies",
    semanticId = "iri:https://admin-shell.io/idta/DataRetentionPolicies/1/0",
    description = "Submodel containing data retention policies for data elements in the Asset Administration Shell.",
    versionIdentifier = "IDTA 02056-1-0",
    fields = {
      AasField {
        name = "InheritedFrom",
        semanticId = "iri:https://admin-shell.io/idta/DataRetentionPolicies/InheritedFrom/1/0",
        type = refBy(AasReferenceType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Reference to a DataRetentionPolicies Submodel whose policies are extended or changed by the policies defined in this Submodel."
      },
      AasField {
        name = "Policies",
        semanticId = "iri:https://admin-shell.io/idta/DataRetentionPolicies/Policies/1/0",
        type = refBy(Policies),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"zeroToMany"},
        description = "Defines the set of policies."
      }
    }
  };

  AasSubmodelElementCollectionType Policies = {
    name = "Policies",
    semanticId = "iri:https://admin-shell.io/idta/DataRetentionPolicies/Policies/1/0",
    description = "Defines the set of policies.",
    versionIdentifier = "IDTA 02056-1-0",
    fields = {
      AasField {
        name = "Policy",
        semanticId = "iri:https://admin-shell.io/idta/DataRetentionPolicies/Policy/1/0",
        counting = true,
        type = refBy(Policy),
        minimumInstances = 0,
        description = "Describes a single data retention policy."
      }
    }
  };

  AasSubmodelElementCollectionType Policy = {
    name = "Policy",
    semanticId = "iri:https://admin-shell.io/idta/DataRetentionPolicies/Policy/1/0",
    description = "Describes a single data retention policy.",
    versionIdentifier = "IDTA 02056-1-0",
    fields = {
      AasField {
        name = "RetentionTime",
        semanticId = "iri:https://admin-shell.io/idta/DataRetentionPolicies/RetentionTime/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"3 Years"},
        description = "Specifies how long an element must be retained before it can be deleted."
      },
      AasField {
        name = "SemanticIds",
        semanticId = "iri:https://admin-shell.io/idta/DataRetentionPolicies/SemanticIds/1/0",
        type = refBy(AasGenericSubmodelElementCollection),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"oneToMany"},
        description = "References to semantic identifiers of elements that this policy shall apply to. Order Relevant: No List Element Type: [Ref] List Element Semantic ID: [IRI] https://admin- shell.io/idta/DataRetentionPolicies/SemanticId/1/0."
      },
      AasField {
        name = "Overrides",
        semanticId = "iri:https://admin-shell.io/idta/DataRetentionPolicies/Overrides/1/0",
        type = refBy(AasRelationType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Establishes a relationship with a policy that is being overridden by this policy."
      },
      AasField {
        name = "Immutable",
        semanticId = "iri:https://admin-shell.io/idta/DataRetentionPolicies/Immutable/1/0",
        type = refBy(BooleanType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"True"},
        description = "Specifies whether the policy is immutable."
      },
      AasField {
        name = "CreatedBy",
        semanticId = "iri:https://admin-shell.io/idta/DataRetentionPolicies/CreatedBy/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Legal name of the entity who created the policy."
      },
      AasField {
        name = "CreationTime",
        semanticId = "iri:https://admin-shell.io/idta/DataRetentionPolicies/CreationTime/1/0",
        type = refBy(DateTimeType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"2024-01-15T13:50:15.070Z"},
        description = "Timestamp of when the policy was created."
      },
      AasField {
        name = "Issuer",
        semanticId = "iri:https://admin-shell.io/idta/DataRetentionPolicies/Issuer/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"European Parliament"},
        description = "Legal name of the entity that issued the policy."
      },
      AasField {
        name = "Source",
        semanticId = "iri:https://admin-shell.io/idta/DataRetentionPolicies/Source/1/0",
        type = refBy(Source),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Identity of the policy source within the context of the issuer."
      },
      AasField {
        name = "EffectiveFrom",
        semanticId = "iri:https://admin-shell.io/idta/DataRetentionPolicies/EffectiveFrom/1/0",
        type = refBy(DateTimeType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"2018-05-25T00:00:00.000Z"},
        description = "Date and time that the policy is valid from."
      },
      AasField {
        name = "EffectiveUntil",
        semanticId = "iri:https://admin-shell.io/idta/DataRetentionPolicies/EffectiveUntil/1/0",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Date and time until when the policy is valid."
      },
      AasField {
        name = "AuditLog",
        semanticId = "iri:https://admin-shell.io/idta/DataRetentionPolicies/AuditLog/1/0",
        type = refBy(AuditLog),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"oneToMany"},
        description = "Activity log of changes to the policy. Order Relevant: Yes."
      }
    }
  };

  AasSubmodelElementCollectionType Source = {
    name = "Source",
    semanticId = "iri:https://admin-shell.io/idta/DataRetentionPolicies/Source/1/0",
    description = "Describes the source of a policy.",
    versionIdentifier = "IDTA 02056-1-0",
    fields = {
      AasField {
        name = "Identifier",
        semanticId = "iri:https://admin-shell.io/idta/DataRetentionPolicies/SourceIdentifier/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Regulation 2016/679"},
        description = "Identifier of the source (e.g., DOI)."
      },
      AasField {
        name = "Document",
        semanticId = "iri:https://admin-shell.io/idta/DataRetentionPolicies/SourceDocument/1/0",
        type = refBy(AasFileResourceType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Reference to an external file representing the policy source (e.g., contract document)."
      },
      AasField {
        name = "Link",
        semanticId = "iri:https://admin-shell.io/idta/DataRetentionPolicies/SourceLink/1/0",
        type = refBy(AasAnyURIType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Link to an external source (e.g., web page)."
      },
      AasField {
        name = "Reference",
        semanticId = "iri:https://admin-shell.io/idta/DataRetentionPolicies/SourceReference/1/0",
        type = refBy(AasReferenceType),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Reference to a source in the same or an external Asset Administration Shell."
      }
    }
  };

  AasSubmodelElementListType AuditLog = {
    name = "AuditLog",
    semanticId = "iri:https://admin-shell.io/idta/DataRetentionPolicies/AuditLog/1/0",
    description = "Contains the activity log of changes to the policy.",
    versionIdentifier = "IDTA 02056-1-0",
    fields = {
      AasField {
        name = "<NoIdShort>",
        semanticId = "iri:https://admin-shell.io/idta/DataRetentionPolicies/Activity/1/0",
        isGeneric = true,
        type = refBy(Generic__no_idShort__1),
        minimumInstances = 1,
        description = "An activity related to the policy."
      }
    }
  };

  AasSubmodelElementCollectionType Generic__no_idShort__1 = {
    name = "<no_idShort>_1",
    semanticId = "iri:https://admin-shell.io/idta/DataRetentionPolicies/Activity/1/0",
    description = "An activity related to the policy.",
    versionIdentifier = "IDTA 02056-1-0",
    isGeneric = true,
    fields = {
      AasField {
        name = "Timestamp",
        semanticId = "iri:https://admin-shell.io/idta/DataRetentionPolicies/Timestamp/1/0",
        type = refBy(DateTimeType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Time when the change was made."
      },
      AasField {
        name = "Identity",
        semanticId = "iri:https://admin-shell.io/idta/DataRetentionPolicies/Identity/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Username or legal name of the entity that made the change."
      },
      AasField {
        name = "Operation",
        semanticId = "iri:https://admin-shell.io/idta/DataRetentionPolicies/Operation/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Description of the action taken."
      },
      AasField {
        name = "Reason",
        semanticId = "iri:https://admin-shell.io/idta/DataRetentionPolicies/Reason/1/0",
        type = refBy(AasMultiLangStringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Reason for the change."
      },
      AasField {
        name = "Signature",
        semanticId = "iri:https://admin-shell.io/idta/DataRetentionPolicies/Signature/1/0",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Signature of the Policy after the change was made."
      }
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
