import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { firstValueFrom, Subscription } from 'rxjs';
import { platformResponse, statusCollection, statusMessage} from 'src/interfaces';
import { EnvConfigService } from './env-config.service';
import { webSocket, WebSocketSubject } from 'rxjs/webSocket';

@Injectable({
  providedIn: 'root'
})



export class PlanDeployerService {

  ip: string = "";
  urn: string = "";

  sub: Subscription | undefined;

  statusSubmodel: any;


  webSocket: WebSocketSubject<any>;
  public StatusCollection: statusCollection[] = [];



  constructor(private http: HttpClient,
    private envConfigService: EnvConfigService) {
    const env = this.envConfigService.getEnv();
    //the ip and urn are taken from the json.config
    if(env && env.ip) {
      this.ip = env.ip;
    }
    if (env && env.urn) {
      this.urn = env.urn;
    }

    let wsIp = this.ip;
    wsIp = wsIp.replace('http', 'ws');
    wsIp = wsIp.replace('https', 'wws');
    wsIp = wsIp.slice(0, wsIp.indexOf(":", wsIp.indexOf(":") + 1));
    wsIp = wsIp.concat(":10000/status");
    this.webSocket = webSocket(wsIp);
    this.webSocket.asObservable().subscribe(dataFromServer => this.recieveStatus(dataFromServer));
    // this.webSocket.subscribe(   msg => console.log('message received: ' + msg),
    // // Called whenever there is a message from the server
    // err => console.log(err),
    // // Called if WebSocket API signals some kind of error
    // () => console.log('complete')
    // // Called when connection is closed (for whatever reason)
    // );
   }



  public async deployPlan(params: any, undeploy?: boolean) {

    let response;
    let basyxFunc;

    if (undeploy) {
      basyxFunc = "undeployPlanAsync";
    } else {
      basyxFunc = "deployPlanAsync";
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

    return response;
  }


  public async undeployPlanById(params: any) {
    let response;
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

    return response;
  }

  private recieveStatus(Status: statusMessage) {
    console.log(Status);

    let isFinished = false;
    let isSuccesful = true;
    if(Status.taskId) {
      if(Status.action === "RESULT") {
        isFinished = true;
      }
      if(Status.action === "ERROR") {
        isSuccesful = false;
      }
      const process = this.StatusCollection.find(process => process.taskId === Status.taskId)
      if(process) {
        process.messages.push(Status);
        process.isFinished = isFinished;
        process.isSuccesful = isSuccesful;

      } else {
        this.StatusCollection.push({taskId: Status.taskId, isFinished: isFinished, isSuccesful: isSuccesful, messages: [Status]});
      }
    }

  }

  public dismissStatus(taskId: string) {
    let process = this.StatusCollection.find(process => process.taskId = taskId)
    if(process) {
      this.StatusCollection.splice(this.StatusCollection.indexOf(process), 1);
    }
  }

}
