import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { EnvConfigService } from './env-config.service';
import { Resource } from 'src/interfaces';

@Injectable({
  providedIn: 'root'
})
export class EditorService {

  dependencies: any;

  constructor(public http: HttpClient, private envConfigService: EnvConfigService) {
  }

  private async getData(url: string) {
    let Data;
    try {
      let cfg = await this.envConfigService.initAndGetCfg();
      Data = await firstValueFrom(this.http.get( cfg?.ip + '/shells/' + cfg?.urn + '/' + url));
    } catch(e) {
      console.log(e);
    }
    return Data;
  }

  public async getConfigurationType(type: string) {
    const response = await this.getData('/aas/submodels/Configuration/submodel/submodelElements/' + type) as Resource;
    return response;
  }

}
