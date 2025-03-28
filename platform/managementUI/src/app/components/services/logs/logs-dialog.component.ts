import { WebsocketService } from './../../../websocket.service';
import { Component, OnInit, HostListener} from '@angular/core';
import { InputVariable } from 'src/interfaces';
import { Subscription } from 'rxjs';
import { EnvConfigService } from 'src/app/services/env-config.service';
import { HttpClient } from '@angular/common/http';
import { AAS_OP_PREFIX_SME, AAS_TYPE_STRING, ApiService } from 'src/app/services/api.service';
//import * as saveAs from 'file-saver';

@Component({
    selector: 'app-logs-dialog',
    templateUrl: './logs-dialog.component.html',
    styleUrls: ['./logs-dialog.component.scss'],
    host: { 'window:beforeunload': 'closeLogsStream' },
    standalone: false
})

export class LogsDialogComponent implements OnInit{

  constructor(private envConfigService: EnvConfigService,
    public http: HttpClient,
    public api: ApiService,
    private websocketService: WebsocketService) {
      this.websocketService = this.websocketService.createInstance();
      this.logs = websocketService.data
      this.subscription = this.websocketService.getMsgSubject().subscribe((val) =>
        {this.data.logs += "\n" + val})
  }

  logs:string | undefined;
  serviceMgr: string | undefined;
  serviceInfo: any;
  private subscription!: Subscription;
  getUrl: () => string = () => window.location.href;

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
    console.debug('[logs-dialog | ngOnInit] running: ' + this.running)
    this.startLogsStream()
  }

  public async startLogsStream() {
    this.getUrlParams()
    console.debug('[logs-dialog | startLogsStream] running: ' + this.running
      + ' idShort: ' + this.data.idShort)
    if (this.running == 0) {
      const inputVariable = await this.getInputVar(this.data.idShort, this.mode)
      if (inputVariable) {
        await this.getWebsocketEndpoint(inputVariable)
        if (this.data.type == this.stdout) {
          if (this.stdoutUrl && this.stdoutUrl.length > 0) {
              this.websocketService.connect(this.stdoutUrl)
              this.running = 1
          } else {
            console.warn("No stdoutUrl available. Cannot start log stream websocket.");
          }
        } else {
          if (this.stderrUrl && this.stderrUrl.length > 0) {
            this.websocketService.connect(this.stderrUrl)
            this.running = 1
          } else {
            console.warn("No stdoutUrl available. Cannot start log stream websocket.");
          }
        }
      }
    } else {
      console.warn('[logs-dialog.comp | startLogsStream] '
        + 'stream cannot be started because it is already running. ')
    }
  }

  public getUrlParams() {
    console.debug('[logs-dialog | getUrlParams] triggered ')
    let url = this.getUrl();
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

  public async getWebsocketEndpoint(inputVariable: InputVariable[]) {
    let resourceId = this.serviceInfo.resource
    let aasElementURL = "/aas/submodels/resources/submodel/submodelElements/"
    let basyxFun = "serviceManagers/a"
      + this.serviceInfo.serviceMgr.replace("@", "_")
      + "/serviceStreamLog"
    const response = await this.api.executeAasJsonOperation("resources", AAS_OP_PREFIX_SME + basyxFun, inputVariable);
    const platfResponse = this.api.getPlatformResponse(response);
    let valid = false;
    if (platfResponse && platfResponse?.result) {
      let result = JSON.parse(platfResponse.result);
      this.stdoutUrl = result[0];
      this.stderrUrl = result[1];
    } else {
      console.warn("No valid platform response, no stream URLs available.");
    }
    console.debug("[logs-dialog | getPlatformResponseResolution]"
      + "Endpoints - \nstdout: "
      + this.stdoutUrl
      + ", \nstderr: "
      + this.stderrUrl)
  }

  public async getInputVar(serviceId:string, mode:string) {
    this.serviceInfo = await this.getServiceInfo(serviceId);

    let inputVariables: InputVariable[] | null;
    if (this.serviceInfo && this.serviceInfo.serviceMgr) {
      inputVariables = [];
      inputVariables.push(ApiService.createAasOperationParameter("id", AAS_TYPE_STRING, this.data.id));
      inputVariables.push(ApiService.createAasOperationParameter("mode", AAS_TYPE_STRING, mode))
      this.inputVarPlaceholder = inputVariables
    } else {
      inputVariables = null;
      console.warn(`No service info for ${serviceId} available.`);
    }
    return inputVariables
  }

  public async getServiceInfo(serviceId: string){
    let response: any;
    response = await this.api.getSubmodelElement("services", "services/" + serviceId)

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

  public async reset() {
    this.data.logs = ""
  }

  public close() {
    console.debug("[log-dialog | close] triggered")
    this.websocketService.close()
    if (this.running != 0) {
      this.closeLogsStream()
      this.running = 0
    }
  }

  inputVarPlaceholder: InputVariable[] | undefined

  public async closeLogsStream() {
    console.debug("[log-dialog | closeLogs Async] triggered")
    let inputVar = await this.getInputVar(this.data.idShort, "STOP")
    if (inputVar) {
      let basyxFun = "serviceManagers/a"
        + this.serviceInfo.serviceMgr.replace("@", "_")
        + "/serviceStreamLog"
      const response = await this.api.executeAasJsonOperation("resources", AAS_OP_PREFIX_SME + basyxFun, inputVar);
      console.debug("[log-dialog | closeStreamLog] platform response: ")
      console.debug(response?.executionState)
    }
  }

  // ----------- sync -----------------


  public closeLogsStreamSync() {
    console.debug("[log-dialog | closeLogs Sync] triggered")

    let inputVariable = this.inputVarPlaceholder
    if (inputVariable) {
      if (inputVariable[1].value) {
        inputVariable[1].value.value = "STOP"
      }
      let cfg = this.envConfigService.getCfg();
      var url = ApiService.constructOperationCallUrl(cfg, "resources", AAS_OP_PREFIX_SME + this.serviceInfo.resource + "/"
        + "serviceManagers/a" + this.serviceInfo.serviceMgr.replace("@", "_") + "/serviceStreamLog");
      let data = ApiService.constructOperationCallBody(inputVariable);
  
      var request = new XMLHttpRequest()
      request.open('POST', url, false)
      request.setRequestHeader("Content-Type", "application/json")
      request.onreadystatechange = function() {
        if (request.readyState === XMLHttpRequest.DONE) {
          if (request.status === 200) {
            const response = JSON.parse(request.responseText)
            console.debug('Response: ' + response)
          } else {
            console.error('Error ' + request.status, request.statusText)
          }
        }
      }
      request.send(JSON.stringify(data))
  
      if (request.status === 200) {
        this.running = 0
        return request.response
      } else if (request.status === 201) {
        this.running = 0
        return "HTTP status 201"
      } else {
        throw new Error("request failed " + request.response )
      }
    }
  }

  @HostListener('window:beforeunload', ['$event'])
  beforeunloadHandler(event: any) {
    console.debug("[log-dialog | beforeunloadHandler] triggered")
    if (this.running != 0) {
      this.closeLogsStreamSync()
    }
  }
}
