import { Component, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { OnlyIdPipe } from 'src/app/pipes/only-id.pipe';
import { ApiService, ArtifactKind } from 'src/app/services/api.service';
import { PlanDeployerService } from 'src/app/services/plan-deployer.service';
import { WebsocketService } from 'src/app/websocket.service';
import { Resource, PlatformArtifacts, DEFAULT_UPLOAD_CHUNK, editorInput, configMetaEntry, configMetaContainer, Artifact, MT_metaVariable, MT_metaType } from 'src/interfaces';
import { DataUtils, Utils } from 'src/app/services/utils.service';
import { StatusCollectionNotifier } from 'src/app/services/status-collection.service';
import { chunkInput } from '../file-upload/file-upload.component';
import { MatSnackBar } from '@angular/material/snack-bar';
import { IvmlFormatterService } from '../services/ivml/ivml-formatter.service';
import { MatDialog } from '@angular/material/dialog';
import { EditorComponent } from '../editor/editor.component';

@Component({
    selector: 'app-deployment-plans',
    templateUrl: './deployment-plans.component.html',
    styleUrls: ['./deployment-plans.component.scss'],
    standalone: false
})
export class DeploymentPlansComponent extends Utils implements OnInit {

  artifacts: PlatformArtifacts = {};
  deploymentPlans: Resource | undefined = {};
  deploymentPlansConfig: Resource | undefined = {};
  selected: Resource | undefined;
  deployPlanInput: any;
  undeployPlanInput: any;
  undeployPlanByIdInput: any;
  instanceId: string[] = []; //for id input of undeployPlanById
  responseMessage: string | undefined;
  taskId: string = "";
  http: any;
  uploadFileType = ".yaml, .yml"; // suggested file extensions in browser upload dialog
  uploadEnabled = true;
  varValue = "varValue"
  filteredData: any;

  // Display ---------------------------------------------------------------------
  paramToDisplay = [
    ["ver", "Version: ", ""],
    ["version", "Version: ", ""],
    ["name", "", ""],
    ["description", "", ""]
  ]

  private subscription!: Subscription;

  constructor(public api: ApiService,
    private deployer: PlanDeployerService,
    private onlyId: OnlyIdPipe,
    private snackBar: MatSnackBar,
    private ivmlFormatter: IvmlFormatterService, 
    public dialog: MatDialog,
    public websocket: WebsocketService) {
      super();
  }

  async ngOnInit() {
    this.getDisplayData();
    this.websocket.connectToStatusUri(this.api);
  }

  public async getDisplayData() {
    this.deploymentPlansConfig = await this.ivmlFormatter.getCacheType("DeploymentPlan");
    if (this.deploymentPlansConfig) {
      this.filterDeploymentPlans();
    }
    await this.getArtifacts();
  }
  // for testing
  public setFinishedNotifier(notifier: StatusCollectionNotifier) {
    this.deployer.setFinishedNotifier(notifier);
  }

  public async getArtifacts() {
    const response = await this.api.getArtifacts();
    if(response.submodelElements) {
      this.deploymentPlans = response.submodelElements.find(
        item => item.idShort === "DeploymentPlans");
      if (this.deploymentPlansConfig) {
        let plans = DataUtils.deepCopy(this.deploymentPlansConfig);
        plans?.value?.forEach(plan => {
          let tempPlan = DataUtils.deepCopy(plan);
          let planId = DataUtils.getProperty(DataUtils.getPropertyValue(plan.value, "id"), "varValue");
          planId.idShort = "id";
          let planName = DataUtils.getProperty(DataUtils.getPropertyValue(plan.value, "name"), "varValue");
          planName.idShort = "name";
          let planDescription = DataUtils.getProperty(DataUtils.getPropertyValue(plan.value, "description"), "varValue");
          planDescription.idShort = "description";
          let planEnabled = DataUtils.getProperty(DataUtils.getPropertyValue(plan.value, "disabled"), "varValue");
          planEnabled.idShort = "enabled";
          planEnabled.value = !planEnabled.value;
          tempPlan.value = [planId, planName, planDescription, planEnabled];
          
          this.deploymentPlans?.value?.push(tempPlan);
      });
      }

      this.deployPlanInput = response.submodelElements.find(item => item.idShort === "deployPlan")?.inputVariables;
      this.undeployPlanInput = response.submodelElements.find(item => item.idShort === "undeployPlan")?.inputVariables;
      this.undeployPlanByIdInput = response.submodelElements.find(item => item.idShort === "undeployPlanWithId")?.inputVariables;

      this.instanceId.fill("", 0, this.deploymentPlans?.value?.length);

      if (this.deploymentPlans) {
        this.deploymentPlans.value?.sort((a, b) => this.getIdValue(a).localeCompare(this.getIdValue(b)));
      }
    }
  }

  private getIdValue(item: any) : string{
    let name = DataUtils.getPropertyValue(item.value, "id");
    return name;
  }

  public async hasNewPlanAppeared(): Promise<boolean> {
    const response = await this.api.getArtifacts();

    if (!response.submodelElements) {
      return false;
    }

    const newPlans = response.submodelElements.find(
      item => item.idShort === "DeploymentPlans"
    );

    return (newPlans?.value?.length ?? 0) >
          (this.deploymentPlans?.value?.length ?? 0);
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
      if(response && response.outputArguments[0] && response.outputArguments[0].value) {
        this.taskId = this.onlyId.transform(response.outputArguments[0].value.value);
      }
    }
  }

  public getDescription(plan: any) {
    let desc = plan.value.find(
      (item: { idShort: string; }) => item.idShort === "description").value
    return desc
  }

  public getPlanType(plan: any) {
    return this.isArtifact(plan) ? "Yaml Artifact" : "Ivml Configuration"
  }
  
  public getId(plan: any) {
    let id = plan.value.find(
      (item: { idShort: string; }) => item.idShort === "id").value
    return id
  }

  public getEnabled(plan: any) {
    let enabled = plan.value.find(
      (item: {idShort: string;}) => item.idShort === "enabled").value
    return enabled
  }

  public isArtifact(plan: any): boolean {
    return plan?.value?.some(
      (item: { idShort: string }) => item.idShort === "uri"
    ) ?? false;
  }

  /**
   * Called when a deployment plan shall be uploaded.
   * 
   * @param file the file to upload
   */
  public uploadFile(file: File) {
    this.uploadEnabled = false;
    chunkInput(file, DEFAULT_UPLOAD_CHUNK, (chunk, seqNr) => {
      this.api.uploadFileAsArrayBuffer(ArtifactKind.DEPLOYMENT_PLAN, seqNr, file.name, chunk);
    }, async () => {
      const timeout = Date.now() + 20000; // 10 seconds

      while (!(await this.hasNewPlanAppeared()) && Date.now() < timeout) {
        await new Promise(resolve => setTimeout(resolve, 1000));
      }
      let responseMessage = Date.now() < timeout ?
                            `Upload complete: ${file.name}` :
                            `Upload failed: ${file.name}`;
      // Toast: upload finished
      this.snackBar.open(
        responseMessage,
        'Close',
        { 
          duration: 10000,
          horizontalPosition: 'center',
          verticalPosition: 'top' 
        }
      );
      await this.getArtifacts();
      this.uploadEnabled = true;
    });

  }

  metaBackup: Resource | undefined;

  public async new() {
    this.metaBackup = await this.api.getMeta();
    let type = "DeploymentPlan";
    let meta_entry: configMetaEntry = {
      modelType: { name: "" },
      kind: "",
      value: "",
      idShort: "value"
    };
    let editorInput: editorInput = {
      name: "value",
      type: type, value: "",
      description: [{ language: '', text: '' }],
      refTo: false, multipleInputs: false, meta: meta_entry,
      isReadOnly: false, isRequired: false
    };

    let uiGroups = this.ivmlFormatter.calculateUiGroupsInf(editorInput, this.metaBackup);
    let parts = this.ivmlFormatter.partitionUiGroups(uiGroups);

    let dialogRef = this.dialog.open(EditorComponent, this.configureDialog('90%', '90%', parts));
    let component = dialogRef.componentInstance;
    component.category = type;
    component.selectedType = this.metaBackup?.value?.find(item => item.idShort === type) as configMetaContainer;
    component.meta = this.metaBackup;
    component.metaBackup = this.metaBackup;
    if (component.generateInputs) { // fails in tests
      component.generateInputs();
    }

    // Subscribe to dialog close event
    dialogRef.afterClosed().subscribe(result => {
        this.getDisplayData();
    });
  }

  /**
   * Called when an edit button in a row is pressed.
   * 
   * @param item the editor row item where the button was pressed  
   */
  public async edit(item: any) {
    this.metaBackup = await this.api.getMeta();
    this.filteredData;
    let plan = this.filteredData.find((row: RowEntry) => row.varName === item.idShort);
    
    if (plan) {
      let meta_entry: configMetaEntry = {
        modelType: { name: "" },
        kind: "",
        value: "",
        idShort: "value"
      };
      let editorInput: editorInput = {
        name: "value",
        type: plan.varType, value: plan.varValue,
        description: [{ language: '', text: '' }],
        refTo: false, multipleInputs: false, meta: meta_entry,
        isReadOnly: false, isRequired: false
      };

      let uiGroups = this.ivmlFormatter.calculateUiGroupsInf(editorInput, this.metaBackup);
      let parts = this.ivmlFormatter.partitionUiGroups(uiGroups);
      let dialogRef = this.dialog.open(EditorComponent, this.configureDialog('90%', '90%', parts));
      let component = dialogRef.componentInstance;
      editorInput.name = plan.idShort;
      component.variableName = plan.varName;
      component.type = editorInput;
      //component.showDropdown = false;
      component.category = plan.varType;
      component.selectedType = this.metaBackup?.value?.find(type => type.idShort === plan.varType) as configMetaContainer;
    
      // Subscribe to dialog close event
      dialogRef.afterClosed().subscribe(result => {
          this.getDisplayData();
      });
    }
  }

  public async del(item: any) {
    this.snackBar.open(
      `Deleting: ${item.idShort}`,
      undefined,
      { 
        duration: 7000,
        horizontalPosition: 'center',
        verticalPosition: 'top' 
      }
    );
    await this.ivmlFormatter.deleteVariable(item.idShort);

    this.getDisplayData();
  }

  /**
   * Filters the row data/display data for the deployment plan category.
   */
  public filterDeploymentPlans() {
    let temp: any = [];
    let name: any;
    this.filteredData = this.createRows(this.deploymentPlansConfig?.value, (row, rowType) => {
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
      let val: any = [];
      let varName = DataUtils.getPropertyValue(tableRow.value, MT_metaVariable) || tableRow.idShort;
      let rowEntry = new RowEntry({ idShort: tableRow.idShort, varName: varName, varValue: val });
      let rowType = DataUtils.getPropertyValue(tableRow.value, MT_metaType) //tableRow.value[0].value
      this.api.createRowValue(tableRow.value, val, true, row => {
        let result = rowFn(row, rowType);
        if (row.idShort == MT_metaType) {
          rowEntry.varType = row.value
        }
        return result;
      });
      rowEntry.varValue = val;
      resultFn(rowEntry);
      result.push(rowEntry);
    }
    return result;
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
        let new_rowValue = { "value": param[1] + tmp + param[2] };
        result.push(new_rowValue);
      }
    }
  }
}

class RowEntry {

  idShort: any;
  logo: any;
  value: any;
  varName: any;
  varType: any;
  varValue: any;

  constructor(init?: Partial<RowEntry>) {
    Object.assign(this, init);
  }

}