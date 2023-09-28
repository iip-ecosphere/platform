import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-mesh-feedback',
  templateUrl: './mesh-feedback.component.html',
  styleUrls: ['./mesh-feedback.component.scss']
})
export class MeshFeedbackComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

  feedback:string = "default value"
}
