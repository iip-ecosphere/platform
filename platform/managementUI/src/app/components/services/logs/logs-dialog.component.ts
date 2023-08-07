import { WebsocketService } from './../../../websocket.service';
import { Component, OnInit, HostListener } from '@angular/core';
import { InputVariable, platformResponse } from 'src/interfaces';
import { firstValueFrom, Subscription } from 'rxjs';
import { EnvConfigService } from 'src/app/services/env-config.service';
import { HttpClient } from '@angular/common/http';
import { ApiService } from 'src/app/services/api.service';

@Component({
  selector: 'app-logs-dialog',
  templateUrl: './logs-dialog.component.html',
  styleUrls: ['./logs-dialog.component.scss'],
  host: {'window:beforeunload':'closeLogsStream'}
})

export class LogsDialogComponent implements OnInit{

  constructor(
    private envConfigService: EnvConfigService,
    public http: HttpClient,
    public api: ApiService,
    private websocketService: WebsocketService
    ) {
      const env = this.envConfigService.getEnv();
      if(env && env.ip) {
        this.ip = env.ip;
      }
      if (env && env.urn) {
        this.urn = env.urn;
      }
      this.logs = websocketService.data
      this.subscription = this.websocketService.getMsg().subscribe((val) =>
        {this.data.logs += "\n" + val})
  }

  ip: string = "";
  urn: string = "";
  logs:string | undefined;
  serviceMgr: string | undefined; // todo do we need it?
  serviceInfo: any;
  private subscription!: Subscription;

  /* logs stream mode mode="START"|"TAIL"
  (start=log from start, tail=continue at end as selecter by user)*/
  mode = "START"
  stdoutUrl:string = ""; // websocket endpoints
  stderrUrl:string = "";

  data:any = {
    id: "",
    idShort: "test_service_id",
    logs: "",
    type: null
  }
   // logs type
   stdout = 'stdout'
   stderr = 'stderr'

  idParam =  'id='
  idShortParam = 'idShort='
  typeParam = 'type='

  running:number = 0

  ngOnInit(): void {
    console.log('[logs-dialog | ngOnInit] running: ' + this.running)
    this.startLogsStream()
  }

  public async startLogsStream() {
    this.getUrlParams()
    console.log('[logs-dialog | startLogsStream] running: ' + this.running
      + ' idShort: ' + this.data.idShort)
    if (this.running == 0) {
      await this.getWebsocketEndpoint(this.data.idShort)
      if (this.data.type == this.stdout) {
        this.websocketService.connect(this.stdoutUrl)
      } else {
        this.websocketService.connect(this.stderrUrl)
      }
      this.running = 1
    } else {
      console.log('[logs-dialog.comp | startLogsStream] '
        + 'stream cannot be started because it is already running. ')
    }
  }

  public getUrlParams() {
    console.log('[logs-dialog | getUrlParams] triggered ')
    let url = window.location.href
    let params =  url.match(/id=.*/);

    if(params) {
      let paramsList = params[0].split("&", 3)
      let serviceId = paramsList[0]
      serviceId = serviceId.replace(this.idParam, '')
      this.data.id = serviceId

      let serviceIdShort = paramsList[1]
      serviceIdShort = serviceIdShort.replace(this.idShortParam, '')
      this.data.idShort = serviceIdShort

      let type = paramsList[2]
      type = type.replace(this.typeParam, '')
      this.data.type = type
    }
  }

  public async getWebsocketEndpoint(serviceIdShort: string) {
    const inputVariable = await this.getInputVar(serviceIdShort)

    let resourceId = this.serviceInfo.resource
    let aasElementURL = "/aas/submodels/resources/submodel/submodelElements/"
    let basyxFun = "serviceManagers/a"
      + this.serviceInfo.serviceMgr.replace("@", "_")
      + "/serviceStreamLog"

    const response = await this.api.executeFunction(
      resourceId,
      aasElementURL,
      basyxFun,
      inputVariable) as unknown as platformResponse

    this.getPlatformResponseResolution(response)
    console.log("[logs-dialog | getPlatformResponseResolution]"
      + "Endpoints - \nstdout: "
      + this.stdoutUrl
      + ", \nstderr: "
      + this.stderrUrl)
  }

  public getPlatformResponseResolution(response:platformResponse) {
    if(response && response.outputArguments) {
      let output = response.outputArguments[0]?.value?.value;
      if (output) {
        let temp = JSON.parse(output);
        if (temp.result) {
          let result = JSON.parse(temp.result);
          this.stdoutUrl = result[0]
          this.stderrUrl = result[1]
        }
      }
    }
  }

  public async getInputVar(serviceId:string) {
    this.serviceInfo = await this.getServiceInfo(serviceId)

    let inputVariables: InputVariable[] = [];
    let input0:InputVariable = {
      value: {
        modelType: {
          name: "Property"
        },
        valueType: "string",
        idShort: "id",
        kind: "Template",
        value: this.data.id
      }
    }
    let input1:InputVariable = {
      value: {
        modelType: {
          name: "Property"
        },
        valueType: "string",
        idShort: "mode",
        kind: "Template",
        value: this.mode
      }
    }
    inputVariables.push(input0)
    inputVariables.push(input1)

    return inputVariables
  }

  public async getServiceInfo(serviceId: string){
    let response: any;
    response = await this.getPlatformData("services", "services/" + serviceId)

    let serviceResource
    let serviceServiceMgr
    if (response) {
      serviceResource = response.value.find(
        (val: { idShort: string; }) => val.idShort === "resource").value
      serviceServiceMgr = response.value.find(
        (val: { idShort: string; }) => val.idShort === "serviceMgr").value
    }
    this.serviceMgr = serviceServiceMgr
    return {resource: serviceResource, serviceMgr: serviceServiceMgr}

  }

  public async getPlatformData(submodel: any, submodelElement: any){
    let response: any;

    try {
        response = await firstValueFrom(
          this.http.get(this.ip + '/shells/'
        + this.urn
        + "/aas/submodels/"
        + submodel
        + "/submodel/submodelElements/"
        + submodelElement));
      } catch(e) {
        console.log(e);
      }
    return response
  }

  public reset() {
    this.data.logs = ""
  }

  public closeLogsStream() {
    this.websocketService.close()
    this.running = 0
  }
}
