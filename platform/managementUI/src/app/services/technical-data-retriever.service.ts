import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Subscription, Subject } from 'rxjs';
import { Resource, ResourceProductPicture, TechnicalDataResponse } from 'src/interfaces';

@Injectable({
  providedIn: 'root'
})
export class TechnicalDataRetrieverService {


  TechnicalResponses: TechnicalDataResponse[] = [];
  Subscriptions: Subscription[] = [];
  emitter: Subject<ResourceProductPicture>;
  ResourcePictures: ResourceProductPicture[] = [];

  constructor(private http: HttpClient) {
    this.emitter = new Subject<ResourceProductPicture>();
  }

  public getTechnicalData(resources: Resource[]) {
    for(const resource of resources) {
      if(resource.value && resource.idShort) {
        const deviceAAS = resource.value.find(item => item.idShort === 'deviceAas');
        if(deviceAAS && deviceAAS.value) {
          const urn: string = deviceAAS.value;
          const obs = this.retrieve(urn);
          if(obs) {
            this.Subscriptions.push(obs.subscribe((item: TechnicalDataResponse) => {
              this.TechnicalResponses.push(item);
              const GeneralInformation = item.submodelElements.find(item => item.idShort === 'GeneralInformation');
              if(GeneralInformation && GeneralInformation.value) {
                const ProductImage = GeneralInformation.value.find(item => item.idShort.includes('ProductImage'))
                if(ProductImage && ProductImage.value && resource.idShort) {
                  let pic = {
                    picture: ProductImage.value,
                    idShort: resource.idShort
                  }
                  this.ResourcePictures.push(pic);
                  this.emitter.next(pic);
                }
              }
            } ));
          }
        }
      }
    }
  }

  private retrieve(urn: string) {
    try {
      const Obs = this.http.get<TechnicalDataResponse>(urn + '/submodels/TechnicalData/submodel');
      return Obs;
    } catch(e) {
      console.log(e);
      return undefined
    }
  }
}




