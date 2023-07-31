import { HttpClient } from '@angular/common/http';
import { Component, NgZone, OnInit } from '@angular/core';
import { ApiService } from 'src/app/services/api.service';
import { EnvConfigService } from 'src/app/services/env-config.service';
import { PlatformArtifacts, Resource, PlatformServices}
  from 'src/interfaces';
import { firstValueFrom } from 'rxjs';
import {MatRadioChange} from '@angular/material/radio';
import { DialogService } from 'src/app/services/dialog.service';


@Component({
  selector: 'app-services',
  templateUrl: './services.component.html',
  styleUrls: ['./services.component.scss']
})
export class ServicesComponent implements OnInit {

  constructor(public http: HttpClient,
    public api: ApiService,
    private envConfigService: EnvConfigService,
    private zone: NgZone
    )
    {
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
  dataToDisplay: any;
  technicalData: any; // manufacture info for services
  currentTab:string = "";

  selected: Resource | undefined;
  deployPlanInput: any;
  undeployPlanInput: any;
  undeployPlanByIdInput: any;
  taskId: string = "";

  // filtern
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
    ["name", "", ""],
    ["id", "Id: ", ""],
    ["applicationInstanceId", "App instance: ", ""]
  ]

  // Radio-Button in 'Running Services' tab
  options: string[] = ["active", "all"]
  selectedOption: string = this.options[0]; // default option
  correctStates = ["STARTING", "RUNNING", "STOPPING"] // Service state

  // logs type
  stdout = 'stdout'
  stderr = 'stderr'

  ngOnInit(): void {
  }


  // --------------------- Button -------------------

  debug:number = 0

  public getDialog(id:string, idShort:string, logsType:string) {
    console.log("[serviceComp | getDialog] getDialog with logs type: " + logsType)
    this.zone.run(() => {
      let data = Date.now()
      let url = document.URL
      url = url.replace('services', 'logs')

      if (this.debug == 0) {
        window.open(
          url
          + '?id=' + id
          + '&idShort=' + idShort
          + '&type=' + logsType,
          'Dialog' + data,
          "height=800,width=700"
        )
      } else {
        window.open(url
          + '?id=' + id
          + '&idShort=' + idShort
          + '&type=' + logsType)
      }
    });
  }

  // ------------------ Filter -----------------------------------

  public async getPlatformData(submodel: any, submodelElement: any){
    let response: any;

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
    return response
  }

  public async getDisplayData(tab:string, submodel:any, submodelElement: string) {
    this.currentTab = tab
    if(tab != "instances") {
      await this.loadData(submodel, submodelElement)
      //console.log(this.filteredData)

      switch(this.currentTab) {
        case "running services":
          this.filterServices();
          break;
        case "running artifacts":
          this.filterArtifacts();
          break;
        default:
          break;
      }
    }
  }

  public async loadData(submodel: any, submodelElement: any){
    let response;
    response = await this.getPlatformData(submodel, submodelElement)
    this.filteredData = response

    if(this.currentTab != "instances") {
      this.filteredData = this.filteredData.value
    }
  }

  // todo not ready yet
  public async getTechnicalData(url:string) {
    let response;
    let technicalDataUrl = url + "/submodels/TechnicalData/submodel"
    console.log("# (getTechnicalData) \nURL: " + technicalDataUrl)
    try {
      response = await firstValueFrom(this.http.get(technicalDataUrl));

    } catch(e) {
      console.log(e);
    }
    this.technicalData = response
    //console.log("# (getTechnicalData) response:")
    //console.log(response)
  }

  public async getManufacturerInfo(url:string) {
    console.log("# (getManfInfo)")
    console.log("url: " + url)
    //let dummyUrl = "http://192.168.2.1:9001/shells/urn%3A%3A%3AAAS%3A%3A%3Aservice_CamSource%23/aas"
    //url = dummyUrl
    await this.getTechnicalData(url)

    // for local testing
    /*
    let dummyTechData = {
      idShort: "TechnicalData",
      submodelElements:[
      {idShort: "ProductImage", value: ""},
      {idShort: "ManufacturerLogo", value: "../../../assets/SSE-Logo.png"},
      {idShort: "ManufacturerName", value: "SSE"}
    ]}
    this.technicalData = dummyTechData
    console.log("dummy")
    console.log(this.technicalData)
    */
    let result = []
    if (this.technicalData) {
      for (let value of this.technicalData.submodelElements) {
        console.log("# (getManfInfo) single values from response:")
        console.log(value.idShort + ": " + value.value)
        result.push([value.idShort, value.value])
      }
    }
    console.log("# (getManfInfo) result:")
    console.log(result)
    return result
  }

  public async filterServices() {
    let result = []
    for (let tableRow of this.filteredData) {
      let temp = []
      let id
      let imgPath
      let itemIdShort = tableRow.idShort
      for (let rowValues of tableRow.value) {
        if (rowValues.idShort == "id") {
          id = rowValues.value
        }

        for (let param of this.paramToDisplay) {
          if (rowValues.idShort == param[0] && rowValues.value != "") {
            let new_rowValue =  { [param[0]]: rowValues.value }
            temp.push(new_rowValue)
          }
        }

        // getting manufacturer info TODO
        /*
        if (rowValues.idShort == "serviceAas") {
          let serviceTechData = this.getManufacturerInfo(rowValues.value)
          for (let value of await serviceTechData) {
            let new_rowValue = {}
            if (value[0] == "ManufacturerLogo") {
              imgPath =  value[1]
            } else {
              new_rowValue =  { "value":  value[0] + ": " + value[1]}
            }
            temp.push(new_rowValue)
          }
        }
        */
      }
      let new_value = {id: id, idShort: itemIdShort, logo: imgPath, value: temp}
      result.push(new_value)
    }
    this.filteredData = result
    this.dataToDisplay = result
    this.filterForCorrectState()


    console.log("# (filterService) filteredData:")
    console.log(this.filteredData)
    console.log('# (filterServices) dataToDisplay:')
    console.log(this.dataToDisplay)

  }

  public filterForCorrectState() {
    if (this.selectedOption == "all") {
      this.dataToDisplay = this.filteredData
    } else {
      let temp = []
      for (let item of this.filteredData) {
        for (let value of item.value) {
          if (this.correctStates.includes(value["state"])) {
            temp.push(item)
          }
        }
      }
      this.dataToDisplay = temp
    }
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

  public hasCorrectState(state: string) {

    if (this.correctStates.includes(state)) {
      return true
    } else {
      return false
    }
  }

  public onRadioChange(event: MatRadioChange) {
    this.filterForCorrectState()
  }

  // --------------------- display ------------------------------
  /** Create a 'human-friendly' string to display
   * based on values in this.paramToDisplay
   e.g. instead of '0.1.0' it is 'Version: 0.1.0' */
  public displayValue(item: any) {
    let key = Object.entries(item)[0][0]
    let value = Object.entries(item)[0][1]
    let valueToDisplay = value
    for (let param of this.paramToDisplay) {
      if(param[0] == key) {
        valueToDisplay = param[1] + value
      }
    }
    return valueToDisplay
  }

  public isDataToDisplayEmpty() {
    let result = false
    let i = 0
    if (typeof this.dataToDisplay === "undefined") {
      result = true
    } else {
      for(let x of this.dataToDisplay) {
        i++
      }
      if (i == 0) {
        result = true
      }
    }
    return result
  }

  //------------------------ helpers ------------------------

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
