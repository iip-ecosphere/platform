import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { PlatformResources, PlatformServices } from 'src/interfaces';
import { environment } from 'src/environments/environment';
import { firstValueFrom } from 'rxjs';
import { EnvConfigService } from './env-config.service';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  ip = environment.ip;
  urn = environment.urn;

  constructor(public http: HttpClient, private envConfigService: EnvConfigService) {
    const env = envConfigService.getEnv();

    //the ip and urn are taken from the json.config
    //if theconfig doesnt contain values, then the ip and urn are taken from the environment.ts
    if(env && env.ip) {
      this.ip = env.ip;
    }
    if (env && env.urn) {
      this.urn = env.urn;
    }

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
    const Data = await this.getData('aas/submodels/Artifacts/submodel') as PlatformResources;
    return Data;
  }

  private async getData(url: string) {
    let Data;
    try {
      Data = await firstValueFrom(this.http.get( this.ip + '/shells/' + this.urn + '/' + url));
      console.log(Data);
    } catch(e) {
      console.log(e);
    }
    return Data;
  }

  public async doThing() {
    let thing;
    try {
      thing = await firstValueFrom(this.http.post("http://192.168.0.199:9001/shells/urn%3A%3A%3AAAS%3A%3A%3AiipEcosphere%23/aas/submodels/resources/submodel/submodelElements/D43D7E502D25/addArtifact/invoke"
      , {"inputArguments":[{"value":{"modelType":{"name":"Property"},"idShort":"url","value":"file:///C:/Users/Chris/Desktop/Install/gen/artifacts/SimpleMeshTestingApp-0.1.0-SNAPSHOT-bin.jar","kind":"Template","valueType":"string"},"modelType":{"name":"OperationVariable"}}],"requestId":"1bfeaa30-1512-407a-b8bb-f343ecfa28cf","inoutputArguments":[],"timeout":10000}));
    } catch(e) {
      console.log(e);
    }
    console.log(thing);
  }

  public async executeFunction(resource: string, basyxFunc: string, params: any) {
    let response;
    try {
      response = await firstValueFrom(this.http.post(this.ip + '/shells/' + this.urn + "/aas/submodels/resources/submodel/submodelElements/" + resource + "/" + basyxFunc + "/invoke"
      ,{"inputArguments": params,"requestId":"1bfeaa30-1512-407a-b8bb-f343ecfa28cf", "inoutputArguments":[], "timeout":10000}));
    } catch(e) {
      console.log(e);
    }
  }

  public async getResource(id: string) {
    if(!this.resources) {
      this.getResources();
    }
    return this.resources.submodelElements?.find(resource => resource.idShort === id);
  }

}
