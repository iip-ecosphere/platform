import { Component, EventEmitter, Input, OnChanges, OnInit, Output } from '@angular/core';
import { Resource, editorInput, configMeta, metaTypes, DR_type, IvmlRecordValue } from 'src/interfaces';
import { DataUtils, EditorPartition, Utils, WIDTH_CARD, WIDTH_CARD_GRID } from 'src/app/services/utils.service';
import { EditorComponent } from '../editor.component';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { ApiService } from 'src/app/services/api.service';
import { IvmlFormatterService } from 'src/app/components/services/ivml/ivml-formatter.service';

@Component({
    selector: 'app-input-ref-select',
    templateUrl: './input-ref-select.component.html',
    styleUrls: ['./input-ref-select.component.scss'],
    standalone: false
})
export class InputRefSelectComponent extends Utils implements OnInit {

  @Input() activeTextinput = false;
  @Input() input: editorInput = {name: '', type: '', description: [{text: '', language: ''}], refTo: true, value: undefined, isReadOnly: false};
  @Input() meta: Resource | undefined;
  @Input() rows: number = -1; // unset, calculate
  @Output() saveEvent = new EventEmitter<SaveEvent>();
  
  textInput: string | null = null;
  isSetOf = false;
  isSequenceOf = false;
  refTo = '';
  references: Resource[] = [];
  selectedRef: configMeta | undefined;
  selector: boolean = false;
  parent: InputRefSelectComponent | null = null;
  dialog: MatDialogRef<InputRefSelectComponent, any> | null = null;
  actualRows : number = 2;

  constructor(private api: ApiService, public subDialog: MatDialog, private ivmlFormatter: IvmlFormatterService) { 
    super();
  }

  async ngOnInit() {
    if (!this.selector) {
      let type = this.input.type;
      await this.init(type);
    }
  }

  /**
   * The width of the floating container in em.
   * 
   * @returns the with in em
   */
  getContainerWidth() {
    let boxesWidth = this.actualRows * WIDTH_CARD;
    let distWidth = Math.max(0, this.actualRows - 1) * WIDTH_CARD_GRID;    
    return `${boxesWidth + distWidth}em`;
  }

  /**
   * Closes the selector dialog.
   * 
   * @param save whether the selected value shall be saved or the dialog shall just be closed
   */
  closeSelector(save:boolean) {
    if (this.parent && save) {
      this.parent.selectedRef = this.selectedRef;
      this.parent.textInput = this.textInput;
      if (this.activeTextinput) {
        this.parent.addFromTextfield();
      } else {
        this.parent.addFromRef();
      }
    }
    if (this.dialog) {
      this.dialog.close();
    }
  }

  /**
   * Opens this component as a selector dialog focussing on the IVML element type in the parent IVML reference/collection.
   */
  selectElement() {
    if (this.references[0] || this.activeTextinput) {
      let parts = [{count : 1, columns : 1}] as EditorPartition[];
      let dialog = this.subDialog.open(InputRefSelectComponent, this.configureDialog('40%', '40%', parts));
      let comp = dialog.componentInstance;
      // transfer relevant values
      comp.selector = true;
      comp.references = DataUtils.deepCopy(this.references);
      comp.activeTextinput = this.activeTextinput;
      comp.input = DataUtils.deepCopy(this.input);
      // link to this as parent and record dialog for closing
      comp.parent = this;
      comp.dialog = dialog;
    }
  }

  private async init(type: string) {
    if (type) {
      if (DataUtils.isIvmlSet(type)) {
        this.isSetOf = true;
      }
      if (DataUtils.isIvmlRefTo(type)) {
        this.refTo = DataUtils.stripGenericType(type);
        await this.getConfigurationType(this.refTo);
        this.activeTextinput = false;
      }
      if (DataUtils.isIvmlSequence(type)) {
        this.isSequenceOf = true;
      }
    }
    if (!this.input.value) {
      if (this.isSetOf || this.isSequenceOf) {
        this.input.value = [];
      } else {
        this.input.value = null;
      }
    }
    if (this.input.metaTypeKind == 10 || this.input.metaTypeKind == 2) {
      this.activeTextinput = false;
    }
    // auto-init rows if not given
    if (this.rows <= 0) {
      if (this.isSetOf || this.isSequenceOf) {
        this.actualRows = 2;
      } else {
        this.actualRows = 1;
      }
    }
  }

  public async getConfigurationType(type: string) {
    const response = await this.api.getConfiguredElements(type);
    if (response && response.value) {
      for (let dep of response.value) {
        if (dep.idShort && metaTypes.indexOf(dep.idShort) === -1) {
          this.references.push(dep);
        }
      }
    }
  }

  /**
   * Adds a selected value to the collection/reference value. 
   */
  public addFromRef() {
    if (this.selectedRef && this.selectedRef.idShort) {
      if (this.isSetOf) {
        this.input.value.push(this.selectedRef.idShort);
      } else {
        this.input.value = this.selectedRef.idShort;
      }
    }
  }

  /**
   * Adds a text value to the collection/reference value. 
   */
  public addFromTextfield() {
    if (this.textInput) {
      if (this.isSetOf || this.isSequenceOf) {
        this.input.value.push(this.textInput);
      } else {
        this.input.value = this.textInput;
      }
    }
  }

  public async editInputValue(editIndex: number) {
    let varName = null;
    let input = DataUtils.deepCopy(this.input);
    input.value = this.input.value[editIndex];
    if (input.value._type && input.value.value) { // IVMLValue -> extract
      let type = input.value._type;
      if (DataUtils.isIvmlRefTo(type)) { // we have a reference, resolve to ease UI
        varName = input.value.value;
        let data = await this.api.getConfiguredElements(DataUtils.stripGenericType(type));
        input.value = {};
        this.api.createRowValue(DataUtils.getPropertyValue(data.value, varName), input.value, false, r => true);
      } else {
        input.value = input.value.value;
      }
    }
    input.type = DataUtils.stripGenericType(input.type);
    if (input.value[DR_type]) { // override with dynamic IVML type if known
      input.type = input.value[DR_type];
    }
    let uiGroups = this.ivmlFormatter.calculateUiGroupsInf(input, this.meta);
    let parts = this.ivmlFormatter.partitionUiGroups(uiGroups);
    let dialogRef = this.subDialog.open(EditorComponent, this.configureDialog('80%', '80%', parts));
    dialogRef.componentInstance.type = input;
    dialogRef.componentInstance.metaBackup = this.meta;
    if (varName) {
      dialogRef.componentInstance.variableName = varName;
    }
  }

  public removeInputValue(removeIndex: number) {
    let newInputs = [];
    let index = 0;
    for (const input of this.input.value) {
      if (index != removeIndex) {
        newInputs.push(this.input.value[index]);
      }
      index++;
    }
    this.input.value = newInputs;
  }

  // public valueboxClass() {
  //   let cssclass = '';
  //   if(this.isSetOf || this.isSequenceOf) {
  //     cssclass = 'valuebox';
  //   } else {
  //     cssclass = 'singlevaluebox';
  //   }

  //   return cssclass;
  // }

  //true: left, false: right
  public moveSequenceElement(direction: boolean, index: number) {
    const inputValues = this.input.value
    if (direction) {
      if (inputValues[index - 1]) {
        const temp = inputValues[index - 1];
        inputValues[index - 1] = inputValues[index];
        inputValues[index] = temp;
      }
    } else {
      if (inputValues[index + 1]) {
        const temp = inputValues[index + 1];
        inputValues[index + 1] = inputValues[index];
        inputValues[index] = temp;
      }
    }
  }

  onChildSaveEvent(event: SaveEvent) {
    // Optionally, do something with the event first
    this.saveEvent.emit(event); // forwards to grandparent
  }
}

export interface SaveEvent {
  idShort: string;
  value: IvmlRecordValue; 
  multipleInputs?: boolean;
}
