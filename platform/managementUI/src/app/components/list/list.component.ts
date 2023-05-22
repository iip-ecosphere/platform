import { ApiService } from 'src/app/services/api.service';
import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { EnvConfigService } from 'src/app/services/env-config.service';
import { Router } from '@angular/router';
import { MatDialog, MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import { MAT_PROGRESS_SPINNER_DEFAULT_OPTIONS_FACTORY } from '@angular/material/progress-spinner';
import { EditorComponent } from '../editor/editor.component';
//import { table } from 'console';

@Component({
  selector: 'app-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss']
})
export class ListComponent implements OnInit {
  ip: string = "";
  urn: string = "";
  currentTab: string | null = null;
  rawData: any;
  filteredData: any;
  varValue = "varValue"
  imgPath = "../../../assets/"

  constructor(private router: Router,
    public http: HttpClient,
    private envConfigService: EnvConfigService,
    public api: ApiService,
    public dialog: MatDialog) {
      const env = this.envConfigService.getEnv();
       //the ip and urn are taken from the json.config
      if(env && env.ip) {
        this.ip = env.ip;
      }
      if (env && env.urn) {
        this.urn = env.urn;
      }
    }

  // Filter ---------------------------------------------------------------------
  // Information how the raw data for a given tab should be filtered:
  // with the metaProject or with a name of the submodelElement
  tabsParam = [
    {tabName: "Setup", metaProject:"TechnicalSetup", submodelElement:null},
    {tabName: "Constants", metaProject:"AllConstants", submodelElement:null},
    {tabName: "Types", metaProject:"AllTypes", submodelElement:null},
    {tabName: "Dependencies", metaProject: "", submodelElement: "Dependency"},
    {tabName: "Nameplates", metaProject:null, submodelElement:"Manufacturer"},
    {tabName: "Services", metaProject:null, submodelElement:"ServiceBase"},
    {tabName: "Servers", metaProject:null, submodelElement:"Server"},
    {tabName: "Meshes", metaProject:null, submodelElement:"ServiceMesh"},
    {tabName: "Applications", metaProject:null, submodelElement:"Application"}
  ]

  // Display ---------------------------------------------------------------------
  paramToDisplay = [
    ["ver", "Version: ", ""],
    ["version", "Version: ", ""],
    ["name", "", ""],
    //["kind", "", ""],
    ["host", "Host: ", ""],
    ["globalHost", "Global host: ", ""],
    ["port", "Port: ", ""],
    ["description", "", ""],
   // ["running", "Running: ", ""],
    ["schema", "Schema: ", ""],
    ["waitingTime", "Waiting time: ", " sec"],
    ["type", "Type: ", ""],
    ["manufacturerLogo", "", ""],
    ["address", "Address: ", ""]
  ]

  //addressParams = ["department", "street"] TODO loe

  ngOnInit(): void {
  }

  public async getDisplayData(tabName:string, metaProject: string | null, submodelElement: string | null) {
    this.currentTab = tabName

    await this.loadData(metaProject, submodelElement)

    // filter
    switch(this.currentTab) {
      case "Setup":
        this.filterSetup();
        break;
      case "Constants":
        this.filterConstants();
        break;
      case "Types":
        this.filterTypes();
        break;
      case "Dependencies":
          this.filterDependencies();
          break;
      case "Nameplates":
        this.filterManufacturer();
        break;
      case "Services":
        this.filterServices();
        break;
      case "Servers":
        this.filterServices();
        break;
    case "Meshes":
        this.filterMeshes();
        break;
    case "Applications":
        this.filterServices();
        break;
    default:
        break;
    }
    console.log("filteredData")
    console.log(this.filteredData)
  }

  public async loadData(metaProject: any, submodelElement: any){
    if (submodelElement) {
      this.rawData = await this.getData(submodelElement);
      this.filteredData = this.rawData.value
    } else {
      this.rawData = await this.getData("")
      this.filteredData = this.prefilter(metaProject)
    }
  }

  /**
   * It returns the whole "Configuration" submodel or only one submodelElement
   * (depending on the submodelElement parameter e.g. "Application") */
  public async getData(submodelElement: string) {
    let response;
    try {
        response = await firstValueFrom(
          this.http.get(this.ip + '/shells/'
        + this.urn
        + "/aas/submodels/Configuration/submodel/submodelElements/"
        + submodelElement));
      } catch(e) {
        console.log(e);
        //this.noData = true;
      }
    return response;
  }

  /**It returns items with given metaProject */
  public prefilter(metaProject: string | null) {
    let result = []
    for(const submodelElement of this.rawData) {
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
    return result;
  }

  // Filter ---------------------------------------------------------------------------

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
          for (let param of this.paramToDisplay) {
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

  public filterDependencies() {
    let result = []
    for (let tableRow of this.filteredData) {
      let temp = []
      let name = tableRow.idShort
      for (let value of tableRow.value) {
        if (value.idShort == "version") {
          let new_value = value.value.find(
            (elemt: { idShort: string; }) => elemt.idShort === this.varValue).value
          if(new_value != "") {
            temp.push({ "value":  "Version: " + new_value})
          }
        }
      }
      let new_value = {idShort: name, value:temp}
      result.push(new_value)
    }
    this.filteredData = result
  }

  public filterConstants() {
    let result = []
    for (let tableRow of this.filteredData) {
      let temp = []
      for (let rowValues of tableRow.value) {
        if (rowValues.idShort == this.varValue) {
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
        for (let param of this.paramToDisplay) {
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

  public filterManufacturer() {
    console.log("methode filterMan")
    console.log(this.filteredData)
    let result = []
    for (let tableRow of this.filteredData) {
      let temp = []
      let name
      let logo = null
      for (let rowValues of tableRow.value) {
        if (rowValues.idShort == "manufacturerName") {
          name = rowValues.value[0].value
        }
        for (let param of this.paramToDisplay) {
          if (rowValues.idShort == param[0]) {
            if (param[0] == "manufacturerLogo") {
              let logoValue = rowValues.value[0].value
              if(logoValue !== "") {
                logo = this.imgPath + logoValue
              }
            } else if (param[0] == "address"){
              for (let val of rowValues.value) {
                if (val.value[0].value) {
                  let addressValue = {"value": this.removeChar('@de', val.value[0].value)}
                  temp.push(addressValue)
                }
              }
            }
          }
        }
      }
      let new_value = {idShort: this.removeChar('@de', name), logo: logo, value: temp}
      result.push(new_value)
    }
    this.filteredData = result
  }

  public removeChar(char:string, str:string){
    return str.replace(char, '')
  }

  public filterServices() {
    let result = []
    for (let tableRow of this.filteredData) {
      let temp = []
      let name
      for (let rowValues of tableRow.value) {
        if (rowValues.idShort == "id") {
          name = rowValues.value[0].value
        }
        for (let param of this.paramToDisplay) {
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

  // ---- buttons ---------------------------------------------------------------

  public edit(item: any) {
    if(this.currentTab === "Meshes") { // TODO
      this.router.navigateByUrl('flowchart/' + item.idShort);
    }
  }

  public del(item: any) {

  }

  public createMesh() {

  }

  public create() {
    //this.router.navigateByUrl("list/editor/all");
    let dialogRef = this.dialog.open(EditorComponent, {
      height: '90%',
      width:  '90%',
    })
  }

  // ---- icons ------------------------------------------------------------------

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
  */

}
