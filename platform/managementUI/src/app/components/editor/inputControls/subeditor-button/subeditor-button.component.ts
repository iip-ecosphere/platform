import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { IvmlRecordValue, MT_metaAbstract, MT_metaRefines, Resource, ResourceAttribute, editorInput, metaTypes } from 'src/interfaces';
import { EditorComponent } from '../../editor.component';
import { DataUtils, Utils } from 'src/app/services/utils.service';
import { IvmlFormatterService } from 'src/app/services/ivml-formatter.service';

@Component({
    selector: 'app-subeditor-button',
    templateUrl: './subeditor-button.component.html',
    styleUrls: ['./subeditor-button.component.scss'],
    standalone: false
})
export class SubeditorButtonComponent extends Utils implements OnInit {

  @Input() meta: Resource | undefined;
  @Input() input!: editorInput;
  @Input() buttonText: string = "edit";
  @Input() matIcon: string = "";
  @Input() showValue: string = "false";
  @Output() saveEvent = new EventEmitter<SaveEvent>();

  errorMsg: string = 'loading...';
  disabled = false;

  refinedTypes: ResourceAttribute[] = [];
  selectedRefinedType: ResourceAttribute | null = null;
  
  constructor(public subDialog: MatDialog, private ivmlFormatter: IvmlFormatterService) { 
    super();
  }

  ngOnInit(): void {
    this.disabled = this.validateEditorInputType(this.input)
  }

  public openSubeditor(type: editorInput) {
    if (this.meta) {
      let uiGroups = this.ivmlFormatter.calculateUiGroupsInf(type, this.meta);
      let parts = this.ivmlFormatter.partitionUiGroups(uiGroups);
      let dialogRef = this.subDialog.open(EditorComponent, this.configureDialog('80%', '80%', parts));
      let component = dialogRef.componentInstance;
      if (this.refinedTypes[0]) {
        component.refinedTypes = this.refinedTypes;
      } else {
        component.type = type;
      }
      component.metaBackup = this.meta;
      component.dialog = dialogRef;
      component.topLevel = false;
      component.saveEvent = this.saveEvent;
    }
  }

  public validateEditorInputType(type:editorInput) {
    if (this.meta && this.meta.value) {
      let typeMeta = DataUtils.getProperty(this.meta.value, DataUtils.stripGenericType(type.type));
      if (!typeMeta) {
        this.errorMsg = 'ERROR: Metadata not found in Configuration'
        return true;
      } else {
        const Abstract = DataUtils.getProperty(typeMeta.value, MT_metaAbstract);
        if(Abstract.value && typeMeta.idShort) {
          this.getRefinedTypes(typeMeta.idShort);
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

  private getRefinedTypes(searchTerm: string) {
    if (this.meta && this.meta.value) {
      let refinedTypes = [];
      for (const type of this.meta.value) {
        const refined = DataUtils.getProperty(type.value, MT_metaRefines);
        if (refined && refined.value != '') {
          if (searchTerm === refined.value) {
            const abstract = DataUtils.getProperty(type.value, MT_metaAbstract);
            if(abstract && abstract.value && type.idShort) {
              this.getRefinedTypes(type.idShort);
            } else {
              refinedTypes.push(type);
            }
          }
        }
      }
      if (!refinedTypes[0]) {
        console.warn('No refined types found for abstract type ' + searchTerm);
      } else {
        this.refinedTypes = this.refinedTypes.concat(refinedTypes);
      }
    }
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