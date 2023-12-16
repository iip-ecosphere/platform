import { ApiService, ArtifactKind } from 'src/app/services/api.service';
import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Subscription, firstValueFrom } from 'rxjs';
import { EnvConfigService } from 'src/app/services/env-config.service';
import { Router } from '@angular/router';
import { MatDialog, MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import { MAT_PROGRESS_SPINNER_DEFAULT_OPTIONS_FACTORY } from '@angular/material/progress-spinner';
import { EditorComponent } from '../editor/editor.component';
import { DEFAULT_UPLOAD_CHUNK, DR_displayName, DR_idShort, DR_type, InputVariable, MTK_compound, MTK_container, MT_metaDisplayName, MT_metaSize, MT_metaType, MT_metaTypeKind, MT_metaVariable, MT_varValue, allMetaTypes, configMetaEntry, editorInput, platformResponse } from 'src/interfaces';
import { Utils, DataUtils } from 'src/app/services/utils.service';
import { WebsocketService } from 'src/app/websocket.service';
import { StatusCollectionService } from 'src/app/services/status-collection.service';
import { chunkInput } from '../file-upload/file-upload.component';

/**
 * Information on a file being uploaded.
 */
interface FileUploadInfo {
  
  /**
   * File object representing the data to upload.
   */
  file: File,

  /**
   * User flag to indicate processing on this file info.
   */
  uploading: boolean

}

class RowEntry {

  idShort: any; 
  logo: any;
  value: any;
  varName: any; 
  varType: any;
  varValue: any;

  constructor(init?:Partial<RowEntry>) {
    Object.assign(this, init);
  }

}

@Component({
  selector: 'app-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss']
})
export class ListComponent extends Utils implements OnInit {
  //currentTab: string | null = null;
  currentTab = "";
  rawData: any;
  filteredData: any;
  varValue = "varValue"
  imgPath = "../../../assets/"
  sub: Subscription | undefined;
  uploadFileTypes = ".zip"; // suggested file extensions in browser upload dialog
  private appFiles = new Map<string, FileUploadInfo>();

  constructor(private router: Router,
    public http: HttpClient,
    private envConfigService: EnvConfigService,
    public api: ApiService,
    public dialog: MatDialog,
    public websocketService: WebsocketService, 
    public collector: StatusCollectionService) {
      super();
      this.sub = websocketService.getMsgSubject().subscribe((value: any) => {
        collector.receiveStatus(JSON.parse(value)) 
      })      
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

  async ngOnInit() {
    this.websocketService.connectToStatusUri(this.api);
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
      case "Meshes":
        this.filterMeshes();
        break;
      case "Services":
      case "Servers":
      case "Applications":
        this.filterServicesServersApps();
        break;
      default:
        break;
    }
  }

  public async loadData(metaProject: any, submodelElement: any){
    if (submodelElement) {
      this.rawData = await this.getData(submodelElement);
      if (this.rawData) {
        this.filteredData = this.rawData.value
      } else {
        this.rawData = []
        this.filteredData = []
      }
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
        let cfg = await this.envConfigService.initAndGetCfg();
        response = await firstValueFrom(
          this.http.get(cfg?.ip + '/shells/'
        + cfg?.urn
        + "/aas/submodels/Configuration/submodel/submodelElements/"
        + submodelElement));
      } catch(e) {
        console.error(e);
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
            let val = String(valElemtSubmodelElement.value);
            if(valElemtSubmodelElement.idShort == "metaProject" && 
                (val == metaProject || val.startsWith(metaProject + "Part")) ) { // part check for testing models
                result.push(elemtSubmodelElement)
            }
          }
        }
      }
    }
    return result;
  }

  // Filter ---------------------------------------------------------------------------

  /**
   * Filters the row data/display data for the setup category.
   */
  public filterSetup() {
    let temp : any = [];
    let val: any;
    this.filteredData = this.createRows(this.filteredData, (row, rowType) => {
      let goOn = false;
      if ((typeof rowType) == "string") {
        if (row.idShort == MT_varValue) {
          let new_rowValue = {"value": row.value}
          val = row.value
          temp.push(new_rowValue)
        }
      } else if ((typeof rowType) == "number") {
        if (row.idShort == MT_varValue) {
          val = row.value
        }
        // AAS  startup timeout
        let new_rowValue = {"value": rowType  + " sec"} // TODO is it true?
        temp.push(new_rowValue)
      } else {
        goOn = true;
        /*if (row.idShort == MT_varValue) {
          val = row.value
        }*/
        this.composeValueByFilter(row, temp, this.paramToDisplay);
      }
      return goOn;
    }, rowResult => {
      if (val) { // do this only for string and number, do not override compound/array values
        rowResult.varValue = val;
      }
      rowResult.value = temp; 
      temp = [];  
      val = undefined;
    });
  }

  /**
   * Recursive function to turn a single AAS JSON data row into an internal data structure. Considers (recursive) IVML collection sub-structures. 
   * Takes metaType, metaSize and varValue from the platform generated AAS entries into account.
   * 
   * @param values the row values as AAS JSON 
   * @param result to accumulate the result of this row, a RowEntry on top level, a array on nested level
   * @param top is this call a top level call or a nested recursive call
   * @param rowFn additional function to apply on row data to extract further data
   */
  createRowValue(values: any, result: any, top:boolean, rowFn: (row:any) => boolean) {
    for (let value of values) {
      let fieldName = value.idShort;
      let goOn = rowFn(value);
      if (goOn && !allMetaTypes.includes(fieldName)) {
        let displayName = null;
        let val: any = null;
        if (this.isArray(value.value)) {
          displayName = DataUtils.getPropertyValue(value.value, MT_metaDisplayName);
          let fieldTypeKind = DataUtils.getPropertyValue(value.value, MT_metaTypeKind);
          if (fieldTypeKind == MTK_container) {
            let fieldSize = DataUtils.getPropertyValue(value.value, MT_metaSize);
            if (fieldSize) {
              val = [];
              for (let i = 0; i < fieldSize; i++) {
                let fVal : any = {}; // TODO not if contained type is primitive
                let fName = fieldName + "__" + i + "_";
                let fProp = DataUtils.getProperty(value.value, fName);
                if (fProp && fProp.value) {
                  this.createRowValue(fProp.value, fVal, false, v => true);
                  let fId = fVal["id"] || fVal["name"] || String(i);
                  fVal[DR_idShort] = fId;
                  this.addPropertyFromData(fVal, DR_type, fProp.value, MT_metaType);
                  val.push(fVal);
                }
              }
            }
          } else if (fieldTypeKind == MTK_compound) {
            val = {};
            this.createRowValue(value.value, val, false, v => true);
          } else {
            val = DataUtils.getPropertyValue(value.value, MT_varValue);
          }
        }
        if (top) {
          let instance: any = {idShort: fieldName, value:val};
          if (displayName) {
            instance[DR_displayName] = displayName;
          }
          result.push(instance);
        } else {
          result[fieldName] = val;
        }
      }
    }
  }

  private addPropertyFromData(object: any, propertyName: string, data: any[], dataPropertyName: string) {
    let value = DataUtils.getPropertyValue(data, dataPropertyName);
    if (value) {
      object[propertyName] = value;
    }
  }

  /**
   * Creates an internal table data structure for AAS JSON data.
   * 
   * @param data the AAS JSON data
   * @param rowFn customizing function to process the data per row, e.g., to store information into local variables and
   *   to add this data to the result in "resultFn"
   * @param resultFn customizing function to finalize the row result initialized with default information
   * @returns the data structure as nesting of arrays, objects and values
   */
  createRows(data: any, rowFn: (row: any, rowType: any) => boolean, resultFn: (rowResult: RowEntry) => void) {
    let result = []
    for (let tableRow of data) {
      let val : any = [];
      let varName = DataUtils.getPropertyValue(tableRow.value, MT_metaVariable) || tableRow.idShort;
      let rowEntry = new RowEntry({idShort: tableRow.idShort, varName: varName, varValue: val});
      let rowType = tableRow.value[0].value
      this.createRowValue(tableRow.value, val, true, row => {
        let result = rowFn(row, rowType);
        if (row.idShort == MT_metaType) {
          rowEntry.varType = row.value
        }
        return result;
      });
      rowEntry.varValue = val;
      resultFn(rowEntry);
      result.push(rowEntry)
    }
    return result;
  }

  /**
   * Filters the row data/display data for the types category.
   */
  public filterTypes() {
    this.filteredData = this.createRows(this.filteredData, (row, rowType) => true, rowResult => {});
  }

  /**
   * Filters the row data/display data for the dependencies category.
   */
  public filterDependencies() {
    let temp : any = [];
    this.filteredData = this.createRows(this.filteredData, (row, rowType) => {
      if (row.idShort == "version") {
        let new_value = DataUtils.getPropertyValue(row.value, this.varValue);
        if(new_value != "") {
          temp.push({ "value":  "Version: " + new_value})
        }
      }
      return true;
    }, rowResult => {
      rowResult.value = temp; 
      temp = [];
    });
  }

  /**
   * Filters the row data/display data for the constants category.
   */
  public filterConstants() {
    let temp : any = [];
    let val: any;
    this.filteredData = this.createRows(this.filteredData, (row, rowType) => {
      if (row.idShort == this.varValue) {
        let new_rowValue = {"value": row.value};
        temp.push(new_rowValue);
        val = row.value;
      }
      return true;
    }, rowResult => {
      rowResult.value = temp; 
      temp = [];  
      rowResult.varValue = val
    });
  }

  /**
   * Filters the row data/display data for the meshes category.
   */
  public filterMeshes() {
    let temp : any = [];
    this.filteredData = this.createRows(this.filteredData, (row, rowType) => {
      this.composeValueByFilter(row, temp, this.paramToDisplay);
      return true;
    }, rowResult => {
      rowResult.value = temp; 
      temp = [];  
    });
  }

  /**
   * Filters the row data/display data for the manufacturer category.
   */
  public filterManufacturer() {
    let temp : any = [];
    let name: any;
    let logo: any = null;
    this.filteredData = this.createRows(this.filteredData, (row, rowType) => {
      if (row.idShort == "manufacturerName") {
        name = row.value[0].value
      }
      for (let param of this.paramToDisplay) {
        if (row.idShort == param[0]) {
          if (param[0] == "manufacturerLogo") {
            let logoValue = row.value[0].value
            if(logoValue !== "") {
              logo = this.imgPath + logoValue
            }
          } else if (param[0] == "address") {
            for (let val of row.value) {
              if (this.isArray(val.value) && val.value[0].value) {
                let addressValue = {"value": DataUtils.getLangStringText(val.value[0].value)}
                temp.push(addressValue)
              }
            }
          }
        }
      }
      return true;      
    }, rowResult => {
      rowResult.idShort = DataUtils.getLangStringText(name);
      rowResult.logo = logo;
      rowResult.value = temp; 
      temp = [];
      logo = null;
    });
  }

  private removeChar(char:string, str:string){
    return str.replace(char, '')
  }

  /**
   * Filters the row data/display data for the services, servers and apps category.
   */
  public filterServicesServersApps() {
    let temp : any = [];
    let name: any;
    this.filteredData = this.createRows(this.filteredData, (row, rowType) => {
      if (row.idShort == "id") {
        name = row.value[0].value
      } 
      this.composeValueByFilter(row, temp, this.paramToDisplay);
      return true;
    }, r => {
      r.idShort = name;
      r.value = temp; 
      temp = [];  
    });
  }

  /**
   * Composes a display value by filter.
   * 
   * @param value the AAS value to take as basis 
   * @param result the result array containing the lines to display
   * @param filter  the filtering idShorts
   */
  private composeValueByFilter(value: any, result: any[], filter: any[]) {
    for (let param of filter) {
      if (value.idShort == param[0]) {
        let tmp = DataUtils.getPropertyValue(value.value, this.varValue);
        let new_rowValue = { "value":  param[1] + tmp + param[2]};
        result.push(new_rowValue);
      }
    }
  }

  // ---- buttons ---------------------------------------------------------------

  /**
   * Called when an edit button in a row is pressed.
   * 
   * @param item the editor row item where the button was pressed  
   */
  public edit(item: any) {
    if (this.currentTab === "Meshes") {
      this.router.navigateByUrl('flowchart/' + item.idShort);
    } else {
      let dialogRef = this.dialog.open(EditorComponent, {
        height: '90%',
        width:  '90%',
      })

      let meta_entry:configMetaEntry = {
        modelType: {name: ""},
        kind: "",
        value: "",
        idShort: "value"
      }
      
      let editorInput:editorInput =
        {name: "value", type: item.varType, value:item.varValue,
        description: [{language: '', text: ''}],
        refTo: false, multipleInputs: false, meta:meta_entry}

      let component = dialogRef.componentInstance;
      editorInput.name = item.idShort;
      component.variableName = item.varName;
      component.type = editorInput; 
      component.showDropdown = false;
      component.category = this.currentTab;
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

  public new() {
    if(this.currentTab == 'Meshes') {
      this.router.navigateByUrl("flowchart");
    } else {
      let dialogRef = this.dialog.open(EditorComponent, {
        height: '90%',
        width:  '90%',
      })
      dialogRef.componentInstance.category = this.currentTab;
    }
  }

  public genTemplate(appId: string) {
    let inputVariables: InputVariable[] = [];
    let input0:InputVariable = {
      value: {
        modelType: {
          name: "Property"
        },
        valueType: "string",
        idShort: "appId",
        kind: "Template",
        value: appId
      }
    }
    inputVariables.push(input0)
    this.execFunctionInConfig("genAppsNoDepsAsync", inputVariables)
  }

  public isUploading(appId: string) {
    let info = this.appFiles.get(appId);
    if (info) {
      return info.uploading;
    } else {
      return false;
    }
  }

  public uploadAppFile(appId: string, file: File) {
    let info : FileUploadInfo = {file: file, uploading: true};
    this.appFiles.set(appId, info);
    chunkInput(file, DEFAULT_UPLOAD_CHUNK, (chunk, seqNr) => {
      this.api.uploadFileAsArrayBuffer(ArtifactKind.IMPLEMENTATION_ARTIFACT, seqNr, file.name, chunk);
    }, () => {
      info.uploading = false;
    });
  }

  public async genApp(appId: string) {
    let inputVariables: InputVariable[] = [];
    let input0:InputVariable = {
      value: {
        modelType: {
          name: "Property"
        },
        valueType: "string",
        idShort: "appId",
        kind: "Template",
        value: appId
      }
    }

    let fileInfo = this.appFiles.get(appId);
    let fileName = fileInfo?.file.name  || '';

    // TODO UPLOAD SOMEHOW, AAS?

    let input1:InputVariable = {
      value: {
        modelType: {
          name: "Property"
        },
        valueType: "string",
        idShort: "codeFile",
        kind: "Template",
        value: fileName
      }
    }
    inputVariables.push(input0)
    inputVariables.push(input1)
    this.execFunctionInConfig("genAppsAsync", inputVariables)
  }

  public async execFunctionInConfig(basyxFun: string, inputVariables: any) {
    let resourceId = ""
    let aasElementURL = "/aas/submodels/Configuration/submodel/submodelElements/"

    const response = await this.api.executeFunction(
      resourceId,
      aasElementURL,
      basyxFun,
      inputVariables) as unknown as platformResponse
  }

  // ---- icons ------------------------------------------------------------------

  /*icons = [ // TODO too specific, used at all=
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
  }*/

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
