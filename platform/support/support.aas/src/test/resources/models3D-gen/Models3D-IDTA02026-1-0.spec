AAS Models3DExample
 ASSET ci INSTANCE
 SUBMODEL Models3D (semanticId: iri:https://admin-shell.io/idta/Models3D/1/0)
  SMC Model3D (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/1/0)
   SMC Capability (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/1/0)
    PROPERTY EmbeddedInfo = Reference Points (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/EmbeddedInfo/1/0)
    PROPERTY NegModelPurpose = Rendering (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/NegModelPurpos/1/0)
    PROPERTY ObjectType = Assembly (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/ObjectType/1/0)
    PROPERTY Origin = Modeling (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/Origin/1/0)
    PROPERTY PosModelPurpose = FDM Simulation (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/PosModelPurpose/1/0)
    SMC Simplification (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/Simplification/1/0)
     REFERENCE DerivedFrom -> true (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/Simplification/DerivedFrom/1/0)
     PROPERTY Description = In comparison to the 3D model this 3D model was derived from, all screw joints were removed. (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/Simplification/LevelDescription/1/0)
     PROPERTY ReducedElements = Screw Joints (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/Simplification/ReducedElements/1/0)
    PROPERTY State = Manufacturing in (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Capability/State/1/0)
   SMC File (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/1/0)
    SMC ConsumingApplication (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/ConsumingApplication/1/0)
     SMC Api_3 (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/ConsumingApplication/Api/1/0)
      PROPERTY ApiDocumentationUrl = https://learn.openapis.org/ (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File//ConsumingApplication/Api/ApiDocumentationUrl/1/0)
      PROPERTY ApiSpecificationUrl = https://github.com/OAI/OpenA PI- (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/ConsumingApplication/Api/ApiSpecificationUrl/1/0)
      PROPERTY ApiVersion = OpenAPI Specification â€“ Version 3.0.1 (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/ConsumingApplication/Api/ApiVersion/1/0)
     PROPERTY ApplicationName = STEP (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/ConsumingApplication/ApplicationName/1/0)
     PROPERTY ApplicationQualifier = STEP-2.03 (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/ConsumingApplication/ApplicationQualifier1/0)
     PROPERTY ApplicationVersion = AP242 (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/ConsumingApplication/ApplicationVersion/1/0)
     SMC VendorOrganization (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/SourceApplication/VendorOrganization/1/0)
      PROPERTY OrganizationName = IDTA (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/SourceApplication/VendorOrganization/OrganizationName/1/0)
      PROPERTY OrganizationOfficialName = Industrial Digital Twin Association e. V. (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/SourceApplication/VendorOrganization/OrganizationOfficialName/1/0)
    SMC FileClassification (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileClassification/1/0)
     PROPERTY ClassId = 02-02 (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileClassification/ClassId/1/0)
     MLP ClassName (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileClassification/ClassName/1/0)
     PROPERTY ClassificationSystem = VDI2770:2020 (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/ClassificationSystem/1/0)
    SMC FileId (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileId/1/0)
     PROPERTY FileDomainId = TEST (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileId/FileDomaniId/1/0)
     PROPERTY IsPrimary = false (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileId/IsPrimary/1/0)
     PROPERTY ValueId = TEST (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileId/ValueId/1/0)
    SMC FileVersion (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/1/0)
     SMC BasedOn (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/BasedOn/1/0)
     FILE DigitalFile (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/DigitalFile/1) length 0
     SMC ExternalFile (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/ExternalFile/1/0)
      SMC Api (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/ExternalFile/Api/1/0)
       PROPERTY ApiDocumentationUrl = https://learn.openapis.org/ (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/ExternalFile/Api/ApiDocumentationUrl/1/0)
       PROPERTY ApiSpecificationUrl = https://github.com/OAI/OpenA PI- (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/ExternalFile/Api/ApiSpecificationUrl/1/0)
       PROPERTY ApiVersion = OpenAPI Specification â€“ Version 3.0.1 (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/ExternalFile/Api/ApiVersion/1/0)
      PROPERTY ExternalUrl = https://admin-shell- io.com/5001/ (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/ExternalFile/ExternalUrl/1/0)
      PROPERTY FileIdentifier = https://boschrexroth.com/ids/ aas?p=p652370&m=R90150 807&s=1201694127 (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/ExternalFile/FileIdentifier/1/0)
      SMC HostOrganization (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/ExternalFile/HostOrganization/1/0)
       PROPERTY OrganizationName = IDTA (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/ExternalFile/HostOrganization/OrganizationName1/0)
       PROPERTY OrganizationOfficialName = Industrial Digital Twin Association e. V. (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/ExternalFile/HostOrganization/OrganizationName1/0)
     SMC FileFormat (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/FileFormat/1/0)
      PROPERTY FormatName = STEP (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/FileFormat/FormatName/1/0)
      PROPERTY FormatQualifier = STEP-2.03 (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/FileFormat/FormatQualifier/1/0)
      PROPERTY FormatVersion = AP242 (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/FileFormat/FileVersion/1/0)
     PROPERTY FileName = example-name (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/FileName/1)
     PROPERTY FileVersionId = 4.2.1 (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/FileVersionID/1/0)
     FILE PreviewFile (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/PreviewFile) length 0
     SMC ProvidingOrganization (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/ProvidingOrganization/1/0)
      PROPERTY OrganizationName = IDTA (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/ProvidingOrganization/OrganizationName/1/0)
      PROPERTY OrganizationOfficialName = Industrial Digital Twin Association e. V. (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/ProvidingOrganization//OrganizationOfficialName/1/0)
     SMC RefersTo (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/RefersTo/1/0)
     PROPERTY SetDate = 2024/01/01 00:01:00 (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/SetDate/1/0)
     SMC SourceApplication (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/SourceApplication/1/0)
      SMC Api_2 (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/SourceApplication/Api/1/0)
       PROPERTY ApiDocumentationUrl = https://learn.openapis.org/ (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/SourceApplication/Api/ApiDocumentationUrl/1/0)
       PROPERTY ApiSpecificationUrl = https://github.com/OAI/OpenA PI- (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/SourceApplication/Api/ApiSpecificationUrl/1/0)
       PROPERTY ApiVersion = OpenAPI Specification â€“ Version 3.0.1 (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/SourceApplication/Api/ApiVersion/1/0)
      PROPERTY ApplicationName = STEP (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/SourceApplication/ApplicationName/1/0)
      PROPERTY ApplicationQualifier = STEP-2.03 (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/SourceApplication/ApplicationQualifier/1/0)
      PROPERTY ApplicationVersion = AP242 (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/SourceApplication/ApplicationVersion/1/0)
      SMC VendorOrganization (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/SourceApplication/VendorOrganization/1/0)
       PROPERTY OrganizationName = IDTA (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/SourceApplication/VendorOrganization/OrganizationName/1/0)
       PROPERTY OrganizationOfficialName = Industrial Digital Twin Association e. V. (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/SourceApplication/VendorOrganization/OrganizationOfficialName/1/0)
     PROPERTY StatusValue = Released (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/StatusValue/1/0)
     MLP Title (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/File/FileVersion/Title/1/0)
   SMC Geometry (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/1/0)
    SMC CartBoundingBox (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/1/0)
     PROPERTY BoundingBoxKind = MaxEnvelope (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/BoundingBoxKind/1/0)
     SMC CartBoundingVector (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/CartBoundingVector/1/0)
      PROPERTY X = 42.0 (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/CartBoundingVector/X/1/0)
      PROPERTY Y = 42.0 (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/CartBoundingVector/Y/1/0)
      PROPERTY Z = 42.0 (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/CartBoundingVector/Z/1/0)
     SMC CartRefSystem (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/CartRefSystem/1/0)
      SMC CartOffsetVector (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/CartRefSystem/CartOffsetVector/1/0)
       PROPERTY X = 42.0 (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/CartRefSystem/CartOffsetVector/X/1/0)
       PROPERTY Y = 42.0 (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/CartRefSystem/CartOffsetVector/Y/1/0)
       PROPERTY Z = 42.0 (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/CartRefSystem/CartOffsetVector/Z/1/0)
      SMC NormOrientationVector (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/CartRefSystem/NormOrientationVector/1/0)
       PROPERTY X = 42.0 (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/CartRefSystem/NormOrientationVector/X/1/0)
       PROPERTY Y = 42.0 (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/CartRefSystem/NormOrientationVector/Y/1/0)
       PROPERTY Z = 42.0 (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartBoundingBox/CartRefSystem/NormOrientationVector/Z/1/0)
    SMC CartRefSystem_2 (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartRefSystem/1/0)
     SMC CartOffsetVector_2 (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartRefSystem/CartOffsetVector/1/0)
      PROPERTY X = 42.0 (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartRefSystem/CartOffsetVector/X/1/0)
      PROPERTY Y = 42.0 (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartRefSystem/CartOffsetVector/Y/1/0)
      PROPERTY Z = 42.0 (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartRefSystem/CartOffsetVector/Z/1/0)
     SMC NormOrientationVector_2 (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartRefSystem/NormOrientationVector/1/0)
      PROPERTY X = 42.0 (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartRefSystem/NormOrientationVector/X/1/0)
      PROPERTY Y = 42.0 (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartRefSystem/NormOrientationVector/Y/1/0)
      PROPERTY Z = 42.0 (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/CartRefSystem/NormOrientationVector/Z/1/0)
    PROPERTY LengthUnit = mm (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/LengthUnit/1/0)
    PROPERTY Representation = Wire Frame (semanticId: iri:https://admin-shell.io/idta/Models3D/Model3D/Geometry/Representation/1/0)