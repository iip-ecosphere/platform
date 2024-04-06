AAS ProcessEquipmentAssemblyExample
 ASSET ci INSTANCE
 SUBMODEL ProcessEquipmentAssembly (semanticId: iri:https://admin-shell.io/vdi/2658/1/0/PEASubmodel)
  MLP Description (semanticId: iri:https://admin-shell.io/vdi/2658/1/0/PEASubmodel/Description)
  MLP DisplayName (semanticId: iri:https://admin-shell.io/vdi/2658/1/0/peaSubmodel/DisplayName)
  FILE MTPFile (semanticId: iri:https://admin-shell.io/vdi/2658/1/0/MTPSUCLib/ModuleTypePackage) length 21
  SMC DocumentationReferences (semanticId: iri:https://admin-shell.io/vdi/2658/1/0/MTPReferences)
   RELATIONSHIP generic01 (semanticId: iri:https://admin-shell.io/vdi/2658/1/0/MTPReference)
  SMC SourceList (semanticId: iri:https://admin-shell.io/vdi/2658/1/0/MTPSUCLib/CommunicationSet/SourceList)
   SMC generic01 (semanticId: iri:https://admin-shell.io/vdi/2658/1/0/MTPCommunicationSUCLib/ServerAssembly/OPCUAServer)
    PROPERTY ApplicationUri01 = urn:org.com:PEA1:UA Server (semanticId: iri:https://admin-shell.io/idta/opcua-server-datasheet/1/0/application-uri)
    PROPERTY DiscoveryUrl01 = opc.tcp://localhost:4800 (semanticId: iri:https://admin-shell.io/idta/opcua-server-datasheet/1/0/discovery-url)