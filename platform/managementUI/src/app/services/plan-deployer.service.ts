import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, firstValueFrom, Observable, Subscription } from 'rxjs';
import { platformResponse, StatusMsg } from 'src/interfaces';
import { OnlyIdPipe } from '../pipes/only-id.pipe';
import { EnvConfigService } from './env-config.service';

@Injectable({
  providedIn: 'root'
})



export class PlanDeployerService {

  ip: string = "";
  urn: string = "";

  sub: Subscription | undefined;

  status: StatusMsg = {
    executionState: "",
    messages: [""]
  }
  isDone = false;

  emitter: BehaviorSubject<StatusMsg>;


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
      this.emitSendMessage("undeploy");
    } else {
      basyxFunc = "deployPlanAsync";
      this.emitSendMessage("deploy");
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

  public async undeployPlanById(params: any) {
    let response;
    try {
      response = await firstValueFrom(this.http.post<platformResponse>(this.ip + '/shells/' + this.urn + "/aas/submodels/Artifacts/submodel/undeployPlanWithId/invoke"
      ,{"inputArguments": params,"requestId":"1bfeaa30-1512-407a-b8bb-f343ecfa28cf", "inoutputArguments":[], "timeout":10000}
      , {responseType: 'json', reportProgress: true}));
      this.emitSendMessage("undeployId");
    } catch(e) {
      console.log(e);
    }
    const message = response?.outputArguments[0]?.value?.value;
    const execState = response?.executionState;
    if(message && execState) {
      this.emitter.next({executionState: execState, messages: [message]});
    }

    return response;
  }

  public getStatus(id: string | undefined) {
    let response: Observable<platformResponse> = new Observable<platformResponse>();
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
          console.log(response);
          this.sub = response.subscribe((status: any )=> {
            console.log(status);
            if(status.outputArguments[0].value && status.outputArguments[0].value.value && this.status.messages && status.executionState) {
              this.status.executionState = status.executionState;
              this.status.messages[0] = status.outputArguments[0].value.value;
              this.emitter.next(this.status);
            }
}, err => (this.emitter.next({executionState: "ERROR", messages: ["an error occured while getting the task status"]})));

    } catch(e) {

      console.log(e);
    }
  }

  private emitSendMessage(msg?: string) {
    let state = "request sent";
    if(msg) {
      state = state.concat(" (" + msg + ")");
    }
    this.emitter.next( {
      executionState: state,
      messages: ["waiting for response"]
    })
  }

}
