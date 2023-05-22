import { Component, Input, OnInit } from '@angular/core';
import { EditorService } from 'src/app/services/editor.service';
import { Resource, ResourceAttribute } from 'src/interfaces';

@Component({
  selector: 'app-input-ref-select',
  templateUrl: './input-ref-select.component.html',
  styleUrls: ['./input-ref-select.component.scss']
})
export class InputRefSelectComponent implements OnInit {

  @Input() textfield = false;
  @Input() input: ResourceAttribute = {};


  textInput = '';
  isSetOf = false;
  isSequenceOf = false;
  refTo = '';
  inputValues: string[] = [];
  references: Resource[] = [];
  selectedRef: Resource = {};

  tooltip= '';

  refTypes = ['Dependency', 'Resource', 'DataType', 'Server', 'ServiceMesh', 'MeshConnector', 'MeshElement', 'ServiceBase'];
  metaTypes = ['metaState', 'metaProject', 'metaSize', 'metaType', 'metaRefines', 'metaAbstract'];

  constructor(private edit: EditorService) { }

  ngOnInit(): void {
    this.init(this.input.value);

    if(this.input.description && this.input.description[0]) {
      this.tooltip =this.input.description[0].language + ', ' + this.input.description[0].text;
    } else if (this.input.description && typeof(this.input.description) === 'string' ) {
      this.tooltip = this.input.description;
    }
  }

  private init(value: string) {
    value = value.toLowerCase();
    console.log(value);
    if(value) {
      if(value.indexOf('refto') >= 0) {
        if(value.indexOf('setof') >= 0) {
          this.isSetOf = true;
        }
        const startIndex = value.indexOf('refto') + 6;
        this.refTo = value.substring(startIndex, value.indexOf(')', startIndex));
        this.getReferences(this.refTo);
      }
      if(value.indexOf('sequenceof') >= 0) {
        console.log('sequenceOf ' + value);
        this.isSequenceOf = true;
      }
    }
  }

  private async getReferences(refTo: string) {
    console.log('get ref of ' + refTo);
    if(refTo === 'dependency') {
      const response = await this.edit.getDependencies() as Resource;
      if(response && response.value) {
        for(let ele of response.value) {
          for(let dep of ele.value) {
            if(dep.idShort && this.metaTypes.indexOf(dep.idShort) === -1) {
              this.references.push(dep);
            }
          }

        }

      }
    } else if(refTo === 'server') {
      const response = await this.edit.getServers() as Resource;
      if(response && response.value) {
        for(let dep of response.value) {
          if(dep.idShort && this.metaTypes.indexOf(dep.idShort) === -1) {
            this.references.push(dep);
          }

        }
      }
    }
    console.log(this.references);
  }

  public addFromRef() {
    let idShort = '';
    if(this.selectedRef && this.selectedRef.idShort) {
      if(this.selectedRef.value && this.selectedRef.value[0]) {
        console.log(this.selectedRef);
        let temp = this.selectedRef.value?.find(item => item.idShort == 'varValue');
        if(temp && temp.idShort) {
          idShort = temp.value;
        } else {
          idShort = this.selectedRef.idShort;
        }
      } else {
        idShort = this.selectedRef.idShort;
      }
      if(this.isSetOf) {
        this.inputValues.push(idShort);
      } else {
        this.inputValues = [];
        this.inputValues.push(idShort);
      }
    }
  }



  public addFromTextfield() {
    if(this.isSetOf || this.isSequenceOf) {
      this.inputValues.push(this.textInput);
    } else {
      this.inputValues = [];
      this.inputValues.push(this.textInput);
    }
  }

  public removeInputValue(removeIndex: number) {
    let newInputs = [];
    let index = 0;
    for(const input of this.inputValues) {
      if(index != removeIndex) {
        newInputs.push(this.inputValues[index]);
      }
      index++;
    }
    this.inputValues = newInputs;
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

  public displayIdShort(element: any) {
    let displayName = '';
    console.log(element);
    if(this.refTo === 'dependency') {
      displayName = element.value.find((item: { idShort: string; value: string;}) => item.idShort === 'varValue').value;
    } else if(this.refTo === 'server') {
      displayName = element.idShort;
    }
    // if(element.value && typeof(element.value) === 'object' ) {

    // } else if(element.value && typeof(element.value) === 'string' ) {
    //   displayName = element.value
    // }

    return displayName;
  }

  //true: left, false: right
  public moveSequenceElement(direction: boolean, index: number) {
    console.log(this.isSequenceOf);
    const inputValues = this.inputValues
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
