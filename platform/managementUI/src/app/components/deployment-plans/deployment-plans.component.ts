import { Component, OnInit } from '@angular/core';
import { ApiService } from 'src/app/services/api.service';
import { PlatformResources, ResourceSubmodelElement, ResourceValue } from 'src/interfaces';

@Component({
  selector: 'app-deployment-plans',
  templateUrl: './deployment-plans.component.html',
  styleUrls: ['./deployment-plans.component.scss']
})
export class DeploymentPlansComponent implements OnInit {

  constructor(public api: ApiService) { }

  async ngOnInit() {
    await this.getArtifacts();
  }

  artifacts: PlatformResources = {};
  deploymentPlans: ResourceSubmodelElement | undefined = {};
  selected: ResourceValue[] | undefined;
  deployPlanInput: any;
  undeployPlanInput: any;

  public async getArtifacts() {
    const response = await this.api.getArtifacts();
    if(response.submodelElements) {
      this.deploymentPlans = response.submodelElements.find(item => item.idShort === "DeploymentPlans");
      this.deployPlanInput = response.submodelElements.find(item => item.idShort === "deployPlan")?.inputVariables;
      this.undeployPlanInput = response.submodelElements.find(item => item.idShort === "undeployPlan")?.inputVariables;
      // console.log(response);
      // console.log(this.deploymentPlans);
      // console.log(this.deployPlanInput);
      // console.log(this.undeployPlanInput);
    }
  }

  public isArray(value: any) {
    const bo = Array.isArray(value);
    return bo;
  }

  public test() {
    console.log(this.selected);
  }

  public async deploy() {
    if(this.selected) {
      let params = this.deployPlanInput;
       let value = this.selected.find(item => item.idShort === "uri");
       console.log(value);
       if (value) {
        params[0].value.value = value.value;
       }
       console.log(params);
      const response = await this.api.deployPlan(params);
      console.log(response);
    }

  }

  public async undeploy() {
    if(this.selected) {
      let params = this.deployPlanInput;
       let value = this.selected.find(item => item.idShort === "uri");
       console.log(value);
       if (value) {
        params[0].value.value = value.value;
       }
       console.log(params);
      const response = await this.api.deployPlan(params, true);
      console.log(response);
    }
  }

}
