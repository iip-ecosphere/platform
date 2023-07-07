import { Component, OnInit, Input, Inject } from '@angular/core';
import { InputVariable, statusCollection, statusMessage } from 'src/interfaces';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { firstValueFrom } from 'rxjs';
import { EnvConfigService } from 'src/app/services/env-config.service';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-logs-dialog',
  templateUrl: './logs-dialog.component.html',
  styleUrls: ['./logs-dialog.component.scss']
})

export class LogsDialogComponent implements OnInit {

  constructor(
    private dialogRef: MatDialogRef<LogsDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public logsData: any,
    private envConfigService: EnvConfigService,
    public http: HttpClient) {
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
  serviceMgr: string | undefined;

  ngOnInit(): void {
    console.log("logsData:")
    console.log(this.logsData)
    console.log(this.logsData.serviceId)
    console.log("-------")
    this.logs = "test data"
  }

  public async getLogsData(serviceId: string) {
    const inputVariable = await this.getInputVar(serviceId)
  }

  public async getInputVar(serviceId:string) {
    let serviceInfo = this.getServiceInfo(serviceId)
    //console.log("service info: " + (await serviceInfo).resource + ", " + (await serviceInfo).serviceMgr)

    let inputVariables: InputVariable[] = [];
    let input0:InputVariable = {
      value: {
        modelType: {
          //name: "OperationVariable"
          name: "Property"
        },
        valueType: "string",
        idShort: "id",
        kind: "Template",
        value: (await serviceInfo).resource
      }
    }
  }

  public async getServiceInfo(serviceId: string){
    let response: any;
    response = await this.getPlatformData("services", "services" + serviceId)

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
}
