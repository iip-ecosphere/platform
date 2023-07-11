import { WebsocketService } from './../../../websocket.service';
import { Component, OnInit, Input, Inject } from '@angular/core';
import { InputVariable, platformResponse, statusCollection, statusMessage } from 'src/interfaces';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { firstValueFrom } from 'rxjs';
import { EnvConfigService } from 'src/app/services/env-config.service';
import { HttpClient } from '@angular/common/http';
import { ApiService } from 'src/app/services/api.service';
import { webSocket, WebSocketSubject } from 'rxjs/webSocket';

@Component({
  selector: 'app-logs-dialog',
  templateUrl: './logs-dialog.component.html',
  //template: '<h1> lalala </h1>',
  styleUrls: ['./logs-dialog.component.scss']
})

export class LogsDialogComponent implements OnInit {

  constructor(
    //private dialogRef: MatDialogRef<LogsDialogComponent>,
    //@Inject(MAT_DIALOG_DATA) public logsData: any,
    private envConfigService: EnvConfigService,
    public http: HttpClient,
    public api: ApiService,
    private websocketService: WebsocketService) {
      const env = this.envConfigService.getEnv();
      if(env && env.ip) {
        this.ip = env.ip;
      }
      if (env && env.urn) {
        this.urn = env.urn;
      }

    }

  ip: string = "";
  urn: string = "";
  logs:string | undefined;
  serviceMgr: string | undefined; // todo do we need it?
  serviceInfo: any;
  temp:any; // todo loe

  /* logs stream mode mode="START"|"TAIL"
  (start=log from start, tail=continue at end as selecter by user)*/
  mode = "START"
  // websocket endpoints
  stdoutUrl:string = "";
  stderrUrl:any

  data:any = {
    idShort: "test_service_id",
    value: "test values"
  }

  ngOnInit(): void {
    console.log("LOGS-DIALOG-COM")
    /*
    console.log(this.logsData)
    console.log("id: " + this.logsData.id)
    console.log("idShort: " + this.logsData.idShort)
    console.log("-------")
    this.getLogsData(this.logsData.idShort)
    this.logs = "test data"
    */
  }

  public voice() {
    console.log("logs dialog component is giving voice")
  }

  /*
  public async getLogsData(serviceIdShort: string) {
    this.getWebsocketEndpoint(serviceIdShort)
    this.websocketService.connect(this.stderrUrl)
  }

  public async getWebsocketEndpoint(serviceIdShort: string) {
    const inputVariable = await this.getInputVar(serviceIdShort)
    let resourceId = this.serviceInfo.resource
    let aasElementURL = "/aas/submodels/resources/submodel/submodelElements/"
    let basyxFun = "serviceManagers/a" + this.serviceInfo.serviceMgr.replace("@", "_") + "/serviceStreamLog"
    const response = await this.api.executeFunction(resourceId, aasElementURL, basyxFun, inputVariable) as unknown as platformResponse
    this.getPlatformResponseResolution(response)
    console.log("Endpoints - \nstdout: " + this.stderrUrl + ", \nstderr: " + this.stderrUrl)
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
        value: this.logsData.id
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

  close():void {
    this.dialogRef.close();
  }
  */
}
