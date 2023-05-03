import { Component, OnInit } from '@angular/core';
import { ApiService } from 'src/app/services/api.service';
import { PlanDeployerService } from 'src/app/services/plan-deployer.service';
import { InputVariable, Resource } from 'src/interfaces';

@Component({
  selector: 'app-instances',
  templateUrl: './instances.component.html',
  styleUrls: ['./instances.component.scss']
})
export class InstancesComponent implements OnInit {

  filteredData: any[]= [];
  deploymentPlans: Resource | undefined;
  undeployInput: InputVariable[] = [];

  constructor(private api: ApiService, private deployer: PlanDeployerService) { }

  async ngOnInit() {

    this.getInstances();
    const artifacts = await this.api.getArtifacts();
    if(artifacts) {
      this.deploymentPlans = artifacts.submodelElements?.find(item => item.idShort === "DeploymentPlans");
      this.undeployInput= artifacts.submodelElements?.find(item => item.idShort === "undeployPlanWithId")?.inputVariables;
    }
  }

  public async getInstances() {
    const data = await this.api.getInstances();
    console.log(data);
    if(data) {
      for(const element of data) {
        this.filteredData.push(element);
      }
    }
  }

  public undeploy(item: any) {
    const planId = item.value.find((item: { idShort: string; }) => item.idShort === "planId")?.value;
    if(this.undeployInput && this.undeployInput != [] && this.undeployInput[0] && this.undeployInput[1]) {
      let input = this.undeployInput;
      if(input[0].value && input[1].value && this.deploymentPlans && this.deploymentPlans.value) {
        let compareId;
        for(const plan of this.deploymentPlans.value) {
          compareId = plan.value.find((item: { idShort: string; }) => item.idShort === "id")?.value;
          if(compareId === planId) {
            input[0].value.value = plan.value.find((item: { idShort: string; }) => item.idShort === "uri")?.value;
            input[1].value.value = item.value.find((item: { idShort: string; }) => item.idShort === "instanceId")?.value;
            break;
          }
        }
        console.log(input);
      }
      const response = this.deployer.undeployPlanById(input);
      console.log(response);
    }
  }

  public printDetails(idShort: string, value: any) {
    const temp = Number(value);
    let print = idShort + ': ';
    let date = new Date(temp);
    if(!isNaN(temp) && temp > 1000000) {
      print = print.concat(new Date(temp).toString());
    } else {
      print = print.concat(value);
    }

    return print;
  }

}
