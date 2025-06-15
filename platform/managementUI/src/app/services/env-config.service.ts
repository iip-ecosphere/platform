import { Injectable } from '@angular/core';
import { Routes, Route } from '@angular/router';

export type { Configuration };
declare var window: any;

export enum Environment {
  Prod = 'prod',
  Staging = 'staging',
  Test = 'test',
  Dev = 'dev',
  Local = 'local',
}

export enum MetaModelVersion {
  v2 = 'v2',
  v3 = 'v3',
}

interface Configuration {
  ip: string;
  smIp?: string;
  urn?: string;
  stage?: Environment;
  inTest?: boolean;
  requireAuthentication?: boolean;
  metaModelVersion?: MetaModelVersion;
  httpTimeout? : number;
  enableDebug? : boolean;
  enableInfo? : boolean;
  enableLog? : boolean;
  enableWarn? : boolean;
}

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
        ).catch(e => {});
        if (cfg != undefined && EnvConfigService.env == undefined) {
          EnvConfigService.env = cfg;
          console.log("Loading setup from " + location + ": " + JSON.stringify(cfg));
          window.config = cfg;
        }
      }
    }

    public static async init() {
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

    public getCfg() {
      return EnvConfigService.env;      
    }

    public getHttpTimeout() : number {
      return EnvConfigService.env?.httpTimeout || 1000;
    }

    public getRequireAuthentication() : boolean {
      return EnvConfigService.env?.requireAuthentication || false;
    }

    public getMetaModelVersion() : MetaModelVersion {
      return EnvConfigService.env?.metaModelVersion || MetaModelVersion.v2;
    }

    public async initAndGetCfg() {
      await EnvConfigService.init(); // only if needed
      return EnvConfigService.env;
    }

    public static ifAuth(routes: Routes, login: Route): Routes {
      if (EnvConfigService.env?.requireAuthentication) {
        for (let r of routes) {
          if (r.path == '') {
            r.redirectTo = login.path;
          }
        }
        routes.push(login);
      }
      return routes;
    }

}
