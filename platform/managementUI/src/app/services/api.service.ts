import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { PlatformArtifacts, PlatformResources, PlatformServices, ResourceAttribute, InputVariable, platformResponse, Resource, PlatformData, DEFAULT_AAS_OPERATION_TIMEOUT, JsonPlatformOperationResult } from 'src/interfaces';
import { firstValueFrom, Subject } from 'rxjs';
import { EnvConfigService } from './env-config.service';
import { DataUtils } from './utils.service';

@Injectable({
  providedIn: 'root'
})
export class ApiService {


  errorMsg : any;

  errorEmitter: Subject<HttpErrorResponse>;

  constructor(public http: HttpClient, private envConfigService: EnvConfigService) {
    this.errorEmitter = new Subject();
  }

  resources: PlatformResources = {};
  //services: PlatformServices = {};
  meta: Resource = {};

  public async getResources() {
    this.resources = await this.getData('aas/submodels/resources/submodel') as PlatformResources;
    return this.resources;
  }

  public async getServices() {
      const Data = await this.getData('aas/submodels/services/submodel')as PlatformServices;
      return Data;
  }

  public async getArtifacts() {
    const Data = await this.getData('aas/submodels/Artifacts/submodel') as PlatformArtifacts;
    return Data;
  }

  public async getPlatformData() {
    const Data = await this.getData('aas/submodels/platform/submodel/submodelElements') as ResourceAttribute[];
    return Data;
  }

  public async getInstances() {
    const Data = await this.getData('aas/submodels/ApplicationInstances/submodel/submodelElements') as Resource[];
    return Data;
  }

  public async getMeta() {
    if(!this.meta || !this.meta.value) {
      const Data = await this.getData('aas/submodels/Configuration/submodel/submodelElements/meta') as Resource;
      if(Data) {
        this.meta = Data;
      }
    }
    return this.meta;
  }

  public async getData(url: string) {
    let Data;
    try {
      let cfg = await this.envConfigService.initAndGetCfg();
      Data = await firstValueFrom(
        this.http.get(cfg?.ip + '/shells/' + cfg?.urn + '/' + url));
    } catch(e) {
      console.error(e);
      this.errorEmitter.next(e as HttpErrorResponse);
    }
    return Data;
  }

  // TODO unify with executeAasJsonOperation
  public async executeFunction(resourceId: string,
    aasElementURL:string, basyxFunc: string, params: any) {
    /*
    console.log("api: " + this.ip + '/shells/'
      + this.urn
      + aasElementURL
      + resourceId + "/"
      + basyxFunc + "/invoke")
      */
    let response;
    try {
      let cfg = await this.envConfigService.initAndGetCfg();
      response = await firstValueFrom(this.http.post(
        cfg?.ip
        + '/shells/'
        + cfg?.urn
        + aasElementURL
        + resourceId + "/"
        + basyxFunc + "/invoke"
      ,{"inputArguments": params,
      "requestId":"1bfeaa30-1512-407a-b8bb-f343ecfa28cf",
      "inoutputArguments":[], "timeout":10000}));
    } catch(e) {
      console.error(e);
    }
    return response;
  }

/**
 * Creates an AAS operation input parameter.
 * 
 * @param idShort the idShort of the parameter
 * @param aasType the AAS type of the parameter, see AAS_TYPE_STRING
 * @param value the value of the parameter
 * @returns the input parameter instance
 */
public static createAasOperationParameter(idShort: string, aasType: string, value: any) : InputVariable {
  let result : InputVariable = {
    value: {
      modelType: {
        name: "Property"
      },
      valueType: aasType,
      idShort: idShort,
      kind: "Template",
      value: value
    }
  }
  return result;
}

/**
 * Executes an AAS operation with default timeout DEFAULT_AAS_OPERATION_TIMEOUT (response type JSON, reporting progress).
 * 
 * @param submodel the submodel defining the operation
 * @param operationName the name of the operation, may also be a sub-path within the submodel
 * @param params the parameters for the operation
 * @returns the response as platformResponse
 */
public async executeAasJsonOperation(submodel: string, operationName: string, params: any) {
  return await this.executeAasJsonOperationWithTimeout(submodel, operationName, DEFAULT_AAS_OPERATION_TIMEOUT, params);
}

/**
 * Executes an AAS operation with given timeout (response type JSON, reporting progress).
 * 
 * @param submodel the submodel defining the operation
 * @param operationName the name of the operation, may also be a sub-path within the submodel
 * @param timeout the call timeout in ms
 * @param params the parameters for the operation
 * @returns the response as platformResponse
 */
public async executeAasJsonOperationWithTimeout(submodel: string, operationName: string, timeout: number, params: any) {
  let response;
  try {
    let cfg = await this.envConfigService.initAndGetCfg();
    response = await firstValueFrom(this.http.post<platformResponse>(
      `${cfg?.ip}/shells/${cfg?.urn}/aas/submodels/${submodel}/submodel/${operationName}/invoke`,
      {"inputArguments": params,
      "requestId":"1bfeaa30-1512-407a-b8bb-f343ecfa28cf", // generate ???
      "inoutputArguments":[], "timeout":timeout}, 
      {responseType: 'json', 
      reportProgress: true}));
  } catch(e) {
    console.error(e); // TODO create failing response?
  }
  return response;
}

/**
 * Returns the value returned by a JSON platform operation call.
 * 
 * @param response the response returned from an execution function 
 * @returns the value as JsonPlatformOperationResult
 */
public getPlatformResponse(response:platformResponse | undefined): JsonPlatformOperationResult | null {
  let output : JsonPlatformOperationResult | null = null
  if (response && response.outputArguments) {
    output = response.outputArguments[0]?.value?.value as JsonPlatformOperationResult;
  }
  return output;
}

/**
 * Calls the platform file upload operation.
 * 
 * @param kind the kind of 
 * @param sequenceNr 
 * @param fileName 
 * @param data 
 * @returns 
 */
public async uploadFileAsArrayBuffer(kind: ArtifactKind, sequenceNr: number, fileName: string, data: ArrayBuffer | null) {
  if (data) {
    let params: InputVariable[] = [
      ApiService.createAasOperationParameter("kind", AAS_TYPE_STRING, "" + kind),
      ApiService.createAasOperationParameter("sequenceNr", AAS_TYPE_INTEGER, sequenceNr),
      ApiService.createAasOperationParameter("name", AAS_TYPE_STRING, fileName),
      ApiService.createAasOperationParameter("data", AAS_TYPE_STRING, DataUtils.arrayBufferToBase64(data))
    ];
    return await this.executeAasJsonOperation(IDSHORT_SUBMODEL_ARTIFACTS, IDSHORT_OPERATION_ARTIFACTS_UPLOAD, params);
  } else {
    return Promise.resolve();
  }
}

  public async getGraph() {
    let response;
    let input: InputVariable[] = [{
      modelType: {name: "OperationVariable"},
      value: {
        idShort: "varName",
        kind: "Template",
        valueType: "string",
        modelType: {
          name: "Property"
        },
        value: "myMesh"
      }
    },
    {
      modelType: {name: "OperationVariable"},
      value: {
        idShort: "format",
        kind: "Template",
        valueType: "string",
        modelType: {
          name: "Property"
        },
        value: "drawflow"
      }
    }]

    try {
      let cfg = await this.envConfigService.initAndGetCfg();
      response = await firstValueFrom(this.http.post(cfg?.ip + '/shells/' + cfg?.urn + "/aas/submodels/Configuration/submodel/submodelElements/getGraph/invoke"
      ,{"inputArguments": input,"requestId":"1bfeaa30-1512-407a-b8bb-f343ecfa28cf", "inoutputArguments":[], "timeout":10000})) as platformResponse;
    } catch(e) {
      console.error(e);
    }
    return response;
  }

  public async getResource(id: string) {
    if(!this.resources || !this.resources.submodelElements) {
      await this.getResources();
    }
    return this.resources.submodelElements?.find(resource => resource.idShort === id);
  }

  /**
   * Returns the platform WS status URI.
   * 
   * @returns the platform WS status URI
   */
  public async getStatusUri() {
    const url = "/aas/submodels/Status/submodel/submodelElements/status/uri"
    let resp = await this.getData(url) as PlatformData
    let statusUri: any = ""
    if (resp) {
      statusUri = resp.value
    }
    return statusUri;
  }

}

export const AAS_TYPE_STRING = "string";
export const AAS_TYPE_INTEGER = "integer";

export const AAS_OP_PREFIX_SME = "submodelElements/";

export const IDSHORT_SUBMODEL_ARTIFACTS = "Artifacts";
export const IDSHORT_OPERATION_ARTIFACTS_UNDEPLOYPLANASYNC = "undeployPlanAsync";
export const IDSHORT_OPERATION_ARTIFACTS_DEPLOYPLANASYNC = "deployPlanAsync";
export const IDSHORT_OPERATION_ARTIFACTS_DEPLOYPLANWITHIDASYNC = "undeployPlanWithIdAsync";
export const IDSHORT_OPERATION_ARTIFACTS_UPLOAD = "upload";

export const IDSHORT_SUBMODEL_CONFIGURATION = "Configuration";

export enum ArtifactKind {
  SERVICE_ARTIFACT = "SERVICE_ARTIFACT",
  CONTAINER = "CONTAINER",
  DEPLOYMENT_PLAN = "DEPLOYMENT_PLAN",
  IMPLEMENTATION_ARTIFACT = "IMPLEMENTATION_ARTIFACT"
}
