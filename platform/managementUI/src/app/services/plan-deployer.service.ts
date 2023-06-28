import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { firstValueFrom, Subscription } from 'rxjs';
import { platformResponse, statusCollection, statusMessage} from 'src/interfaces';
import { EnvConfigService } from './env-config.service';
import { webSocket, WebSocketSubject } from 'rxjs/webSocket';
import { OnlyIdPipe } from '../pipes/only-id.pipe';

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
    private envConfigService: EnvConfigService,
    private onlyId: OnlyIdPipe) {
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
      console.log(response);
      if(response.outputArguments[0].value && response.outputArguments[0].value.value) {
        this.requestRecievedMessage(basyxFunc, this.onlyId.transform(response.outputArguments[0].value.value));
      }

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
        + "/aas/submodels/Artifacts/submodel/undeployPlanWithIdAsync/invoke"
      ,{"inputArguments": params,
        "requestId":"1bfeaa30-1512-407a-b8bb-f343ecfa28cf",
        "inoutputArguments":[], "timeout":10000}
      , {responseType: 'json'}));

      if(response.outputArguments[0].value && response.outputArguments[0].value.value) {
        this.requestRecievedMessage("undeploy", this.onlyId.transform(response.outputArguments[0].value.value));
      }
    } catch(e) {
      console.log(e);
    }

    return response;
  }

  public async requestRecievedMessage(deploy: string, taskId: string) {
    let message = "";
    if(deploy.indexOf("undeploy") >= 0) {
      message = "undeploy request recieved";
    } else if(deploy.indexOf("deploy") >= 0) {
      message="deploy request recieved"
    }
    const status: statusMessage = {taskId: taskId, action: "Recieved", aliasIds: [], componentType: "", description: message, deviceId: "", id: "", progress: 0, subDescription: ""};
    this.StatusCollection.push({taskId: taskId, isFinished: false, messages: [status]});

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
        //status messages might not be recieved in order of the respective process step occuring,
        //therefore, once a result or error message was recieved, isFinished must stay true once it was set to true.
        if(process.isFinished = false) {
          process.isFinished = isFinished;
        }
        if(process.isSuccesful = true) {
          process.isSuccesful = isSuccesful;
        }
      } else {
        this.StatusCollection.push({taskId: Status.taskId, isFinished: isFinished, isSuccesful: isSuccesful, messages: [Status]});
      }
    } else {
      console.log("WARNING: Recieved status without taskId: ");
      console.log(Status);
    }

  }

  public dismissStatus(taskId: string) {
    let process = this.StatusCollection.find(process => process.taskId = taskId)
    if(process) {
      this.StatusCollection.splice(this.StatusCollection.indexOf(process), 1);
    }
  }

}
