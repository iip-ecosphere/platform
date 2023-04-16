import { Component, OnInit } from '@angular/core';
import { ApiService } from 'src/app/services/api.service';
import { PlanDeployerService } from 'src/app/services/plan-deployer.service';
import { ResourceAttribute, Resource, PlatformArtifacts, InputVariable } from 'src/interfaces';

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
  undeployPlanByIdInput: any;

  instanceId: string[] = []; //for id input of undeployPlanById
  responseMessage: string | undefined;

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
      this.undeployPlanByIdInput = response.submodelElements.find(item => item.idShort === "undeployPlanWithId")?.inputVariables;

      this.instanceId.fill("", 0, this.deploymentPlans?.value?.length);
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
      console.log("deploy")
      console.log("deploy plan:")
      console.log(plan)
      console.log(plan.value)
      let value = plan.value.find(item => item.idShort === "uri");
      console.log("uri value: ")
      console.log(value)
      if(value) {
        console.log(value.value)
        params[0].value.value = value.value;
      }
      const response = await this.deployer.deployPlan(params);

    }

  }

  public async undeploy(index: number, plan?: Resource,) {
    if(plan && this.instanceId[index] && this.instanceId[index] != "" ) {
      this.undeployById(plan, index);
    } else {
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
        const response = await this.deployer.deployPlan(params, true);
      }
    }

  }

  public async undeployById(plan: Resource, index: number) {
    let params: InputVariable[] = this.undeployPlanByIdInput;
    if(params[0].value && params[1].value) {
      params[0].value.value = plan.value?.find(item => item.idShort === "uri")?.value;
      params[1].value.value = this.instanceId[index];
    }
    const response = await this.deployer.undeployPlanById(params);
    if(response) {
      this.responseMessage = response.outputArguments[0].value?.value;
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

  public applyLineStyle(index: number) {

    let style = "white-line";

    if(index % 2 === 0) {
      style = "grey-line";
    }
    return (style);

  }

}
