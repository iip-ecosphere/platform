import { Component, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { PlanDeployerService } from 'src/app/services/plan-deployer.service';

@Component({
  selector: 'app-status-box',
  templateUrl: './status-box.component.html',
  styleUrls: ['./status-box.component.scss']
})
export class StatusBoxComponent implements OnInit {

  statusSub: Subscription;
  status = {
    executionState: "",
    messages: [""]
  }

  constructor(private deployer: PlanDeployerService) {
    this.statusSub = this.deployer.emitter.subscribe((status: { executionState: string,messages: string[]}) => {this.status = status});
  }

  ngOnInit(): void {
  }

  public dismiss() {
    this.status.executionState = "";
    this.status.messages = [""];
  }

}
