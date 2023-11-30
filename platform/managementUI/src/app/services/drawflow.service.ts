import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { InputVariable, platformResponse } from 'src/interfaces';
import { EnvConfigService } from './env-config.service';

@Injectable({
  providedIn: 'root'
})
export class DrawflowService {

  constructor(private http: HttpClient, private envConfigService: EnvConfigService) {
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

    console.debug("getGraph | input variable")
    console.debug(input)

    try {
      let cfg = await this.envConfigService.initAndGetCfg();
      response = await firstValueFrom(this.http.post(
        cfg?.ip
        + '/shells/'
        + cfg?.urn
        + "/aas/submodels/Configuration/submodel/submodelElements/getGraph/invoke"
      ,{"inputArguments": input,
      "requestId":"1bfeaa30-1512-407a-b8bb-f343ecfa28cf",
      "inoutputArguments":[], "timeout":10000})) as platformResponse;
    } catch(e) {
      console.error(e);
    }
    console.debug("response")
    console.debug(response)
    return response;
  }

  public async getServices() {
    let response;
    try {
      let cfg = await this.envConfigService.initAndGetCfg();
      response = await firstValueFrom(this.http.get(cfg?.ip + '/shells/' + cfg?.urn + "/aas/submodels/Configuration/submodel/submodelElements/ServiceBase"));
    } catch(e) {
      console.log(e);
    }
    return response;
  }

  public async getServiceMeshes() {
    let response;
    try {
      let cfg = await this.envConfigService.initAndGetCfg();
      response = await firstValueFrom(this.http.get(cfg?.ip + '/shells/' + cfg?.urn + "/aas/submodels/Configuration/submodel/submodelElements/ServiceMesh"));
    } catch(e) {
      console.log(e);
    }
    return response;
  }

}
