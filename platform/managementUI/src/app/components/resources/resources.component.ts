import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { ApiService } from 'src/app/services/api.service';
import { PlatformResources, ResourceValue } from 'src/interfaces';

@Component({
  selector: 'app-resources',
  templateUrl: './resources.component.html',
  styleUrls: ['./resources.component.scss']
})
export class ResourcesComponent implements OnInit {

  Data: PlatformResources = {};

  errorSub: Subscription;
  errorMsg: string | undefined;

  clicked: boolean = false;
  test = [1, 2, 3, 4, 5, 6, 7, 8, 9]

  defaultImageUrl = '../../../assets/devideDefault.jpg';

  constructor(public http: HttpClient, public api: ApiService, public router: Router) {
    this.errorSub = this.api.errorEmitter.subscribe((error: HttpErrorResponse) => {this.errorMsg = error.message});
  }

  ngOnInit(): void {
    this.getData();
  }

  public async getData() {
    this.Data = await this.api.getResources();
    console.log(this.Data);
  }

  public isArray(value: any) {
    const bo = Array.isArray(value);
    return bo;
  }

  public async details(resource: ResourceValue[] | undefined) {
    let id: string | undefined = undefined;
    if(this.isArray(resource)){
      const test = await resource?.find(item => item.idShort === "managedId");
      if (test) {
        id = test.value;
      }
    }
    if (id) {
      this.router.navigateByUrl("/resources/" + id);

    } else {
      console.log("ERROR: resource does not have a managedId");
    }

  }

  public async details2(resource: ResourceValue[] | undefined) {
    let id: string | undefined = undefined;
    if(this.isArray(resource)){
      const test = await resource?.find(item => item.idShort === "managedId");
      if (test) {
        id = test.value;
      }
    }
    if (id) {
      this.router.navigateByUrl("/services/" + id);
    } else {
      console.log("ERROR: resource does not have a managedId");
    }

  }

}
