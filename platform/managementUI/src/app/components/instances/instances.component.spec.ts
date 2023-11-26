import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InstancesComponent } from './instances.component';
import { HttpClientModule } from '@angular/common/http';
import { EnvConfigService } from '../../services/env-config.service';

describe('InstancesComponent', () => {

  let component: InstancesComponent;
  let fixture: ComponentFixture<InstancesComponent>;

  beforeEach(async () => {
    await EnvConfigService.initAsync();
    await TestBed.configureTestingModule({
      imports: [ HttpClientModule ],
      declarations: [ InstancesComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InstancesComponent);
    component = fixture.componentInstance;
    await component.ngOnInit();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should show app instances', async() => {
    await fixture.detectChanges();
    let compiled = fixture.nativeElement as HTMLElement;

    // TODO requires started instance from deployment-plans and test frame
    expect(component).toBeTruthy();
  });

});
