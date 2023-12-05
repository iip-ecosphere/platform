import { Component, Input, OnInit } from '@angular/core';
import { EditorService } from 'src/app/services/editor.service';
import { Resource, editorInput, configMeta, metaTypes } from 'src/interfaces';
import { DataUtils, Utils } from 'src/app/services/utils.service';
import { EditorComponent } from '../editor.component';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'app-input-ref-select',
  templateUrl: './input-ref-select.component.html',
  styleUrls: ['./input-ref-select.component.scss']
})
export class InputRefSelectComponent extends Utils implements OnInit {

  @Input() activeTextinput = false;
  @Input() input: editorInput = {name: '', type: '', description: [{text: '', language: ''}], refTo: true, value: undefined};
  @Input() meta: Resource | undefined;

  textInput = '';
  isSetOf = false;
  isSequenceOf = false;
  refTo = '';
  references: Resource[] = [];
  selectedRef: configMeta | undefined;

  constructor(private edit: EditorService, public subDialog: MatDialog) { 
    super();
  }

  ngOnInit(): void {
    let type = this.input.type;
    this.init(type);
  }

  private init(type: string) {
console.log("INPUT-REF " + type);    
    if(type) {
      if(type.indexOf('setOf') >= 0) {
        this.isSetOf = true;
      }
      if(type.indexOf('refTo') >= 0) {
        const startIndex = type.indexOf('refTo') + 6;
        this.refTo = type.substring(startIndex, type.indexOf(')', startIndex));
        this.getConfigurationType(this.refTo);
        this.activeTextinput = false;
      }
      if(type.indexOf('sequenceOf') >= 0) {
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
    if(this.input.metaTypeKind == 10 || this.input.metaTypeKind == 2) {
      this.activeTextinput = false;
    }
  }

  public async getConfigurationType(type: string) {
    const response = await this.edit.getConfigurationType(type);
    if(response && response.value) {
      for(let dep of response.value) {
        if(dep.idShort && metaTypes.indexOf(dep.idShort) === -1) {
          this.references.push(dep);
        }
      }
    }
  }

  public addFromRef() {
    if(this.selectedRef && this.selectedRef.idShort) {
      if(this.isSetOf) {
        this.input.value.push('refTo(' + this.selectedRef.idShort + ')');
      } else {
        this.input.value = 'refTo(' + this.selectedRef.idShort + ')';
      }
    }
  }

  public addFromTextfield() {
    if(this.isSetOf || this.isSequenceOf) {
      this.input.value.push(this.textInput);
    } else {
      this.input.value = this.textInput;
    }
  }

  public editInputValue(editIndex: number) {
    let dialogRef = this.subDialog.open(EditorComponent, {
      height: '80%',
      width:  '80%',
    })
    let input = DataUtils.deepCopy(this.input);
    input.value = this.input.value[editIndex];
    dialogRef.componentInstance.type = input;
    dialogRef.componentInstance.metaBackup = this.meta;
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

  public getDisplayName(element: any) {
    if(typeof(element) === 'string') {
      return element
    } else if(element.name){
      return element.name;
    } else {
      let idShort = '';
      let temp = element.value?.find((item: { idShort: string; }) => item.idShort == 'varValue');
      if(temp && temp.idShort) {
        idShort = temp.value;
      } else {
        idShort = element.idShort;
      }

      return idShort;
    }
  }

  //true: left, false: right
  public moveSequenceElement(direction: boolean, index: number) {
    const inputValues = this.input.value
    if(direction) {
      if(inputValues[index - 1]) {
        const temp = inputValues[index - 1];
        inputValues[index - 1] = inputValues[index];
        inputValues[index] = temp;
      }
    } else {
      if(inputValues[index + 1]) {
        const temp = inputValues[index + 1];
        inputValues[index + 1] = inputValues[index];
        inputValues[index] = temp;
      }
    }
  }

}
