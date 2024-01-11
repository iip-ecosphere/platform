import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from 'src/app/services/api.service';
import { LANG_ENGLISH, SemanticResolutionService } from 'src/app/services/semantic-resolution.service';
import { Resource, ResourceAttribute } from 'src/interfaces';

@Component({
  selector: 'app-resource-details',
  templateUrl: './resource-details.component.html',
  styleUrls: ['./resource-details.component.scss']
})
export class ResourceDetailsComponent implements OnInit {

  id: string | null = null;
  resource: Resource | undefined;
  platformURL:string = "/aas/submodels/platform/submodel/submodelElements/";

  constructor(public http: HttpClient, public api: ApiService, public resolver: SemanticResolutionService,
    public route: ActivatedRoute) { }

  async ngOnInit() {
    this.id = this.route.snapshot.paramMap.get('id')
    if (this.id) {
      await this.getResource(this.id);
    }
  }

  private async getResource(id: string) {
    this.resource = await this.api.getResource(id);
    this.resolveSemanticId();
  }

  // col 0: attribute name from AAS
  // col 1: name displayed in UI
  // col 2: category of the attribute in UI
  attributeNames = [
    ["Storage_Free", "Free", "Storage"],
    ["Storage_Capacity", "Capacity", "Storage"],
    ["Storage_Usable", "Usable", "Storage"],
    ["Allocated_Storage", "Allocated", "Storage"],
    ["Memory_Free", "Free", "Memory"],
    ["Memory_Capacity", "Capacity", "Memory"],
    ["Allocated_Memory", "Allocated", "Memory"],
    ["Memory_Used", "Used", "Memory"],
    ["CPU_Temperature", "CPU", "Temperature"],
    ["Case_Temperature", "Case", "Temperature"],
    ["containerSystemName", "Name", "Container System"],
    ["OS", "OS", "Device"],
    ["CPU_Architecture", "CPU Architecture", "Device"],
    ["CPU_Capacity", "CPU Capacity", "Device"],
    ["runtimeName", "Name", "Runtime"],
    ["runtimeVersion", "Version", "Runtime"],
    ["deviceAas", "Device AAS", "Device"],
    ["containerSystemVersion", "Version", "Container System"],
    ["managedId", "Managed ID", "Device"],
    ["ip", "IP", "Device"]
  ]
  categories = ["Storage", "Memory", "Container System", "Device", "Runtime", "Temperature"]

  // index: 1 - attribute name
  //        2 - attribute category (e.g. storage, memory)
  public getAttributeInfo(element: any, col: number) {
    let row = this.attributeNames.find(item => item[0] == element)
    if(row != undefined) {
      return row[col]
    } else {
      return null
    }
  }

  // ---------- Semantic Id -------------------------
  public async resolveSemanticId() {
    if (this.resource && this.resource.value) {
      let resourceAttributes = this.resource.value;
      let semanticIds = [] as string[];
      for (const attribute of resourceAttributes) {
        semanticIds.push(SemanticResolutionService.validateId(attribute.semanticId?.keys[0].value));
      }
      let resolvedInfo = await this.resolver.resolveSemanticIds(semanticIds, LANG_ENGLISH);

      let j = 0;
      for (const semInfo of resolvedInfo) {
        if (!this.resource!.value![j]) { // TODO workaround for testing, check async chain
          this.resource!.value![j] = {} as ResourceAttribute;
        }
        let attr = this.resource!.value![j];
        if (semInfo.name == "Percent") {
          ResourceDetailsComponent.convertPercent(attr);
        } else if (semInfo.name == "byte") {
          ResourceDetailsComponent.convertGByte(attr);
        } else {
          attr.semanticName = semInfo.name;
        }
        attr.semanticDescription = semInfo.description;
        j++;
      }
    }
  }

  /**
   * Converts a resource attribute for display to percent.
   * 
   * @param attr the attribute
   */
  private static convertPercent(attr: ResourceAttribute) {
    attr.semanticName = "%";
    attr.value = (attr.value * 100).toFixed(1);
  }

  // Converts byte value of resource attribute:
  // e.g. conversion to GB - dominator: 1000000000, unitName: GB

  private static convertGByte(attr: ResourceAttribute) {
    ResourceDetailsComponent.convertByte(attr, 1000000000, "GB");
  }

    /**
   * Converts a resource attribute for display from byte to GB.
   * 
   * @param attr the attribute
   */
  private static convertByte(attr: ResourceAttribute, dominator:any, unitName:string) {
    attr.semanticName = unitName;
    let temp_value = attr.value;
    temp_value = (temp_value/dominator).toFixed(2)
    attr.value = temp_value
  }

}
