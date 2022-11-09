import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from 'src/app/services/api.service';
import { PlatformResources, PlatformServices, Resource } from 'src/interfaces';

@Component({
  selector: 'app-resource-details',
  templateUrl: './resource-details.component.html',
  styleUrls: ['./resource-details.component.scss']
})
export class ResourceDetailsComponent implements OnInit {

  services: PlatformServices = {};
  servicesToggle: boolean[] = [];

  artifacts: PlatformResources = {};
  artifactsToggle: boolean[] = []; //probably not needed

  id: string | null = null;
  resource: Resource | undefined;

  constructor(public http: HttpClient, public api: ApiService, public route: ActivatedRoute) { }

  ngOnInit(): void {
    this.id = this.route.snapshot.paramMap.get('id')
    if (this.id) {
      this.getResource(this.id);

    }
    this.getServices();
    this.getArtifacts();
  }

  private async getResource(id: string) {
    this.resource = await this.api.getResource(id);
  }

  public async getServices() {
    this.services = await this.api.getServices();
    if(this.services && this.services.submodelElements) {
      this.servicesToggle = new Array(this.services.submodelElements.length).fill(false);
    }
  }

  public async getArtifacts() {
    this.artifacts = await this.api.getArtifacts();
    if(this.artifacts && this.artifacts.submodelElements) {
      this.artifactsToggle = new Array(this.artifacts.submodelElements.length).fill(false);
    }
  }

  public isObject(value: any) {
    return (typeof value === 'object');
  }

  public serToggle(index: number) {
    if(this.servicesToggle) {
      this.servicesToggle[index] = !this.servicesToggle[index]
    }
  }

}
