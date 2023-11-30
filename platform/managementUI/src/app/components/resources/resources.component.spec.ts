import { ComponentFixture, TestBed  } from '@angular/core/testing';

import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { ResourcesComponent } from './resources.component';
import { HttpClientModule } from '@angular/common/http';
import { EnvConfigService } from '../../services/env-config.service';
import { Location } from "@angular/common";
import { RouterTestingModule } from "@angular/router/testing";
import { Router } from "@angular/router";
import { routes } from "../../app-routing.module";
import { retry } from '../../services/utils.service';

describe('ResourcesComponent', () => {
  
  let fixture: ComponentFixture<ResourcesComponent>;
  let component: ResourcesComponent;
  let location: Location;
  let router: Router;
  let expectedManufacturer : string = "Unknown Manufacturer";

  beforeEach(async () => {
    await EnvConfigService.init();
    await TestBed
      .configureTestingModule({
        imports: [ HttpClientModule, RouterTestingModule.withRoutes(routes) ],
        declarations: [ ResourcesComponent ],
        schemas: [CUSTOM_ELEMENTS_SCHEMA]
      })
      .compileComponents()
      .then(() => {
        fixture = TestBed.createComponent(ResourcesComponent);
        component = fixture.componentInstance;
        router = TestBed.inject(Router);
        location = TestBed.inject(Location);
      });
    await component.getData(); // could be ngOnInit but not async in original code
    await retry({ // techData resolved asynchronously
      fn: function () {
        if (component.techDataResolved) {
          expectedManufacturer = "Dell";
          return true;
        }
        return false;
      },
      maxAttempts: 3,
      delay: 500,
    }).catch(e => {});
    await fixture.detectChanges();        
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('shall have a structured "box" in resources for "local"', async() => {
      await fixture.detectChanges();
      await fixture.whenRenderingDone();

      const compiled = fixture.nativeElement as HTMLElement;
      const box = compiled.querySelector('div[id="box"]') as HTMLElement;
      expect(box).toBeTruthy();
      
      const divHead = box.querySelector('div[id="head"]') as HTMLElement;
      expect(divHead).toBeTruthy();
      expect(divHead.querySelector('h1')?.textContent).toMatch(/^local$/);
      expect(divHead?.textContent).toContain(expectedManufacturer);

      const divPicture = box.querySelector('div[id="picture"]') as HTMLElement;
      expect(divPicture).toBeTruthy();
      const divPictureImg = divPicture.querySelector('img');
      expect(divPictureImg).toBeTruthy(); // must be there in both cases

      const divBottom = compiled.querySelector('div[id="bottom"]') as HTMLElement;
      expect(divBottom).toBeTruthy();
      expect(divBottom.querySelector('button')).toBeTruthy();

      let navigateSpy = spyOn(router, 'navigateByUrl');
      let button = compiled.querySelector('div[id="bottom"] button') as HTMLElement;
      expect(button?.innerText).toEqual('resource details');
      button.click();
      expect(navigateSpy).toHaveBeenCalledWith('/resources/local');
  });

});
