
export interface PlatformResources {
  submodelElements?: Resource[]
}

//to be replaced by ResourceAttribute
// export interface ResourceValue {
//   idShort?: string;
//   kind?: string;
//   valueType?: string;
//   value?: any;
//   description?: any;
//   inoutputVariables?: any;
//   inputVariables?: any;
//   outputVariables?: any;
//   invokable?: any;
// }

export interface Resource {
  allowDuplicates?: boolean;
  idShort?: string;
  identification?: any;
  kind?: string;
  valueType?: string;
  value?: ResourceAttribute[];

  pic?: string; //for displaying pictures on resource view
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
  keys: [
    idType: string,
    type: string,
    value: string,
    local: boolean
  ]
}

export interface ResourceProductPicture {
  idShort: string; //id of the resource
  picture: string; //encoded picture
}
