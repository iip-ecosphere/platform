import { HttpClient } from '@angular/common/http';
import { ConstantPool } from '@angular/compiler';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from 'src/app/services/api.service';
import { SnackbarService } from 'src/app/services/snackbar.service';
import { outputArgument, PlatformResources, platformResponse, PlatformServices, ResourceSubmodelElement, ResourceValue } from 'src/interfaces';

@Component({
  selector: 'app-services',
  templateUrl: './services.component.html',
  styleUrls: ['./services.component.scss']
})
export class ServicesComponent implements OnInit {

  constructor(public http: HttpClient, public api: ApiService, public route: ActivatedRoute, public bar: SnackbarService) { }

  services: PlatformServices = {};
  servicesToggle: boolean[] = [];

  artifacts: PlatformResources = {};
  artifactsToggle: boolean[] = [];

  id: string | null = null;
  resource: ResourceSubmodelElement | undefined;
  selectedBasyxFunc : any;
  value: ResourceValue[] = [];
  message: string = '';

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
    this.message = ''

  }

  public async send() {
    if(this.id) {
      const response = await this.api.executeFunction(this.id, this.selectedBasyxFunc.idShort, this.value) as platformResponse;
      console.log(response);
      if(response && response.outputArguments) {
        this.openSnackbar(response.outputArguments);
      }
    }

  }

  private openSnackbar(output: outputArgument[]) {
    try {
      let message = '';
      if(output[0].value) {
        //this.bar.openSnackbar(output[0].value.value);
        for(let bit of output) {
          message = message.concat(bit.value.value);
          message = message.concat('  ')
        }
      }
      this.message = message;
    } catch(e) {
      console.log(e);
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
