import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { ST_ERROR, ST_RECEIVED, ST_RESULT, statusCollection, statusMessage} from 'src/interfaces';

@Injectable({
  providedIn: 'root'
})
export class StatusCollectionService {

  public StatusCollection: statusCollection[] = [];
  public reloadingDataSubject = new Subject<any>();
  private finishedNotifier: StatusCollectionNotifier = ()=> {};

  constructor() {
  }

  public receiveStatus(Status: statusMessage) {
    let isFinished = false;
    let isSuccesful = true;
    if (Status.taskId) {
      if (Status.action === ST_RESULT) {
        isFinished = true;
        this.finishedNotifier(true);
        // reload page
        this.triggerDataReloadingAction();
      }
      if (Status.action === ST_ERROR) {
        isSuccesful = false;
        this.finishedNotifier(false);
      }
      const process = this.StatusCollection.find(process => process.taskId === Status.taskId)
      if (process) {
        process.messages.push(Status);
        //status messages might not be recieved in order of the respective process step occuring,
        //therefore, once a result or error message was recieved, isFinished must stay true once it was set to true.
        if (process.isFinished === false) {
          process.isFinished = isFinished;
        }
        if (process.isSuccesful === true) {
          process.isSuccesful = isSuccesful;
        }
      } else {
        this.StatusCollection.push({taskId: Status.taskId, isFinished: isFinished, isSuccesful: isSuccesful, messages: [Status]});
      }
    } /*else {
      console.warn("WARNING: Recieved status without taskId: ");
      console.warn(Status);
    }*/
  }

  public async addReceivedMessage(message: string, taskId: string) {
    const status: statusMessage = {taskId: taskId, action: ST_RECEIVED, aliasIds: [], componentType: "", description: message, deviceId: "", id: "", progress: 0, subDescription: ""};
    this.StatusCollection.push({taskId: taskId, isFinished: false, isSuccesful: true, messages: [status]});
  }

  public dismissStatus(taskId: string) {
    let process = this.StatusCollection.find(process => process.taskId = taskId)
    if(process) {
      this.StatusCollection.splice(this.StatusCollection.indexOf(process), 1);
    }
  }

  public triggerDataReloadingAction() {
    console.debug("Data reloading has been triggered.")
    this.reloadingDataSubject.next(null);
  }

    // for testing
  public setFinishedNotifier(notifier: StatusCollectionNotifier) {
    this.finishedNotifier = notifier;
  }
  
}

export type StatusCollectionNotifier = (success:boolean) => void; 
