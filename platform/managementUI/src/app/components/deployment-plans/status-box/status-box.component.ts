import { PlatformData, PlatformResources, platformResponse } from './../../../../interfaces';
import { ApiService } from 'src/app/services/api.service';
import { Component, OnInit } from '@angular/core';
import { PlanDeployerService } from 'src/app/services/plan-deployer.service';
import { statusCollection, statusMessage } from 'src/interfaces';
import { MatDialog } from '@angular/material/dialog';
import { StatusDetailsComponent } from './status-details/status-details.component';
import { WebsocketService } from 'src/app/websocket.service';
import { Subject, Subscription } from 'rxjs';

@Component({
  selector: 'app-status-box',
  templateUrl: './status-box.component.html',
  styleUrls: ['./status-box.component.scss']
})
export class StatusBoxComponent implements OnInit {

  //statusSub: Subscription;
  hidden =  ["TaskId", "AliasIds", "SubDescription"];

  StatusCollection: statusCollection[];
  //StatusCollection2: statusCollection[];
  private subscription!: Subscription;

  showAll = false;

  constructor(private deployer: PlanDeployerService,
    public dialog: MatDialog,
    public api: ApiService,
    private websocketService: WebsocketService) {
    // this.statusSub = this.deployer.emitter.subscribe(
    //   (status: StatusMsg) => {this.status = status});
    // this.allStatusSub = this.deployer.allEmitter.subscribe(
    //   (status: Resource[]) => {this.statusSubmodel = status});

      this.StatusCollection = deployer.StatusCollection;
      /*
      this.StatusCollection2 = websocketService.data
      this.subscription = this.websocketService.getMsg().subscribe(
        dataFromServer => this.receiveStatus(dataFromServer)
      )*/
  }

  statusUri:any

  ngOnInit(): void {
    //this.getStatusData()
    //console.log("[status-box | ngOnInit] status uri: " + this.statusUri)

  }

  public dismiss(process: statusCollection) {
    //let process = this.StatusCollection.find(process => process.taskId = taskId)
    if(process) {
      this.StatusCollection.splice(this.StatusCollection.indexOf(process), 1);
    }
  }

  public filter(element: string | undefined) {
    let print = true;
    if(element) {
      if(this.hidden.indexOf(element) >= 0) {
        print = false;
      }
    }
    return print;
  }

  // public checkForTaskId(id: string) {
  //   if(this.showAll) {
  //     return true;
  //   } else if(this.TaskIDs.indexOf(id) >= 0) {
  //     return true;
  //   } else {
  //     return false;
  //   }

  // }

  public getLastStatus(status: statusMessage[]) {
    if(status.length > 0) {
      return status[status.length - 1];
    } else {
      return null;
    }

  }

  public details(process: statusCollection) {
      let dialogRef = this.dialog.open(StatusDetailsComponent, {
        maxHeight: '80%',
        maxWidth:  '80%',

      })
      dialogRef.componentInstance.process = process;
  }
  /*
  private receiveStatus(Status: statusMessage) {

    let isFinished = false;
    let isSuccesful = true;
    if(Status.taskId) {
      if(Status.action === "RESULT") {
        isFinished = true;
        // reload page
        this.triggerDataReloadingAction();
      }
      if(Status.action === "ERROR") {
        isSuccesful = false;
      }
      //const process = this.StatusCollection.find(process => process.taskId === Status.taskId)
      const process = this.StatusCollection2.find(process => process.taskId === Status.taskId)
      if(process) {
        process.messages.push(Status);
        //status messages might not be recieved in order of the respective process step occuring,
        //therefore, once a result or error message was recieved, isFinished must stay true once it was set to true.
        if(process.isFinished === false) {
          process.isFinished = isFinished;
        }
        if(process.isSuccesful === true) {
          process.isSuccesful = isSuccesful;
        }
      } else {
        //this.StatusCollection.push({taskId: Status.taskId, isFinished: isFinished, isSuccesful: isSuccesful, messages: [Status]});
        this.StatusCollection2.push({taskId: Status.taskId, isFinished: isFinished, isSuccesful: isSuccesful, messages: [Status]});
      }
      console.log("[status-box | receive] process ")
      console.log(process);
    } else {
      console.log("WARNING: Recieved status without taskId: ");
      console.log(Status);
    }
  }*/

  public reloadingDataSubject = new Subject<any>();

  public triggerDataReloadingAction() {
    console.log("Data reloading has been triggered.")
    this.reloadingDataSubject.next(null);
  }

  public async getStatusData() {
    await this.getStatusUri()
    this.websocketService.connect(this.statusUri)
  }

  public async getStatusUri() {
    let url = "/aas/submodels/Status/submodel/submodelElements/status/uri"
    let resp = await this.api.getData(url) as PlatformData
    if(resp) {
      this.statusUri = resp.value
    }
    console.log("[status-box | getUri] status uri: " + this.statusUri)
  }
}
