import { ApiService } from 'src/app/services/api.service';
import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { EnvConfigService } from 'src/app/services/env-config.service';
import { ActivatedRoute } from '@angular/router';
import { Router } from '@angular/router';

@Component({
  selector: 'app-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.scss']
})
export class ListComponent implements OnInit {
  ip: string = "";
  urn: string = "";
  ls: string | null = null;
  data: any;
  filteredData: any;
  //noData: boolean = false;
  //unwantedTypes = ["metaState", "metaType", "metaProject", "metaAas"];
  //dataToDisplay: any;

  constructor(private router: Router,
    private route: ActivatedRoute,
    public http: HttpClient,
    private envConfigService: EnvConfigService,
    public api: ApiService) {
      const env = this.envConfigService.getEnv();
       //the ip and urn are taken from the json.config
      if(env && env.ip) {
        this.ip = env.ip;
      }
      if (env && env.urn) {
        this.urn = env.urn;
      }
    }

  filterParam = [
    {tabName: "Setup", metaProject:"TechnicalSetup", type:null},
    {tabName: "Constants", metaProject:"AllConstants", type:null},
    {tabName: "Types", metaProject:"AllTypes", type:null},
    {tabName: "Services", metaProject:null, type:"ServiceBase"},
    {tabName: "Servers", metaProject:null, type:"Server"},
    {tabName: "Meshes", metaProject:null, type:"ServiceMesh"},
    {tabName: "Applications", metaProject:null, type:"Application"}
  ]

  ngOnInit(): void {
  }

  // TODO change string | null to any
  public async loadData(metaProject: string | null, type: string | null) {
    if (type) {
      this.ls = type //TODO make it more elegant
      this.data = await this.getData(type);
      this.filteredData = this.data.value
    } else {
      this.data = await this.getData("")
      this.filter(metaProject)
    }
  }

  public async getData(type: string) {
    let response;
    try {
        response = await firstValueFrom(
          this.http.get(this.ip + '/shells/'
        + this.urn
        + "/aas/submodels/Configuration/submodel/submodelElements/"
        + type));
      } catch(e) {
        console.log(e);
        //this.noData = true;
      }
    return response;
  }

  public filter(metaProject: string | null) {
    let result = []
    for(const submodelElement of this.data) {
      if(submodelElement.value) {
        for(const elemtSubmodelElement of submodelElement.value) {
          for(const valElemtSubmodelElement of elemtSubmodelElement.value) {
            if(valElemtSubmodelElement.idShort == "metaProject" && valElemtSubmodelElement.value == metaProject) {
                result.push(elemtSubmodelElement)
            }
          }
        }
      }
    }
    this.filteredData = result;
  }

  // ---- buttons -----

  public edit(item: any) {
    if(this.ls === "ServiceMesh") {
      this.router.navigateByUrl('flowchart/' + item.idShort);
    }
  }

  public del(item: any) {

  }

  public createMesh() {

  }

  // --- display of details
   /*
  public getId(serviceValue: any[]) {
    return serviceValue.find(item => item.idShort === 'id').value;
  }
  */

  // old version
  /*
  public async getData(type: string) {
    let response;
    let path;
    if (this.ls == "artifact") {
      path = "/aas/submodels/services/submodel/submodelElements/";
      this.ls = "services"
    } else {
      path = "/aas/submodels/Configuration/submodel/submodelElements/";
    }
    try {
        response = await firstValueFrom(
          this.http.get(this.ip + '/shells/'
        + this.urn
        + path
        + this.ls));
      } catch(e) {
        console.log(e);
        this.noData = true;
      }
    return response;
  }
  */


  //help method to instantly read all idShort of submodelElement collections in configuration
  /*
  public async getSubmodelElements() {
    let response;
    try {
      response = await firstValueFrom(this.http.get(
        this.ip + '/shells/'
      + this.urn
      + "/aas/submodels/Configuration/submodel/submodelElements"));
    } catch(e) {
      console.log(e);
      this.noData = true;
    }
    return response;
  }
  */


  /*
  ip: string = "";
  urn: string = "";

  ls: string | null = null; //the idShort of the collection to get from the configuration
  noData: boolean = false;
  data: any;
  submodelElements: any; //help
  unwantedTypes = ["metaState", "metaType", "metaProject"];

  constructor(public http: HttpClient,
    private envConfigService: EnvConfigService,
    private route: ActivatedRoute) {
    const env = this.envConfigService.getEnv();
    //the ip and urn are taken from the json.config
    if(env && env.ip) {
      this.ip = env.ip;
    }
    if (env && env.urn) {
      this.urn = env.urn;
    }
   }

  async ngOnInit() {
    this.ls = this.route.snapshot.paramMap.get('ls');
    if(this.ls) {
      this.data = await this.getData(this.ls);
      console.log(this.data)
      this.filter();
    }
    this.submodelElements = await this.getSubmodelElements();

  }

  public filter() {
    const relevantValues = []
    let indicesToRemove = []
    let i = 0;
    for(const item of this.data.value) {
      if(!this.unwantedTypes.includes(item.idShort)) {
        indicesToRemove.push(i)
        relevantValues.push(item)
      }
      i++;
    }
    this.data.value = relevantValues
  }

  public async getData(list: string) {
    let response;
    try {
      response = await firstValueFrom(
        this.http.get(this.ip + '/shells/'
      + this.urn
      + "/aas/submodels/Configuration/submodel/submodelElements/"
      + list));
    } catch(e) {
      console.log(e);
      this.noData = true;
    }
    return response;
  }

  //help method to instantly read all idShort of submodelElement collections in configuration
  public async getSubmodelElements() {
    let response;
    try {
      response = await firstValueFrom(this.http.get(this.ip + '/shells/'
      + this.urn + "/aas/submodels/Configuration/submodel/submodelElements"));
    } catch(e) {
      console.log(e);
      this.noData = true;
    }
    return response;
  }

  public edit(item: any) {

  }

  public del(item: any) {

  }

  public createMesh() {

  }
  */

}
