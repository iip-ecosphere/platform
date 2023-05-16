import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ApiService } from 'src/app/services/api.service';
import { EnvConfigService } from 'src/app/services/env-config.service';
import { PlatformArtifacts, Resource, PlatformServices }
  from 'src/interfaces';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { PlanDeployerService } from 'src/app/services/plan-deployer.service';
import { OnlyIdPipe } from 'src/app/pipes/only-id.pipe';

@Component({
  selector: 'app-services',
  templateUrl: './services.component.html',
  styleUrls: ['./services.component.scss']
})
export class ServicesComponent implements OnInit {

  constructor(public http: HttpClient,
    public api: ApiService,
    private router: Router,
    private envConfigService: EnvConfigService){

      const env = this.envConfigService.getEnv();
      if(env && env.ip) {
        this.ip = env.ip;
      }
      if (env && env.urn) {
        this.urn = env.urn;
      }
  }

  services: PlatformServices = {};
  servicesToggle: boolean[] = [];

  artifacts: PlatformArtifacts = {};
  artifactsToggle: boolean[] = [];

  ip: string = "";
  urn:string = "";
  filteredData: any;
  currentTab:string = "";

  //artifacts: PlatformArtifacts = {};
  //deploymentPlans: Resource | undefined = {};
  selected: Resource | undefined;
  deployPlanInput: any;
  undeployPlanInput: any;
  undeployPlanByIdInput: any;
  taskId: string = "";

  tabsParam = [
    {tabName: "deployment plans",
      submodel: "Artifacts",
      submodelElement: "DeploymentPlans"},
    {tabName: "instances",
      submodel: "ApplicationInstances",
      submodelElement: ""},
    {tabName: "running services",
      submodel: "services",
      submodelElement: "services"},
    {tabName: "running artifacts",
      submodel: "services",
      submodelElement: "artifacts"}
    //{tabName: "available artifacts"}
  ]

  paramToDisplay = [
    ["description", "", ""],
    ["planId", "Deployment plan: ", ""],
    ["instanceId", "Instance: ", ""],
    ["timestamp", "Started on: ", ""],
    ["applicationId", "App: ", ""],
    ["state", "State: ", ""],
    ["version", "Version: ", ""],
    ["resource", "Resource: ", ""],
    ["name", "", ""]
  ]

  ngOnInit(): void {
    this.getServices();
    this.getArtifacts();
  }

  public async getDisplayData(tab:string, submodel:any, submodelElement: string) {
    console.log("runtime/service component")
    this.currentTab = tab
    await this.loadData(submodel, submodelElement)
    console.log(this.currentTab)
    switch(this.currentTab) {
      case "deployment plans":
        this.filterDeplPlans();
        break;
      case "instances":
        this.filterInstances();
        break;
      case "running services":
        this.filterServices();
        break;
      case "running artifacts":
        this.filterArtifacts();
        break;
      default:
        break;
    }
    console.log("Filtered data:")
    console.log(this.filteredData)
  }

  public async loadData(submodel: any, submodelElement: any){

    let response;
    try {
        response = await firstValueFrom(
          this.http.get(this.ip + '/shells/'
        + this.urn
        + "/aas/submodels/"
        + submodel
        + "/submodel/submodelElements/"
        + submodelElement));
      } catch(e) {
        console.log(e);
      }
    console.log("load data:")
    console.log(response)
    this.filteredData = response

    if(this.currentTab != "instances") {
      this.filteredData = this.filteredData.value
    }

    //this.filteredData = response
    /*
    if (submodelElement) {
      console.log("-> submodel")
      this.rawData = await this.getData(submodelElement);
      this.filteredData = this.rawData.value
    } else {
      console.log("-> metaproject")
      this.rawData = await this.getData("")
      this.filteredData = this.prefilter(metaProject)
    }*/
  }

  // Filter

  public filterDeplPlans() {
    let result = []
    for (let tableRow of this.filteredData) {
      let temp = []
      let name
      for (let rowValues of tableRow.value) {
        if (rowValues.idShort == "id") {
          name = rowValues.value
        }
        for (let param of this.paramToDisplay) {
          if (rowValues.idShort == param[0]) {
            let new_rowValue =  { "value":  param[1] + rowValues.value + param[2]}
            temp.push(new_rowValue)
          }
        }
      }
      let new_value = {idShort: name, value: temp, plan: tableRow}
      result.push(new_value)
    }
    this.filteredData = result
  }
  /*
  private getValue(rowVal: any, param:any) {
    let value = rowVal.value.find(
      (elemt: { idShort: string; }) => elemt.idShort === this.varValue).value
    return { "value":  param[1] + value + param[2]}
  }
  */

  public filterInstances() {
    let result = []
    for (let tableRow of this.filteredData) {
      let temp = []
      let name
      if(tableRow.value) {
        for (let rowValues of tableRow.value) {
          if(rowValues.idShort == "appId") {
            name = rowValues.value
          }
          for (let param of this.paramToDisplay) {
            if (rowValues.idShort == param[0]) {
              let new_rowValue
              if(rowValues.idShort == "timestamp") {
                const temp = Number(rowValues.value);
                let date = new Date(temp);
                new_rowValue =  { "value":  param[1] + date}
              } else {
                new_rowValue =  { "value":  param[1] + rowValues.value + param[2]}
              }
              //let new_rowValue =  { "value":  param[1] + rowValues.value + param[2]}
              temp.push(new_rowValue)
            }
          }
        }
      }
      if (name) { // TODO with this if I remove this strage item with "....max" name
        let new_value = {idShort: name, value: temp}
        result.push(new_value)
      }
    }
    this.filteredData = result
  }

  public filterServices() {
    let result = []
    for (let tableRow of this.filteredData) {
      let temp = []
      let name
      for (let rowValues of tableRow.value) {
        if (rowValues.idShort == "name") {
          name = rowValues.value
        }
        for (let param of this.paramToDisplay) {
          if (rowValues.idShort == param[0]) {
            let new_rowValue =  { "value":  param[1] + rowValues.value + param[2]}
            temp.push(new_rowValue)
          }
        }
      }
      let new_value = {idShort: name, value: temp}
      result.push(new_value)
    }
    this.filteredData = result
  }

  public filterArtifacts() {
    let result = []
    for (let tableRow of this.filteredData) {
      let temp = []
      if(tableRow.value) {
        for (let rowValues of tableRow.value) {
          for (let param of this.paramToDisplay) {
            if (rowValues.idShort == param[0]) {
              let new_rowValue =  { "value":  param[1] + rowValues.value + param[2]}
              temp.push(new_rowValue)
            }
          }
        }
      }
      let new_value = {idShort: tableRow.idShort, value: temp}
      result.push(new_value)
    }
    this.filteredData = result
  }

  // ------------------------- buttons ---------------------------
  public async deploy(plan: Resource) {

  }



  //--------------------------------- old ------------------------

  public async getServices() {
    this.services = await this.api.getServices();
    if(this.services && this.services.submodelElements) {
      this.servicesToggle = new Array(
        this.services.submodelElements.length).fill(false);
    }
  }

  public serToggle(index: number) {
    if(this.servicesToggle) {
      this.servicesToggle[index] = !this.servicesToggle[index]
    }
  }

  public async getArtifacts() {
    this.artifacts = await this.api.getArtifacts();
    if(this.artifacts && this.artifacts.submodelElements) {
      this.artifactsToggle = new Array(
        this.artifacts.submodelElements.length).fill(false);
    }
  }

  public artToggle(index: number) {
    if(this.artifactsToggle) {
      this.artifactsToggle[index] = !this.artifactsToggle[index]
    }
  }

  public isArray(value: any) {
    const bo = Array.isArray(value);
    return bo;
  }

  public isObject(value: any) {
    return (typeof value === 'object');
  }

  public isNonEmptyString(value: any) {
    let result = false
    if (typeof value == "string" && value.length > 0) {
      result = true
    }
    return result
  }



}
