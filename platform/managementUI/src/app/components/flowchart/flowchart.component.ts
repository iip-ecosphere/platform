import { Component, OnInit, ViewChild } from '@angular/core';
import Drawflow from 'drawflow';

@Component({
  selector: 'app-flowchart',
  templateUrl: './flowchart.component.html',
  styleUrls: ['./flowchart.component.scss']
})
export class FlowchartComponent implements OnInit {


  constructor() {

  }

  editor: any;


  ngOnInit(): void {
    const drawFlowHtmlElement = <HTMLElement>document.getElementById('drawflow');
    console.log(drawFlowHtmlElement);
    if(drawFlowHtmlElement) {
      this.editor = new Drawflow(drawFlowHtmlElement);
      this.editor.reroute = true;
      this.editor.curvature = 0.5;
      this.editor.reroute_fix_curvature = true;
      this.editor.reroute_curvature = 0.5;
      this.editor.force_first_input = false;
      this.editor.line_path = 1;
      this.editor.editor_mode = 'edit';

      this.editor.start();

      // this.editor.on('nodeCreated', function(id) {
      //   console.log("Node created " + id);
      // })

      let data = {name: 'cool name', coolNumber: '2' }
      const htmlTemplate = `<div">${data.name} ${data.coolNumber}</div>`;

      this.editor.addNode('Home', 1, 1, 50, 50, '', {name: 'cool node', coolNumber: '1' }, htmlTemplate);
      this.editor.addNode('Home', 1, 1, 300, 100, '', {name: 'cool node', coolNumber: '2' }, htmlTemplate);

      console.log(this.editor);

    }

  }

  public zoomIn() {
    console.log('zoom zoom');
    this.editor.zoom_in();

  }

  public zoomOut() {
    this.editor.zoom_out();
  }

}