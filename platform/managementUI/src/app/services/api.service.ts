import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { PlatformArtifacts, PlatformResources, PlatformServices, ResourceAttribute, InputVariable } from 'src/interfaces';
import { firstValueFrom, Subject } from 'rxjs';
import { EnvConfigService } from './env-config.service';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  ip: string = "";
  urn: string = "";

  errorMsg : any;

  errorEmitter: Subject<HttpErrorResponse>;

  constructor(public http: HttpClient, private envConfigService: EnvConfigService) {
    this.errorEmitter = new Subject();
    const env = this.envConfigService.getEnv();
    //the ip and urn are taken from the json.config
    if(env && env.ip) {
      this.ip = env.ip;
    }
    if (env && env.urn) {
      this.urn = env.urn;
    }
    console.log( "ip: " + this.ip)
   }

  resources: PlatformResources = {};
  //services: PlatformServices = {};
  connectionTypes: any;

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

  private async getData(url: string) {
    let Data;
    try {
      Data = await firstValueFrom(this.http.get( this.ip + '/shells/' + this.urn + '/' + url));
    } catch(e) {
      console.log(e);
      this.errorEmitter.next(e as HttpErrorResponse);
    }
    return Data;
  }

  public async executeFunction(resource: string, basyxFunc: string, params: any) {
    let response;
    try {
      response = await firstValueFrom(this.http.post(this.ip + '/shells/' + this.urn + "/aas/submodels/resources/submodel/submodelElements/" + resource + "/" + basyxFunc + "/invoke"
      ,{"inputArguments": params,"requestId":"1bfeaa30-1512-407a-b8bb-f343ecfa28cf", "inoutputArguments":[], "timeout":10000}));
    } catch(e) {
      console.log(e);
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
        value: ""
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
        value: ""
      }
    }]

    try {
      response = await firstValueFrom(this.http.post(this.ip + '/shells/' + this.urn + "/aas/submodels/Configuration/submodel/submodelElements/getGraph/invoke"
      ,{"inputArguments": input,"requestId":"1bfeaa30-1512-407a-b8bb-f343ecfa28cf", "inoutputArguments":[], "timeout":10000}));
    } catch(e) {
      console.log(e);
    }
    return response;
  }

  public async getResource(id: string) {
    if(!this.resources || !this.resources.submodelElements) {
      await this.getResources();
    }
    return this.resources.submodelElements?.find(resource => resource.idShort === id);
  }


}
