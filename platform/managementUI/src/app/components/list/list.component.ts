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
  noData: boolean = false;
  submodelElements: any; //help
  unwantedTypes = ["metaState", "metaType", "metaProject"];

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

  listTitles = [
    {name:"Setup", value:"EndpointAddress"},
    {name:"Constants", value:"String"},
    {name:"Types", value:"RecordType"},
    {name:"Services", value:"Service"},
    {name:"Servers", value:"ImplAddress"}, // TODO
    {name:"Meshes", value:"ServiceMesh"},
    {name:"Applications", value:"Application"},
    {name:"Artifacts", value:"artifact"}  // TODO
  ]

  ngOnInit(): void {
  }

  public async loadData(list: string) {
    //this.router.navigateByUrl('list/' + list)
    //this.ls = this.route.snapshot.paramMap.get('ls');
    //this.ls = this.route.snapshot.paramMap.get(list);
    this.ls = list; // TODO is it ok like this?
    if(this.ls) {
      console.log(this.ls)
      this.data = await this.getData(this.ls);
      this.filter();
    }
    this.submodelElements = await this.getSubmodelElements();
    // TODO do I need it?
    console.log(this.data)
  }

  public async getData(list: string) {
    let response;
    let path;
    if (list == "artifact") {
      path = "/aas/submodels/services/submodel/submodelElements/";
      list = "services"
    } else {
      path = "/aas/submodels/Configuration/submodel/submodelElements/";
    }

    try {
        response = await firstValueFrom(
          this.http.get(this.ip + '/shells/'
        + this.urn
        + path
        + list));
      } catch(e) {
        console.log(e);
        this.noData = true;
      }

    return response;
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

  //help method to instantly read all idShort of submodelElement collections in configuration
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

  public edit(item: any) {
    if(this.ls === "ServiceMesh") {
      this.router.navigateByUrl('flowchart/' + item.idShort);
    }


  }

  public del(item: any) {

  }

  public createMesh() {

  }



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
