import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Subject, Subscription } from 'rxjs';
import { EnvConfigService } from './env-config.service';
import { OnlyIdPipe } from '../pipes/only-id.pipe';
import { WebsocketService } from '../websocket.service';
import { StatusCollectionNotifier, StatusCollectionService } from './status-collection.service';
import { ApiService, IDSHORT_OPERATION_ARTIFACTS_DEPLOYPLANASYNC, IDSHORT_OPERATION_ARTIFACTS_DEPLOYPLANWITHIDASYNC, IDSHORT_OPERATION_ARTIFACTS_UNDEPLOYPLANASYNC, IDSHORT_SUBMODEL_ARTIFACTS } from './api.service';

@Injectable({
  providedIn: 'root'
})
export class PlanDeployerService {

  wsSubscription: Subscription | undefined;
  statusSubmodel: any;

  public reloadingDataSubject = new Subject<any>();

  constructor(private api: ApiService,
    private http: HttpClient,
    private envConfigService: EnvConfigService,
    private onlyId: OnlyIdPipe,
    private websocketService: WebsocketService,
    private collector: StatusCollectionService) {

    this.wsSubscription = websocketService.getMsgSubject().subscribe((value: any) => {
      collector.receiveStatus(JSON.parse(value)) 
    })
  }

  public async deployPlan(params: any, undeploy?: boolean) {
    let basyxFunc;

    if (undeploy) {
      basyxFunc = IDSHORT_OPERATION_ARTIFACTS_UNDEPLOYPLANASYNC;
    } else {
      basyxFunc = IDSHORT_OPERATION_ARTIFACTS_DEPLOYPLANASYNC;
    }

    let response = await this.api.executeAasJsonOperation(IDSHORT_SUBMODEL_ARTIFACTS, basyxFunc, params);

    if (response && response.outputArguments[0].value && response.outputArguments[0].value.value) {
      this.requestReceivedMessage(basyxFunc, this.onlyId.transform(response.outputArguments[0].value.value));
    }

    return response;
  }


  public async undeployPlanById(params: any) {
    let response = await this.api.executeAasJsonOperation(IDSHORT_SUBMODEL_ARTIFACTS, IDSHORT_OPERATION_ARTIFACTS_DEPLOYPLANWITHIDASYNC, params);

    if (response && response.outputArguments[0].value && response.outputArguments[0].value.value) {
      this.requestReceivedMessage("undeploy", this.onlyId.transform(response.outputArguments[0].value.value));
    }

    return response;
  }

  public async requestReceivedMessage(deploy: string, taskId: string) {
    let message = "";
    if (deploy.indexOf("undeploy") >= 0) {
      message = "undeploy request recieved";
    } else if (deploy.indexOf("deploy") >= 0) {
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
