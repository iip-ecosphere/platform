import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from 'src/app/services/api.service';
import { PlatformResources, PlatformServices, ResourceSubmodelElement, ResourceValue } from 'src/interfaces';

@Component({
  selector: 'app-services',
  templateUrl: './services.component.html',
  styleUrls: ['./services.component.scss']
})
export class ServicesComponent implements OnInit {

  constructor(public http: HttpClient, public api: ApiService, public route: ActivatedRoute) { }

  services: PlatformServices = {}
  artifacts: PlatformResources = {};
  id: string | null = null;
  resource: ResourceSubmodelElement | undefined;
  selectedBasyxFunc : any;
  value: ResourceValue[] = [];
  test = "fuu";

  ngOnInit(): void {
    this.id = this.route.snapshot.paramMap.get('id')
    if (this.id) {
      this.getResource(this.id);
    }
    this.getServices();
  }

  public async getServices() {
    this.services = await this.api.getServices();
  }

  public isObject(value: any) {
    return (typeof value === 'object');
  }

  public async getArtifacts() {
    this.artifacts = await this.api.getArtifacts();
  }

  private async getResource(id: string) {
    this.resource = await this.api.getResource(id);
  }

  public showFuncInput(basyxFunc: ResourceValue) {
    this.value = basyxFunc.inputVariables;
    this.selectedBasyxFunc = basyxFunc;

  }

  public async send() {
    if(this.id) {
      console.log(this.value);
      await this.api.executeFunction(this.id, this.selectedBasyxFunc.idShort, this.value);
      console.log({"inputArguments": this.value});
      console.log({"inputArguments":[{"value":{"modelType":{"name":"Property"},"idShort":"url","value":"file:///C:/Users/Chris/Desktop/Install/gen/artifacts/SimpleMeshTestingApp-0.1.0-SNAPSHOT-bin.jar","kind":"Template","valueType":"string"},"modelType":{"name":"OperationVariable"}}]});
    }

  }
}
