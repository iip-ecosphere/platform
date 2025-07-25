import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import Drawflow from 'drawflow';
import { IvmlFormatterService } from 'src/app/components/services/ivml/ivml-formatter.service';
import { MeshFeedbackComponent } from './feedback/mesh-feedback/mesh-feedback.component';
import { MatDialog } from '@angular/material/dialog';
import { ApiService, GRAPHFORMAT_DRAWFLOW } from 'src/app/services/api.service';
import { MT_metaType, MT_varValue } from 'src/interfaces';

interface Bus {
  id: string;
  color: string;
}

@Component({
    selector: 'app-flowchart',
    templateUrl: './flowchart.component.html',
    styleUrls: ['./flowchart.component.scss'],
    standalone: false
})
export class FlowchartComponent implements OnInit {


  constructor(private api: ApiService, 
    private route: ActivatedRoute,
    public ivmlFormatter:IvmlFormatterService,
    public dialog: MatDialog) {}

  @Input() inputMesh: any

  // editor: Drawflow | undefined;
  editor: any;
  services: any;
  serviceMeshes: any;
  private displayAttributes = ['ver', 'type'];
  private meta = ['metaType', 'metaProject', 'metaState', 'metaAas', 'metaRefined', 'metaAbstract', 'metaTypeKind', 'metaVariable'];

  selectedService: any;

  meshUnchanged: any;
  servicesLoading = true;
  mesh: any;

  Busses: Bus[] = [];
  busColors = ["red", "orange", "cyan", "yellow", "green", "purple", "magenta"];

  toggleBus = false;


  async ngOnInit() {
    this.services = await this.api.getConfiguredServices();
    if(this.services) {
      this.servicesLoading = false;
    }
    this.serviceMeshes = await this.api.getConfiguredServiceMeshes();

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
      this.editor.on('click',() => (this.addService(event)));

      this.editor.createCurvature = function(start_pos_x: number,
        start_pos_y : number, end_pos_x: number, end_pos_y: number,
        curvature_value: any, type: any) {
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

  public assignIcon(type: string, htmlTag?: boolean) {
    type = type.toLowerCase();
    let icon: string = '';
    let iconHtml = '';
    
    if (type.includes("java")) {
      icon = "../../../assets/java.png";
    } else if (type.includes("flower")) {
      icon = "../../../assets/flower.png";
    } else if (type.includes("opc")) {
      icon = "../../../assets/opc.png";
    } else if (type.includes("mqtt")) {
      icon = "../../../assets/mqtt.png";
    } else if (type.includes("py")) {
      icon = "../../../assets/py.png";
    }
    if (icon != '' && htmlTag) {
      iconHtml= "<img src=\""+ icon +"\" height=\"25px\">";
    } else if (icon != '') {
      iconHtml= icon;
    }
    return iconHtml;
  }

  public zoomIn() {
    this.editor.zoom_in();

  }

  public zoomOut() {
    this.editor.zoom_out();
  }

  public async getGraph(mesh: string) {
    let opRes = await this.api.getConfiguredServiceMeshGraph(mesh, GRAPHFORMAT_DRAWFLOW);
    if (opRes && opRes.result) {
      this.Busses = [];
      let graph2 = JSON.parse(opRes.result);
      //this.meshUnchanged = JSON.parse(JSON.stringify(graph2)); //for debug purposes
      this.mesh = graph2;
      let nodes = graph2.drawflow.Home.data
      let busOut: string[] = [];
      let busIn: string[] = [];
      let count = 0;
      for(let node in nodes) {

        const a = nodes[node];
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
            console.error("Maximum amount of busses exceeded (7)");
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

        let iconHtml= this.assignIcon(a.data.type as string, true);

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
    if (this.displayAttributes.includes(attribute)){
      contains = true;
    }
    return contains;
  }

  public isMeta(input: string) {
    let isMeta = false;
    if (this.meta.includes(input)) {
      isMeta = true;
    }
    return isMeta;
  }

  public getId(serviceValue: any[]) {
    let value = serviceValue.find(item => item.idShort === 'id').value;
    return value.find((item: { idShort: string; }) => item.idShort === MT_varValue).value;
  }
 
  public getType(serviceValue: any[]) {
    return serviceValue.find(item => item.idShort === MT_metaType).value;
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

  public selectService(service: any) {
    this.selectedService = service;
  }

  public addService(event: any) {
    if(this.selectedService) {

      let name = this.getServiceValue(this.selectedService, "name")
      let kind = this.getServiceValue(this.selectedService, "kind")
      let ver = this.getServiceValue(this.selectedService, "ver")
      let id = this.getServiceValue(this.selectedService, "id")

      console.log("name of the node: " + name)
      this.editor.addNode(name, 1, 1, event.layerX, event.layerY, '', {},
        '<div>'
        + id + '<br><p class="subtext">name: '
        + name + '</p><p class="subtext">kind: '
        + kind + '</p><p class="subtext">ver: '
        + ver + '</p><div>'
        , false);
      this.selectedService = undefined;
    }
  }

  getServiceValue(service: any, idShortValue: string) {
    let result = null
    if (service.value) {
      let values = service.value.find((x: { idShort: string; }) => x.idShort == idShortValue).value
      let varValue = values.find((x: {idShort: string;}) => x.idShort == "varValue").value
      result = varValue
    }
    return result
  }

  meshName:string = ""

  public async create() {
    let drawflowRaw = JSON.stringify(this.editor.drawflow.drawflow.Home.data);
    let drawflow = drawflowRaw.replace("drawflow: ", "");
    let feedbackInternal = await this.ivmlFormatter.setMesh("", "", this.meshName, drawflow);
    const dialogRef = this.dialog.open(MeshFeedbackComponent, {});
    dialogRef.componentInstance.feedback = feedbackInternal.feedback;
  }

}
