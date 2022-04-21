import { HttpClient } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ApiService } from 'src/app/services/api.service';
import { PlatformServices } from 'src/interfaces';

@Component({
  selector: 'app-services',
  templateUrl: './services.component.html',
  styleUrls: ['./services.component.scss']
})
export class ServicesComponent implements OnInit {

  constructor(public http: HttpClient, public api: ApiService) { }

  Data: PlatformServices = {}

  ngOnInit(): void {
    this.getData();
  }
  public async getData() {

    this.Data = await this.api.getServices();
  }

  public isObject(value: any) {
    return (typeof value === 'object');
  }
}
