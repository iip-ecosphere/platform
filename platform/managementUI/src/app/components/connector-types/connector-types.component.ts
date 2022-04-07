import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { PlatformResources } from 'src/interfaces';

@Component({
  selector: 'app-connector-types',
  templateUrl: './connector-types.component.html',
  styleUrls: ['./connector-types.component.scss']
})
export class ConnectorTypesComponent implements OnInit {

  constructor(public http: HttpClient) { }

  Data: PlatformResources = {}

  ngOnInit(): void {
    this.getData();
  }
  public async getData() {

    try {
      this.Data = await this.http.get("http://192.168.159.128:9001/shells/urn%3A%3A%3AAAS%3A%3A%3AiipEcosphere%23/aas/submodels/types/submodel").toPromise() as PlatformResources;
      console.log(this.Data);
    } catch(e) {
      console.log(e);
    }

  }



}
