import { PlatformData, PlatformResources, ST_ERROR, ST_RESULT, platformResponse } from './../../../../interfaces';
import { ApiService } from 'src/app/services/api.service';
import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { StatusCollectionService } from 'src/app/services/status-collection.service';
import { statusCollection, statusMessage } from 'src/interfaces';
import { MatDialog } from '@angular/material/dialog';
import { StatusDetailsComponent } from './status-details/status-details.component';
import { WebsocketService } from 'src/app/websocket.service';
import { Subject, Subscription } from 'rxjs';
import { Utils } from 'src/app/services/utils.service';

@Component({
    selector: 'app-status-box',
    templateUrl: './status-box.component.html',
    styleUrls: ['./status-box.component.scss'],
    standalone: false
})
export class StatusBoxComponent extends Utils implements OnInit {

  //hidden =  ["TaskId", "AliasIds", "SubDescription"];

  StatusCollection: statusCollection[];
  private subscription!: Subscription;
  statusUri:any
  showAll = false;
  @ViewChild('result') private resultContainer!: ElementRef;

  constructor(private collector: StatusCollectionService,
    public dialog: MatDialog,
    public api: ApiService,
    private websocketService: WebsocketService) {
      super();
      this.StatusCollection = collector.StatusCollection;
  }

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

  /*public filter(element: string | undefined) {
    let print = true;
    if(element) {
      if(this.hidden.indexOf(element) >= 0) {
        print = false;
      }
    }
    return print;
  }*/

  // public checkForTaskId(id: string) {
  //   if(this.showAll) {
  //     return true;
  //   } else if(this.TaskIDs.indexOf(id) >= 0) {
  //     return true;
  //   } else {
  //     return false;
  //   }

  // }

  public getLastStatus(process: statusCollection) {
    let len = process.messages.length;
    if (len > 0) {
      let last = process.messages[len - 1];
      let text = "";
      if (process.isFinished && process.isSuccesful) {
        if (last.action == ST_RESULT) {
          if (last.id == "Configuration") {
            if (last.result && this.isNonEmptyString(last.result)) {
              let tmp = JSON.parse(last.result);
              if (Array.isArray(tmp) && tmp.length > 0) {
                text = "Template download: " + 
                  tmp.flatMap(u => `<a href="${u}">${u.substring(u.lastIndexOf('/') + 1)}</a>`)
                    .join(", ");
              }
            }
          }
        } else if (last.action == ST_ERROR) {
          if (this.isNonEmptyString(last.result)) {
            text = "Error: " + last.result;
          } else {
            text = "Error: unknown";
          }
        }
      }
      if (this.resultContainer && this.resultContainer.nativeElement && text.length > 0) {
        this.resultContainer.nativeElement.innerHTML = text;
      }
      return last;
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

}
