import { Component, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { PlanDeployerService } from 'src/app/services/plan-deployer.service';
import { Resource, StatusMsg } from 'src/interfaces';

@Component({
  selector: 'app-status-box',
  templateUrl: './status-box.component.html',
  styleUrls: ['./status-box.component.scss']
})
export class StatusBoxComponent implements OnInit {

  statusSub: Subscription;
  status: StatusMsg = {
    executionState: "",
    messages: [""]
  }
  hidden =  ["TaskId", "AliasIds", "SubDescription"];

  allStatusSub: Subscription;
  statusSubmodel: Resource[] = [];


  constructor(private deployer: PlanDeployerService) {
    this.statusSub = this.deployer.emitter.subscribe((status: StatusMsg) => {this.status = status});
    this.allStatusSub = this.deployer.allEmitter.subscribe((status: Resource[]) => {this.statusSubmodel = status});
  }

  ngOnInit(): void {
  }

  public dismiss() {
    this.status.executionState = "";
    this.status.messages = [""];
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

}
