import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, firstValueFrom, Subscription } from 'rxjs';
import { platformResponse } from 'src/interfaces';
import { OnlyIdPipe } from '../pipes/only-id.pipe';
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
  isDone = false;

  emitter: BehaviorSubject<{ executionState: string,messages: string[]}>;


  constructor(private http: HttpClient, private envConfigService: EnvConfigService, private onlyId: OnlyIdPipe) {
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
      basyxFunc = "undeployPlanAsync";
    } else {
      basyxFunc = "deployPlanAsync";
      this.emitter.next( {
        executionState: "deployment plan sent",
        messages: ["waiting for response"]
      })
    }
    try {
      response = await firstValueFrom(this.http.post<platformResponse>(this.ip + '/shells/' + this.urn + "/aas/submodels/Artifacts/submodel/" + basyxFunc + "/invoke"
      ,{"inputArguments": params,"requestId":"1bfeaa30-1512-407a-b8bb-f343ecfa28cf", "inoutputArguments":[], "timeout":10000}
      , {responseType: 'json', reportProgress: true}));
    } catch(e) {
      console.log(e);
    }
    console.log(response);
    // this.sub = response?.subscribe((dep: platformResponse) => {
    //   //replace this with saving of taskId from the asynchronous response and use getTaskStatus to refresh the status in a set time interval
    //   this.status.executionState = dep.executionState;
    //   console.log(dep);
    //   for(const message of dep.outputArguments) {
    //     if(message.value) {
    //       this.status.messages.push(message.value.value as string);
    //     }
    //   }
    //   this.emitter.next(this.status);
    // });
    // if (this.status.executionState = "") {

    // }
    if(response && response.outputArguments[0] && response.outputArguments[0].value) {
      this.getStatus(response.outputArguments[0].value.value);
    }
  }

  public getStatus(id: string | undefined) {
    let response;
    let params = [
      {
        modelType: {
          name: "operationsVariable"
        },
        value: {
          idShort: "taskId",
          kind: "template",
          valueType: "string",
          modelType: {
            name: "Property"
          },
          value: ""
        }
    }];

    params[0].value.value = this.onlyId.transform(id);

      try {
      response = this.http.post<platformResponse>(this.ip + '/shells/' + this.urn + "/aas/submodels/Artifacts/submodel/getTaskStatus/invoke"
      ,{"inputArguments": params,"requestId":"1bfeaa30-1512-407a-b8bb-f343ecfa28cf", "inoutputArguments":[], "timeout":10000}
      , {responseType: 'json', reportProgress: true});
      this.sub = response.subscribe((status: platformResponse )=> {
        if(status.outputArguments[0].value && status.outputArguments[0].value.value) {
          this.status.executionState = status.executionState;
          this.status.messages[0] = status.outputArguments[0].value.value;
          this.emitter.next(this.status);
        }
        console.log(status);
      });
    } catch(e) {
      console.log(e);
    }
  }

}
