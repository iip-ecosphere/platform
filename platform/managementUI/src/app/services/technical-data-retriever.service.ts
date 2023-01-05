import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Subscription, Subject } from 'rxjs';
import { Resource, TechnicalDataResponse, GeneralInformation, AddressPart } from 'src/interfaces';

@Injectable({
  providedIn: 'root'
})
export class TechnicalDataRetrieverService {


  TechnicalResponses: TechnicalDataResponse[] = [];
  Subscriptions: Subscription[] = [];
  emitter: Subject<GeneralInformation>;
  ResourcePictures: GeneralInformation[] = [];

  constructor(private http: HttpClient) {
    this.emitter = new Subject<GeneralInformation>();
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
                const ProductImage = GeneralInformation.value.find(item => item.idShort.includes('ProductImage'));
                const ManufacturerName = GeneralInformation.value.find(item => item.idShort.includes('ManufacturerName'));
                const Address = GeneralInformation.value.find(item => item.idShort.includes('Address'));
                  let GeneralInfo = {
                    picture: ProductImage?.value,
                    resourceIdShort: resource?.idShort,
                    ManufacturerName: ManufacturerName?.value,
                    Address: Address
                  };
                  console.log(GeneralInfo);
                  this.ResourcePictures.push(GeneralInfo); //make it so that generalInformation is stored in the api services resource array
                  this.emitter.next(GeneralInfo);

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




