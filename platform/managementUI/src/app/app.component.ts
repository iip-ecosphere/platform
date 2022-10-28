import { Component, OnInit } from '@angular/core';
import { buildInformation, platformResponse, ResourceAttribute } from 'src/interfaces';
import { ApiService } from './services/api.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit{
  title = 'IIPES_Web';


  Data: buildInformation = {
    version: '',
    buildId: ''
  };

  constructor(private api: ApiService) {}

  async ngOnInit() {
    const response = await this.api.getTechData();
    if(response) {
      const version = response.find(item => item.idShort === 'version');
      const buildId = response.find(item => item.idShort === 'buildId');
      if(version && version.value) {
        this.Data.version = version.value;
      }
      if(buildId && buildId.value) {
        this.Data.buildId = buildId.value;
      }
    }
    console.log(response);
  }
}
