import { ApiService } from 'src/app/services/api.service';
import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { EnvConfigService } from 'src/app/services/env-config.service';
import { ActivatedRoute } from '@angular/router';
import { Router } from '@angular/router';
import { MAT_PROGRESS_SPINNER_DEFAULT_OPTIONS_FACTORY } from '@angular/material/progress-spinner';
//import { table } from 'console';

@Component({
  selector: 'app-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss']
})
export class ListComponent implements OnInit {
  ip: string = "";
  urn: string = "";
  tab: string | null = null;
  data: any;
  filteredData: any;
  varValue = "varValue"
  //displayAttributes = ["varValue"]
  //attrServices = ["name"]
  //helper = 1;
  //noData: boolean = false;
  //unwantedTypes = ["metaState", "metaType", "metaProject", "metaAas"];
  //dataToDisplay: any;

  constructor(private router: Router,
    private route: ActivatedRoute,
    public http: HttpClient,
    private envConfigService: EnvConfigService,
    public api: ApiService) {
      const env = this.envConfigService.getEnv();
       //the ip and urn are taken from the json.config
      if(env && env.ip) {
        this.ip = env.ip;
      }
      if (env && env.urn) {
        this.urn = env.urn;
      }
    }

  tabsParam = [
    {tabName: "Setup", metaProject:"TechnicalSetup", type:null},
    {tabName: "Constants", metaProject:"AllConstants", type:null},
    {tabName: "Types", metaProject:"AllTypes", type:null},
    {tabName: "Services", metaProject:null, type:"ServiceBase"},
    {tabName: "Servers", metaProject:null, type:"Server"},
    {tabName: "Meshes", metaProject:null, type:"ServiceMesh"},
    {tabName: "Applications", metaProject:null, type:"Application"}
  ]

  params = [
    ["ver", "Version: ", ""],
    ["name", "", ""],
    //["kind", "", ""],
    ["host", "Host: ", ""],
    ["globalHost", "Global host: ", ""],
    ["port", "Port: ", ""],
    ["description", "", ""],
   // ["running", "Running: ", ""],
    ["schema", "Schema: ", ""],
    ["waitingTime", "Waiting time: ", " sec"],
    ["type", "Type: ", ""]
  ]

  ngOnInit(): void {
  }

  // TODO change string | null to any
  public async loadData(metaProject: string | null, type: string | null) {
    if (type) {
      this.tab = type //TODO make it more elegant
      this.data = await this.getData(type);
      this.filteredData = this.data.value
    } else {
      this.tab = metaProject // TODO
      this.data = await this.getData("")
      this.prefilter(metaProject)
    }

    switch(this.tab) {
      case "TechnicalSetup":
      //case this.tab:
        this.filterSetup();
        break;
      case "AllConstants":
        this.filterConstants();
        break;
      case "AllTypes":
        this.filterTypes();
        break;
      case "ServiceBase":
        this.filterServices();
        break;
      case "Server":
        this.filterServices();
        break;
    case "ServiceMesh":
        this.filterMeshes();
        break;
    case "Application":
        this.filterServices();
        break;
    default:
        break;
    }
  }

  public async getData(type: string) {
    let response;
    try {
        response = await firstValueFrom(
          this.http.get(this.ip + '/shells/'
        + this.urn
        + "/aas/submodels/Configuration/submodel/submodelElements/"
        + type));
      } catch(e) {
        console.log(e);
        //this.noData = true;
      }
    return response;
  }

  public prefilter(metaProject: string | null) {
    let result = []
    for(const submodelElement of this.data) {
      if(submodelElement.value) {
        for(const elemtSubmodelElement of submodelElement.value) {
          for(const valElemtSubmodelElement of elemtSubmodelElement.value) {
            if(valElemtSubmodelElement.idShort == "metaProject" && valElemtSubmodelElement.value == metaProject) {
                result.push(elemtSubmodelElement)
            }
          }
        }
      }
    }
    this.filteredData = result;
  }

  public filterSetup() {
    let result = []

    for (let tableRow of this.filteredData) {
      let temp = []
      let rowValues = tableRow.value[0].value
      // string
      if((typeof rowValues) == "string") {

        for (let rowValues of tableRow.value) {
          if (rowValues.idShort == "varValue") {
            let new_rowValue = {"value": rowValues.value}
            temp.push(new_rowValue)
          }
        }
      // number
      } else if((typeof rowValues) == "number") {
        // AAS  startup timeout
        let new_rowValue = {"value": rowValues + " sec"} // TODO is it true?
        temp.push(new_rowValue)
      // object
      } else {
        for (let rowValues of tableRow.value) {
          for (let param of this.params) {
            if (rowValues.idShort == param[0]) {
              let new_rowValue = this.getValue(rowValues, param)
              temp.push(new_rowValue)
            }
          }
        }
      }
      let row = {idShort: tableRow.idShort, value: temp}
      result.push(row)
    }
    this.filteredData = result
  }

  public filterTypes() {
    let result = []
    for (let tableRow of this.filteredData) {
      let new_value = {idShort: tableRow.idShort}
      result.push(new_value)
    }
    this.filteredData = result
  }

  public filterConstants() {
    let result = []
    for (let tableRow of this.filteredData) {
      let temp = []
      for (let rowValues of tableRow.value) {
        if (rowValues.idShort == "varValue") {
          let new_rowValue = {
            "value": rowValues.value}
          temp.push(new_rowValue)
        }
      }
      let new_value = {idShort: tableRow.idShort, value: temp}
      result.push(new_value)
    }
    this.filteredData = result
  }

  public filterMeshes() {
    let result = []
    for (let tableRow of this.filteredData) {
      let temp = []
      for (let rowValues of tableRow.value) {
        for (let param of this.params) {
          if (rowValues.idShort == param[0]) {
            let new_rowValue = this.getValue(rowValues, param)
            temp.push(new_rowValue)
          }
        }
      }
      let new_value = {idShort: tableRow.idShort, value: temp}
      result.push(new_value)

    }
    this.filteredData = result
  }

  public filterServices() {
    let result = []
    for (let tableRow of this.filteredData) {
      let temp = []
      let name
      for (let rowValues of tableRow.value) {
        // TODO maybe this version is better bc with params
        // we will get more if as desired
        /*
        if (rowValues.idShort == "name") {
          let new_rowValue = {
            "value": rowValues.value[0].value}
            temp.push(new_rowValue)
        }
        if (rowValues.idShort == "ver") {
          let new_rowValue = {
            "value": "Version: " + rowValues.value[0].value}
            temp.push(new_rowValue)
        }
        if (rowValues.idShort == "kind") {
          let new_rowValue = {
            "value": rowValues.value[0].value}
            temp.push(new_rowValue)
        }

        */
        if (rowValues.idShort == "id") {
          name = rowValues.value[0].value
        }
        for (let param of this.params) {
          if (rowValues.idShort == param[0]) {
            let new_rowValue = this.getValue(rowValues, param)
            temp.push(new_rowValue)
          }
        }
      }
      let new_value = {idShort: name, value: temp}
      result.push(new_value)
    }
    this.filteredData = result
  }

  private getValue(rowVal: any, param:any) {
    let value = rowVal.value.find(
      (elemt: { idShort: string; }) => elemt.idShort === this.varValue).value
    return { "value":  param[1] + value + param[2]}
  }

  // ---- buttons -----

  public edit(item: any) {
    if(this.tab === "ServiceMesh") {
      this.router.navigateByUrl('flowchart/' + item.idShort);
    }
  }

  public del(item: any) {

  }

  public createMesh() {

  }

  // ---- icons -------------

  icons = [
    ["opc.png", ["PlcNextOpcConn", "PlcBeckhoffOpcConn", "DriveBeckhoffOpcConn"]],
    ["java.png", ["CamSource", "AppAas", "ActionDecider", "DriveAppAas"]],
    ["py.png", ["PythonAi", "DriveLenzePythonAi"]],
    ["flower.png", ["FlowerAiServiceClient"]],
    ["mqtt.png", ["DriveLenzeMqttConn", "mqttEnergyConn", "GraphanaMqttConn"]]
  ]

  public getIcon(serviceId:string) {
    let icon_path = null
    let row = this.icons.find(item => item[1].includes(serviceId))
    if(row != undefined) {
      icon_path = "../../../assets/" + row[0]
    }
    return icon_path
  }


   /*
  public getId(serviceValue: any[]) {
    return serviceValue.find(item => item.idShort === 'id').value;
  }
  */

  // old version
  /*
  public async getData(type: string) {
    let response;
    let path;
    if (this.ls == "artifact") {
      path = "/aas/submodels/services/submodel/submodelElements/";
      this.ls = "services"
    } else {
      path = "/aas/submodels/Configuration/submodel/submodelElements/";
    }
    try {
        response = await firstValueFrom(
          this.http.get(this.ip + '/shells/'
        + this.urn
        + path
        + this.ls));
      } catch(e) {
        console.log(e);
        this.noData = true;
      }
    return response;
  }
  */


  //help method to instantly read all idShort of submodelElement collections in configuration
  /*
  public async getSubmodelElements() {
    let response;
    try {
      response = await firstValueFrom(this.http.get(
        this.ip + '/shells/'
      + this.urn
      + "/aas/submodels/Configuration/submodel/submodelElements"));
    } catch(e) {
      console.log(e);
      this.noData = true;
    }
    return response;
  }
  */


  /*
  ip: string = "";
  urn: string = "";

  ls: string | null = null; //the idShort of the collection to get from the configuration
  noData: boolean = false;
  data: any;
  submodelElements: any; //help
  unwantedTypes = ["metaState", "metaType", "metaProject"];

  constructor(public http: HttpClient,
    private envConfigService: EnvConfigService,
    private route: ActivatedRoute) {
    const env = this.envConfigService.getEnv();
    //the ip and urn are taken from the json.config
    if(env && env.ip) {
      this.ip = env.ip;
    }
    if (env && env.urn) {
      this.urn = env.urn;
    }
   }

  async ngOnInit() {
    this.ls = this.route.snapshot.paramMap.get('ls');
    if(this.ls) {
      this.data = await this.getData(this.ls);
      console.log(this.data)
      this.filter();
    }
    this.submodelElements = await this.getSubmodelElements();

  }

  public filter() {
    const relevantValues = []
    let indicesToRemove = []
    let i = 0;
    for(const item of this.data.value) {
      if(!this.unwantedTypes.includes(item.idShort)) {
        indicesToRemove.push(i)
        relevantValues.push(item)
      }
      i++;
    }
    this.data.value = relevantValues
  }

  public async getData(list: string) {
    let response;
    try {
      response = await firstValueFrom(
        this.http.get(this.ip + '/shells/'
      + this.urn
      + "/aas/submodels/Configuration/submodel/submodelElements/"
      + list));
    } catch(e) {
      console.log(e);
      this.noData = true;
    }
    return response;
  }

  //help method to instantly read all idShort of submodelElement collections in configuration
  public async getSubmodelElements() {
    let response;
    try {
      response = await firstValueFrom(this.http.get(this.ip + '/shells/'
      + this.urn + "/aas/submodels/Configuration/submodel/submodelElements"));
    } catch(e) {
      console.log(e);
      this.noData = true;
    }
    return response;
  }

  public edit(item: any) {

  }

  public del(item: any) {

  }

  public createMesh() {

  }
  */

}
