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

/**
 * Return result of an AAS operation call.
 */
export interface outputArgument {
  value?: {
    idShort?: string;
    kind?: string;
    value?: string;
  }
}

/**
 * Value usually returned by an AAS platform operation call.
 */
export interface JsonPlatformOperationResult {
  result?: string;
  exception?: string;
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

// -------------  collection of status messages -----------------

export interface statusCollection {
  taskId: string;
  isFinished: boolean;
  isSuccesful: boolean; //should only be false if error message was recieved
  messages: statusMessage[];
}

// ------------------- status message --------------------------

/**
 * StatusMessage: A device/component was added/created.
 */
export const ST_ADDED = "ADDED";

/**
 * StatusMessage: A device/component was changed.
 */
export const ST_CHANGED = "CHANGED";

/**
 * StatusMessage: A device/component was removed.
 */
export const ST_REMOVED = "REMOVED";

/**
 * Something task is being processed for a longer time (progress display indicated, usually with 
 * taskId and progress).
 */
export const ST_PROCESS = "PROCESS";

/**
 * Result of an execution/ST_PROCESS usually with taskId.
 */
export const ST_RESULT = "RESULT";

/**
 * Result of an execution/ST_PROCESS usually with taskId.
 */
export const ST_ERROR = "ERROR";

/**
 * UI internal status message created/received.
 */
export const ST_RECEIVED = "Recieved"; // typo taken over from intial implementation

/**
 * Status message from the platform.
 */
export interface statusMessage {
  /**
   * The (ids of the) action being carried out (ST_* constants).
   */
  action: string;
  /**
   * Type of component where the message originated from.
   */
  componentType: string;
  /**
   * Description in case of a progress action (action == progress).
   */
  description: string;
  /**
   * Subordinate, detailing description in case of a progress action (action == progress).
   */
  subDescription: string;
  /**
   * Optional id of the device where the message originated at.
   */
  deviceId: string;
  /**
   * Component id where the message originated at.
   */
  id: string;
  /**
   * Optional aliases for the id whereh the message originated at.
   */
  aliasIds: string[];
  /**
   * Unique id if the related execution was started as a task. Status messages with the same 
   * taskId belong to the same execution until there is a result.
   */
  taskId: string;
  /**
   * Optional percentage of completion, not given if negative, else if given [0-100]. 
   * With action==PROGRESS, usually with given taskId.
   */
  progress: number;
  /**
   * If a taskId is given, the result at the end of the execution, usually in JSON.
   */
  result?: string;
}

// ------------------- UI gropus, grouping in editors --------------------------

export interface uiGroup {
  uiGroup: number;
  inputs: editorInput[];
  fullLineInputs: editorInput[];
  optionalInputs: editorInput[];
  fullLineOptionalInputs: editorInput[];
  toggleOptional?: boolean;
}

// ------------------- editor input data structure --------------------------

/**
 * Data structure representing the input to AAS-based editors, sub-editors and editor components.
 */
export interface editorInput {
  /**
   * Value to be displayed by the editor. May be a primitive value (string, boolean), an object or an array of objects.
   */
  value: any;
  /**
   * Default value from the meta model to be displayed instead of value.
   */
  defaultValue?: any;
  /**
   * Optional language of the value if the value is an AasLocalizedString. 
   */
  valueLang?: string;
  /**
   * Optional function, may transform value.
   * 
   * @param the actual editor input 
   * @returns the actual value
   */
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
   * Optional display name overriding name, but only on UI.
   */
  displayName?: string;
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

export type IvmlValue = {
  value: any;
  _type: string;
}

export interface IvmlRecordValue {
  [key: string]: IvmlValue;
};

//export type IvmlRecordValue = Record<string, any>;

/**
 * Represents user feedback created by a service operation to be displayed on the UI.
 */
export interface UserFeedback {
  /**
   * The feedback text.
   */
  feedback: string;

  /**
   * Successful or failed.
   */
  successful: boolean;
}

// ------------------- IVML (meta) AAS constants --------------------------

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
export const MT_metaDisplayName = 'metaDisplayName';
export const MT_varValue = 'varValue';

export const metaTypes = [MT_metaState, MT_metaProject,
  MT_metaSize, MT_metaType, MT_metaRefines, MT_metaAbstract, MT_metaTypeKind]; // TODO same as allMetaTypes???

export const allMetaTypes = [MT_metaState, MT_metaProject,
    MT_metaSize, MT_metaType, MT_metaRefines, MT_metaAbstract, MT_metaTypeKind, MT_metaDefault, MT_metaVariable, MT_metaDisplayName];
  
export const IVML_TYPE_String = "String";
export const IVML_TYPE_Boolean = "Boolean";
export const IVML_TYPE_Real = "Real";
export const IVML_TYPE_Integer = "Integer";
export const primitiveDataTypes = [IVML_TYPE_String, IVML_TYPE_Boolean, IVML_TYPE_Real, IVML_TYPE_Integer];

export const IVML_TYPE_PREFIX_enumeration = "IvmlEnumeration:";  

/*
* On data rows/entries, indicate the actual IVML type.
*/
export const DR_type ="_type";

export const DR_displayName ="_displayName";

/*
* On data rows/entries, indicate the identification/name of the entry.
*/
export const DR_idShort ="idShort";

/**
 * Default file upload chunk from user computer via AAS to platform.
 */
export const DEFAULT_UPLOAD_CHUNK = 1024 * 1024;
export const DEFAULT_AAS_OPERATION_TIMEOUT = 10000;
