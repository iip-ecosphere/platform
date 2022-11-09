import { Component, Input, OnInit } from '@angular/core';
import { ApiService } from 'src/app/services/api.service';
import { outputArgument, platformResponse, Resource, ResourceAttribute } from 'src/interfaces';

@Component({
  selector: 'app-operation-query',
  templateUrl: './operation-query.component.html',
  styleUrls: ['./operation-query.component.scss']
})
export class OperationQueryComponent implements OnInit {

  @Input() resource: Resource | undefined;
  @Input() managedId: string | null = null;

  functions: ResourceAttribute[] = [];
  selected: ResourceAttribute | null = null;
  inputVariables: ResourceAttribute[] = [];
  inputVariablesValues: any[] = [];

  message: string = '';

  constructor(private api: ApiService) {

  }

  ngOnInit() {
    if(this.resource && this.resource.value) {
      for(const func of this.resource.value) {
        if(func.invokable) {
          this.functions.push(func);
        }
      }
    }
  }

    public async send() {
      await this.setInputValues();
      if(this.selected && this.managedId && this.selected && this.selected.idShort) {
        console.log(this.selected);
        const response = await this.api.executeFunction(this.managedId, this.selected.idShort, this.inputVariables) as platformResponse;
        console.log(response);
        if(response && response.outputArguments) {
          this.showMessage(response.outputArguments);
        }
      }

    }

    private showMessage(output: outputArgument[]) {
      try {
        let message = '';
        if(output[0].value) {
          for(let bit of output) {
            if(bit.value && bit.value.value) {
              message = message.concat(bit.value.value);
              message = message.concat('  ')
            }
          }
        }
        this.message = message;
      } catch(e) {
        console.log(e);
      }
    }

    private setInputValues() {
      if(this.selected && this.selected.inputVariables) {

        this.inputVariables = this.selected.inputVariables;
        console.log(this.selected);
      }
      let i = 0
      for(const input of this.inputVariables) {
        input.value.value = this.inputVariablesValues[i];
        i++;
      }
    }
}
