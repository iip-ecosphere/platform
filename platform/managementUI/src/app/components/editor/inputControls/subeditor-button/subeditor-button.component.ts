import { Component, Input, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MT_metaAbstract, MT_metaRefines, Resource, ResourceAttribute, editorInput, metaTypes } from 'src/interfaces';
import { EditorComponent } from '../../editor.component';
import { DataUtils } from 'src/app/services/utils.service';

@Component({
  selector: 'app-subeditor-button',
  templateUrl: './subeditor-button.component.html',
  styleUrls: ['./subeditor-button.component.scss']
})
export class SubeditorButtonComponent implements OnInit {

  @Input() meta: Resource | undefined;
  @Input() input!: editorInput;

  errorMsg: string = 'loading...';
  disabled = false;

  refinedTypes: ResourceAttribute[] = [];
  selectedRefinedType: ResourceAttribute | null = null;
  value = "";

  constructor(public subDialog: MatDialog) { }

  ngOnInit(): void {
    this.disabled = this.validateEditorInputType(this.input)
  }

  public openSubeditor(type: editorInput) {
    if(this.meta) {
      let dialogRef = this.subDialog.open(EditorComponent, {
        height: '80%',
        width:  '80%',
      })
      if(this.refinedTypes[0]) {
        dialogRef.componentInstance.refinedTypes = this.refinedTypes;
      } else {
        dialogRef.componentInstance.type = type;
      }
      dialogRef.componentInstance.metaBackup = this.meta;
    }

  }

  public validateEditorInputType(type:editorInput) {
    if(this.meta && this.meta.value) {
      let typeMeta = DataUtils.getProperty(this.meta.value, DataUtils.stripGenericType(type.type));
      if(!typeMeta) {
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
    if(this.meta && this.meta.value) {
      let refinedTypes = [];
      for(const type of this.meta.value) {
        const refined = DataUtils.getProperty(type.value, MT_metaRefines);
        if(refined && refined.value != '') {
          if(searchTerm === refined.value) {
            const abstract = DataUtils.getProperty(type.value, MT_metaAbstract);
            if(abstract && abstract.value && type.idShort) {
              this.getRefinedTypes(type.idShort);
            } else {
              refinedTypes.push(type);
            }
          }
        }
      }
      if(!refinedTypes[0]) {
        console.log('WARNING: no refined types found for abstract type ' + searchTerm);
      } else {
        this.refinedTypes = this.refinedTypes.concat(refinedTypes);
      }
    }
  }

  private hasInputFields(meta: ResourceAttribute) {
    if(Array.isArray(meta.value)) {
      for(let element of meta.value) {
        if(!metaTypes.includes(element.idShort)) {
          return true;
        }
      }
      return false;
    } else {
      console.log('ERROR: meta value is not an Array');
      return false;
    }
  }
  
}
