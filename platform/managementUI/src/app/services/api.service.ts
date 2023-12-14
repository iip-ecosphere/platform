import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { PlatformArtifacts, PlatformResources, PlatformServices, ResourceAttribute, InputVariable, platformResponse, Resource, PlatformData } from 'src/interfaces';
import { firstValueFrom, Subject } from 'rxjs';
import { EnvConfigService } from './env-config.service';

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
