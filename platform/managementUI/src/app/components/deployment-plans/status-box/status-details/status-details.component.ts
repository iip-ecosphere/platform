import { Component, Input, OnInit } from '@angular/core';
import { statusCollection } from 'src/interfaces';

@Component({
  selector: 'app-status-details',
  templateUrl: './status-details.component.html',
  styleUrls: ['./status-details.component.scss']
})
export class StatusDetailsComponent implements OnInit {

  @Input()  process: statusCollection | undefined;

  constructor() { }

  ngOnInit(): void {
  }

}
