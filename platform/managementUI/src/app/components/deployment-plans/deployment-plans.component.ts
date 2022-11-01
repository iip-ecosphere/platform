import { Component, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { ApiService } from 'src/app/services/api.service';
import { PlanDeployerService } from 'src/app/services/plan-deployer.service';
import { PlatformResources, ResourceSubmodelElement, ResourceValue } from 'src/interfaces';

@Component({
  selector: 'app-deployment-plans',
  templateUrl: './deployment-plans.component.html',
  styleUrls: ['./deployment-plans.component.scss']
})
export class DeploymentPlansComponent implements OnInit {

  artifacts: PlatformResources = {};
  deploymentPlans: ResourceSubmodelElement | undefined = {};
  selected: ResourceSubmodelElement | undefined;
  deployPlanInput: any;
  undeployPlanInput: any;

  constructor(public api: ApiService, private deployer: PlanDeployerService) {
  }

  async ngOnInit() {
    await this.getArtifacts();
  }



  public async getArtifacts() {
    const response = await this.api.getArtifacts();
    if(response.submodelElements) {
      this.deploymentPlans = response.submodelElements.find(item => item.idShort === "DeploymentPlans");
      this.deployPlanInput = response.submodelElements.find(item => item.idShort === "deployPlan")?.inputVariables;
      this.undeployPlanInput = response.submodelElements.find(item => item.idShort === "undeployPlan")?.inputVariables;
    }
  }

  public isArray(value: any) {
    return Array.isArray(value);
  }

  public async deploy() {
    if(this.selected && this.selected.value) {
      let params = this.deployPlanInput;
       let value = this.selected.value.find(item => item.idShort === "uri");
       if (value) {
        params[0].value.value = value.value;
       }
      const response = await this.deployer.deployPlan(params);
      this.selected = undefined;
    }

  }

  public async undeploy() {
    if(this.selected && this.selected.value) {
      let params = this.deployPlanInput;
       let value = this.selected.value.find(item => item.idShort === "uri");
       if (value) {
        params[0].value.value = value.value;
       }
      const response = this.deployer.deployPlan(params, true);
      this.selected = undefined;
      console.log(response);
    }
  }

  //this method gets called multiple times when the select in the template is clicked, this is not desired
  public getName(plan: ResourceValue[]) {
    // console.log(plan);
    let name: any = plan.find(item => item.idShort === 'name');
    if(!name) {
      name = ''
    } else {
      name = name.value;
    }
    return name;
  }

}
