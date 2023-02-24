import { PlatformData, SemanticId } from './../../../interfaces';
//import { SemanticId, outputArgument } from './../../../interfaces';
import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from 'src/app/services/api.service';
import { Resource, ResourceAttribute, InputVariable, platformResponse, outputArgument } from 'src/interfaces';

@Component({
  selector: 'app-resource-details',
  templateUrl: './resource-details.component.html',
  styleUrls: ['./resource-details.component.scss']
})
export class ResourceDetailsComponent implements OnInit {

  // services: PlatformServices = {};
  // servicesToggle: boolean[] = [];

  // artifacts: PlatformResources = {};
  // artifactsToggle: boolean[] = [];

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
    // this.getServices();
    // this.getArtifacts();
  }

  private async getResource(id: string) {
    this.resource = await this.api.getResource(id);
    this.resolveSemanticId();

    // iterate through resource
    //this.resource?.value?.find(val => val.semanticId? === "0173-1#02-AAO247#002"); // semanticName?: string;
    // find the right place in resource
    // return this.resources.submodelElements?.find(resource => resource.idShort === id);
    // and setVariable
  }

  //currently not used
  // public async getServices() {
  //   let Data = await this.api.getServices();
  //   if(typeof(Data) != 'number') {
  //     this.services = Data
  //   }
  //   if(this.services && this.services.submodelElements) {
  //     this.servicesToggle = new Array(this.services.submodelElements.length).fill(false);
  //   }
  // }

  // public async getArtifacts() {
  //   this.artifacts = await this.api.getArtifacts();
  //   if(this.artifacts && this.artifacts.submodelElements) {
  //     this.artifactsToggle = new Array(this.artifacts.submodelElements.length).fill(false);
  //   }
  // }

  public isObject(value: any) {
    return (typeof value === 'object');
  }

  // public serToggle(index: number) {
  //   if(this.servicesToggle) {
  //     this.servicesToggle[index] = !this.servicesToggle[index]
  //   }
  // }

  public async resolveSemanticId() {
    await this.setInputValues();

    let resolvedIds = []
    let i = 0;
    for(const value of this.inputVariables) {
      const input:InputVariable[] = [value]
      const response = await this.api.executeFunction(
        "",
        this.platformURL,
        "resolveSemanticId",
        input) as platformResponse;
      // TODO retrieving only the name for given semanticId (for now)
      resolvedIds.push(this.getResolvedId(response))
      i++;
    }
    // Setting a semanticName for each attribute
    let j = 0;
    for(const name of resolvedIds) {
      if(name=="byte") {
        this.convertByte(j, 1000000000, "GB")
      } else {
        this.resource!.value![j].semanticName =  name;
      }
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

  // Converts byte value of resource attribute:
  // e.g. conversion to GB - dominator: 1000000000, unitName: GB
  public convertByte(index:any, dominator:any, unitName:string) {
    this.resource!.value![index].semanticName =  unitName;
        let temp_value = this.resource!.value![index].value;
        temp_value = (temp_value/dominator).toFixed(2)
        this.resource!.value![index].value = temp_value
  }
}
