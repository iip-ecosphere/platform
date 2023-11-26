import { Injectable } from '@angular/core';
import { from, map } from 'rxjs';
import  * as environment  from '../../assets/config/config.json'; // TODO needed?
export type { Configuration };
declare var window: any;

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
  inTest?: boolean;
  enableDebug? : boolean;
  enableInfo? : boolean;
  enableLog? : boolean;
  enableWarn? : boolean;
}

export const primitiveDataTypes
  = ["String", "Boolean", "Real", "Integer"]

export const ivmlEnumeration = "IvmlEnumeration:";

@Injectable({
  providedIn: 'root'
})
export class EnvConfigService {

  private static readonly configLocations = ['../../test/tmp/config.json', '../../assets/config/config.json'];

    constructor() {}

    private static env: Configuration | undefined;

    private static async load() {
      for(let location of this.configLocations) {
        var cfg = await fetch(location).then(
          (response) => {
            if (response.ok) {
              return response.json();
            } else {
              return undefined;
            }
          }
        )
        if (cfg != undefined && EnvConfigService.env == undefined) {
          EnvConfigService.env = cfg;
          console.log("Loading setup from " + location + ": " + JSON.stringify(cfg));
          window.config = cfg;
        }
      }
    }

    public static async initAsync() {
      if (EnvConfigService.env == undefined) {
        await EnvConfigService.load();
        if (EnvConfigService.env) {
          let env = EnvConfigService.env as Configuration; 
          if (env.enableDebug == false) {
            window.console.debug = function() {};
          }
          if (env.enableInfo == false) {
            window.console.info = function() {};
          }
          if (env.enableLog == false) {
            window.console.log = function() {};
          }
          if (env.enableWarn == false) {
            window.console.warn = function() {};
          }
        }
      }
    }

    public init() {
      EnvConfigService.initAsync();      
    }

    public static getConfig() : Configuration | undefined {
      return EnvConfigService.env;
    }

    public static imp(importNoPlatform: any, importPlatform: any): any {
      return EnvConfigService.inPlatformTest() ? importPlatform : importNoPlatform;
    }

    public static inPlatformTest() : boolean {
      return EnvConfigService.env == undefined ? false : 
        EnvConfigService.env?.inTest == undefined ? false : EnvConfigService.env?.inTest;
    }

    public getEnv() {
      return window.config
    }
}
