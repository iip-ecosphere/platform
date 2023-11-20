import { ComponentFixture, TestBed, waitForAsync  } from '@angular/core/testing';

import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { ResourcesComponent } from './resources.component';
import { HttpClientModule } from '@angular/common/http';
import { EnvConfigService } from '../../services/env-config.service';

describe('ResourcesComponent', () => {
  
  let fixture: ComponentFixture<ResourcesComponent>;
  let component: ResourcesComponent;

  beforeEach(async () => {
    await EnvConfigService.initAsync();
    await TestBed
      .configureTestingModule({
        imports: [ HttpClientModule ],
        declarations: [ ResourcesComponent ],
        schemas: [CUSTOM_ELEMENTS_SCHEMA]
      })
      .compileComponents()
      .then(() => {
        fixture = TestBed.createComponent(ResourcesComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
      });
    await component.getData(); // could be ngOnInit but not async in original code
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('shall have a structured "box" in resources for "local"', async() => {
      await fixture.detectChanges();
      const compiled = fixture.nativeElement as HTMLElement;
      const box = compiled.querySelector('div[id="box"]') as HTMLElement;
      expect(box).toBeTruthy();
      
      const divHead = box.querySelector('div[id="head"]') as HTMLElement;
      expect(divHead).toBeTruthy();
      expect(divHead.querySelector('h1')?.textContent).toMatch(/^local$/);
      expect(divHead?.textContent).toMatch(/^local.*Unknown Manufacturer$/);

      const divPicture = box.querySelector('div[id="picture"]') as HTMLElement;
      expect(divPicture).toBeTruthy();
      const divPictureImg = divPicture.querySelector('img');
      expect(divPictureImg).toBeTruthy(); // must be there in both cases

      const divBottom = compiled.querySelector('div[id="bottom"]') as HTMLElement;
      expect(divBottom).toBeTruthy();
      expect(divBottom.querySelector('button')).toBeTruthy();

      //spyOn(component, 'onEditButtonClick');
      let button = compiled.querySelector('div[id="bottom"] button') as HTMLElement;
      expect(button?.innerText).toEqual('resource details');
      button.click();

      // nothing happens
  });

});
