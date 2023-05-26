import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, firstValueFrom, interval, Observable, Subject, Subscription }
  from 'rxjs';
import { PlatformResources, platformResponse, Resource, StatusMsg } from 'src/interfaces';
import { OnlyIdPipe } from '../pipes/only-id.pipe';
import { EnvConfigService } from './env-config.service';

@Injectable({
  providedIn: 'root'
})



export class PlanDeployerService {

  ip: string = "";
  urn: string = "";

  sub: Subscription | undefined;

  statusSubmodel: any;

  status: StatusMsg = {
    executionState: "",
    messages: [""]
  }

  emitter: BehaviorSubject<StatusMsg>;
  allEmitter: Subject<Resource[]>


  constructor(private http: HttpClient,
    private envConfigService: EnvConfigService,
    private onlyId: OnlyIdPipe) {
    this.emitter = new BehaviorSubject(this.status);
    this.allEmitter = new Subject();
    //this.getDetailedStatus();
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
      response = await firstValueFrom(this.http.post<platformResponse>(
        this.ip
        + '/shells/'
        + this.urn
        + "/aas/submodels/Artifacts/submodel/"
        + basyxFunc
        + "/invoke"
      ,{"inputArguments": params,
      "requestId":"1bfeaa30-1512-407a-b8bb-f343ecfa28cf",
      "inoutputArguments":[], "timeout":10000}
      , {responseType: 'json', reportProgress: true}));
    } catch(e) {
      console.log(e);
    }

    if(response && response.outputArguments[0]
      && response.outputArguments[0].value) {
      this.getStatus(response.outputArguments[0].value.value);
      console.log(response.outputArguments[0].value.value);
    }
    return response;
  }

  public async undeployPlanById(params: any) {
    let response;
    this.emitSendMessage("undeployId");
    try {
      response = await firstValueFrom(this.http.post<platformResponse>(
        this.ip
        + '/shells/'
        + this.urn
        + "/aas/submodels/Artifacts/submodel/undeployPlanWithId/invoke"
      ,{"inputArguments": params,
        "requestId":"1bfeaa30-1512-407a-b8bb-f343ecfa28cf",
        "inoutputArguments":[], "timeout":10000}
      , {responseType: 'json'}));
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
          kind: "Template",
          valueType: "string",
          modelType: {
            name: "Property"
          },
          value: ""
        }
    }];
    params[0].value.value = this.onlyId.transform(id);
      try {
          response = this.http.post<platformResponse>(
            this.ip
            + '/shells/'
            + this.urn
            + "/aas/submodels/Artifacts/submodel/getTaskStatus/invoke"
          ,{"inputArguments":
            params,"requestId":"1bfeaa30-1512-407a-b8bb-f343ecfa28cf",
            "inoutputArguments":[], "timeout":10000}
          , {responseType: 'json', reportProgress: true});
          console.log(response);
          this.sub = response.subscribe((status: any ) => {
            console.log(status);
            if(status.outputArguments[0].value
              && status.outputArguments[0].value.value
              && this.status.messages
              && status.executionState) {
              this.status.executionState = status.executionState;
              this.status.messages[0] = this.onlyId.transform(
                status.outputArguments[0].value.value);
              this.emitter.next(this.status);
            }
}, err => (this.emitter.next({
  executionState: "ERROR",
  messages: ["an error occured while getting the task status"]})));

    } catch(e) {
      console.log(e);
    }
  }

  public async getDetailedStatus() {

    let subscription: Subscription = new Subscription();

    subscription = interval(4000).subscribe(
      (val) => { this.getStatusSubmodel(); console.log("tick")});

  }

  private async getStatusSubmodel() {
    let response;
    try {
      response = await firstValueFrom(this.http.get(
        this.ip
        + '/shells/'
        + this.urn
        + "/aas/submodels/Status/submodel")) as PlatformResources;
      if(response) {
        this.statusSubmodel = response.submodelElements;
        this.allEmitter.next(this.statusSubmodel);
      }
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
