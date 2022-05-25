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

  services: PlatformServices = {};
  servicesToggle: boolean[] = [];

  artifacts: PlatformResources = {};
  artifactsToggle: boolean[] = [];

  id: string | null = null;
  resource: ResourceSubmodelElement | undefined;
  selectedBasyxFunc : any;
  value: ResourceValue[] = [];

  ngOnInit(): void {
    this.id = this.route.snapshot.paramMap.get('id')
    if (this.id) {
      this.getResource(this.id);
    }
    this.getServices();
    this.getArtifacts();
  }

  public async getServices() {
    this.services = await this.api.getServices();
    if(this.services && this.services.submodelElements) {
      this.servicesToggle = new Array(this.services.submodelElements.length).fill(false);
    }
  }

  public serToggle(index: number) {
    if(this.servicesToggle) {
      this.servicesToggle[index] = !this.servicesToggle[index]
    }
  }

  public async getArtifacts() {
    this.artifacts = await this.api.getArtifacts();
    if(this.artifacts && this.artifacts.submodelElements) {
      this.artifactsToggle = new Array(this.artifacts.submodelElements.length).fill(false);
    }
  }

  public artToggle(index: number) {
    if(this.artifactsToggle) {
      this.artifactsToggle[index] = !this.artifactsToggle[index]
    }
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
      const response = await this.api.executeFunction(this.id, this.selectedBasyxFunc.idShort, this.value);
      console.log(response);
    }

  }

  public isArray(value: any) {
    const bo = Array.isArray(value);
    return bo;
  }

  public isObject(value: any) {
    return (typeof value === 'object');
  }
}
