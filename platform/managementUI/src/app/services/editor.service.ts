import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { EnvConfigService } from './env-config.service';

@Injectable({
  providedIn: 'root'
})
export class EditorService {

  ip: string = "";
  urn: string = "";

  dependencies: any;

  constructor(public http: HttpClient, private envConfigService: EnvConfigService) {
    const env = this.envConfigService.getEnv();
    //the ip and urn are taken from the json.config
    if(env && env.ip) {
      this.ip = env.ip;
    }
    if (env && env.urn) {
      this.urn = env.urn;
    }
   }

   private async getData(url: string) {
    let Data;
    try {
      Data = await firstValueFrom(this.http.get( this.ip + '/shells/' + this.urn + '/' + url));
    } catch(e) {
      console.log(e);
    }
    return Data;
  }

  public async getApplications() {
    const response = await this.getData('aas/submodels/Configuration/submodel/submodelElements/Application');
    return response;
   }

   public async getDependencies() {
    const response = await this.getData('aas/submodels/Configuration/submodel/submodelElements/Dependency');
    return response;
   }

   public async getServers() {
    const response = await this.getData('aas/submodels/Configuration/submodel/submodelElements/Server');
    return response;
   }

    public async getManufacturers() {
    const response = await this.getData('aas/submodels/Configuration/submodel/submodelElements/Manufacturer');
    return response;
   }

   public async getServiceMeshes() {
    const response = await this.getData('aas/submodels/Configuration/submodel/submodelElements/ServiceMesh');
    return response;
   }

   public async getDataTypess() {
    const response = await this.getData('aas/submodels/Configuration/submodel/submodelElements/DataType');
    return response;
   }

   public async getEcsDevices() {
    const response = await this.getData('aas/submodels/Configuration/submodel/submodelElements/EcsDevice');
    return response;
   }

      public async getServiceBases() {
    const response = await this.getData('aas/submodels/Configuration/submodel/submodelElements/ServiceBase');
    return response;
   }

   public async getMeshElements() {
    const response = await this.getData('aas/submodels/Configuration/submodel/submodelElements/MeshElement');
    return response;
   }

   public async getMeshConnectors() {
    const response = await this.getData('aas/submodels/Configuration/submodel/submodelElements/MeshConnector');
    return response;
   }
}
