import { Component, OnInit } from '@angular/core';
import { buildInformation } from 'src/interfaces';
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
    buildId: '',
    isRelease: undefined,
  };

  constructor(private api: ApiService) {}

  async ngOnInit() {
    const response = await this.api.getPlatformData();
    if(response) {
      const version = response.find(item => item.idShort === 'version');
      const buildId = response.find(item => item.idShort === 'buildId');
      const isRelease = response.find(item => item.idShort === 'isRelease');
      if(version && version.value) {
        this.Data.version = version.value;
      }
      if(buildId && buildId.value) {
        this.Data.buildId = buildId.value;
      }
      if(isRelease && isRelease.value != undefined) {
        this.Data.isRelease = isRelease.value;
      }
    }
    console.log(response);
  }
}
