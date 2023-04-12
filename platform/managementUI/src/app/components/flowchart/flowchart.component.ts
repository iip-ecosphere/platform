import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import Drawflow from 'drawflow';
import { DrawflowService } from 'src/app/services/drawflow.service';

interface Bus {
  id: string;
  color: string;
}

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
  private meta = ['metaType', 'metaProject', 'metaState', 'metaAas'];

  meshUnchanged: any;
  servicesLoading = true;
  mesh: any;

  Busses: Bus[] = [];
  busColors = ["red", "orange", "cyan", "yellow", "green", "purple", "magenta"];

  toggleBus = false;


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

      const paramMesh = this.route.snapshot.paramMap.get('mesh');
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
    if(data?.outputArguments[0].value?.value) {
      this.Busses = [];
      let graph = JSON.parse(data?.outputArguments[0].value?.value);
      let graph2 = JSON.parse(graph.result);
      //this.meshUnchanged = JSON.parse(JSON.stringify(graph2)); //for debug purposes
      console.log(graph2);
      this.mesh = graph2;
      let nodes = graph2.drawflow.Home.data
      let busOut: string[] = [];
      let busIn: string[] = [];
      let count = 0;
      for(let node in nodes) {

        const a = nodes[node];
        console.log(a)
        if(a.data["bus-receive"]) {
          busIn = a.data["bus-receive"];
        }
        if(a.data["bus-send"]) {
          busOut = a.data["bus-send"];
        }

        for(let bus of busIn) {
          if(!this.Busses.find(item => item.id === bus) && count < 7) {
            this.Busses.push({id: bus, color: this.busColors[count]});
            count++;
          } else if(count >= 7) {
            console.log("ERROR: maximum amount of busses exceeded (7)");
          }
        }

        let busInHtml = "";
        if(busIn && busIn.length > 0) {
          for(let busName of busIn) {
            let bus = this.Busses.find(item => item.id === busName);
            if(bus) {
              //the line below contains
              busInHtml = busInHtml.concat("<i class=\"material-icons\" style=\"color:"+ bus.color +"\">keyboard_double_arrow_down</i>");
            }
          }
        }

        let busOutHtml = "";
        if(busOut && busOut.length > 0) {
          for(let busName of busOut) {
            let bus = this.Busses.find(item => item.id === busName);
            if(bus) {
              busOutHtml = busOutHtml.concat("<i class=\"material-icons\" style=\"color:"+ bus.color +"\">keyboard_double_arrow_up</i>");
            }
          }
        }

        let icon="";
        let type= a.data.type as string;
        type = type.toLowerCase();
        let iconHtml="";
        if(type.includes("java")) {
          icon = "../../../assets/java.png";
        } else if(type.includes("flower")) {
          icon = "../../../assets/flower.png";
        } else if(type.includes("opc")) {
          icon = "../../../assets/opc.png";
        } else if(type.includes("mqtt")) {
          icon = "../../../assets/mqtt.png";
        } else if(type.includes("py")) {
          icon = "../../../assets/py.png";
        }
        if(icon != "") {
          iconHtml= "<img src=\""+ icon +"\" height=\"25px\">";
        }

        a.html = "<div style=\"width:100%;\">" +
          "<table style=\"width:100%; margin-top: 0px\"><tr><td style=\"background-color:rgb(247,247,247)\"><div style=\"text-align: left\">"
          + iconHtml + "</div></td><td style=\"background-color:rgb(247,247,247)\"><div style=\"text-align: right\">"
          + busInHtml + busOutHtml + "</div></td></tr></table>" +
          "<h3> " + a.data.id + "</h3>" +
          "<p class=\"subtext-small\">"+ a.data.type + "</p>" +
          "<p class=\"subtext-small\">"+ a.data.kind + "</p>" +
          "</div>";

        busIn = [];
        busOut = [];
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

  public getId(serviceValue: any[]) {
    let value = serviceValue.find(item => item.idShort === 'id').value;
    return value.find(
      (item: { idShort: string; }) => item.idShort === 'varValue').value;
  }

  //to be removed, keeping in case i need to get the coordinates of a mesh again
  public showCoords() {
    for(let i=1; i <=8; i++) {
      console.log(this.editor.getNodeFromId(i).id);
      console.log(this.editor.getNodeFromId(i).data.id);
      console.log("x: " + this.editor.getNodeFromId(i).pos_x);
      console.log("y: " + this.editor.getNodeFromId(i).pos_y);
    }
  }

}
