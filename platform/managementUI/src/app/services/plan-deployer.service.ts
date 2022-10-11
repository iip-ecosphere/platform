import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Subscription } from 'rxjs';
import { platformResponse } from 'src/interfaces';
import { EnvConfigService } from './env-config.service';

@Injectable({
  providedIn: 'root'
})
export class PlanDeployerService {

  ip: string = "";
  urn: string = "";

  sub: Subscription | undefined;

  status = {
    executionState: "",
    messages: [""]
  }

  emitter: BehaviorSubject<{ executionState: string,messages: string[]}>;


  constructor(private http: HttpClient, private envConfigService: EnvConfigService) {
    this.emitter = new BehaviorSubject(this.status);
    const env = this.envConfigService.getEnv();
    //the ip and urn are taken from the json.config
    if(env && env.ip) {
      this.ip = env.ip;
    }
    if (env && env.urn) {
      this.urn = env.urn;
    }
   }

  public async deployPlan(params: any, undeploy?: boolean) {
    this.status = {
      executionState: "",
      messages: [""]
    }

    let response;
    let basyxFunc;
    if (undeploy) {
      basyxFunc = "undeployPlan";
    } else {
      basyxFunc = "deployPlan";
      this.emitter.next( {
        executionState: "deployment plan sent",
        messages: ["waiting for response"]
      })
    }
    try {
      response = await this.http.post<platformResponse>(this.ip + '/shells/' + this.urn + "/aas/submodels/Artifacts/submodel/" + basyxFunc + "/invoke"
      ,{"inputArguments": params,"requestId":"1bfeaa30-1512-407a-b8bb-f343ecfa28cf", "inoutputArguments":[], "timeout":10000}
      , {responseType: 'json', reportProgress: true});
    } catch(e) {
      console.log(e);
    }
    this.sub = response?.subscribe((dep: platformResponse) => {
      this.status.executionState = dep.executionState;
      let i = 0;
      console.log(dep);
      for(const message of dep.outputArguments) {
        if(message.value) {
          this.status.messages.push(message.value.value as string);
        }
        i++;
      }
      this.emitter.next(this.status);
    });
    if (this.status.executionState = "") {

    }
    return response;
  }

}
