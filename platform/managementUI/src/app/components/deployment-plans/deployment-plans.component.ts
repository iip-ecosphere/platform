import { Component, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { OnlyIdPipe } from 'src/app/pipes/only-id.pipe';
import { ApiService, ArtifactKind } from 'src/app/services/api.service';
import { PlanDeployerService } from 'src/app/services/plan-deployer.service';
import { WebsocketService } from 'src/app/websocket.service';
import { Resource, PlatformArtifacts, DEFAULT_UPLOAD_CHUNK } from 'src/interfaces';
import { Utils } from 'src/app/services/utils.service';
import { StatusCollectionNotifier } from 'src/app/services/status-collection.service';
import { chunkInput } from '../file-upload/file-upload.component';

@Component({
    selector: 'app-deployment-plans',
    templateUrl: './deployment-plans.component.html',
    styleUrls: ['./deployment-plans.component.scss'],
    standalone: false
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
  uploadFileType = ".yaml, .yml"; // suggested file extensions in browser upload dialog
  uploadEnabled = true;

  private subscription!: Subscription;

  constructor(public api: ApiService,
    private deployer: PlanDeployerService,
    private onlyId: OnlyIdPipe,
    public websocket: WebsocketService) {
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

  public getEnabled(plan: any) {
    let enabled = plan.value.find(
      (item: {idShort: string;}) => item.idShort === "enabled").value
    return enabled
  }

  /**
   * Called when a deployment plan shall be uploaded.
   * 
   * @param file the file to upload
   */
  public uploadFile(file: File) {
    this.uploadEnabled = false;
    chunkInput(file, DEFAULT_UPLOAD_CHUNK, (chunk, seqNr) => {
      this.api.uploadFileAsArrayBuffer(ArtifactKind.DEPLOYMENT_PLAN, seqNr, file.name, chunk);
    }, () => {
      this.uploadEnabled = true;
    });
  }

}
