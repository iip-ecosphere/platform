import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { InputVariable, platformResponse } from 'src/interfaces';
import { EnvConfigService } from './env-config.service';

@Injectable({
  providedIn: 'root'
})
export class DrawflowService {

  ip: string = "";
  urn: string = "";

  constructor(private http: HttpClient, private envConfigService: EnvConfigService) {
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

  public async getGraph(mesh: string) {
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
        value: mesh
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
      response = await firstValueFrom(this.http.post(this.ip + '/shells/' + this.urn + "/aas/submodels/Configuration/submodel/submodelElements/getGraph/invoke"
      ,{"inputArguments": input,"requestId":"1bfeaa30-1512-407a-b8bb-f343ecfa28cf", "inoutputArguments":[], "timeout":10000})) as platformResponse;
    } catch(e) {
      console.log(e);
    }
    return response;
  }

  public async  getServices() {
    let response;
    try {
      response = await firstValueFrom(this.http.get(this.ip + '/shells/' + this.urn + "/aas/submodels/Configuration/submodel/submodelElements/Service"));
      console.log(response);
    } catch(e) {
      console.log(e);
    }
    return response;
  }

  public async getServiceMeshes() {
    let response;
    try {
      response = await firstValueFrom(this.http.get(this.ip + '/shells/' + this.urn + "/aas/submodels/Configuration/submodel/submodelElements/ServiceMesh"));
      console.log(response);
    } catch(e) {
      console.log(e);
    }
    return response;
  }

}
