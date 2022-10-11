

export interface PlatformResources {
  submodelElements?: ResourceSubmodelElement[]
}

export interface ResourceSubmodelElement {
  idShort?: string;
  kind?: string;
  value?: ResourceValue[];
  description?: any;
  invokable?: any;
  inputVariables?: any; //for deploymentPlans, deploymentPlan will need its own interface to avoid confusion

}

//to be replaced by ResourceAttribute
export interface ResourceValue {
  idShort?: string;
  kind?: string;
  valueType?: string;
  value?: any;
  description?: any;
  inoutputVariables?: any;
  inputVariables?: any;
  outputVariables?: any;
  invokable?: any;
}

export interface Resource {
  allowDuplicates: boolean;
  idShort?: string;
  identification?: any;
  kind?: string;
  valueType?: string;
  value?: ResourceAttribute[];
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


