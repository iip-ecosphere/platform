export interface PlatformResources {
  submodelElements?: SubmodelElement[]
}

export interface SubmodelElement {
  idShort?: string;
  kind?: string;
  value?: SubmodelElementValue[];
  description?: any;

}

export interface SubmodelElementValue {
  idShort?: string;
  kind?: string;
  valueType?: string;
  value?: any;
  description?: any;
  inoutputVariables?: any;
  invokable?: any;
}
