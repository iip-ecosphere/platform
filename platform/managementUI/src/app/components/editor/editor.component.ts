import { Component, EventEmitter, Input, OnInit } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';
import { ApiService } from 'src/app/services/api.service';
import { IvmlFormatterService } from 'src/app/services/ivml-formatter.service';
import { Resource, uiGroup, editorInput, configMetaContainer, ResourceAttribute, 
  primitiveDataTypes, IvmlRecordValue, IvmlValue, UserFeedback, MT_metaRefines} from 'src/interfaces';
import { Utils, DataUtils } from 'src/app/services/utils.service';
import { SaveEvent } from './inputControls/subeditor-button/subeditor-button.component';

@Component({
  selector: 'app-editor',
  templateUrl: './editor.component.html',
  styleUrls: ['./editor.component.scss']
})
export class EditorComponent extends Utils implements OnInit {

  //type to generate subeditor for, null if this editor instance is not a subeditor
  @Input() type: editorInput | null = null;

  //for generating dropdown options of abstract type
  @Input() refinedTypes: ResourceAttribute[] | null = null;


  category: string = 'all';
  meta: Resource | undefined;
  /* backup needed for data recovery before re-filtering data
   for the next tab */
  metaBackup: Resource | undefined;
  selectedType: Resource | undefined;

  uiGroups: uiGroup[] = [];

  //showDropdown = true;
  showInputs = true;
  variableName = '';
  ivmlType:string = "";
  feedback: string = ""
  topLevel: boolean = true;
  saveEvent: EventEmitter<SaveEvent> | null = null;

  constructor(private api: ApiService,
    public dialog: MatDialogRef<EditorComponent>,
    public ivmlFormatter: IvmlFormatterService) {
      super();
  }

  async ngOnInit() {
    if (this.refinedTypes) {
      this.meta = {
        idShort: 'meta',
        value: this.refinedTypes
      }
    } else if(!this.type) {
      await this.populateMeta();
    } else if (this.type.type){
      if (!this.metaBackup || !this.metaBackup.value) {
        await this.populateMeta();
      }
      if (this.metaBackup && this.metaBackup.value) {
        let type = DataUtils.stripGenericType(this.type.type);
        this.selectedType = this.metaBackup.value.find(item => item.idShort === type);
        this.generateInputs()
      }
    }
    if(this.metaBackup && this.metaBackup.value) {
      let searchTerm = 'Field'
      for(const type of this.metaBackup.value) {
        const refined = DataUtils.getProperty(type.value, MT_metaRefines);
        if(refined && refined.value != '') {
          if(searchTerm === refined.value) {
            console.debug("TYPE " + type);
          }
        }
      }
    }
  }

  private async populateMeta() {
    this.meta = await this.api.getMeta();
    this.metaBackup = DataUtils.deepCopy(this.meta);
    this.meta = this.ivmlFormatter.filterMeta(this.metaBackup, this.category);
    // single item
    let newMetaValues = this.meta!.value; 
    if (newMetaValues && newMetaValues.length == 1) {
      this.selectedType = newMetaValues[0];
      this.generateInputs();
    }
  }

  /**
   * Returns the display title of the dialog.
   * 
   * @returns the display title
   */
  public getDisplayTitle() {
    let result = this.category;
    if (this.selectedType && this.selectedType.idShort) {
      result = this.selectedType.idShort;
    }
    return result;
  }

  public displayName(property: Resource | string) {
    let displayName = '';
    if(typeof(property) == 'string') {
      displayName = property;
    } else if(property.value) {
      displayName = property.value.find(
        item => item.idShort === 'name')?.value;
    }
    return displayName;
  }

  public generateInputs() {
    const selectedType = this.selectedType as configMetaContainer;
    this.ivmlType = selectedType.idShort
    this.uiGroups = this.ivmlFormatter.calculateUiGroups(selectedType, this.type, this.meta, this.metaBackup);
  }

  public toggleOptional(uiGroup: uiGroup) {
    uiGroup.toggleOptional = !uiGroup.toggleOptional;
  }

  /**
   * Called when creating a new variable is requested from the editor.
   */
  public async create() {
    let creationData: IvmlRecordValue = {};
    this.showInputs = false;
    this.transferUiGroups(this.uiGroups, creationData);
    let variableName = this.ivmlFormatter.generateVariableName(this.ivmlType, creationData);
    if (this.selectedType?.idShort == "Application") {
      this.handleFeedback(await this.ivmlFormatter.createApp(variableName, creationData));
    } else {
      this.handleFeedback(await this.ivmlFormatter.createVariable(variableName, creationData, this.ivmlType));
    }
  }

  /**
   * Called to close the editor.
   */
  public close() {
    this.dialog.close();
  };

  /**
   * Called from the editor to save the entered values into type.value. This may be called in the top-level editor or a sub-level editor.
   */
  public async save() {
    let complexType: IvmlRecordValue = {};

    if (this.type) {
      this.transferUiGroups(this.uiGroups, complexType);
      if (!DataUtils.isEmpty(complexType)) {
        if (this.topLevel) {
          this.handleFeedback(await this.ivmlFormatter.setVariable(this.variableName, complexType, this.ivmlType));
        } else if (this.saveEvent) {
          this.saveEvent.emit({idShort: this.type.name, value: complexType, multipleInputs: this.type.multipleInputs});
        }
      }
    }
    this.dialog.close(); 
  }

  /**
   * From subeditor via subeditor-button when dialog is being saved. If called, this is in the parent type/value.
   * 
   * @param event the event
   */
  public saveEventHandler(event: SaveEvent) {
    let prop = DataUtils.getProperty(this.type?.value, event.idShort); // sub-level editor comes from an existing property, shall exist
    let host;
    if (event.multipleInputs) {
      host = {value: []};
    } else {
      host = prop;
    }
    for (let entry in event.value) {
      let src = event.value[entry];
      if (host.value.hasOwnProperty(entry)) {
        host.value[entry] = src.value;
      } else {
        if (this.isArray(host.value)) {
          host.value.push(src.value); 
        } else {
          host.value[entry] = src.value;
        }
      }
    }
    if (event.multipleInputs) {
      prop.value.push(host);
    }
  }

  private handleFeedback(feedback: UserFeedback) {
    this.feedback = feedback.feedback;
  }

  /**
   * Transfers all inputs from uiGroups to result.
   * Calls {@link this.transferInputs}.
   * 
   * @param uiGroups the UI groups 
   * @param result the results object to be modified as a side effect
   */
  private transferUiGroups(uiGroups: uiGroup[], result: IvmlRecordValue) {
    for (let uiGroup of this.uiGroups) {
      this.transferInputs(uiGroup.inputs, result);
      this.transferInputs(uiGroup.optionalInputs, result);
      this.transferInputs(uiGroup.fullLineInputs, result);
      this.transferInputs(uiGroup.fullLineOptionalInputs, result);
    }
  }

  /**
   * Transfers the values in the given inputs into properties of result filtering out unchanged IVML default values.
   * Calls {@link this.getValue}.
   * 
   * @param inputs the editor inputs to process 
   * @param result the results object to be modified as a side effect
   */
  private transferInputs(inputs: editorInput[], result: IvmlRecordValue) {
    for (let input of inputs) {
      let tmp: any = null;
      if (input.meta) {
        tmp = this.getValue(input);
      } else if (primitiveDataTypes.includes(input.type)) { // was only in prepareCreation and only for uiGroup.inputs
        tmp = this.getValue(input);
      }
      if (tmp && tmp != input.defaultValue) { // don't write back IVML default values
        let val : IvmlValue = {value: tmp, _type: input.type};
        result[input.name] = val;
        //result[input.name] = tmp;
      }
    }
  }

}
