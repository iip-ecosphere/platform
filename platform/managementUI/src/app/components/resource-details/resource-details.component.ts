//import { PlatformData, SemanticId, Resource } from './../../../interfaces';
//import { SemanticId, outputArgument } from './../../../interfaces';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from 'src/app/services/api.service';
import { Resource, ResourceAttribute, InputVariable, platformResponse } from 'src/interfaces';

@Component({
  selector: 'app-resource-details',
  templateUrl: './resource-details.component.html',
  styleUrls: ['./resource-details.component.scss']
})
export class ResourceDetailsComponent implements OnInit {

  id: string | null = null;
  resource: Resource | undefined;
  inputVariables: InputVariable[] = [];
  resourceAttributes: ResourceAttribute[] = [];
  platformURL:string = "/aas/submodels/platform/submodel/submodelElements/";

  constructor(public http: HttpClient, public api: ApiService,
    public route: ActivatedRoute) { }

  ngOnInit(): void {
    this.id = this.route.snapshot.paramMap.get('id')
    if (this.id) {
      this.getResource(this.id);
    }
  }

  private async getResource(id: string) {
    this.resource = await this.api.getResource(id);
    this.resolveSemanticId();
    console.log(this.resource)
  }

  public isObject(value: any) {
    return (typeof value === 'object');
  }

  // col 0: attribute name from AAS
  // col 1: name displayed in UI
  // col 2: category of the attribute in UI
  attributeNames = [
    ["Storage_Free", "Free", "Storage"],
    ["Storage_Capacity", "Capacity", "Storage"],
    ["Storage_Usable", "Usable", "Storage"],
    ["Allocated_Storage", "Allocated", "Storage"],
    ["Memory_Free", "Free", "Memory"],
    ["Memory_Capacity", "Capacity", "Memory"],
    ["Allocated_Memory", "Allocated", "Memory"],
    ["Memory_Used", "Used", "Memory"],
    ["CPU_Temperature", "CPU", "Temperature"],
    ["Case_Temperature", "Case", "Temperature"],
    ["containerSystemName", "Name", "Container System"],
    ["OS", "OS", "Device"],
    ["CPU_Architecture", "CPU Architecture", "Device"],
    ["CPU_Capacity", "CPU Capacity", "Device"],
    ["runtimeName", "Name", "Runtime"],
    ["runtimeVersion", "Version", "Runtime"],
    ["deviceAas", "Device AAS", "Device"],
    ["containerSystemVersion", "Version", "Container System"],
    ["managedId", "Managed ID", "Device"],
    ["ip", "IP", "Device"]
  ]
  categories = ["Storage", "Memory", "Container System", "Device", "Runtime", "Temperature"]

  // index: 1 - attribute name
  //        2 - attribute category (e.g. storage, memory)
  public getAttributeInfo(element: any, col: number) {
    let row = this.attributeNames.find(item => item[0] == element)
    if(row != undefined) {
      return row[col]
    } else {
      return null
    }
  }

  public isNonEmptyString(value: any) {

    let result = false
    if (typeof value == "string" && value.length > 0) {
      result = true
    }
    return result
  }

  // ---------- Semantic Id -------------------------
  public async resolveSemanticId() {
    await this.setInputValues();

    let resolvedInfo = []
    let i = 0;
    for(const value of this.inputVariables) {
      const input:InputVariable[] = [value]
      const response = await this.api.executeFunction(
        "",
        this.platformURL,
        "resolveSemanticId",
        input) as platformResponse;

      resolvedInfo.push(this.getSemanticInfo(response))
      i++;
    }
    let j = 0;
    for(const value of resolvedInfo) {
      if(value[0]=="byte") {
        this.convertByte(j, 1000000000, "GB")
      } else {
        this.resource!.value![j].semanticName = value[0];
      }
      this.resource!.value![j].semanticDescription = value[1];
      j++;
    }
  }

  // Creates a list of input parameters for the aas operation "resolveSemanticId"
  // based on the semanticId of the resource attributes.
  public setInputValues() {
    if(this.resource && this.resource.value) {
      this.resourceAttributes = this.resource.value;

      let i = 0;
      for(const attribute of this.resourceAttributes ) {
        let input_value:InputVariable = {
          value: {
            modelType: {
              name: "Property"
            },
            valueType: "string",
            idShort: "semanticId",
            kind: "Template",
            value: this.resource.value[i].semanticId?.keys[0].value
          }
        }
        this.inputVariables.push(input_value);
        i++;
      }
    }
  }

  // Retrieves a semantic name from the platform response and give it back as string
  // (or null if there is none).
  public getResolvedId(response:platformResponse) {
    if(response && response.outputArguments) {
      let output = response.outputArguments[0]?.value?.value;
      if (output) {
        let temp = JSON.parse(output);
        if (temp.result) {

          let result = JSON.parse(temp.result);
          return result.naming.en.name
        } else {
          return null
        }
      }
    }
  }

  // Returns an array [name, description]
  public getSemanticInfo(response:platformResponse) {
    let return_value = [null, null];
    if(response && response.outputArguments) {
      let output = response.outputArguments[0]?.value?.value;
      if (output) {
        let temp = JSON.parse(output);
        if (temp.result) {
          let result = JSON.parse(temp.result);
          if (result.naming.en.description) {
            return_value = [result.naming.en.name, result.naming.en.description]
          } else {
            return_value = [result.naming.en.name, null]
          }
        }
      }
    }
    return return_value
  }

  // Converts byte value of resource attribute:
  // e.g. conversion to GB - dominator: 1000000000, unitName: GB
  public convertByte(index:any, dominator:any, unitName:string) {
    this.resource!.value![index].semanticName =  unitName;
        let temp_value = this.resource!.value![index].value;
        temp_value = (temp_value/dominator).toFixed(2)
        this.resource!.value![index].value = temp_value
  }
}
