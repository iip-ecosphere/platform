import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { PlatformResources, PlatformServices } from 'src/interfaces';
import { environment } from 'src/environments/environment';
import { firstValueFrom } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  constructor(public http: HttpClient) { }

  ip = environment.ip;
  urn = environment.urn;

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
