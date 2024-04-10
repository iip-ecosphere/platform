project IDTA_02017_AssetInterfacesDescription {

  version v1.0;

  import AASDataTypes;

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasSubmodelType AssetInterfacesDescription = {
    name = "AssetInterfacesDescription",
    semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/Submodel",
    description = "Definition of the Submodel Asset Interfaces Description identified by its semanticId. The Submodel idShort can be picked freely.",
    versionIdentifier = "IDTA 02017-1-0",
    fields = {
      AasField {
        name = "Interface",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/Interface",
        multiSemanticIds = true,
        counting = true,
        type = refBy(Interface),
        minimumInstances = 1,
        examples = {"Interface00 Interface_MQTT Modbus"},
        description = "supplementalSemandicId: [IRI] (only if modbus is used) http://www.w3.org/2011/modbus [IRI] (only if mqtt is used) http://www.w3.org/2011/mqtt [IRI] (only if http is used) http://www.w3.org/2011/http [IRI] https://www.w3.org/2019/wot/td Indicates entry point for a particular asset interface description based on Modbus, MQTT, or HTTP (indicated by its semanticId)."
      }
    }
  };

  AasSubmodelElementCollectionType Interface = {
    name = "Interface",
    semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/Interface",
    multiSemanticIds = true,
    description = "This SubmodelElementCollection holds the information for EndpointMetadata and InteractionMetadata. Note: The Interface SMC may also be used to describe interfaces with protocols not yet covered by the AID yet (e.g., only the ExternalDescriptor will be used for providing a GSDML reference for a Profinet communication). In such a case, an appropriate supplementalSemanticId is recommended to identify the purpose of this interface.",
    versionIdentifier = "IDTA 02017-1-0",
    fields = {
      AasField {
        name = "title",
        semanticId = "iri:https://www.w3.org/2019/wot/td#title",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Robot Modbus Interface"},
        description = "Provides a human-readable title to give a human- readable context of the interface."
      },
      AasField {
        name = "created",
        semanticId = "iri:http://purl.org/dc/terms/created",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2022-12-27 08:26:49.219717"},
        description = "Provides  information  when  the  AID  Submodel  was created."
      },
      AasField {
        name = "modified",
        semanticId = "iri:http://purl.org/dc/terms/modified",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"2022-12-27 08:26:49.219717"},
        description = "Provides  information  when  the  AID  Submodel  was modified."
      },
      AasField {
        name = "support",
        semanticId = "iri:https://www.w3.org/2019/wot/td#supportContact",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"mailto:aidsupport@idta.com"},
        description = "Provides an address on how to contact the maintainer of AID Submodel as URI scheme."
      },
      AasField {
        name = "EndpointMetadata",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/EndpointMaetadata",
        type = refBy(EndpointMetadata),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Provides the metadata of the asset’s endpoint (base, content type that is used for interaction, etc)."
      },
      AasField {
        name = "InteractionMetadata",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/InteractionMetadata",
        type = refBy(InteractionMetadata),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "supplementalSem.Id: [IRI]https://www.w3.org/2019/wot/td#InteractionAfforda nce Provides the metadata of the actually interfaces such as which datapoints and functions are provided by the properties, actions, and events interaction abstraction."
      },
      AasField {
        name = "ExternalDescriptor",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/ExternalDescriptor",
        type = refBy(ExternalDescriptor),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Provides a place for existing description files (e.g., Thing Description, GSDML, etc,)."
      }
    }
  };

  AasSubmodelElementCollectionType EndpointMetadata = {
    name = "EndpointMetadata",
    semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/EndpointMetadata",
    description = "This SubmodelElementCollection holds information about asset’s entry point, security and data format serialization.",
    versionIdentifier = "IDTA 02017-1-0",
    fields = {
      AasField {
        name = "base",
        semanticId = "iri:https://www.w3.org/2019/wot/td#baseURI",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"modbus+tcp://192.168.99.159:502/"},
        description = "Defines asset connection entry point. Each protocol specifies a base pattern. Please see Annex B.1 for more details."
      },
      AasField {
        name = "contentType",
        semanticId = "iri:https://www.w3.org/2019/wot/hypermedia#forContentType",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"application/json"},
        description = "Defines content type based on a media type (e.g., text/plain) and potential character decoding/encoding type (e.g., charset=utf-8) for the media type (see RFC2046) of the whole interface."
      },
      AasField {
        name = "securityDefinitions",
        semanticId = "iri:https://www.w3.org/2019/wot/td#definesSecurityScheme",
        type = refBy(securityDefinitions),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"securityDefinitions","basic_sc","..."},
        description = "Defines the security scheme according to W3C: ?      BasicSecurityScheme (basic_sc) ?      DigestSecurityScheme (digest_sc) ?      APIKeySecurityScheme (apikey_sc) ?      BearerSecurityScheme (bearer_sc) ?      PSKSecurityScheme (psk_sc) ?      OAuth2SecurityScheme (oauth2_sc) ?      AutoSecurityScheme (auto_sc) ?      NoSecurityScheme (nosec_sc)."
      },
      AasField {
        name = "security",
        semanticId = "iri:https://www.w3.org/2019/wot/td#hasSecurityConfiguration",
        type = refBy(security),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"security[Ref to basic_sc in securityDefinitions]"},
        description = "Selects one or more of the security scheme(s) that can be applied at runtime from the collection of security schemes defines in securityDefinitions."
      },
      AasField {
        name = "modv_mostSignificantByte",
        semanticId = "iri:https://www.w3.org/2019/wot/modbus#hasMostSignificantByte",
        type = refBy(BooleanType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"true"},
        description = "This property is only applicable for Modbus-based communication. When modv_mostSignificantByte is true, it describes that the byte order of the data in the Modbus message is the most significant byte first (i.e., Big-Endian). When false, it describes the least significant byte first (i.e., Little-Endian)."
      },
      AasField {
        name = "modv_mostSignificantWord",
        semanticId = "iri:https://www.w3.org/2019/wot/modbus#hasMostSignificantWord",
        type = refBy(BooleanType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"true"},
        description = "This property is only applicable for Modbus-based communication. When modv_mostSignificantWord is true, it describes that the word order of the data in the Modbus message is the most significant word first (i.e., no word swapping). When false, it describes the least significant word first (i.e. word swapping)."
      }
    }
  };

  AasSubmodelElementCollectionType InteractionMetadata = {
    name = "InteractionMetadata",
    semanticId = "iri:https://www.w3.org/2019/wot/td#InteractionAffordance",
    description = "This SubmodelElementCollection holds the information of the interaction affordances with properties, actions, and events. An interaction property exposes typically state as datapoint via asset’s interface. This state can then be retrieved (read) and/or observed (subscription). An interaction action allows to invoke a function via asset’s interface, which manipulates state (e.g., toggling a lamp on or off) or triggers a process on the asset (e.g., dim a lamp over time). An interaction event describes an event source via asset’s interface, which asynchronously pushes event data to receivers (e.g., overheating alerts).",
    versionIdentifier = "IDTA 02017-1-0",
    fields = {
      AasField {
        name = "properties",
        semanticId = "iri:https://www.w3.org/2019/wot/td#PropertyAffordance",
        type = refBy(properties),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"properties |_status","voltage","..."},
        description = "Collection of asset’s datapoint definitions as property SMC (also see Section 2.8)."
      },
      AasField {
        name = "actions",
        semanticId = "iri:https://www.w3.org/2019/wot/td#ActionAffordance",
        type = refBy(AasGenericSubmodelElementCollection),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"actions |_onOff","fadeIn","..."},
        description = "Collection of functions that can be done on asset as action SMC."
      },
      AasField {
        name = "events",
        semanticId = "iri:https://www.w3.org/2019/wot/td#EventAffordance",
        type = refBy(AasGenericSubmodelElementCollection),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"events |_overheading","alarm1","..."},
        description = "Collection of events triggerable by datapoint state as event SMC."
      }
    }
  };

  AasSubmodelElementCollectionType ExternalDescriptor = {
    name = "ExternalDescriptor",
    semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/ExternalDescriptor",
    description = "Provides a place for existing description files (e.g., Thing Description, GSDML, etc,).",
    versionIdentifier = "IDTA 02017-1-0",
    fields = {
      AasField {
        name = "externalDescriptorName",
        displayName = "{descriptorName}",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/externalDescriptorName",
        isGeneric = true,
        type = refBy(AasFileResourceType),
        minimumInstances = 1,
        examples = {"./sensor_device.td.jsonld","[string]","./gsdml-v21-ed2.xml","[string] https://example.com/myDe scriptorFile"},
        description = "File reference (local in AASX or outside) to an external descriptor description (e.g., Thing Description, GSDML, MTP, etc,)."
      }
    }
  };

  AasSubmodelElementCollectionType properties = {
    name = "properties",
    semanticId = "iri:https://www.w3.org/2019/wot/td#hasPropertyAffordance",
    description = "This SubmodelElementCollection collects the interaction affordance properties.",
    versionIdentifier = "IDTA 02017-1-0",
    fields = {
      AasField {
        name = "PropertyDefinition",
        displayName = "{property_name}",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/PropertyDefinition",
        isGeneric = true,
        type = refBy(Generic__property_name__2),
        minimumInstances = 0,
        examples = {"? [idShort] pump_speed","[idShort] TemperatureValue"},
        description = "supplementalSemandicId: [IRI] https://www.w3.org/2019/wot/td#name Defines an interaction property that covers usually a datapoint definition that can be read or subscribed to."
      }
    }
  };

  AasSubmodelElementCollectionType Generic__property_name__1 = {
    name = "property_name_1",
    semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/PropertyDefinition",
    multiSemanticIds = true,
    description = "This SubmodelElementCollection defines characteristics of an interaction affordances with its datapoint specifications and how to address it via a specific protocol (e.g., Modbus register).",
    versionIdentifier = "IDTA 02017-1-0",
    isGeneric = true,
    fields = {
      AasField {
        name = "key",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"temperature-value"},
        description = "https://admin-shell.io/idta/AssetInterfacesDescription/1/0/key Optional element when the idShort of {property_name} cannot be used to reflect the desired property name due to the idShort restrictions (e.g., payload message uses “temperature-value” as key term)."
      },
      AasField {
        name = "title",
        semanticId = "iri:https://www.w3.org/2019/wot/td#title",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Rotation speed"},
        description = "Provides a human-readable title of this interaction (e.g., display a text for UI representation)."
      },
      AasField {
        name = "observable",
        semanticId = "iri:https://www.w3.org/2019/wot/td#isObservable",
        type = refBy(BooleanType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"true"},
        description = "An indicator that tells that the interaction datapoint can be observed with a, e.g., subscription mechanism by an underlying protocol. In case of MQTT, it is recommended that observable=true for each interaction property."
      },
      AasField {
        name = "forms",
        semanticId = "iri:https://www.w3.org/2019/wot/td#hasFormContainsaboutdatapointresource",
        type = refBy(forms),
        minimumInstances = 1,
        maximumInstances = 1,
        description = "Note, forms is only available at the top level {property_name}."
      },
      AasField {
        name = "type",
        semanticId = "iri:https://www.w3.org/1999/02/22-rdf-syntax-ns#type",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"integer"},
        description = "Indicates the abstract data type (one of object, array, string, number, integer, boolean, or null) of the described datapoint."
      },
      AasField {
        name = "const",
        semanticId = "iri:https://www.w3.org/2019/wot/json-schema#const",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"My device name"},
        description = "Provides a constant value for defined datapoint. The data type should be identical to the one as provided by the Property type."
      },
      AasField {
        name = "enum",
        semanticId = "iri:https://www.w3.org/2019/wot/json-schema#enum",
        type = refBy(AasGenericSubmodelElementCollection),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"enum[‘On’, ‘Off’, ‘Error’]"},
        description = "Provides a list of restricted set of values that the asset can provide as datapoint value."
      },
      AasField {
        name = "default",
        semanticId = "iri:https://www.w3.org/2019/wot/json-schema#default",
        type = refBy(BooleanType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"true"},
        description = "Provides a default value that must of the type as the datapoint valueType. The data type should be identical to the one as provided by the Property type."
      },
      AasField {
        name = "unit",
        semanticId = "iri:https://schema.org/unitCode",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"degree:celcius"},
        description = "Provides information about the datapoint’s unit. It is recommended that the unit value is assigned with a valueId from known."
      },
      AasField {
        name = "min_max",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/minMaxRange",
        multiSemanticIds = true,
        type = refBy(AasRangeType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"or [float] 12..56 or 0..9.99"},
        description = "supplementalSemandicId: [IRI] (only if minimum is used) https://www.w3.org/2019/wot/json-schema#minimum [IRI] (only if maximum is used) https://www.w3.org/2019/wot/json-schema#maximum Specifies a minimum and/or maximum numeric value for the datapoint. This term is only used when type element is number or integer. When it is number, the range data type has to be float and when it is integer, the range data type has to be integer."
      },
      AasField {
        name = "lengthRange",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/lengthRange",
        multiSemanticIds = true,
        type = refBy(AasRangeType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"10 - 23"},
        description = "supplimentalSemanticId: [IRI] (only if minimum is used) https://www.w3.org/2019/wot/json-schema#minLength [IRI] (only if maximum is used) https://www.w3.org/2019/wot/json-schema#maxLength Specifies the minimum and maximum length of a string."
      },
      AasField {
        name = "items",
        semanticId = "iri:https://www.w3.org/2019/wot/json-schema#items",
        type = refBy(AasGenericSubmodelElementCollection),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"items |_type=integer","min_max=0..100"},
        description = "Used to define the data schema characteristics (as specified within Section 2.9) of an array payload."
      },
      AasField {
        name = "itemsRange",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/itemsRange",
        multiSemanticIds = true,
        type = refBy(AasRangeType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"4 - 10"},
        description = "supplimentalSemanticId: [IRI] (only if minimum is used) https://www.w3.org/2019/wot/json-schema#minItems  [IRI] (only if maximum is used) https://www.w3.org/2019/wot/json-schema#maxItems Defines the minimum and maximum number of items that have to be in an array payload."
      },
      AasField {
        name = "valueSemantics",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/valueSemantics",
        type = refBy(AasReferenceType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"à conceptDescription"},
        description = "Provides additional semantic information of the value that is read/subscribed at runtime."
      },
      AasField {
        name = "properties",
        semanticId = "iri:https://www.w3.org/2019/wot/json-schema#properties",
        type = refBy(properties_2),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"properties |_timestamp","type=string","format=date-time","temperature","type=number","min_max=-20..47","unit=°C"},
        description = "Nested definitions of a datapoint. Only applicable if type=object."
      }
    }
  };

  AasSubmodelElementCollectionType properties_2 = {
    name = "properties_2",
    semanticId = "iri:https://www.w3.org/2019/wot/json-schema#properties",
    description = "This SubmodelElementCollection collects the nested data definition of a complex-based datapoint.",
    versionIdentifier = "IDTA 02017-1-0",
    fields = {
      AasField {
        name = "json_schema_propertyName",
        displayName = "{property_name}",
        semanticId = "iri:https://www.w3.org/2019/wot/json-schema#propertyName",
        isGeneric = true,
        type = refBy(Generic__property_name__2),
        minimumInstances = 1,
        examples = {"? [idShort] timestemp","[idShort] temperature"},
        description = "Defines a data element within an object-based datapoint."
      }
    }
  };

  AasSubmodelElementCollectionType Generic__property_name__2 = {
    name = "property_name_2",
    semanticId = "iri:https://www.w3.org/2019/wot/json-schema#propertyName",
    description = "This SubmodelElementCollection defines characteristics of a datapoint element (e.g., data type, restrictions, and semantics).",
    versionIdentifier = "IDTA 02017-1-0",
    isGeneric = true,
    fields = {
      AasField {
        name = "key",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/key",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"temperature-value"},
        description = "Optional element when the idShort of {property_name} cannot be used to reflect the desired property name due to the idShort restrictions (e.g., payload message uses “temperature-value” as key term)."
      },
      AasField {
        name = "title",
        semanticId = "iri:https://www.w3.org/2019/wot/td#title",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Festo_Robot1"},
        description = "Provides a human-readable title (e.g., display a text for UI representation)."
      },
      AasField {
        name = "type",
        semanticId = "iri:https://www.w3.org/1999/02/22-rdf-syntax-ns#type",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"integer"},
        description = "Indicates the abstract data type (one of object, array, string, number, integer, boolean, or null) of the described datapoint."
      },
      AasField {
        name = "const",
        semanticId = "iri:https://www.w3.org/2019/wot/json-schema#const",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"My device name"},
        description = "Provides a constant value for defined datapoint. The data type should be identical to the one as provided by the Property type."
      },
      AasField {
        name = "enum",
        semanticId = "iri:https://www.w3.org/2019/wot/json-schema#enum",
        type = refBy(AasGenericSubmodelElementCollection),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"enum[‘On’, ‘Off’, ‘Error’]"},
        description = "Provides a list of restricted set of values that the asset can provide as datapoint value."
      },
      AasField {
        name = "default",
        semanticId = "iri:https://www.w3.org/2019/wot/json-schema#default",
        type = refBy(BooleanType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"true"},
        description = "Provides a default value that must of the type as the datapoint valueType. The data type should be identical to the one as provided by the Property type."
      },
      AasField {
        name = "unit",
        semanticId = "iri:https://schema.org/unitCode",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"degree:celcius"},
        description = "Provides information about the datapoint’s unit. It is recommended that the unit value is assigned with a valueId from known unit namespaces/ontologies."
      },
      AasField {
        name = "min_max",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/minMaxRange",
        multiSemanticIds = true,
        type = refBy(AasRangeType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"or [float] 12..56 or 0..9.99"},
        description = "supplementalSemandicId: [IRI] (only if minimum is used) https://www.w3.org/2019/wot/json-schema#minimum [IRI] (only if maximum is used) https://www.w3.org/2019/wot/json-schema#maximum Specifies a minimum and/or maximum numeric value for the datapoint. This term is only used when type element is number or integer. When it is number, the range data type has to be float and when it is integer, the range data type has to be integer."
      },
      AasField {
        name = "lengthRange",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/lengthRange",
        multiSemanticIds = true,
        type = refBy(AasRangeType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"10 - 23"},
        description = "supplimentalSemanticId: [IRI] (only if minimum is used) https://www.w3.org/2019/wot/json-schema#minLength [IRI] (only if maximum is used) https://www.w3.org/2019/wot/json-schema#maxLength Specifies the minimum and maximum length of a string."
      },
      AasField {
        name = "items",
        semanticId = "iri:https://www.w3.org/2019/wot/json-schema#items",
        type = refBy(AasGenericSubmodelElementCollection),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"items |_type=integer","min_max=0..100"},
        description = "Used to define the data schema characteristics of an array payload."
      },
      AasField {
        name = "itemsRange",
        multiSemanticIds = true,
        type = refBy(AasRangeType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"4 - 10"},
        description = "https://admin- shell.io/idta/AssetInterfacesDescription/1/0/itemsRange supplimentalSemanticId: [IRI] (only if minimum is used) https://www.w3.org/2019/wot/json-schema#minItems [IRI] (only if maximum is used) https://www.w3.org/2019/wot/json-schema#maxItems Defines the minimum and maximum number of items that have to be in an array payload."
      },
      AasField {
        name = "properties",
        semanticId = "iri:https://www.w3.org/2019/wot/json-schema#properties",
        type = refBy(properties_2),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"properties","timestamp","type=string","format=date-time","temperature","type=number","min_max=-20..47","unit=°C"},
        description = "Nested definitions of a datapoint. Only applicable if type=object."
      },
      AasField {
        name = "valueSemantics",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/valueSemantics",
        type = refBy(AasReferenceType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"à conceptDescription"},
        description = "Provides additional semantic information of the value that is read/subscribed at runtime."
      }
    }
  };

  AasSubmodelElementCollectionType forms = {
    name = "forms",
    semanticId = "iri:https://www.w3.org/2019/wot/td#hasForm",
    description = "This SubmodelElementCollection defines asset datapoint endpoint resource.",
    versionIdentifier = "IDTA 02017-1-0",
    fields = {
      AasField {
        name = "contentType",
        semanticId = "iri:https://www.w3.org/2019/wot/hypermedia#forContentType",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"application/json"},
        description = "Indicates the datapoint media type specified by IANA."
      },
      AasField {
        name = "href",
        semanticId = "iri:https://www.w3.org/2019/wot/hypermedia#hasTarget",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"/properties/voltage","[string] http://127.0.0.1/mydata","[string] sensor/temperature","[string] 40001?quantity=2"},
        description = "Target IRI relative path or full IRI of asset’s datapoint. The relative endpoint definition in href is always relative to base defined in EndpointMetadata. E.g., if the base in EndpointMetadata provides “http://example.com” and the local href has “/datapoint1” as value. The full datapoint address would be “http://example.com/datapoint1”. The specific addressing pattern for the Modbus, MQTT, and HTTP is explained in Annex B.2."
      },
      AasField {
        name = "subprotocol",
        semanticId = "iri:https://www.w3.org/2019/wot/hypermedia#forSubProtocol",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"longpoll, websub or sse"},
        description = "Indicates the exact mechanism by which an interaction will be accomplished for a given protocol when there are multiple options."
      },
      AasField {
        name = "security",
        semanticId = "iri:https://www.w3.org/2019/wot/td#hasSecurityConfiguration",
        type = refBy(security),
        minimumInstances = 0,
        maximumInstances = 1,
        description = "Selects one or more of the security scheme(s) that can be applied at runtime from the collection of security schemes defines in securityDefinitions SMC."
      },
      AasField {
        name = "htv_methodName",
        semanticId = "iri:https://www.w3.org/2011/http#methodName",
        type = refBy(StringType),
        aspect = "HTTP",
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"GET"},
        description = "Defines the action to be performed datapoint IRI."
      },
      AasField {
        name = "htv_headers",
        semanticId = "iri:https://www.w3.org/2011/http#headersDefinestobesentwithintheHTTPheader",
        type = refBy(htv_headers),
        aspect = "HTTP",
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"htv_header[{ htv_fieldName=Accept-Charset, htv_fieldValue= utf-8"},
        description = "Defines additional information to be sent within the HTTP header message."
      },
      AasField {
        name = "modv_function",
        semanticId = "iri:https://www.w3.org/2019/wot/modbus#hasFunction",
        type = refBy(StringType),
        aspect = "Modbus",
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"readCoil","[string]","readHoldingRegisters"},
        description = "Abstraction of the Modbus function code sent during a request. A function value can be either readCoil, readDeviceIdentification, readDiscreteInput, readHoldingRegisters, readInputRegisters, writeMultipleCoils, writeMultipleHoldingRegisters, writeSingleCoil, or  writeSingleHoldingRegister."
      },
      AasField {
        name = "modv_entity",
        semanticId = "iri:https://www.w3.org/2019/wot/modbus#hasEntity",
        type = refBy(StringType),
        aspect = "Modbus",
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"Coil ?   [string]","HoldingRegisters"},
        description = "A registry type to let the runtime automatically detect the right function code. An entity value can be Coil, DiscreteInput, HoldingRegister, or InputRegister."
      },
      AasField {
        name = "modv_zeroBasedAddressing",
        semanticId = "iri:https://www.w3.org/2019/wot/modbus#hasZeroBasedAddressingFlag",
        type = refBy(BooleanType),
        aspect = "Modbus",
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"true"},
        description = "Modbus implementations can differ in the way addressing works, as the first coil/register can be either referred to as True or False."
      },
      AasField {
        name = "modv_pollingTime",
        semanticId = "iri:https://www.w3.org/2019/wot/modbus#hasPollingTime",
        type = refBy(IntegerType),
        aspect = "Modbus",
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"5"},
        description = "Modbus TCP maximum polling rate. The Modbus specification does not define a maximum or minimum allowed polling rate, however specific implementations might introduce such limits. Defined as integer of milliseconds."
      },
      AasField {
        name = "modv_timeout",
        semanticId = "iri:https://www.w3.org/2019/wot/modbus#hasTimeout",
        type = refBy(IntegerType),
        aspect = "Modbus",
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"5"},
        description = "Modbus response maximum waiting time. Defines how much time in milliseconds the runtime should wait until it receives a reply from the device."
      },
      AasField {
        name = "modv_type",
        semanticId = "iri:https://www.w3.org/2019/wot/modbus#hasPayloadDataType",
        type = refBy(StringType),
        aspect = "Modbus",
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"xs:float •    [string] xs:unsignedInt","•    [string] xs:string"},
        description = "Defines the data type of the modbus asset payload. type in terms of possible sign, base type. the modv_type offers a set a types defined in XML schema defined in [12]. The set of supported types value are as follows:  xsd:float, xs:short ,xs:unsignedInt,,xs:string, xs:byte, xs:int, xs:boolean, xs:integer,xs:double, xs:hexbinary, xs:decimal, xs:long, xs:unsignedbyte, xs:unsignedshort, xs:unsignedint, xs:unsignedlong,."
      },
      AasField {
        name = "modv_mostSignificantByte",
        semanticId = "iri:https://www.w3.org/2019/wot/modbus#hasMostSignificantByte",
        type = refBy(BooleanType),
        aspect = "Modbus",
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"true"},
        description = "When modv_mostSignificantByte is true, it describes that the byte order of the data in the Modbus message is the most significant byte first (i.e., Big-Endian). When false, it describes the least significant byte first (i.e., Little-Endian)."
      },
      AasField {
        name = "modv_mostSignificantWord",
        semanticId = "iri:https://www.w3.org/2019/wot/modbus#hasMostSignificantWord",
        type = refBy(BooleanType),
        aspect = "Modbus",
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"true"},
        description = "When modv_mostSignificantWord is true, it describes that the word order of the data in the Modbus message is the most significant word first (i.e., no word swapping). When false, it describes the least significant word first (i.e. word swapping)."
      },
      AasField {
        name = "mqv_retain",
        semanticId = "iri:https://www.w3.org/2019/wot/mqtt#hasRetainFlag",
        type = refBy(BooleanType),
        aspect = "MQTT",
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"1 or 0, true or false"},
        description = "It is an indicator that tells the broker to always retain last published payload."
      },
      AasField {
        name = "mqv_controlPacket",
        semanticId = "iri:https://www.w3.org/2019/wot/mqtt#ControlPacket",
        type = refBy(StringType),
        aspect = "MQTT",
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"one of “subscribe”","“publish” and “unsubscribe”"},
        description = "Defines the method associated to the datapoint in relation to the broker."
      },
      AasField {
        name = "mqv_qos",
        semanticId = "iri:https://www.w3.org/2019/wot/mqtt#hasQoSFlag",
        type = refBy(StringType),
        aspect = "MQTT",
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"default = 0 one of 0,1 and 2"},
        description = "Defined the level of guarantee for message delivery between clients. 0 = atMostOnce 1 = atLeastOnce 2 = exactlyOnce."
      }
    }
  };

  AasSubmodelElementListType htv_headers = {
    name = "htv_headers",
    semanticId = "iri:https://www.w3.org/2011/http#headers",
    description = "This SML holds the information for http message headers definition as a SMC.",
    versionIdentifier = "IDTA 02017-1-0",
    fields = {
      AasField {
        name = "<NoIdShort>",
        isGeneric = true,
        type = refBy(AasGenericSubmodelElementCollection),
        minimumInstances = 1,
        description = "message header content."
      }
    }
  };

  AasSubmodelElementCollectionType NoIdShort_0 = {
    name = "<NoIdShort>",
    semanticId = "iri:https://www.w3.org/2011/http#headers",
    description = "This SMC holds the information for http message header definition as a SMC.",
    versionIdentifier = "IDTA 02017-1-0",
    fields = {
      AasField {
        name = "htv_fieldName",
        semanticId = "iri:https://www.w3.org/2011/http#fieldName",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"Accept-Charset","[string]","Content-Length"},
        description = "Defines message header name."
      },
      AasField {
        name = "htv_fieldValue",
        semanticId = "iri:https://www.w3.org/2011/http#fieldValue",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"utf-8 ?    [string] 56"},
        description = "Defines message header value."
      }
    }
  };

  AasSubmodelListType security = {
    name = "security",
    semanticId = "iri:https://www.w3.org/2019/wot/td#hasSecurityConfiguration",
    description = "Specifies one or more security scheme that are applied for all interactions (when defined in SMC EndpointMetadata) or is valid for a specific property interaction affordance (when defined in SMC forms).",
    versionIdentifier = "IDTA 02017-1-0",
    fields = {
      AasField {
        name = "<NoIdShort>",
        isGeneric = true,
        type = refBy(AasReferenceType),
        minimumInstances = 1,
        description = "within the SML points to a sercurity scheme definition in the SMC securityDefinitions."
      }
    }
  };

  AasSubmodelElementCollectionType securityDefinitions = {
    name = "securityDefinitions",
    semanticId = "iri:https://www.w3.org/2019/wot/td#definesSecurityScheme",
    description = "This SubmodelElementCollection holds the information about security mechanism used to access the asset.",
    versionIdentifier = "IDTA 02017-1-0",
    fields = {
      AasField {
        name = "SecurityScheme",
        displayName = "{SecurityScheme}",
        isGeneric = true,
        type = refBy(AasGenericSubmodelElementCollection),
        minimumInstances = 1,
        description = "A collection that holds the definition of one or more security mechanisms supported by AID."
      }
    }
  };

  AasSubmodelElementCollectionType nosec_sc = {
    name = "nosec_sc",
    description = "This SubmodelElementCollection holds the information about security mechanism used to access the asset.",
    versionIdentifier = "IDTA 02017-1-0",
    fields = {
      AasField {
        name = "proxy",
        semanticId = "iri:https://www.w3.org/2019/wot/security#proxy",
        type = refBy(AasAnyURIType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"'http://136.243.47.220:3128/'"},
        description = "Provides address information of the proxy server the security configuration provides access to."
      },
      AasField {
        name = "scheme",
        semanticId = "iri:https://www.w3.org/2019/wot/security#SecurityScheme",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"apikey"},
        description = "Defines the security mechanism that used during access. Supported modes one of nosec, basic, digest, bearer, psk, oauth2, apikey or auto."
      }
    }
  };

  AasSubmodelElementCollectionType basic_sc = {
    name = "basic_sc",
    semanticId = "iri:https://www.w3.org/2019/wot/security#BasicSecurityScheme",
    description = "This SubmodelElements holds the information about security mechanism based on basic or apikey security.",
    versionIdentifier = "IDTA 02017-1-0",
    fields = {
      AasField {
        name = "proxy",
        semanticId = "iri:https://www.w3.org/2019/wot/security#proxy",
        type = refBy(AasAnyURIType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"'http://136.243.47.220:3128/'"},
        description = "Provides address information of the proxy server the security configuration provides access to."
      },
      AasField {
        name = "scheme",
        semanticId = "iri:https://www.w3.org/2019/wot/security#SecurityScheme",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"apikey"},
        description = "Defines the security mechanism that used during access. Supported modes one of nosec, basic, digest, bearer, psk, oauth2, apikey or auto."
      },
      AasField {
        name = "name",
        semanticId = "iri:https://www.w3.org/2019/wot/security#name",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"adminKey"},
        description = "Name for query, header, cookie, or uri parameters."
      },
      AasField {
        name = "in",
        semanticId = "iri:https://www.w3.org/2019/wot/security#in",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"header"},
        description = "Specifies the location of security authentication information. Proposed values are header, query, body, cookie or auto."
      }
    }
  };

  AasSubmodelElementCollectionType digest_sc = {
    name = "digest_sc",
    semanticId = "iri:https://www.w3.org/2019/wot/security#DigestSecurityScheme",
    description = "This SubmodelElements holds the information about security mechanism based on digest security.",
    versionIdentifier = "IDTA 02017-1-0",
    fields = {
      AasField {
        name = "proxy",
        semanticId = "iri:https://www.w3.org/2019/wot/security#proxy",
        type = refBy(AasAnyURIType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"'http://136.243.47.220:3128/'"},
        description = "Provides address information of the proxy server the security configuration provides access to."
      },
      AasField {
        name = "scheme",
        semanticId = "iri:https://www.w3.org/2019/wot/security#SecurityScheme",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"apikey"},
        description = "Defines the security mechanism that used during access. Supported modes one of nosec, basic, digest, bearer, psk, oauth2, apikey or auto."
      },
      AasField {
        name = "name",
        semanticId = "iri:https://www.w3.org/2019/wot/security#name",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"adminKey"},
        description = "Name for query, header, cookie, or uri parameters."
      },
      AasField {
        name = "in",
        semanticId = "iri:https://www.w3.org/2019/wot/security#in",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"header"},
        description = "Specifies the location of security authentication information. Proposed values are header, query, body, cookie or auto."
      },
      AasField {
        name = "qop",
        semanticId = "iri:https://www.w3.org/2019/wot/security#qop",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"auth"},
        description = "Defines Quality of protection. Values is one of auth or auth-int."
      }
    }
  };

  AasSubmodelElementCollectionType bearer_sc = {
    name = "bearer_sc",
    semanticId = "iri:https://www.w3.org/2019/wot/security#BearerSecurityScheme",
    description = "This SubmodelElements holds the information about security mechanism based on bearer security.",
    versionIdentifier = "IDTA 02017-1-0",
    fields = {
      AasField {
        name = "proxy",
        semanticId = "iri:https://www.w3.org/2019/wot/security#proxy",
        type = refBy(AasAnyURIType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"'http://136.243.47.220:3128/'"},
        description = "Provides address information of the proxy server the security configuration provides access to."
      },
      AasField {
        name = "scheme",
        semanticId = "iri:https://www.w3.org/2019/wot/security#SecurityScheme",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"apikey"},
        description = "Defines the security mechanism that used during access. Supported modes one of nosec, basic, digest, bearer, psk, oauth2, apikey or auto."
      },
      AasField {
        name = "name",
        semanticId = "iri:https://www.w3.org/2019/wot/security#name",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"key"},
        description = "Name for query, header, cookie, or uri parameters."
      },
      AasField {
        name = "in",
        semanticId = "iri:https://www.w3.org/2019/wot/security#in",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"query"},
        description = "Specifies the location of security authentication information. Proposed values are header, query, body, cookie or auto."
      },
      AasField {
        name = "authorization",
        semanticId = "iri:https://www.w3.org/2019/wot/security#authorization",
        type = refBy(AasAnyURIType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"http://136.243.47.220:3128/"},
        description = "Specifies URI of the authorization server."
      },
      AasField {
        name = "alg",
        semanticId = "iri:https://www.w3.org/2019/wot/security#alg",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"ES256"},
        description = "Defines Encoding, encryption, or digest algorithm (e.g. ES256, ES512-256)."
      },
      AasField {
        name = "format",
        semanticId = "iri:https://www.w3.org/2019/wot/security#format",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"jwt"},
        description = "Specifies format of security authentication information. Options as value are jwt, cwt, jwe or jws."
      }
    }
  };

  AasSubmodelElementCollectionType psk_sc = {
    name = "psk_sc",
    semanticId = "iri:https://www.w3.org/2019/wot/security#PSKSecurityScheme",
    description = "This SubmodelElements holds the information about security mechanism based on psk security.",
    versionIdentifier = "IDTA 02017-1-0",
    fields = {
      AasField {
        name = "proxy",
        semanticId = "iri:https://www.w3.org/2019/wot/security#proxy",
        type = refBy(AasAnyURIType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"'http://136.243.47.220:3128/'"},
        description = "Provides address information of the proxy server the security configuration provides access to."
      },
      AasField {
        name = "scheme",
        semanticId = "iri:https://www.w3.org/2019/wot/security#SecurityScheme",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"apikey"},
        description = "Defines the security mechanism that used during access. Supported modes one of nosec, basic, digest, bearer, psk, oauth2, apikey or auto."
      },
      AasField {
        name = "identity",
        semanticId = "iri:https://www.w3.org/2019/wot/security#identity",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"aid-app"},
        description = "Identifier providing information which can be used for selection or confirmation."
      }
    }
  };

  AasSubmodelElementCollectionType oauth2_sc = {
    name = "oauth2_sc",
    semanticId = "iri:https://www.w3.org/2019/wot/security#OAuth2SecurityScheme",
    description = "This SubmodelElements holds the information about security mechanism based on oauth2 security.",
    versionIdentifier = "IDTA 02017-1-0",
    fields = {
      AasField {
        name = "proxy",
        semanticId = "iri:https://www.w3.org/2019/wot/security#proxy",
        type = refBy(AasAnyURIType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"'http://136.243.47.220:3128/'"},
        description = "Provides address information of the proxy server the security configuration provides access to."
      },
      AasField {
        name = "scheme",
        semanticId = "iri:https://www.w3.org/2019/wot/security#SecurityScheme",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"apikey"},
        description = "Defines the security mechanism that used during access. Supported modes one of nosec, basic, digest, bearer, psk, oauth2, apikey or auto."
      },
      AasField {
        name = "token",
        semanticId = "iri:https://www.w3.org/2019/wot/security#token",
        type = refBy(AasAnyURIType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"'http://136.243.47.220:3128/'"},
        description = "Specifies URI of the token server."
      },
      AasField {
        name = "refresh",
        semanticId = "iri:https://www.w3.org/2019/wot/security#refresh",
        type = refBy(AasAnyURIType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"'http://136.243.47.220:3128/'"},
        description = "Specifies URI of the refresh server."
      },
      AasField {
        name = "authorization",
        semanticId = "iri:https://www.w3.org/2019/wot/security#authorization",
        type = refBy(AasAnyURIType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"'http://136.243.47.220:3128/'"},
        description = "Specifies URI of the authorization server."
      },
      AasField {
        name = "scopes",
        semanticId = "iri:https://www.w3.org/2019/wot/security#scopes",
        type = refBy(AasGenericSubmodelElementCollection),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"scopes[limited","special]"},
        description = "Set of authorization scope identifiers (as Property) provided as an array. These are provided in tokens returned by an authorization server and associated with forms in order to identify what resources a client may access and how."
      },
      AasField {
        name = "flow",
        semanticId = "iri:https://www.w3.org/2019/wot/security#flow",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"code"},
        description = "Defines authorization flow such as code or client."
      }
    }
  };

  AasSubmodelElementCollectionType apikey_sc = {
    name = "apikey_sc",
    semanticId = "iri:https://www.w3.org/2019/wot/security#APIKeySecurityScheme",
    description = "This SubmodelElements holds the information about security mechanism based on basic or apikey security.",
    versionIdentifier = "IDTA 02017-1-0",
    fields = {
      AasField {
        name = "proxy",
        semanticId = "iri:https://www.w3.org/2019/wot/security#proxy",
        type = refBy(AasAnyURIType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"'http://136.243.47.220:3128/'"},
        description = "Provides address information of the proxy server the security configuration provides access to."
      },
      AasField {
        name = "scheme",
        semanticId = "iri:https://www.w3.org/2019/wot/security#SecurityScheme",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"apikey"},
        description = "Defines the security mechanism that used during access. Supported modes one of nosec, basic, digest, bearer, psk, oauth2, apikey or auto."
      },
      AasField {
        name = "name",
        semanticId = "iri:https://www.w3.org/2019/wot/security#name",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"adminKey"},
        description = "Name for query, header, cookie, or uri parameters."
      },
      AasField {
        name = "in",
        semanticId = "iri:https://www.w3.org/2019/wot/security#in",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"header"},
        description = "Specifies the location of security authentication information. Proposed values are header, query, body, cookie or auto."
      }
    }
  };

  AasSubmodelElementCollectionType auto_sc = {
    name = "auto_sc",
    description = "This SubmodelElementCollection holds the information about security mechanism used to access the asset.",
    versionIdentifier = "IDTA 02017-1-0",
    fields = {
      AasField {
        name = "proxy",
        semanticId = "iri:https://www.w3.org/2019/wot/security#proxy",
        type = refBy(AasAnyURIType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"'http://136.243.47.220:3128/'"},
        description = "Provides address information of the proxy server the security configuration provides access to."
      },
      AasField {
        name = "scheme",
        semanticId = "iri:https://www.w3.org/2019/wot/security#SecurityScheme",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"apikey"},
        description = "Defines the security mechanism that used during access. Supported modes one of nosec, basic, digest, bearer, psk, oauth2, apikey or auto."
      }
    }
  };

  AasSubmodelElementCollectionType combo_sc = {
    name = "combo_sc",
    semanticId = "iri:https://www.w3.org/2019/wot/security#ComboSecurityScheme",
    description = "This SubmodelElements holds the information about security mechanism based on combo security.",
    versionIdentifier = "IDTA 02017-1-0",
    fields = {
      AasField {
        name = "proxy",
        semanticId = "iri:https://www.w3.org/2019/wot/security#proxy",
        type = refBy(AasAnyURIType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"'http://136.243.47.220:3128/'"},
        description = "Provides address information of the proxy server the security configuration provides access to."
      },
      AasField {
        name = "scheme",
        semanticId = "iri:https://www.w3.org/2019/wot/security#SecurityScheme",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"apikey"},
        description = "Defines the security mechanism that used during access. Supported modes one of nosec, basic, digest, bearer, psk, oauth2, apikey or auto."
      },
      AasField {
        name = "oneOf",
        semanticId = "iri:https://www.w3.org/2019/wot/security#oneOf",
        type = refBy(AasGenericSubmodelElementCollection),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"oneOf[Ref to basic_sc, Ref to bearer_sc]"},
        description = "Array of two or more strings identifying other named security scheme definitions, any one of which, when satisfied, will allow access. Only one may be chosen for use."
      },
      AasField {
        name = "allOf",
        semanticId = "iri:https://www.w3.org/2019/wot/security#allOf",
        type = refBy(AasGenericSubmodelElementCollection),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"allOf[Ref to basic_sc, Ref to apikey_key]"},
        description = "Array of two or more strings identifying other named security scheme definitions, all of which must be satisfied for access."
      }
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
