import { Injectable } from '@angular/core';
import  * as environment  from '../../assets/config/config.json';


export enum Environment {
  Prod = 'prod',
  Staging = 'staging',
  Test = 'test',
  Dev = 'dev',
  Local = 'local',
}

interface Configuration {
  ip: string;
  urn?: string;
  stage?: Environment;
}

@Injectable({
  providedIn: 'root'
})
export class EnvConfigService {

  private readonly configUrl = 'assets/config/config.json';

    constructor() {}

    private env: Configuration | undefined;

    public load(): Configuration {
      console.log('loading ip');
      this.env = environment;
      console.log(environment);
      return this.env;
    }

    public getEnv() {
      if(!this.env) {
        return this.load();
      } else {
        return this.env;
      }
    }
}
