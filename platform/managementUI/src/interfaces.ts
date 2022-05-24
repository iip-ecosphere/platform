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
