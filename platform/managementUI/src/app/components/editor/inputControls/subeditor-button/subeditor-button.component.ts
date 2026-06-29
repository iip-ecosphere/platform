import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { IvmlRecordValue, IvmlValue, MT_metaAbstract, MT_metaRefines, Resource, ResourceAttribute, editorInput, metaTypes } from 'src/interfaces';
import { EditorComponent } from '../../editor.component';
import { DataUtils, Utils } from 'src/app/services/utils.service';
import { IvmlFormatterService } from 'src/app/components/services/ivml/ivml-formatter.service';

@Component({
    selector: 'app-subeditor-button',
    templateUrl: './subeditor-button.component.html',
    styleUrls: ['./subeditor-button.component.scss'],
    standalone: false
})
export class SubeditorButtonComponent extends Utils implements OnInit {

  @Input() meta: Resource | undefined;
  @Input() input!: editorInput;
  @Input() selectedType: Resource | undefined;
  @Input() buttonText: string = "edit";
  @Input() matIcon: string = "";
  @Input() showValue: string = "false";
  @Output() saveEvent = new EventEmitter<SaveEvent>();

  errorMsg: string = 'loading...';
  disabled = false;
  fieldsMeta: Resource | undefined;

  refinedTypes: ResourceAttribute[] = [];
  selectedRefinedType: ResourceAttribute | null = null;
  
  constructor(public subDialog: MatDialog, private ivmlFormatter: IvmlFormatterService) { 
    super();
  }

  ngOnInit(): void {
    this.disabled = this.validateEditorInputType(this.input);
    if (this.meta) {
      const typeMeta = DataUtils.getProperty(this.meta.value!, DataUtils.stripGenericType(this.input.type));
      if (typeMeta && this.ivmlFormatter.isAbstract(typeMeta) /*&& !this.ivmlFormatter.isMetaRef(typeMeta)*/) {
          const refinedTypes = DataUtils.getRefinedTypes(typeMeta.idShort, this.meta);
          this.fieldsMeta = this.ivmlFormatter.filterMeta(this.meta, '');
          refinedTypes?.forEach(type => {
            this.fieldsMeta?.value?.push(type);
          });
      } else if (this.input.name === "fields") {
        this.fieldsMeta = this.ivmlFormatter.filterMeta(this.meta, 'Fields');
      }
    }

    if (this.fieldsMeta && this.fieldsMeta.value && this.fieldsMeta.value?.length > 0) {
      this.fieldsMeta.value = this.fieldsMeta?.value?.sort((a, b) => (a.idShort ?? '').localeCompare(b.idShort ?? ''));
      this.selectedType = this.fieldsMeta.value[0]
    }

  }

  public openSubeditor(type: editorInput, selectedType: Resource | undefined) {
    if (this.meta) {
      // Clear previous selected value for refined abstract types e.g. change machine formatter for Json File connector from "TextLineFormatter" to "JavaMachineFormatter"
      if (selectedType && selectedType.idShort !== DataUtils.stripGenericType(type.type)) {
        type.value = '';
      }
      type.type = getFieldType(type.type, selectedType);
      let uiGroups = this.ivmlFormatter.calculateUiGroupsInf(type, this.meta);
      if (uiGroups.length == 0) {
        let emptyType: IvmlRecordValue = createEmptyType(type.type);
        this.saveEvent.emit({ idShort: type.name, value: emptyType, multipleInputs: type.multipleInputs });
      } else {
        let parts = this.ivmlFormatter.partitionUiGroups(uiGroups);
        let dialogRef = this.subDialog.open(EditorComponent, this.configureDialog('80%', '80%', parts));
        let component = dialogRef.componentInstance;
        component.type = type;
        component.metaBackup = this.meta;
        component.dialog = dialogRef;
        component.topLevel = false;
        component.saveEvent = this.saveEvent;
      }
    }
  }

  public validateEditorInputType(type:editorInput) {
    if (this.meta && this.meta.value) {
      let typeMeta = DataUtils.getProperty(this.meta.value, DataUtils.stripGenericType(type.type));
      if (!typeMeta) {
        this.errorMsg = 'ERROR: Metadata not found in Configuration'
        return true;
      } else {
        if(this.ivmlFormatter.isAbstract(typeMeta) && typeMeta.idShort) {
          this.refinedTypes = DataUtils.getRefinedTypes(typeMeta.idShort, this.meta);
          return false;
        } else if(!this.hasInputFields(typeMeta)){
          this.errorMsg = 'ERROR: Configuration does not provide input fields for non abstract type'
          return true;
        }
      }
    }
    this.errorMsg = '';
    return false;
  }

  private hasInputFields(meta: ResourceAttribute) {
    if (Array.isArray(meta.value)) {
      for (let element of meta.value) {
        if (!metaTypes.includes(element.idShort)) {
          return true;
        }
      }
      return false;
    } else {
      console.error('Meta value is not an Array');
      return false;
    }
  }

  // preliminary
  getDisplayValue() {
    if (this.input.value) {
      return this.getElementDisplayName(this.input.value, true);
    } else {
      return "";      
    }
  }
  
}

export interface SaveEvent {
  idShort: string;
  value: IvmlRecordValue; 
  multipleInputs?: boolean;
}

function getFieldType(type: string, selectedType: Resource | undefined): string {
  if (selectedType && selectedType.idShort) {
    if (!DataUtils.isIvmlSimpleType(type)) {
      let innerType = DataUtils.stripGenericType(type)
      if (innerType !== selectedType.idShort) {
        return type.replace(innerType, selectedType.idShort);
      }
    } else {
      return selectedType.idShort;
    }
  }
  return type;
}

/**
 * Create an empty value for a specific type e.g. 'JsonFormatter{}'
 * 
 * @param type the empty type
 * @param value the input to search
 * @returns type as Resource
 */
function createEmptyType(type: string): IvmlRecordValue {
  let emptyType: IvmlRecordValue = {};
  let val: IvmlValue = { value: type + '{}', _type: type };
  emptyType[type] = val;
  return emptyType;
}

