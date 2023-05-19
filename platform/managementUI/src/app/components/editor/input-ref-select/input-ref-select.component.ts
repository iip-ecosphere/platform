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
  refTo = '';
  inputValues: string[] = [];
  references: Resource[] = [];
  selectedRef: Resource = {};

  tooltip= '';

  refTypes = ['Dependency', 'Resource', 'DataType', 'Server', 'ServiceMesh', 'MeshConnector', 'MeshElement', 'ServiceBase'];
  metaTypes = ['metaState', 'metaProject', 'metaSize', 'metaType'];

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
    if(value) {
      if(value.indexOf('refto') >= 0) {
        if(value.indexOf('setof') >= 0) {
          this.isSetOf = true;
        }
        const startIndex = value.indexOf('refto') + 6;
        this.refTo = value.substring(startIndex, value.indexOf(')', startIndex));
        this.getReferences(this.refTo);
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
    if(this.selectedRef && this.selectedRef.idShort) {
      if(this.isSetOf) {
        this.inputValues.push(this.selectedRef.idShort);
      } else {
        this.inputValues = [];
        this.inputValues.push(this.selectedRef.idShort);
      }
    }
  }

  public addFromTextfield() {
    if(this.isSetOf) {
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
    if(this.isSetOf) {
      cssclass = 'valuebox';
    } else {
      cssclass = 'singlevaluebox';
    }

    return cssclass;
  }

  displayIdShort(element: any) {
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

}
