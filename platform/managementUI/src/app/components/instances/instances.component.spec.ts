import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InstancesComponent } from './instances.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('InstancesComponent', () => {
  let component: InstancesComponent;
  let fixture: ComponentFixture<InstancesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule ],
      declarations: [ InstancesComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InstancesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
