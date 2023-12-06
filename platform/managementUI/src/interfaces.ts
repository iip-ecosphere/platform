//import { SemanticId } from './interfaces';

export interface PlatformResources {
  submodelElements?: Resource[]
}

export interface Resource {
  allowDuplicates?: boolean;
  idShort?: string;
  identification?: any;
  kind?: string;
  valueType?: string;
  value?: ResourceAttribute[];

  generalInformation?: GeneralInformation; //for displaying pictures on resource view
}

//this is either an Attribute or a Function since both are stored in the same Array
export interface ResourceAttribute {
  idShort?: string;
  kind?: string;
  valueType?: string;
  value?: any;
  description?: any;
  inoutputVariables?: any;
  inputVariables?: InputVariable[];
  outputVariables?: any;
  invokable?: any;
  semanticId?: SemanticId;
  semanticName?: any;
  semanticDescription?: any;
}

//The submodelElements are either basyx functions (i.e. deploy plan) or collections (i.e. KnownServices)
export interface PlatformArtifacts {
  submodelElements?: Artifact[]
}
export interface Artifact {
  idShort?: string;
  kind?: string;
  value?: ResourceAttribute[];
  description?: any;
  invokable?: any;
  inputVariables?: any;

}

export interface InputVariable {
  modelType?: any;
  value?: {
    idShort?: string;
    kind?: string;
    valueType?: string;
    value?: string;
    modelType?: any;
  }
}

export interface outputArgument {
  value?: {
    idShort?: string;
    kind?: string;
    value?: string;
  }
}

export interface PlatformServices {
  submodelElements?: ServiceSubmodelElement[]
}

export interface ServiceSubmodelElement {
  idShort?: string;
  kind?: string;
  value?: ServiceValue[];
  description?: any;
}

export interface ServiceValue {
  idShort?: string;
  kind?: string;
  valueType?: string;
  value?: any[];
  description?: any;
  inoutputVariables?: any;
  invokable?: any;
}

export interface ServiceValueEntry {
  idShort?: string;
  kind?: string;
  valueType?: string;
  value?: any[];
  description?: any;
  inoutputVariables?: any;
  invokable?: any;
}

export interface PlatformData {
  value?: any[];
}

export interface DeviceSubmodel {
  submodelElements: {
    GeneralInformation: {
      value: {
        ManufacturerLogo: {
          mimeType: string;
          value: string;
        }
      }
    }
  }
}

export interface platformResponse {
  executionState: string;
  requestId: string;
  outputArguments: outputArgument[];
}


export interface buildInformation {
  version: string | undefined;
  buildId: string | undefined;
  isRelease: boolean | undefined;
}

export interface TechnicalDataResponse {
  semanticId?: SemanticId;
  idShort?: string;
  kind?: string;
  dataSpecification?: any[];
  submodelElements: TechnicalDataElement[];
}

export interface TechnicalDataElement {
  ordered?: boolean;
  semanticId?: SemanticId;
  idShort: string;
  kind: string;
  modelType: {
    name: string;
  }
  value: TechnicalDataValue[]
}

export interface TechnicalDataValue {
  parent: any;
  semanticId: SemanticId;
  idShort: string;
  kind: string;
  valueType: string;
  modelType: any;
  value: string;
}

export interface TechnicalDataValueValue {
  semanticId: SemanticId;
  idShort: string;
  kind?: string;
  valueType?: string;
  modelType?: {
    name: string;
  }
  value: string;
}

export interface SemanticId {
  keys: [{
    idType: string,
    type: string,
    value: string,
    local: boolean
  }]
}

export interface GeneralInformation {
  resourceIdShort?: string; //id of the resource
  picture?: string; //encoded picture
  ManufacturerName?: string;
  ManufacturerProductDesignation?: string;
  Address?: any;
}

export interface AddressPart {
  idShort?: string;
  value?: [{
    language: string;
    text: string;
  }]
}

export interface statusCollection {
  taskId: string;
  isFinished: boolean;
  isSuccesful: boolean; //should only be false if error message was recieved
  messages: statusMessage[];
}

export interface statusMessage {
  action: string;
  aliasIds: string[];
  componentType: string;
  description: string;
  deviceId: string;
  id: string;
  progress: number;
  result?: string;
  subDescription: string;
  taskId: string;

}
export interface uiGroup {
  uiGroup: number;
  inputs: editorInput[];
  fullLineInputs: editorInput[];
  optionalInputs: editorInput[];
  fullLineOptionalInputs: editorInput[];
  toggleOptional?: boolean;
}

/**
 * Data structure representing the input to AAS-based editors, sub-editors and editor components.
 */
export interface editorInput {
  /**
   * Value to be displayed by the editor. May be a primitive value (string, boolean), an object or an array of objects.
   */
  value: any;
  /**
   * Optional language of the value if the value is an AasLocalizedString. 
   */
  valueLang?: string;

  valueTransform?: (x: editorInput) => any;

  /**
   * The (resolved) IVML type of the value from the model.
   */
  type: string;
  /**
   * The IVML variable name as declared in the model.
   */
  name: string;
  /**
   * Description of the IVML variable given in terms of an AAS LangString.
   */
  description: [{language: string, text: string}];
  /**
   * Does the variable/value represent an IVML reference. Then the value/name is the resolved IVML variable.
   */
  refTo?: boolean;
  /**
   * Shall the editor represent multiple selectable/modifiable inputs.
   */
  multipleInputs?: boolean;
  /**
   * The optional metaTypeKind, a categorization of the IVML type as provided by the platform. One of the MTK_constants 
   * exported from this unit.
   */
  metaTypeKind?: number;
  /**
   * The associated AAS meta/type entry.
   */
  meta?: configMetaEntry;
}

export interface configMetaContainer {
  modelType: {name: string};
  dataSpecifications?: any[];
  embeddedSpecifications?: any[];
  kind: string;
  ordered: true;
  allowDuplicates: boolean;
  idShort: string;
  parent: {keys:  {type: string, local: boolean, value: string, idType: string}[]};
  value: configMeta[];
}

export interface configMeta {
  modelType: {name: string};
  dataSpecifications?: any[];
  embeddedSpecifications?: any[];
  kind: string;
  ordered: true;
  allowDuplicates: boolean;
  idShort: string;
  parent: {keys:  {type: string, local: boolean, value: string, idType: string}[]};
  value: configMetaEntry[];
}

export interface configMetaEntry { //name, type, uiGroup
  modelType: {name: string};
  dataSpecifications?: any[];
  embeddedDataSprecifications?: any[];
  kind: string;
  value: any;
  valueType?: string;
  idShort: string;
  description?: [{ language: string, text: string }]
  parent?: {keys:  {type: string, local: boolean, value: string, idType: string}[]};
}

export interface ivmlTemplate {
  [key: string]: any
}

// from platform, IVML mapper
export const MTK_primitive = 1;
export const MTK_enum = 2;
export const MTK_container = 3;
export const MTK_constraint = 4;
export const MTK_derived = 9;
export const MTK_compound = 10;

// from platform, IVML mapper
export const MT_metaState = 'metaState';
export const MT_metaProject = 'metaProject';
export const MT_metaSize = 'metaSize';
export const MT_metaType = 'metaType';
export const MT_metaRefines = 'metaRefines';
export const MT_metaAbstract = 'metaAbstract';
export const MT_metaTypeKind = 'metaTypeKind';
export const MT_metaDefault = 'metaDefault';
export const MT_metaVariable = 'metaVariable';
export const MT_varValue = 'varValue';

export const metaTypes = [MT_metaState, MT_metaProject,
  MT_metaSize, MT_metaType, MT_metaRefines, MT_metaAbstract, MT_metaTypeKind]; // TODO same as allMetaTypes???

export const allMetaTypes = [MT_metaState, MT_metaProject,
    MT_metaSize, MT_metaType, MT_metaRefines, MT_metaAbstract, MT_metaTypeKind, MT_metaDefault, MT_metaVariable];
  
export const primitiveDataTypes
  = ["String", "Boolean", "Real", "Integer"]

export const ivmlEnumeration = "IvmlEnumeration:";  