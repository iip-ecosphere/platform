AAS AssetInterfacesDescriptionExample
 ASSET ci INSTANCE
 SUBMODEL AssetInterfacesDescription (semanticId: iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/Submodel)
  SMC Interface01 (semanticId: iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/Interface)
   SMC EndpointMetadata (semanticId: iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/EndpointMetadata)
    PROPERTY base = modbus+tcp://192.168.99.159:502/ (semanticId: iri:https://www.w3.org/2019/wot/td#baseURI)
    PROPERTY contentType = application/json (semanticId: iri:https://www.w3.org/2019/wot/hypermedia#forContentType)
    PROPERTY modv_mostSignificantByte = true (semanticId: iri:https://www.w3.org/2019/wot/modbus#hasMostSignificantByte)
    PROPERTY modv_mostSignificantWord = true (semanticId: iri:https://www.w3.org/2019/wot/modbus#hasMostSignificantWord)
    SMC security (semanticId: iri:https://www.w3.org/2019/wot/td#hasSecurityConfiguration)
    SMC securityDefinitions (semanticId: iri:https://www.w3.org/2019/wot/td#definesSecurityScheme)
   SMC ExternalDescriptor (semanticId: iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/ExternalDescriptor)
    FILE generic01 (semanticId: iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/externalDescriptorName) length 0
   SMC InteractionMetadata (semanticId: iri:https://www.w3.org/2019/wot/td#InteractionAffordance)
    SMC actions
    SMC events
    SMC properties (semanticId: iri:https://www.w3.org/2019/wot/td#hasPropertyAffordance)
     SMC generic01 (semanticId: iri:https://www.w3.org/2019/wot/json-schema#propertyName)
      PROPERTY const = My device name (semanticId: iri:https://www.w3.org/2019/wot/json-schema#const)
      PROPERTY default = true (semanticId: iri:https://www.w3.org/2019/wot/json-schema#default)
      SMC enum
      SMC items
      RANGE itemsRange (semanticId: iri:https://oktoflow.de/semId-itemsRange) min 4 max 10
      PROPERTY key = temperature-value (semanticId: iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/key)
      RANGE lengthRange (semanticId: iri:https://oktoflow.de/semId-lengthRange) min 10 max 23
      RANGE min_max (semanticId: iri:https://oktoflow.de/semId-min_max) min 12.0 max 56.0
      SMC properties_2 (semanticId: iri:https://www.w3.org/2019/wot/json-schema#properties)
       SMC generic01 (semanticId: iri:https://www.w3.org/2019/wot/json-schema#propertyName)
      PROPERTY title = Festo_Robot1 (semanticId: iri:https://www.w3.org/2019/wot/td#title)
      PROPERTY type = integer (semanticId: iri:https://www.w3.org/1999/02/22-rdf-syntax-ns#type)
      PROPERTY unit = degree:celcius (semanticId: iri:https://schema.org/unitCode)
      REFERENCE valueSemantics -> true (semanticId: iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/valueSemantics)
   PROPERTY created = 2022-12-27 08:26:49.219717 (semanticId: iri:http://purl.org/dc/terms/created)
   PROPERTY modified = 2022-12-27 08:26:49.219717 (semanticId: iri:http://purl.org/dc/terms/modified)
   PROPERTY support = mailto:aidsupport@idta.com (semanticId: iri:https://www.w3.org/2019/wot/td#supportContact)
   PROPERTY title = Robot Modbus Interface (semanticId: iri:https://www.w3.org/2019/wot/td#title)