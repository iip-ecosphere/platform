AAS DataRetentionPoliciesExample
 ASSET ci INSTANCE
 SUBMODEL DataRetentionPolicies (semanticId: iri:https://admin-shell.io/idta/DataRetentionPolicies/1/0)
  REFERENCE InheritedFrom -> true (semanticId: iri:https://admin-shell.io/idta/DataRetentionPolicies/InheritedFrom/1/0)
  SMC Policies (semanticId: iri:https://admin-shell.io/idta/DataRetentionPolicies/Policies/1/0)
   SMC Policy01 (semanticId: iri:https://admin-shell.io/idta/DataRetentionPolicies/Policy/1/0)
    SML AuditLog (semanticId: iri:https://admin-shell.io/idta/DataRetentionPolicies/AuditLog/1/0)
     SMC generic01 (semanticId: iri:https://admin-shell.io/idta/DataRetentionPolicies/Activity/1/0)
      MLP Identity (semanticId: iri:https://admin-shell.io/idta/DataRetentionPolicies/Identity/1/0)
      MLP Operation (semanticId: iri:https://admin-shell.io/idta/DataRetentionPolicies/Operation/1/0)
      MLP Reason (semanticId: iri:https://admin-shell.io/idta/DataRetentionPolicies/Reason/1/0)
      PROPERTY Signature = TEST (semanticId: iri:https://admin-shell.io/idta/DataRetentionPolicies/Signature/1/0)
      PROPERTY Timestamp = 2024/01/01 00:01:00 (semanticId: iri:https://admin-shell.io/idta/DataRetentionPolicies/Timestamp/1/0)
    PROPERTY CreatedBy = TEST (semanticId: iri:https://admin-shell.io/idta/DataRetentionPolicies/CreatedBy/1/0)
    PROPERTY CreationTime = 2024/01/01 00:01:00 (semanticId: iri:https://admin-shell.io/idta/DataRetentionPolicies/CreationTime/1/0)
    PROPERTY EffectiveFrom = 2024/01/01 00:01:00 (semanticId: iri:https://admin-shell.io/idta/DataRetentionPolicies/EffectiveFrom/1/0)
    PROPERTY EffectiveUntil = 2024/01/01 00:01:00 (semanticId: iri:https://admin-shell.io/idta/DataRetentionPolicies/EffectiveUntil/1/0)
    PROPERTY Immutable = true (semanticId: iri:https://admin-shell.io/idta/DataRetentionPolicies/Immutable/1/0)
    PROPERTY Issuer = European Parliament (semanticId: iri:https://admin-shell.io/idta/DataRetentionPolicies/Issuer/1/0)
    RELATIONSHIP Overrides (semanticId: iri:https://admin-shell.io/idta/DataRetentionPolicies/Overrides/1/0)
    PROPERTY RetentionTime = 3 Years (semanticId: iri:https://admin-shell.io/idta/DataRetentionPolicies/RetentionTime/1/0)
    SMC SemanticIds
    SMC Source (semanticId: iri:https://admin-shell.io/idta/DataRetentionPolicies/Source/1/0)
     FILE Document (semanticId: iri:https://admin-shell.io/idta/DataRetentionPolicies/SourceDocument/1/0) length 0
     PROPERTY Identifier = Regulation 2016/679 (semanticId: iri:https://admin-shell.io/idta/DataRetentionPolicies/SourceIdentifier/1/0)
     PROPERTY Link = http://me.here.de (semanticId: iri:https://admin-shell.io/idta/DataRetentionPolicies/SourceLink/1/0)
     REFERENCE Reference -> true (semanticId: iri:https://admin-shell.io/idta/DataRetentionPolicies/SourceReference/1/0)
