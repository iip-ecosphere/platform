import { Component, Input, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Resource, ResourceAttribute, editorInput } from 'src/interfaces';
import { EditorComponent } from '../../editor.component';

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

  metaTypes = ['metaState', 'metaProject',
  'metaSize', 'metaType', 'metaRefines', 'metaAbstract', 'metaTypeKind'];

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
      let typeMeta = this.meta.value.find(item => item.idShort === this.cleanTypeName(type.type));

      if(!typeMeta) {
        this.errorMsg = 'ERROR: Metadata not found in Configuration'
        return true;
      } else {
        const Abstract = typeMeta.value.find( (item: { idShort: string; }) => item.idShort === "metaAbstract");
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
        const refined = type.value.find((item: { idShort: string; }) => item.idShort === 'metaRefines');
        if(refined && refined.value != '') {
          if(searchTerm === refined.value) {
            const abstract = type.value.find((item: { idShort: string; }) => item.idShort === 'metaAbstract');
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

  private cleanTypeName(type: string) {
    const startIndex = type.lastIndexOf('(') + 1;
    const endIndex = type.indexOf(')');
    if(endIndex > 0){
      return type.substring(startIndex, endIndex);
    } else {
      return type;
    }
  }

  private hasInputFields(meta: ResourceAttribute) {
    if(Array.isArray(meta.value)) {
      for(let element of meta.value) {
        if(!this.metaTypes.includes(element.idShort)) {
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
