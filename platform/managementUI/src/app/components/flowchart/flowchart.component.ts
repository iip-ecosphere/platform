import { Component, OnInit } from '@angular/core';
import Drawflow from 'drawflow';
import { DrawflowService } from 'src/app/services/drawflow.service';

@Component({
  selector: 'app-flowchart',
  templateUrl: './flowchart.component.html',
  styleUrls: ['./flowchart.component.scss']
})
export class FlowchartComponent implements OnInit {


  constructor(private df: DrawflowService) {

  }

  editor: any;
  services: any;
  serviceMeshes: any;
  private displayAttributes = ['kind', 'name', 'ver', 'type'];

  servicesLoading = true;


  async ngOnInit() {
    this.services = await this.df.getServices();
    if(this.services) {
      this.servicesLoading = false;
    }
    this.serviceMeshes = await this.df.getServiceMeshes();
    console.log(this.services);

    const drawFlowHtmlElement = <HTMLElement>document.getElementById('drawflow');
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
    }

  }

  public zoomIn() {
    console.log('zoom zoom');
    this.editor.zoom_in();

  }

  public zoomOut() {
    this.editor.zoom_out();
  }

  public async getGraph(mesh: string) {
    let data = await this.df.getGraph(mesh);
    console.log(data);
    if(data?.outputArguments[0].value?.value) {
      let graph = JSON.parse(data?.outputArguments[0].value?.value);
      let graph2 = JSON.parse(graph.result);
      console.log(graph2);
      let nodes = graph2.drawflow.Home.data
      for(let node in nodes) {
        const a = nodes[node];
        a.html = "<div><div> " + a.data.ivmlVar + "</div><br> <div>kind: "+ a.data.kind + "</div><br> <div> type: "+ a.data.type + "</div></div>";
      }
      this.editor.import(graph2);
    }
  }

  public selectMesh(mesh: string) {
    this.getGraph(mesh);
  }

  public displayAttribute(attribute: string) {
    let contains = false;
    if(this.displayAttributes.includes(attribute)){
      contains = true;
    }
    return contains;
  }

}
