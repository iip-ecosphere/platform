import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Subscription, firstValueFrom } from 'rxjs';
import { OnlyIdPipe } from 'src/app/pipes/only-id.pipe';
import { ApiService } from 'src/app/services/api.service';
import { EnvConfigService } from 'src/app/services/env-config.service';
import { PlanDeployerService } from 'src/app/services/plan-deployer.service';
import { WebsocketService } from 'src/app/websocket.service';
import { ResourceAttribute, Resource, PlatformArtifacts, InputVariable, PlatformData } from 'src/interfaces';
import { Utils } from 'src/app/services/utils.service';
import { StatusCollectionNotifier } from 'src/app/services/status-collection.service';

@Component({
  selector: 'app-deployment-plans',
  templateUrl: './deployment-plans.component.html',
  styleUrls: ['./deployment-plans.component.scss']
})
export class DeploymentPlansComponent extends Utils implements OnInit {

  artifacts: PlatformArtifacts = {};
  deploymentPlans: Resource | undefined = {};
  selected: Resource | undefined;
  deployPlanInput: any;
  undeployPlanInput: any;
  undeployPlanByIdInput: any;
  instanceId: string[] = []; //for id input of undeployPlanById
  responseMessage: string | undefined;
  taskId: string = "";
  http: any;

  private subscription!: Subscription;

  constructor(public api: ApiService,
    private deployer: PlanDeployerService,
    private onlyId: OnlyIdPipe,
    private websocket: WebsocketService) {
      super();
  }

  async ngOnInit() {
    await this.getArtifacts();
    this.websocket.connectToStatusUri(this.api);
  }

  // for testing
  public setFinishedNotifier(notifier: StatusCollectionNotifier) {
    this.deployer.setFinishedNotifier(notifier);
  }

  public async getArtifacts() {
    const response = await this.api.getArtifacts();
    if(response.submodelElements) {
      this.deploymentPlans = response.submodelElements.find(
        item => item.idShort === "DeploymentPlans");
      this.deployPlanInput = response.submodelElements.find(item => item.idShort === "deployPlan")?.inputVariables;
      this.undeployPlanInput = response.submodelElements.find(item => item.idShort === "undeployPlan")?.inputVariables;
      this.undeployPlanByIdInput = response.submodelElements.find(item => item.idShort === "undeployPlanWithId")?.inputVariables;

      this.instanceId.fill("", 0, this.deploymentPlans?.value?.length);
    }
  }

  public async deploy(plan?: Resource) {
    let params = this.deployPlanInput;
    if(!plan && this.selected && this.selected.value) {
       let value = this.selected.value.find(item => item.idShort === "uri");
       if (value) {
        params[0].value.value = value.value;
       }
      const response = await this.deployer.deployPlan(params);
      this.selected = undefined;
    } else if(plan && plan.value) {
      let value = plan.value.find(item => item.idShort === "uri");
      if(value) {
        params[0].value.value = value.value;
      }
      const response = await this.deployer.deployPlan(params);
      if(response && response.outputArguments[0] && response.outputArguments[0].value) {
        this.taskId = this.onlyId.transform(response.outputArguments[0].value.value);
      }
    }
  }

  public getDescription(plan: any) {
    let desc = plan.value.find(
      (item: { idShort: string; }) => item.idShort === "description").value
    return desc
  }

  public getId(plan: any) {
    let id = plan.value.find(
      (item: { idShort: string; }) => item.idShort === "id").value
    return id
  }

  /*public applyLineStyle(index: number) {

    let style = "white-line";

    if(index % 2 === 0) {
      style = "grey-line";
    }
    return (style);

  }*/

  public getEnabled(plan: any) {
    let enabled = plan.value.find(
      (item: {idShort: string;}) => item.idShort === "enabled").value
    return enabled
  }

}
