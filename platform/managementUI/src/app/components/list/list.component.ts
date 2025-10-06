import { AAS_OP_PREFIX_SME, AAS_TYPE_STRING, ApiService, ArtifactKind, IDSHORT_SUBMODEL_CONFIGURATION } from 'src/app/services/api.service';
import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';
import { EditorComponent } from '../editor/editor.component';
import { DEFAULT_UPLOAD_CHUNK, InputVariable, MT_metaType, MT_metaVariable, MT_varValue, Resource, configMetaEntry, editorInput } from 'src/interfaces';
import { Utils, DataUtils } from 'src/app/services/utils.service';
import { WebsocketService } from 'src/app/websocket.service';
import { StatusCollectionService } from 'src/app/services/status-collection.service';
import { chunkInput } from '../file-upload/file-upload.component';
import { IvmlFormatterService } from 'src/app/components/services/ivml/ivml-formatter.service';

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
    styleUrls: ['./list.component.scss'],
    standalone: false
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
  selectedType: Resource | undefined;
  private appFiles = new Map<string, FileUploadInfo>();
  meta: Resource | undefined;
  metaBackup: Resource | undefined;

  constructor(private router: Router,
    public http: HttpClient,
    public api: ApiService,
    public dialog: MatDialog,
    public websocketService: WebsocketService, 
    public collector: StatusCollectionService, 
    private ivmlFormatter: IvmlFormatterService) {
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
    await this.websocketService.connectToStatusUri(this.api);
    this.populateMeta();
  }

  private async populateMeta() {
    this.metaBackup = await this.api.getMeta();
    this.meta = this.ivmlFormatter.filterMeta(this.metaBackup, this.currentTab);
  }

  public async getDisplayData(tabName:string, metaProject: string | null, submodelElement: string | null) {
    this.currentTab = tabName
    this.selectedType = undefined;
    if (this.metaBackup) {
      this.meta = this.ivmlFormatter.filterMeta(this.metaBackup, this.currentTab);
      if (this.meta && this.meta.value && this.meta.value?.length > 0) {
        this.selectedType = this.meta.value[0]; // TODO sort, select most frequent/plausible
      }
    }

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
      this.rawData = await this.api.getConfiguredElements(submodelElement);
      if (this.rawData) {
        this.filteredData = this.rawData.value
      } else {
        this.rawData = []
        this.filteredData = []
      }
    } else {
      this.rawData = await this.api.getConfiguredElements("")
      this.filteredData = this.prefilter(metaProject)
    }
    this.filteredData = this.filterMetaTemplate(this.filteredData);
  }

  /**It returns items with meta_template = false, 
   * If the meta_template not exist, then return the item.
   */
  public filterMetaTemplate(data: any) {
    let result = [];
    let isMetaTemplateExist = false;
    for(const submodelElement of data) {
      if(submodelElement.value) {
        for(const elemtSubmodelElement of submodelElement.value) {
          if (elemtSubmodelElement.idShort == "metaTemplate") {
            isMetaTemplateExist = true;
            if (!elemtSubmodelElement.value) {
              result.push(submodelElement);
              break;
            }
          }
        }
        if (!isMetaTemplateExist) {
          result.push(submodelElement);
        }
      }
    }
    return result;
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
      this.api.createRowValue(tableRow.value, val, true, row => {
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
      let meta_entry:configMetaEntry = {
        modelType: {name: ""},
        kind: "",
        value: "",
        idShort: "value"
      };
      let editorInput:editorInput = {name: "value", 
        type: item.varType, value:item.varValue,
        description: [{language: '', text: ''}],
        refTo: false, multipleInputs: false, meta:meta_entry};

      let uiGroups = this.ivmlFormatter.calculateUiGroupsInf(editorInput, this.metaBackup);
      let parts = this.ivmlFormatter.partitionUiGroups(uiGroups);
      let dialogRef = this.dialog.open(EditorComponent, this.configureDialog('90%', '90%', parts));
      let component = dialogRef.componentInstance;
      editorInput.name = item.idShort;
      component.variableName = item.varName;
      component.type = editorInput; 
      //component.showDropdown = false;
      component.category = this.currentTab;
      component.selectedType = this.selectedType;
    }
  }

  public async del(item: any) {
    if (this.currentTab === "Meshes") {
      console.log("IMPLEMENT DELETE " + JSON.stringify(item));
      //TODO this.ivmlFormatter.deleteMesh(); // needs appName
    } else {
      await this.ivmlFormatter.deleteVariable(item.varName);
    }
    // TODO feedback
  }

  /**
   * Returns the tab-specific tooltip text for the given action (prefixed).
   * 
   * @param action the action 
   * @returns the tooltip text
   */
  public getTooltipText(action: string) {
    let entryType = this.currentTab.toLowerCase();
    if (entryType.endsWith("es")) {
      entryType = entryType.substring(0, entryType.length - 2);
    } else if (entryType.endsWith("s")) {
      entryType = entryType.substring(0, entryType.length - 1);
    }
    return `${action} ${entryType}`;
  }

  /*public createMesh() {
    console.log("IMPLEMENT CREATE MESH");
    // TODO
  }*/

  /*public create() {
    //this.router.navigateByUrl("list/editor/all");
    let dialogRef = this.dialog.open(EditorComponent, this.configureDialog('90%', '90%', null));
  }*/

  public new() {
    if (this.currentTab == 'Meshes') {
      this.router.navigateByUrl("flowchart");
    } else {
      let type = this.selectedType?.idShort || "";
      let meta_entry:configMetaEntry = {
        modelType: {name: ""},
        kind: "",
        value: "",
        idShort: "value"
      };
      let editorInput:editorInput = {name: "value", 
        type: type, value : "",
        description: [{language: '', text: ''}],
        refTo: false, multipleInputs: false, meta:meta_entry};

      let uiGroups = this.ivmlFormatter.calculateUiGroupsInf(editorInput, this.metaBackup);
      let parts = this.ivmlFormatter.partitionUiGroups(uiGroups);

      let dialogRef = this.dialog.open(EditorComponent, this.configureDialog('90%', '90%', parts));
      let component = dialogRef.componentInstance;
      component.category = this.currentTab;
      component.selectedType = this.selectedType;
      if (component.generateInputs) { // fails in tests
        component.generateInputs();
      }
    }
  }

  public genTemplate(appId: string) {
    let inputVariables: InputVariable[] = [];
    inputVariables.push(ApiService.createAasOperationParameter("appId", AAS_TYPE_STRING, appId));
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
    inputVariables.push(ApiService.createAasOperationParameter("appId", AAS_TYPE_STRING, appId));
    let fileInfo = this.appFiles.get(appId);
    let fileName = fileInfo?.file.name  || '';
    this.appFiles.delete(appId);
    inputVariables.push(ApiService.createAasOperationParameter("codeFile", AAS_TYPE_STRING, fileName));
    this.execFunctionInConfig("genAppsAsync", inputVariables)
  }

  public async execFunctionInConfig(basyxFun: string, inputVariables: InputVariable[]) {
      await this.api.executeAasJsonOperation(IDSHORT_SUBMODEL_CONFIGURATION, AAS_OP_PREFIX_SME + basyxFun, inputVariables);
  }

}
