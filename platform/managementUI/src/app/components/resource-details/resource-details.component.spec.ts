import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResourceDetailsComponent } from './resource-details.component';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { TidyPipe } from '../../pipes/tidy.pipe';
import { provideHttpClient, withInterceptorsFromDi } from '@angular/common/http';
import { RouterTestingModule } from "@angular/router/testing";
import { ActivatedRoute, convertToParamMap} from '@angular/router';
import { EnvConfigService } from '../../services/env-config.service';

describe('ResourceDetailsComponent', () => {

  let component: ResourceDetailsComponent;
  let fixture: ComponentFixture<ResourceDetailsComponent>;

  beforeEach(async () => {
    await EnvConfigService.init();
    await TestBed.configureTestingModule({
        declarations: [ResourceDetailsComponent, TidyPipe],
        imports: [MatCardModule, RouterTestingModule, MatIconModule],
        providers: [ResourceDetailsComponent, {
            provide: ActivatedRoute,
            useValue: { snapshot: { paramMap: convertToParamMap({ 'id': 'local' }) } }
        }, provideHttpClient(withInterceptorsFromDi())],
        teardown: {destroyAfterEach: false} // NG0205: Injector has already been destroyed
    })
    .compileComponents()
    .then(() => {
      fixture = TestBed.createComponent(ResourceDetailsComponent);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });
    await component.ngOnInit();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have local data', async() => {
    await fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;

    let container = compiled.querySelector('div[id="resource.header"]') as HTMLElement;
    expect(container).toBeTruthy();
    expect(container.querySelector('img')).toBeTruthy(); // has AAS img in header
    expect(container.innerText).toMatch(".*Resource Details.*"); // has title in header

    container = compiled.querySelector('div[id="resource.idShort"]') as HTMLElement;
    expect(container).toBeTruthy();
    expect(container.innerText).toEqual("local");

    for (let a of component.attributeNames) {
      container = compiled.querySelector(`div[id="resource.${a[0]}"]`) as HTMLElement;      
      if (a[0] != "containerSystemVersion") { // may not be there in simple setup
        expect(container).toBeTruthy();
      }
      if (container) { // may not be there in simple setup
        let elementName = container.querySelector("#name") as HTMLElement;
        expect(elementName).toBeTruthy();
        expect(elementName.innerText.length).toBeGreaterThan(0);
        let elementValue = container.querySelector("#value") as HTMLElement;
        expect(elementValue).toBeTruthy();
        expect(elementName.innerText.length).toBeGreaterThan(0);
        let elementSemName = container.querySelector("#semanticName") as HTMLElement;
        if (elementSemName) { // may not be there
          expect(elementName.innerText.length).toBeGreaterThan(0);
        }
      }
    }
  });

});
