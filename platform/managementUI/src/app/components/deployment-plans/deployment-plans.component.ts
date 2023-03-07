import { Component, OnInit } from '@angular/core';
import { ApiService } from 'src/app/services/api.service';
import { PlanDeployerService } from 'src/app/services/plan-deployer.service';
import { ResourceAttribute, Resource, PlatformArtifacts } from 'src/interfaces';

@Component({
  selector: 'app-deployment-plans',
  templateUrl: './deployment-plans.component.html',
  styleUrls: ['./deployment-plans.component.scss']
})
export class DeploymentPlansComponent implements OnInit {

  artifacts: PlatformArtifacts = {};
  deploymentPlans: Resource | undefined = {};
  selected: Resource | undefined;
  deployPlanInput: any;
  undeployPlanInput: any;

  constructor(public api: ApiService, private deployer: PlanDeployerService) {
  }

  async ngOnInit() {
    await this.getArtifacts();
  }



  public async getArtifacts() {
    const response = await this.api.getArtifacts();
    console.log(response);
    if(response.submodelElements) {
      this.deploymentPlans = response.submodelElements.find(item => item.idShort === "DeploymentPlans");
      this.deployPlanInput = response.submodelElements.find(item => item.idShort === "deployPlan")?.inputVariables;
      this.undeployPlanInput = response.submodelElements.find(item => item.idShort === "undeployPlan")?.inputVariables;
    }
  }

  public isArray(value: any) {
    return Array.isArray(value);
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

    }

  }

  public async undeploy(plan?: Resource) {
    let params = this.undeployPlanInput;
    if(!plan && this.selected && this.selected.value) {
       let value = this.selected.value.find(item => item.idShort === "uri");
       if (value) {
        params[0].value.value = value.value;
       }
      const response = this.deployer.deployPlan(params, true);
      this.selected = undefined;
      console.log(response);
    } else if(plan && plan.value) {
      let value = plan.value.find(item => item.idShort === "uri");
      if(value) {
        params[0].value.value = value.value;
      }
      const response = await this.deployer.deployPlan(params);
    }
  }

  //this method gets called multiple times when the select in the template is clicked, this is not desired
  //select is currently not in use
  public getName(plan: ResourceAttribute[]) {
    // console.log(plan);
    let name: any = plan.find(item => item.idShort === 'name');
    if(!name) {
      name = ''
    } else {
      name = name.value;
    }
    return name;
  }

  // public getDesc(plan: ResourceAttribute[]) {
  //   // console.log(plan);
  //   let name: any = plan.find(item => item.idShort === 'description');
  //   if(!name) {
  //     name = ''
  //   } else {
  //     name = name.value;
  //   }
  //   return name;
  // }

}
