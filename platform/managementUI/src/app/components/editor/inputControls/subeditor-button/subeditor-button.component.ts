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

  errorMsg: string = '';
  disabled = false;

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
      dialogRef.componentInstance.type = type;
      dialogRef.componentInstance.metaBackup = this.meta;
    }

  }

  public validateEditorInputType(type:editorInput) {
    if(this.meta && this.meta.value) {
      let typeMeta = this.meta.value.find(item => item.idShort === this.cleanTypeName(type.type));
      if(!typeMeta) {
        this.errorMsg = 'ERROR: Metadata not found in Configuration'
        return true;
      } else if(!this.hasInputFields(typeMeta)){
        this.errorMsg = 'ERROR: Configuration does not provide input fields for type'
        return true;
      }
    }
    return false;
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
