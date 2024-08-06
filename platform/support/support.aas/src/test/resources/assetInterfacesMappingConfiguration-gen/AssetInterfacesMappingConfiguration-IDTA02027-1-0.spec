AAS AssetInterfacesMappingConfigurationExample
 ASSET ci INSTANCE
 SUBMODEL AssetInterfacesMappingConfiguration (semanticId: iri:https://admin-shell.io/idta/AssetInterfacesMappingConfiguration/1/0/Submodel)
  SMC MappingConfigurations (semanticId: iri:https://admin-shell.io/idta/AssetInterfacesMappingConfiguration/1/0/MappingConfigurations)
   SMC generic01 (semanticId: iri:https://admin-shell.io/idta/AssetInterfacesMappingConfiguration/1/0/MappingConfiguration)
    REFERENCE InterfaceReference -> true (semanticId: iri:https://admin-shell.io/idta/AssetInterfacesMappingConfiguration/1/0/InterfaceReference)
    SMC MappingSourceSinkRelations (semanticId: iri:https://admin-shell.io/idta/AssetInterfacesMappingConfiguration/1/0/MappingSourceSinkRelations)
     RELATIONSHIP generic01 (semanticId: iri:https://admin-shell.io/idta/AssetInterfacesMappingConfiguration/1/0/MappingSourceSinkRelation)
