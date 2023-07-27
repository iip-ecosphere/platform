import { WebsocketService } from './../../../websocket.service';
import { Component, OnInit, Input, Inject, Optional, HostListener, NgZone, PipeTransform } from '@angular/core';
import { InputVariable, platformResponse } from 'src/interfaces';
import { firstValueFrom, Subscription } from 'rxjs';
import { EnvConfigService } from 'src/app/services/env-config.service';
import { HttpClient } from '@angular/common/http';
import { ApiService } from 'src/app/services/api.service';
import { webSocket, WebSocketSubject } from 'rxjs/webSocket';
import { DialogService } from 'src/app/services/dialog.service';
import { ActivatedRoute, Router } from '@angular/router';
import { ChangeDetectionStrategy } from '@angular/compiler';

@Component({
  selector: 'app-logs-dialog',
  templateUrl: './logs-dialog.component.html',
  styleUrls: ['./logs-dialog.component.scss']
})

export class LogsDialogComponent implements OnInit{

  constructor(
    private envConfigService: EnvConfigService,
    public http: HttpClient,
    public api: ApiService,
    private websocketService: WebsocketService,
    private dialogService: DialogService,

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
        {this.updateData = val})
    }
  transform(value: any, ...args: any[]) {
    throw new Error('Method not implemented.');
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
  // websocket endpoints
  stdoutUrl:string = "";
  stderrUrl:any

  data:any = {
    id: "",
    idShort: "test_service_id",
    logs: "test_values"
  }
  updateData: string = "";

  feedback: string = 'no feedback'; // todo loe
  receivedData: any; // todo loe
  counter: number = 0; // todo loe

  ngOnInit(): void {
    console.log("[logs-dialog] ngOnInit has been triggered")
  }

  public async getUrlParams() {
    let idParam = 'id='
    let idShortParam = 'idShort='
    let url = window.location.href
    let resultIdParam = url.match(/id=.*&/);
    let resultIdShortParam = url.match(/idShort=.*/);
    console.log('url: ' + url)

    let serviceId = ''
    if (resultIdParam) {
      serviceId = resultIdParam[0]
      serviceId = serviceId.replace(idParam, '')
      serviceId = serviceId.replace('&', '')
      this.data.id = serviceId
    }
    let serviceIdShort = ''
    if (resultIdShortParam) {
      serviceIdShort = resultIdShortParam[0]
      serviceIdShort = serviceIdShort.replace(idShortParam, '')
      this.data.idShort = serviceIdShort
    }
  }

  /* used for passing service id per dialog service
  public getLogs() {
    let result = "logs data"

    this.dialogService.data$.subscribe(data => {
      this.receivedData = data
    })

    console.log("[logs-dialog] received data: " + this.receivedData)
    this.data.id = this.receivedData[0]
    this.data.idShort = this.receivedData[1]
    console.log("[logs-dialog] \nservice id: " + this.data.id
      + "\nservice idShort: " + this.data.idShort)

    this.getLogsData(this.data.idShort)

    return result
  }

  public async getLogsData(serviceIdShort: string) {
    await this.getWebsocketEndpoint(serviceIdShort)
    this.websocketService.connect(this.stdoutUrl)
    this.websocketService.connect(this.stderrUrl)
  }
  */

  public test() {
    this.feedback = "test function has been triggered: " + this.data.id
    this.counter += 1
    this.getWebsocketEndpoint(this.data.idShort)
    this.websocketService.connect(this.stdoutUrl)
  }

  public async getWebsocketEndpoint(serviceIdShort: string) {
    this.receivedData = "websocket" // todo loe

    serviceIdShort = "SimpleReceiver_Simple_Mesh_Testing_App_1"
    const inputVariable = await this.getInputVar(serviceIdShort)

    let resourceId = this.serviceInfo.resource
    let aasElementURL = "/aas/submodels/resources/submodel/submodelElements/"
    let basyxFun = "serviceManagers/a" + this.serviceInfo.serviceMgr.replace("@", "_") + "/serviceStreamLog"
    const response = await this.api.executeFunction(resourceId, aasElementURL, basyxFun, inputVariable) as unknown as platformResponse

    this.getPlatformResponseResolution(response)
    console.log("[logs-dialog]  Endpoints - \nstdout: " + this.stdoutUrl
    + ", \nstderr: " + this.stderrUrl)
    this.receivedData = this.stdoutUrl  // todo loe
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
   //console.log("service info: " + this.serviceInfo.resource + ", " + this.serviceInfo.serviceMgr)

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

  public closeLogsStream() {
    this.websocketService.close()
  }
}
