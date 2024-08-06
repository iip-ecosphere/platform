project IDTA_02017_AssetInterfacesDescription {

  import AASDataTypes;

  annotate BindingTime bindingTime = BindingTime::compile to .;

  AasSubmodelType AssetInterfacesDescription = {
    name = "AssetInterfacesDescription",
    semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/Submodel",
    description = "AID Template Sample.",
    fields = {
      AasField {
        name = "InterfaceTemplateForHTTP",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/Interface",
        type = refBy(InterfaceTemplateForHTTP),
        minimumInstances = 0
      },
      AasField {
        name = "InterfaceTemplateForMODBUS",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/Interface",
        type = refBy(InterfaceTemplateForMODBUS),
        minimumInstances = 0
      },
      AasField {
        name = "InterfaceTemplateForMQTT",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/Interface",
        type = refBy(InterfaceTemplateForMQTT),
        minimumInstances = 0
      }
    }
  };

  AasSubmodelElementCollectionType InterfaceTemplateForHTTP = {
    name = "InterfaceTemplateForHTTP",
    semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/Interface",
    fields = {
      AasField {
        name = "title",
        semanticId = "iri:https://www.w3.org/2019/wot/td#title",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1
      },
      AasField {
        name = "created",
        semanticId = "iri:http://purl.org/dc/terms/created",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "modified",
        semanticId = "iri:http://purl.org/dc/terms/modified",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "support",
        semanticId = "iri:https://www.w3.org/2019/wot/td#supportContact",
        type = refBy(AasAnyURIType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "EndpointMetadata",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/EndpointMetadata",
        type = refBy(EndpointMetadata),
        minimumInstances = 1,
        maximumInstances = 1
      },
      AasField {
        name = "InteractionMetadata",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/InteractionMetadata",
        type = refBy(InteractionMetadata),
        minimumInstances = 1,
        maximumInstances = 1
      },
      AasField {
        name = "ExternalDescriptor",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/ExternalDescriptor",
        type = refBy(ExternalDescriptor),
        minimumInstances = 0,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType EndpointMetadata = {
    name = "EndpointMetadata",
    semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/EndpointMetadata",
    fields = {
      AasField {
        name = "base",
        semanticId = "iri:https://www.w3.org/2019/wot/td#baseURI",
        type = refBy(AasAnyURIType),
        minimumInstances = 1,
        maximumInstances = 1
      },
      AasField {
        name = "contentType",
        semanticId = "iri:https://www.w3.org/2019/wot/hypermedia#forContentType",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"application/json"}
      },
      AasField {
        name = "security",
        semanticId = "iri:https://www.w3.org/2019/wot/td#hasSecurityConfiguration",
        type = refBy(security),
        minimumInstances = 1,
        maximumInstances = 1
      },
      AasField {
        name = "securityDefinitions",
        semanticId = "iri:https://www.w3.org/2019/wot/td#definesSecurityScheme",
        type = refBy(securityDefinitions),
        minimumInstances = 1,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementListType security = {
    name = "security",
    semanticId = "iri:https://www.w3.org/2019/wot/td#hasSecurityConfiguration",
    fields = {
      AasField {
        type = refBy(AasReferenceType)
      }
    }
  };

  AasSubmodelElementCollectionType securityDefinitions = {
    name = "securityDefinitions",
    semanticId = "iri:https://www.w3.org/2019/wot/td#definesSecurityScheme",
    fields = {
      AasField {
        name = "nosec_sc",
        semanticId = "iri:https://www.w3.org/2019/wot/security#NoSecurityScheme",
        type = refBy(nosec_sc),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "auto_sc",
        semanticId = "iri:https://www.w3.org/2019/wot/security#AutoSecurityScheme",
        type = refBy(auto_sc),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "basic_sc",
        semanticId = "iri:https://www.w3.org/2019/wot/security#BasicSecurityScheme",
        type = refBy(basic_sc),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "combo_sc",
        semanticId = "iri:https://www.w3.org/2019/wot/security#ComboSecurityScheme",
        type = refBy(combo_sc),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "apikey_sc",
        semanticId = "iri:https://www.w3.org/2019/wot/security#APIKeySecurityScheme",
        type = refBy(apikey_sc),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "psk_sc",
        semanticId = "iri:https://www.w3.org/2019/wot/security#PSKSecurityScheme",
        type = refBy(psk_sc),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "digest_sc",
        semanticId = "iri:https://www.w3.org/2019/wot/security#DigestSecurityScheme",
        type = refBy(digest_sc),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "bearer_sc",
        semanticId = "iri:https://www.w3.org/2019/wot/security#BearerSecurityScheme",
        type = refBy(bearer_sc),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "oauth2_sc",
        semanticId = "iri:https://www.w3.org/2019/wot/security#OAuth2SecurityScheme",
        type = refBy(oauth2_sc),
        minimumInstances = 0,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType nosec_sc = {
    name = "nosec_sc",
    semanticId = "iri:https://www.w3.org/2019/wot/security#NoSecurityScheme",
    fields = {
      AasField {
        name = "scheme",
        semanticId = "iri:https://www.w3.org/2019/wot/security#SecurityScheme",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"nosec"}
      }
    }
  };

  AasSubmodelElementCollectionType auto_sc = {
    name = "auto_sc",
    semanticId = "iri:https://www.w3.org/2019/wot/security#AutoSecurityScheme",
    fields = {
      AasField {
        name = "scheme",
        semanticId = "iri:https://www.w3.org/2019/wot/security#SecurityScheme",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"auto"}
      },
      AasField {
        name = "proxy",
        semanticId = "iri:https://www.w3.org/2019/wot/security#proxy",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType basic_sc = {
    name = "basic_sc",
    semanticId = "iri:https://www.w3.org/2019/wot/security#BasicSecurityScheme",
    fields = {
      AasField {
        name = "scheme",
        semanticId = "iri:https://www.w3.org/2019/wot/security#SecurityScheme",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"basic"}
      },
      AasField {
        name = "name",
        semanticId = "iri:https://www.w3.org/2019/wot/security#name",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "in",
        semanticId = "iri:https://www.w3.org/2019/wot/security#in",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "proxy",
        semanticId = "iri:https://www.w3.org/2019/wot/security#proxy",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType combo_sc = {
    name = "combo_sc",
    semanticId = "iri:https://www.w3.org/2019/wot/security#ComboSecurityScheme",
    fields = {
      AasField {
        name = "scheme",
        semanticId = "iri:https://www.w3.org/2019/wot/security#SecurityScheme",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"combo"}
      },
      AasField {
        name = "oneOf",
        semanticId = "iri:https://www.w3.org/2019/wot/security#oneOf",
        type = refBy(oneOf),
        minimumInstances = 1,
        maximumInstances = 1
      },
      AasField {
        name = "allOf",
        semanticId = "iri:https://www.w3.org/2019/wot/security#allOf",
        type = refBy(allOf),
        minimumInstances = 1,
        maximumInstances = 1
      },
      AasField {
        name = "proxy",
        semanticId = "iri:https://www.w3.org/2019/wot/security#proxy",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementListType oneOf = {
    name = "oneOf",
    semanticId = "iri:https://www.w3.org/2019/wot/security#oneOf"
  };

  AasSubmodelElementListType allOf = {
    name = "allOf",
    semanticId = "iri:https://www.w3.org/2019/wot/security#allOf"
  };

  AasSubmodelElementCollectionType apikey_sc = {
    name = "apikey_sc",
    semanticId = "iri:https://www.w3.org/2019/wot/security#APIKeySecurityScheme",
    fields = {
      AasField {
        name = "scheme",
        semanticId = "iri:https://www.w3.org/2019/wot/security#SecurityScheme",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"apikey"}
      },
      AasField {
        name = "name",
        semanticId = "iri:https://www.w3.org/2019/wot/security#name",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "in",
        semanticId = "iri:https://www.w3.org/2019/wot/security#in",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "proxy",
        semanticId = "iri:https://www.w3.org/2019/wot/security#proxy",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType psk_sc = {
    name = "psk_sc",
    semanticId = "iri:https://www.w3.org/2019/wot/security#PSKSecurityScheme",
    fields = {
      AasField {
        name = "scheme",
        semanticId = "iri:https://www.w3.org/2019/wot/security#SecurityScheme",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"psk"}
      },
      AasField {
        name = "identity",
        semanticId = "iri:https://www.w3.org/2019/wot/security#identity",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "proxy",
        semanticId = "iri:https://www.w3.org/2019/wot/security#proxy",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType digest_sc = {
    name = "digest_sc",
    semanticId = "iri:https://www.w3.org/2019/wot/security#DigestSecurityScheme",
    fields = {
      AasField {
        name = "scheme",
        semanticId = "iri:https://www.w3.org/2019/wot/security#SecurityScheme",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"digest"}
      },
      AasField {
        name = "name",
        semanticId = "iri:https://www.w3.org/2019/wot/security#name",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "in",
        semanticId = "iri:https://www.w3.org/2019/wot/security#in",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "qop",
        semanticId = "iri:https://www.w3.org/2019/wot/security#qop",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "proxy",
        semanticId = "iri:https://www.w3.org/2019/wot/security#proxy",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType bearer_sc = {
    name = "bearer_sc",
    semanticId = "iri:https://www.w3.org/2019/wot/security#BearerSecurityScheme",
    fields = {
      AasField {
        name = "scheme",
        semanticId = "iri:https://www.w3.org/2019/wot/security#SecurityScheme",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"bearer"}
      },
      AasField {
        name = "name",
        semanticId = "iri:https://www.w3.org/2019/wot/security#name",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "in",
        semanticId = "iri:https://www.w3.org/2019/wot/security#in",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "authorization",
        semanticId = "iri:https://www.w3.org/2019/wot/security#authorization",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "alg",
        semanticId = "iri:https://www.w3.org/2019/wot/security#alg",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "format",
        semanticId = "iri:https://www.w3.org/2019/wot/security#format",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "proxy",
        semanticId = "iri:https://www.w3.org/2019/wot/security#proxy",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType oauth2_sc = {
    name = "oauth2_sc",
    semanticId = "iri:https://www.w3.org/2019/wot/security#OAuth2SecurityScheme",
    fields = {
      AasField {
        name = "scheme",
        semanticId = "iri:https://www.w3.org/2019/wot/security#SecurityScheme",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"oauth2"}
      },
      AasField {
        name = "token",
        semanticId = "iri:https://www.w3.org/2019/wot/security#token",
        type = refBy(AasAnyURIType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "refresh",
        semanticId = "iri:https://www.w3.org/2019/wot/security#refresh",
        type = refBy(AasAnyURIType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "authorization",
        semanticId = "iri:https://www.w3.org/2019/wot/security#authorization",
        type = refBy(AasAnyURIType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "scopes",
        semanticId = "iri:https://www.w3.org/2019/wot/security#scopes",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "flow",
        semanticId = "iri:https://www.w3.org/2019/wot/security#flow",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"code"}
      },
      AasField {
        name = "proxy",
        semanticId = "iri:https://www.w3.org/2019/wot/security#proxy",
        type = refBy(AasAnyURIType),
        minimumInstances = 0,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType InteractionMetadata = {
    name = "InteractionMetadata",
    semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/InteractionMetadata",
    fields = {
      AasField {
        name = "properties",
        semanticId = "iri:https://www.w3.org/2019/wot/td#PropertyAffordance",
        type = refBy(properties),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "actions",
        semanticId = "iri:https://www.w3.org/2019/wot/td#ActionAffordance",
        type = refBy(actions),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "events",
        semanticId = "iri:https://www.w3.org/2019/wot/td#EventAffordance",
        type = refBy(events),
        minimumInstances = 0,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType properties = {
    name = "properties",
    semanticId = "iri:https://www.w3.org/2019/wot/td#PropertyAffordance",
    fields = {
      AasField {
        name = "PropertyDefinition",
        displayName = "{property_name}",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfaceDescription/1/0/PropertyDefinition",
        type = refBy(property_name),
        minimumInstances = 0,
        description = "Current counter value."
      }
    }
  };

  AasSubmodelElementCollectionType property_name = {
    name = "{property_name}",
    semanticId = "iri:https://admin-shell.io/idta/AssetInterfaceDescription/1/0/PropertyDefinition",
    description = "Current counter value.",
    fields = {
      AasField {
        name = "key",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/key",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "type",
        semanticId = "iri:https://www.w3.org/1999/02/22-rdf-syntax-ns#type",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"float"}
      },
      AasField {
        name = "title",
        semanticId = "iri:https://www.w3.org/2019/wot/td#title",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "observable",
        semanticId = "iri:https://www.w3.org/2019/wot/td#isObservable",
        type = refBy(BooleanType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "const",
        semanticId = "iri:https://www.w3.org/2019/wot/json-schema#const",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "default",
        semanticId = "iri:https://www.w3.org/2019/wot/json-schema#default",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "unit",
        semanticId = "iri:https://schema.org/unitCode",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "min_max",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/minMaxRange",
        type = refBy(AasRangeType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "lengthRange",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/lengthRange",
        type = refBy(AasRangeType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "items",
        semanticId = "iri:https://www.w3.org/2019/wot/json-schema#items",
        type = refBy(items),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "itemsRange",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/itemsRange",
        type = refBy(AasRangeType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "properties",
        semanticId = "iri:https://www.w3.org/2019/wot/json-schema#properties",
        type = refBy(properties),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "valueSemantics",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/valueSemantics",
        type = refBy(AasReferenceType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "forms",
        semanticId = "iri:https://www.w3.org/2019/wot/td#hasForm",
        type = refBy(forms)
      }
    }
  };

  AasSubmodelElementCollectionType items = {
    name = "items",
    semanticId = "iri:https://www.w3.org/2019/wot/json-schema#items",
    fields = {
      AasField {
        name = "type",
        semanticId = "iri:https://www.w3.org/1999/02/22-rdf-syntax-ns#type",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "unit",
        semanticId = "iri:https://schema.org/unitCode",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "default",
        semanticId = "iri:https://www.w3.org/2019/wot/json-schema#default",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "const",
        semanticId = "iri:https://www.w3.org/2019/wot/json-schema#const",
        type = refBy(IntegerType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "observable",
        semanticId = "iri:https://www.w3.org/2019/wot/td#isObservable",
        type = refBy(BooleanType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "title",
        semanticId = "iri:https://www.w3.org/2019/wot/td#title",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "min_max",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/minMaxRange",
        type = refBy(AasRangeType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "lengthRange",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/lengthRange",
        type = refBy(AasRangeType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "valueSemantics",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/valueSemantics",
        type = refBy(AasReferenceType),
        minimumInstances = 0,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType forms = {
    name = "forms",
    semanticId = "iri:https://www.w3.org/2019/wot/td#hasForm",
    fields = {
      AasField {
        name = "href",
        semanticId = "iri:https://www.w3.org/2019/wot/hypermedia#hasTarget",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1,
        examples = {"/sampleDevice/properties/voltage"}
      },
      AasField {
        name = "contentType",
        semanticId = "iri:https://www.w3.org/2019/wot/hypermedia#forContentType",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"application/json"}
      },
      AasField {
        name = "security",
        semanticId = "iri:https://www.w3.org/2019/wot/td#hasSecurityConfiguration",
        type = refBy(security),
        minimumInstances = 1,
        maximumInstances = 1
      },
      AasField {
        name = "htv_methodName",
        semanticId = "iri:https://www.w3.org/2011/http#methodName",
        type = refBy(StringType),
        minimumInstances = 0,
        maximumInstances = 1,
        examples = {"GET"}
      },
      AasField {
        name = "htv_headers",
        semanticId = "iri:https://www.w3.org/2011/http#headers",
        type = refBy(htv_headers),
        minimumInstances = 0,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementListType htv_headers = {
    name = "htv_headers",
    semanticId = "iri:https://www.w3.org/2011/http#headers",
    fields = {
      AasField {
        name = "htv_headers",
        semanticId = "iri:https://www.w3.org/2011/http#headers",
        type = refBy(htv_headers),
        minimumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType actions = {
    name = "actions",
    semanticId = "iri:https://www.w3.org/2019/wot/td#ActionAffordance"
  };

  AasSubmodelElementCollectionType events = {
    name = "events",
    semanticId = "iri:https://www.w3.org/2019/wot/td#EventAffordance"
  };

  AasSubmodelElementCollectionType ExternalDescriptor = {
    name = "ExternalDescriptor",
    semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/ExternalDescriptor",
    fields = {
      AasField {
        name = "fileName",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/externalDescriptorName",
        type = refBy(AasFileResourceType),
        examples = {"File path value must not be empty"}
      }
    }
  };

  AasSubmodelElementCollectionType InterfaceTemplateForMODBUS = {
    name = "InterfaceTemplateForMODBUS",
    semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/Interface",
    fields = {
      AasField {
        name = "title",
        semanticId = "iri:https://www.w3.org/2019/wot/td#title",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1
      },
      AasField {
        name = "created",
        semanticId = "iri:http://purl.org/dc/terms/created",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "modified",
        semanticId = "iri:http://purl.org/dc/terms/modified",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "support",
        semanticId = "iri:https://www.w3.org/2019/wot/td#support",
        type = refBy(AasAnyURIType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "EndpointMetadata",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/EndpointMetadata",
        type = refBy(EndpointMetadata),
        minimumInstances = 1,
        maximumInstances = 1
      },
      AasField {
        name = "InteractionMetadata",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/InteractionMetadata",
        type = refBy(InteractionMetadata),
        minimumInstances = 1,
        maximumInstances = 1
      },
      AasField {
        name = "ExternalDescriptor",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/ExternalDescriptor",
        type = refBy(ExternalDescriptor),
        minimumInstances = 0,
        maximumInstances = 1
      }
    }
  };

  AasSubmodelElementCollectionType InterfaceTemplateForMQTT = {
    name = "InterfaceTemplateForMQTT",
    semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/Interface",
    fields = {
      AasField {
        name = "title",
        semanticId = "iri:https://www.w3.org/2019/wot/td#title",
        type = refBy(StringType),
        minimumInstances = 1,
        maximumInstances = 1
      },
      AasField {
        name = "created",
        semanticId = "iri:http://purl.org/dc/terms/created",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "modified",
        semanticId = "iri:http://purl.org/dc/terms/modified",
        type = refBy(DateTimeType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "support",
        semanticId = "iri:https://www.w3.org/2019/wot/td#support",
        type = refBy(AasAnyURIType),
        minimumInstances = 0,
        maximumInstances = 1
      },
      AasField {
        name = "EndpointMetadata",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/EndpointMetadata",
        type = refBy(EndpointMetadata),
        minimumInstances = 1,
        maximumInstances = 1
      },
      AasField {
        name = "InteractionMetadata",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/InteractionMetadata",
        type = refBy(InteractionMetadata),
        minimumInstances = 1,
        maximumInstances = 1
      },
      AasField {
        name = "ExternalDescriptor",
        semanticId = "iri:https://admin-shell.io/idta/AssetInterfacesDescription/1/0/ExternalDescriptor",
        type = refBy(ExternalDescriptor),
        minimumInstances = 0,
        maximumInstances = 1
      }
    }
  };

  freeze {
    .;
  } but (f|f.bindingTime >= BindingTime.runtimeMon);
}
