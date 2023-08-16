import { Component, Input, OnInit } from '@angular/core';
import { EditorService } from 'src/app/services/editor.service';
import { Resource, editorInput, configMeta } from 'src/interfaces';

@Component({
  selector: 'app-input-ref-select',
  templateUrl: './input-ref-select.component.html',
  styleUrls: ['./input-ref-select.component.scss']
})
export class InputRefSelectComponent implements OnInit {

  @Input() activeTextinput = false;
  @Input() input: editorInput = {name: '', type: '', description: [{text: '', language: ''}], refTo: true, value: []};


  textInput = '';
  isSetOf = false;
  isSequenceOf = false;
  refTo = '';
  references: Resource[] = [];
  selectedRef: configMeta | undefined;

  //refTypes = ['Dependency', 'Resource', 'DataType', 'Server', 'ServiceMesh', 'MeshConnector', 'MeshElement', 'ServiceBase', 'IOType'];
  metaTypes = ['metaState', 'metaProject', 'metaSize', 'metaType', 'metaRefines', 'metaAbstract', 'metaTypeKind'];

  constructor(private edit: EditorService) { }

  ngOnInit(): void {
    let type = this.input.type;
    this.init(type);
  }

  private init(value: string) {

    if(value) {
      if(value.indexOf('setOf') >= 0) {
        this.isSetOf = true;
      }
      if(value.indexOf('refTo') >= 0) {
        const startIndex = value.indexOf('refTo') + 6;
        this.refTo = value.substring(startIndex, value.indexOf(')', startIndex));
        this.getConfigurationType(this.refTo);
        this.activeTextinput = false;
      }
      if(value.indexOf('sequenceOf') >= 0) {
        console.log('sequenceOf ' + value);
        this.isSequenceOf = true;
      }
    }
    if(this.input.metaTypeKind == 10) {
      this.activeTextinput = false;
    }
  }

  public async getConfigurationType(type: string) {
    const response = await this.edit.getConfigurationType(type);
    if(response && response.value) {
      for(let dep of response.value) {
        if(dep.idShort && this.metaTypes.indexOf(dep.idShort) === -1) {
          this.references.push(dep);
        }

      }
    }
  }

  public addFromRef() {
    console.log(this.selectedRef);
    if(this.selectedRef && this.selectedRef.idShort) {
      if(this.isSetOf) {
        this.input.value.push('refTo(' + this.selectedRef.idShort + ')');
      } else {
        this.input.value = [];
        this.input.value.push('refTo(' + this.selectedRef.idShort + ')');
      }
    }
    console.log(this.input.value);
  }



  public addFromTextfield() {
    if(this.isSetOf || this.isSequenceOf) {
      this.input.value.push(this.textInput);
    } else {
      this.input.value = [];
      this.input.value.push(this.textInput);
    }
    console.log(this.input.value);
  }

  public removeInputValue(removeIndex: number) {
    let newInputs = [];
    let index = 0;
    for(const input of this.input.value) {
      if(index != removeIndex) {
        newInputs.push(this.input.value[index]);
      }
      index++;
    }
    this.input.value = newInputs;
  }

  public valueboxClass() {
    let cssclass = '';
    if(this.isSetOf || this.isSequenceOf) {
      cssclass = 'valuebox';
    } else {
      cssclass = 'singlevaluebox';
    }

    return cssclass;
  }

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
    console.log(this.isSequenceOf);
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
