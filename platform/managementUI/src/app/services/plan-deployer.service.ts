import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { firstValueFrom, Subject, Subscription } from 'rxjs';
import { platformResponse } from 'src/interfaces';
import { EnvConfigService } from './env-config.service';
import { OnlyIdPipe } from '../pipes/only-id.pipe';
import { WebsocketService } from '../websocket.service';
import { StatusCollectionNotifier, StatusCollectionService } from './status-collection.service';

@Injectable({
  providedIn: 'root'
})
export class PlanDeployerService {

  wsSubscription: Subscription | undefined;
  statusSubmodel: any;

  public reloadingDataSubject = new Subject<any>();

  constructor(private http: HttpClient,
    private envConfigService: EnvConfigService,
    private onlyId: OnlyIdPipe,
    private websocketService: WebsocketService,
    private collector: StatusCollectionService) {

    this.wsSubscription = websocketService.getMsgSubject().subscribe((value: any) => {
      collector.receiveStatus(JSON.parse(value)) 
    })
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
      let cfg = await this.envConfigService.initAndGetCfg();
      response = await firstValueFrom(this.http.post<platformResponse>(
        cfg?.ip
        + '/shells/'
        + cfg?.urn
        + "/aas/submodels/Artifacts/submodel/"
        + basyxFunc
        + "/invoke"
      ,{"inputArguments": params,
      "requestId":"1bfeaa30-1512-407a-b8bb-f343ecfa28cf",
      "inoutputArguments":[], "timeout":10000}
      , {responseType: 'json', reportProgress: true}));
    } catch(e) {
      console.error(e);
    }

    if(response && response.outputArguments[0].value && response.outputArguments[0].value.value) {
      this.requestReceivedMessage(basyxFunc, this.onlyId.transform(response.outputArguments[0].value.value));
    }

    return response;
  }


  public async undeployPlanById(params: any) {
    let response;
    try {
      let cfg = await this.envConfigService.initAndGetCfg();
      response = await firstValueFrom(this.http.post<platformResponse>(
        cfg?.ip
        + '/shells/'
        + cfg?.urn
        + "/aas/submodels/Artifacts/submodel/undeployPlanWithIdAsync/invoke"
      ,{"inputArguments": params,
        "requestId":"1bfeaa30-1512-407a-b8bb-f343ecfa28cf",
        "inoutputArguments":[], "timeout":10000}
      , {responseType: 'json'}));
    } catch(e) {
      console.error(e);
    }


    if(response && response.outputArguments[0].value && response.outputArguments[0].value.value) {
      this.requestReceivedMessage("undeploy", this.onlyId.transform(response.outputArguments[0].value.value));
    }

    return response;
  }

  public async requestReceivedMessage(deploy: string, taskId: string) {
    let message = "";
    if(deploy.indexOf("undeploy") >= 0) {
      message = "undeploy request recieved";
    } else if(deploy.indexOf("deploy") >= 0) {
      message="deploy request recieved"
    }
    this.collector.addReceivedMessage(message, taskId);
  }

  /*public dismissStatus(taskId: string) {
    this.collector.dismissStatus(taskId);
  }

  public triggerDataReloadingAction() {
    this.collector.triggerDataReloadingAction();
  }*/

  // for testing
  public setFinishedNotifier(notifier: StatusCollectionNotifier) {
    this.collector.setFinishedNotifier(notifier);
  }

}
