import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { PlatformArtifacts, PlatformResources, PlatformServices, ResourceAttribute, InputVariable, platformResponse, Resource, PlatformData, DEFAULT_AAS_OPERATION_TIMEOUT, JsonPlatformOperationResult, SubmodelElementCollection, allMetaTypes, MT_metaDisplayName, MT_metaTypeKind, MTK_container, MT_metaSize, DR_idShort, DR_type, MT_metaType, MTK_compound, MT_varValue, DR_displayName } from 'src/interfaces';
import { firstValueFrom, Subject } from 'rxjs';
import { Configuration, EnvConfigService } from './env-config.service';
import { DataUtils, UtilsService } from './utils.service';

@Injectable({
  providedIn: 'root'
})
export class ApiService extends UtilsService {


  errorMsg : any;

  errorEmitter: Subject<HttpErrorResponse>;

  constructor(public http: HttpClient, private envConfigService: EnvConfigService) {
    super();
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
      const Data = await this.getData('aas/submodels/services/submodel') as PlatformServices;
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
      if (Data) {
        this.meta = Data;
      }
    }
    return this.meta;
  }

  public async getSubmodelElement(submodel: any, submodelElement: any) {
    return await this.getData(`/aas/submodels/${submodel}/submodel/submodelElements/${submodelElement}`);
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
      modelType: {name: "OperationVariable"},
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
  public async executeAasJsonOperation(submodel: string, operationName: string, params: InputVariable[]) {
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
  public async executeAasJsonOperationWithTimeout(submodel: string, operationName: string, timeout: number, params: InputVariable[]) {
    let response;
    try {
      let cfg = await this.envConfigService.initAndGetCfg();
      response = await firstValueFrom(this.http.post<platformResponse>(
        ApiService.constructOperationCallUrl(cfg, submodel, operationName),
        ApiService.constructOperationCallBodyWithTimeout(timeout, params), 
        {responseType: 'json', reportProgress: true}));
    } catch(e) {
      console.error(e); // TODO create failing response?
    }
    return response;
  }

  /**
   * Constructs an operation call URL.
   * 
   * @param cfg the configuration from the configuration service
   * @param submodel the submodel defining the operation
   * @param operationName the name of the operation, may also be a sub-path within the submodel
   * @returns the URL
   */
  public static constructOperationCallUrl(cfg: Configuration | undefined, submodel: string, operationName: string) {
    return `${cfg?.ip}/shells/${cfg?.urn}/aas/submodels/${submodel}/submodel/${operationName}/invoke`;
  }

  /**
   * Constructs an operation call body with DEFAULT_AAS_OPERATION_TIMEOUT.
   * 
   * @param timeout the call timeout in ms
   * @param params the parameters for the operation
   * @returns the body
   */
  public static constructOperationCallBody(params: any) {
    return ApiService.constructOperationCallBodyWithTimeout(DEFAULT_AAS_OPERATION_TIMEOUT, params);
  }

  /**
   * Constructs an operation call body with given timeout.
   * 
   * @param timeout the call timeout in ms
   * @param params the parameters for the operation
   * @returns the body
   */
  public static constructOperationCallBodyWithTimeout(timeout: number, params: any) {
    return {"inputArguments": params,
        "requestId":"1bfeaa30-1512-407a-b8bb-f343ecfa28cf", // generate ???
        "inoutputArguments":[], "timeout":timeout}
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
      let tmp = response.outputArguments[0]?.value?.value; 
      if (tmp && this.isString(tmp)) {
        output = JSON.parse(tmp) as JsonPlatformOperationResult;
      } else {
        output = tmp as JsonPlatformOperationResult;
      }
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

  /**
   * Returns a configured service mesh as JSON.
   * 
   * @param mesh the IVML variable name of the mesh to return  
   * @param format the format, usually GRAPHFORMAT_DRAWFLOW
   * @returns a JsonPlatformOperationResult or null
   */
  public async getConfiguredServiceMeshGraph(mesh: string, format: string) {
    let input: InputVariable[] = [];
    input.push(ApiService.createAasOperationParameter("varName", AAS_TYPE_STRING, mesh));
    input.push(ApiService.createAasOperationParameter("format", AAS_TYPE_STRING, format));
    let response = await this.executeAasJsonOperation("Configuration", "getGraph", input); 
    return this.getPlatformResponse(response);
  }

  /**
   * Returns instances of configured elements.
   * 
   * @param typeName the IVML type name of the elements to return 
   * @returns the elements
   */
  public async getConfiguredElements(typeName: string) {
    return await this.getData(`aas/submodels/Configuration/submodel/submodelElements/${typeName}`) as SubmodelElementCollection;
  }

  /**
   * Returns configured services.
   * 
   * @returns configured services (of type ServiceBase)
   */
  public async getConfiguredServices() {
    return await this.getConfiguredElements('ServiceBase');
  }

  /**
   * Returns configured service meshes.
   * 
   * @returns configured service meshes (of type ServiceMesh)
   */
  public async getConfiguredServiceMeshes() {
    return await this.getConfiguredElements('ServiceMesh');
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

  /**
   * Recursive function to turn a single AAS JSON data row into an internal data structure. Considers (recursive) IVML collection sub-structures. 
   * Takes metaType, metaSize and varValue from the platform generated AAS entries into account.
   * 
   * @param values the row values as AAS JSON 
   * @param result to accumulate the result of this row, a RowEntry on top level, a array on nested level
   * @param top is this call a top level call or a nested recursive call
   * @param rowFn additional function to apply on row data to extract further data
   */
  createRowValue(values: any, result: any, top:boolean, rowFn: (row:any) => boolean) {
    for (let value of values) {
      let fieldName = value.idShort;
      let goOn = rowFn(value);
      if (goOn && !allMetaTypes.includes(fieldName)) {
        let displayName = null;
        let val: any = null;
        if (this.isArray(value.value)) {
          displayName = DataUtils.getPropertyValue(value.value, MT_metaDisplayName);
          let fieldTypeKind = DataUtils.getPropertyValue(value.value, MT_metaTypeKind);
          if (fieldTypeKind == MTK_container) {
            let fieldSize = DataUtils.getPropertyValue(value.value, MT_metaSize);
            if (fieldSize) {
              val = [];
              for (let i = 0; i < fieldSize; i++) {
                let fVal : any = {}; // TODO not if contained type is primitive
                let fName = fieldName + "__" + i + "_";
                let fProp = DataUtils.getProperty(value.value, fName);
                if (!fProp) {
                  fName = "var_" + i;
                  fProp = DataUtils.getProperty(value.value, fName);
                }
                if (fProp && fProp.value) {
                  this.createRowValue(fProp.value, fVal, false, v => true);
                  if (!fVal[DR_idShort]) { // if already set from else below, don't overwrite
                    let fId = fVal["id"] || fVal["name"] || fVal["type"] || String(i);
                    fVal[DR_idShort] = fId;
                  }
                  this.addPropertyFromData(fVal, DR_type, fProp.value, MT_metaType);
                  val.push(fVal);
                }
              }
            }
          } else if (fieldTypeKind == MTK_compound) {
            val = {};
            this.createRowValue(value.value, val, false, v => true);
          } else {
            val = DataUtils.getPropertyValue(value.value, MT_varValue);
          }
        } else {
          result.idShort = value.value;
          result.value = value.value;
          fieldName = null; // prevent adding varValue as field below
          this.addPropertyFromData(result, DR_type, values, MT_metaType);
        }
        if (top) {
          let instance: any = {idShort: fieldName, value:val};
          if (displayName) {
            instance[DR_displayName] = displayName;
          }
          result.push(instance);
        } else if (fieldName) {
          result[fieldName] = val;
        }
      }
    }
  }

  private addPropertyFromData(object: any, propertyName: string, data: any[], dataPropertyName: string) {
    let value = DataUtils.getPropertyValue(data, dataPropertyName);
    if (value) {
      object[propertyName] = value;
    }
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

export const IDSHORT_SUBMODEL_PLATFORM = "platform";
export const IDSHORT_OPERATION_PLATFORM_RESOLVESEMANTICID = "resolveSemanticId";

export const IDSHORT_SUBMODEL_CONFIGURATION = "Configuration";

export const GRAPHFORMAT_DRAWFLOW = "drawflow";

export enum ArtifactKind {
  SERVICE_ARTIFACT = "SERVICE_ARTIFACT",
  CONTAINER = "CONTAINER",
  DEPLOYMENT_PLAN = "DEPLOYMENT_PLAN",
  IMPLEMENTATION_ARTIFACT = "IMPLEMENTATION_ARTIFACT"
}
