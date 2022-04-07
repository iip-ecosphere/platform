import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { ApiService } from 'src/app/services/api.service';
import { PlatformResources } from 'src/interfaces';

@Component({
  selector: 'app-resources',
  templateUrl: './resources.component.html',
  styleUrls: ['./resources.component.scss']
})
export class ResourcesComponent implements OnInit {

  constructor(public http: HttpClient, public api: ApiService) { }

  Data: PlatformResources = {};

  ngOnInit(): void {
    this.getData();
  }

  public async getData() {
    this.Data = await this.api.getResources();
  }

}
