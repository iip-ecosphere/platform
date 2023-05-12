import { Component, Input, OnInit } from '@angular/core';
import { ApiService } from 'src/app/services/api.service';
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
  tooltip= '';

  refTypes = ['Dependency', 'Resource', 'DataType', 'Server', 'ServiceMesh', 'MeshConnector', 'MeshElement', 'ServiceBase'];

  constructor(private api: ApiService) { }

  ngOnInit(): void {
    this.init(this.input.value);
    this.getReferences(this.refTo);

    if(this.input.description && this.input.description[0]) {
      this.tooltip =this.input.description[0].language + ', ' + this.input.description[0].text;
    } else if (this.input.description && typeof(this.input.description) === 'string' ) {
      this.tooltip = this.input.description;
    }
  }

  private init(value: string) {
    if(value) {
      if(value.indexOf('refto') >= 0) {
        if(value.indexOf('setof') >= 0) {
          const startIndex = value.indexOf('setof') + 6;
          this.refTo = value.substring(startIndex, value.indexOf(')', startIndex));
          this.isSetOf = true;
          //do if setof
        } else {
          const startIndex = value.indexOf('refto') + 6;
          this.refTo = value.substring(startIndex, value.indexOf(')', startIndex));
          //do if refto but no setof
        }
      }
    }
  }

  private getReferences(refTo: string) {

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

}
