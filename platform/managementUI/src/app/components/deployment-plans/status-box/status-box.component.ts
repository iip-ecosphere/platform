import { Component, OnInit } from '@angular/core';
import { PlanDeployerService } from 'src/app/services/plan-deployer.service';
import { statusCollection, statusMessage } from 'src/interfaces';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { StatusDetailsComponent } from './status-details/status-details.component';

@Component({
  selector: 'app-status-box',
  templateUrl: './status-box.component.html',
  styleUrls: ['./status-box.component.scss']
})
export class StatusBoxComponent implements OnInit {

  //statusSub: Subscription;
  hidden =  ["TaskId", "AliasIds", "SubDescription"];


  StatusCollection: statusCollection[];

  showAll = false;


  constructor(private deployer: PlanDeployerService,
    public dialog: MatDialog) {
    // this.statusSub = this.deployer.emitter.subscribe(
    //   (status: StatusMsg) => {this.status = status});
    // this.allStatusSub = this.deployer.allEmitter.subscribe(
    //   (status: Resource[]) => {this.statusSubmodel = status});

      this.StatusCollection = deployer.StatusCollection;
  }

  ngOnInit(): void {
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
      let dialogRef = this.dialog.open(StatusDetailsComponent)
      dialogRef.componentInstance.process = process;

  }
}
