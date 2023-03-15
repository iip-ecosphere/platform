import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import Drawflow from 'drawflow';
import { DrawflowService } from 'src/app/services/drawflow.service';

@Component({
  selector: 'app-flowchart',
  templateUrl: './flowchart.component.html',
  styleUrls: ['./flowchart.component.scss']
})
export class FlowchartComponent implements OnInit {


  constructor(private df: DrawflowService, private route: ActivatedRoute) {}

  editor: any;
  services: any;
  serviceMeshes: any;
  private displayAttributes = ['kind', 'name', 'ver', 'type'];
  private meta = ['metaType', 'metaProject', 'metaState'];

  meshUnchanged: any;
  servicesLoading = true;
  mesh: any;


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

      this.editor.createCurvature = function(start_pos_x: number, start_pos_y : number, end_pos_x: number, end_pos_y: number, curvature_value: any, type: any) {
        var line_x = start_pos_x;
        var line_y = start_pos_y;
        var x = end_pos_x;
        var y = end_pos_y;
        var curvature = curvature_value;
        //type openclose open close other
        switch (type) {
          case 'open':
            if(start_pos_x >= end_pos_x) {
              var hx1 = line_x + Math.abs(x - line_x) * curvature;
              var hx2 = x - Math.abs(x - line_x) * (curvature*-1);
            } else {
              var hx1 = line_x + Math.abs(x - line_x) * curvature;
              var hx2 = x - Math.abs(x - line_x) * curvature;
            }
            return ' M '+ line_x +' '+ line_y +' C '+ hx1 +' '+ line_y +' '+ hx2 +' ' + y +' ' + x +'  ' + y;

            break
          case 'close':
            if(start_pos_x >= end_pos_x) {
              var hx1 = line_x + Math.abs(x - line_x) * (curvature*-1);
              var hx2 = x - Math.abs(x - line_x) * curvature;
            } else {
              var hx1 = line_x + Math.abs(x - line_x) * curvature;
              var hx2 = x - Math.abs(x - line_x) * curvature;
            }                                                                                                                  //M0 75H10L5 80L0 75Z

            return ' M '+ line_x +' '+ line_y +' C '+ hx1 +' '+ line_y +' '+ hx2 +' ' + y +' ' + x +'  ' + y +' M '+ (x-11)  + ' ' + y + ' L'+(x-20)+' '+ (y-5)+'  L'+(x-20)+' '+ (y+5)+'Z';
            break;
          case 'other':
            if(start_pos_x >= end_pos_x) {
              var hx1 = line_x + Math.abs(x - line_x) * (curvature*-1);
              var hx2 = x - Math.abs(x - line_x) * (curvature*-1);
            } else {
              var hx1 = line_x + Math.abs(x - line_x) * curvature;
              var hx2 = x - Math.abs(x - line_x) * curvature;
            }
            return ' M '+ line_x +' '+ line_y +' C '+ hx1 +' '+ line_y +' '+ hx2 +' ' + y +' ' + x +'  ' + y;
            break;
          default:

            var hx1 = line_x + Math.abs(x - line_x) * curvature;
            var hx2 = x - Math.abs(x - line_x) * curvature;

            //return ' M '+ line_x +' '+ line_y +' C '+ hx1 +' '+ line_y +' '+ hx2 +' ' + y +' ' + x +'  ' + y;
            return ' M '+ line_x +' '+ line_y +' C '+ hx1 +' '+ line_y +' '+ hx2 +' ' + y +' ' + x +'  ' + y +' M '+ (x-11)  + ' ' + y + ' L'+(x-20)+' '+ (y-5)+'  L'+(x-20)+' '+ (y+5)+'Z';
        }

      }

      this.editor.start();

      const paramMesh = this.route.snapshot.paramMap.get('mesh')
      if(paramMesh) {
        this.getGraph(paramMesh);
      }

    }

  }

  public zoomIn() {
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
      //this.meshUnchanged = JSON.parse(JSON.stringify(graph2)); //for debug purposes
      console.log(graph2);
      this.mesh = graph2;
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

  public isMeta(input: string) {
    let isMeta = false;
    if(this.meta.includes(input)) {
      isMeta = true;
    }
    return isMeta;
  }

  //to be removed, keeping in case i need to get the coordinates of a mesh again
  public showCoords() {
    for(let i=1; i <=8; i++) {
      console.log(this.editor.getNodeFromId(i));
    }
  }

}
