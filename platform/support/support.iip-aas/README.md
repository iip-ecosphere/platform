# Plattform functions on top of the AAS Component in the Support Layer of the IIP-Ecosphere platform

Specific IIP-Ecosphere support function realized on top of the Asset Administration Shell (AAS) abstraction:
* Constructing an AAS from different components via JSL
* Generic AAS client classes for conveniently using AAS from Java
* Network manager AAS sub-model and client
* Platform (nameplate) AAS sub-model
* Mapping Java classes into AAS (types sub-model)
* Component configuration/setup support
* JSON functions for the platform including a format for result and exception
* URI resolution support
* semanticId resolution support (generically based on JSL descriptors, here for Eclass IRDI fragment and asset-shell IRI fragment in terms of two Yaml-based resolvers, catalog files are in src/main/resources)

**Catalog format**

Local catalog format for semanticId resolution:

    definitions:
      - semanticId: <String>
        version: <String>
        revision: <String>
        naming:
          de: 
            name: <String>
            structuredName: <String>
            description: <String>
          en:
            name: <String>
            structuredName: <String>
            description: <String>
      - semanticId: <String>
        ...

`semanticId` specifies the id to be resolved. `version` and `revision` is information that could be extracted from the semanticId but given here for generality. In naming, any number of languages (here `de` and `en`) can be listed with the name of the defined concept, the structured name and a description in the respective language. Further, `publisher` and `kind` (IRI, IRDI, ...) may be given or implicitly set as default values by the resolver implementation.

**Missing**
- IVML type mapping
