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

export interface editorInput {
  value: string[]; //editorInput[]
  type: string;
  name: string;
  description: [{language: string, text: string}];
  refTo?: boolean;
  multipleInputs?: boolean;
  metaTypeKind?: number;

}


