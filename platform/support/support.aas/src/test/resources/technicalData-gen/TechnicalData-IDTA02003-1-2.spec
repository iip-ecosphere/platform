AAS TechnicalDataExample
 ASSET ci INSTANCE
 SUBMODEL TechnicalData (semanticId: iri:https://admin-shell.io/ZVEI/TechnicalData/Submodel/1/2)
  SMC FurtherInformation (semanticId: iri:https://admin-shell.io/ZVEI/TechnicalData/FurtherInformation/1/1)
   MLP TextStatement01 (semanticId: iri:https://admin-shell.io/ZVEI/TechnicalData/TextStatement/1/1)
   PROPERTY ValidDate = 2024/01/01 00:01:00 (semanticId: iri:https://admin-shell.io/ZVEI/TechnicalData/ValidDate/1/1)
  SMC GeneralInformation (semanticId: iri:https://admin-shell.io/ZVEI/TechnicalData/GeneralInformation/1/1)
   PROPERTY ManufacturerArticleNumber = A123-456 (semanticId: irdi:0173-1#02-AAO676#003)
   FILE ManufacturerLogo (semanticId: iri:https://admin-shell.io/ZVEI/TechnicalData/ManufacturerLogo/1/1) length 28
   PROPERTY ManufacturerName = Example Company (semanticId: irdi:0173-1#02-AAO677#002)
   PROPERTY ManufacturerOrderCode = EEA-EX-200-S/47-Q3 (semanticId: irdi:0173-1#02-AAO227#002)
   MLP ManufacturerProductDesignation (semanticId: irdi:0173-1#02-AAW338#001)
   FILE ProductImage01 (semanticId: iri:https://admin-shell.io/ZVEI/TechnicalData/ProductImage/1/1) length 36
  SMC ProductClassifications (semanticId: iri:https://admin-shell.io/ZVEI/TechnicalData/ProductClassifications/1/1)
   SMC ProductClassificationItem01 (semanticId: iri:https://admin-shell.io/ZVEI/TechnicalData/ProductClassificationItem/1/1)
    PROPERTY ClassificationSystemVersion = 9.0 (BASIC) (semanticId: iri:https://admin-shell.io/ZVEI/TechnicalData/ClassificationSystemVersion/1/1)
    PROPERTY ProductClassId = 27-01-88-77 (semanticId: iri:https://admin-shell.io/ZVEI/TechnicalData/ProductClassId/1/1)
    PROPERTY ProductClassificationSystem = ECLASS (semanticId: iri:https://admin-shell.io/ZVEI/TechnicalData/ProductClassificationSystem/1/1)
  SMC TechnicalProperties (semanticId: iri:https://admin-shell.io/ZVEI/TechnicalData/TechnicalProperties/1/1)
   SMC MainSection01
   SMC SubSection01
   PROPERTY generic01 = Length (semanticId: iri:https://admin-shell.io/SemanticIdNotAvailable/1/1)