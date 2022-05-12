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

    if(env && env.ip) {
      this.ip = env.ip;
    }
    if (env && env.urn) {
      this.urn = env.urn;
    }

   }



  resources: PlatformResources = {};
  services: any;
  connectionTypes: any;

  public async getResources() {
    const Data = await this.getData('aas/submodels/resources/submodel') as PlatformResources;
    return Data;
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

}
