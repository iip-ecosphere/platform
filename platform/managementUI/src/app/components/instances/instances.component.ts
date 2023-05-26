import { Component, OnInit } from '@angular/core';
import { Subscription, interval } from 'rxjs';
import { ApiService } from 'src/app/services/api.service';
import { PlanDeployerService } from 'src/app/services/plan-deployer.service';
import { InputVariable, Resource, StatusMsg } from 'src/interfaces';

@Component({
  selector: 'app-instances',
  templateUrl: './instances.component.html',
  styleUrls: ['./instances.component.scss']
})
export class InstancesComponent implements OnInit {

  filteredData: any[]= [];
  deploymentPlans: Resource | undefined;
  undeployInput: InputVariable[] = [];
  private subscription: Subscription = new Subscription();

  statusSub: Subscription;
  status: StatusMsg = {
    executionState: "",
    messages: [""]
  }

  constructor(private api: ApiService,
    private deployer: PlanDeployerService) {
      this.statusSub = this.deployer.emitter.subscribe(
        (status: StatusMsg) => {this.status = status});

    }

  async ngOnInit() {
    this.getData();

    // reloading instances every 3 sec
    this.subscription = interval(3000).subscribe(
      (val) => { this.getInstances()});

      console.log("Status")
      console.log(this.status)
  }

  public async getData() {
    this.getInstances();
    const artifacts = await this.api.getArtifacts();
    if(artifacts) {
      this.deploymentPlans = artifacts.submodelElements?.find(item => item.idShort === "DeploymentPlans");
      this.undeployInput= artifacts.submodelElements?.find(item => item.idShort === "undeployPlanWithId")?.inputVariables;
    }
  }

  public async getInstances() {
    console.log("# refreshing instances")
    console.log("status: ")
    console.log(this.status)
    const data = await this.api.getInstances();
    if(data) {
      this.filteredData = []
      for(const element of data) {
        if(element.value && element.value.length > 1) {
          // preventing double elements
          if (!this.isElement(element, this.filteredData)) {
            this.filteredData.push(element);
          }
        }
      }
    }
    console.log("Filtered Data")
    console.log(this.filteredData)
    console.log("----------------")
    /*
    if(data) {
      for(const element of data) {
        if(element.value && element.value.length > 1) {
          // preventing double elements
          if (!this.isElement(element, this.filteredData)) {
            this.filteredData.push(element);
          }
        }
      }
    }*/
  }

  public isElement(elem: any, list: any) {
    let result = false;
    for(let val of list) {
      if (val.idShort === elem.idShort) {
        return true
      }
    }
    return result
  }

  public undeploy(item: any) {
    const planId = item.value.find(
      (item: { idShort: string; }) => item.idShort === "planId")?.value;
    if(this.undeployInput
      && this.undeployInput != []
      && this.undeployInput[0]
      && this.undeployInput[1]) {
      let input = this.undeployInput;
      if(input[0].value && input[1].value && this.deploymentPlans
        && this.deploymentPlans.value) {
        let compareId;
        for(const plan of this.deploymentPlans.value) {
          compareId = plan.value.find((
            item: { idShort: string; }) => item.idShort === "id")?.value;
          if(compareId === planId) {
            input[0].value.value = plan.value.find(
              (item: { idShort: string; }) => item.idShort === "uri")?.value;
            input[1].value.value = item.value.find(
              (item: { idShort: string; }) => item.idShort === "instanceId")?.value;
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
