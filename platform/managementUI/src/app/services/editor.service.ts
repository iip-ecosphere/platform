import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { EnvConfigService } from './env-config.service';
import { Resource } from 'src/interfaces';

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

  public async getConfigurationType(type: string) {
    console.log(type);
    if(type.substring(0,1).match(new RegExp('[a-z]'))) {
      let upperCaseLetter = type.substring(0,1).toUpperCase();
      type = upperCaseLetter + type.substring(1);
    }
    const response = await this.getData('/aas/submodels/Configuration/submodel/submodelElements/' + type) as Resource;
    return response;

  }
}
