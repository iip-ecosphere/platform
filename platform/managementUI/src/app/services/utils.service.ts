import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { DeviceSubmodel } from 'src/interfaces';

@Injectable({
  providedIn: 'root'
})
export class UtilsService {

  constructor(private http: HttpClient) { }

  public async getManufacturerImage(DeviceSubmodelUrl: string) {
    //unfinished at the time
    const DeviceSubmodel: DeviceSubmodel = await firstValueFrom(this.http.get(DeviceSubmodelUrl + '/submodel')) as DeviceSubmodel;
    const manufacturerImage: string = DeviceSubmodel.submodelElements.GeneralInformation.value.ManufacturerLogo.value;
    const mimeType = DeviceSubmodel.submodelElements.GeneralInformation.value.ManufacturerLogo.mimeType;
    return manufacturerImage;
  }
}
