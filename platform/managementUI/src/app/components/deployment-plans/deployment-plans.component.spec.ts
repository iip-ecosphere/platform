import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeploymentPlansComponent } from './deployment-plans.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('DeploymentPlansComponent', () => {
  let component: DeploymentPlansComponent;
  let fixture: ComponentFixture<DeploymentPlansComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule ],
      declarations: [ DeploymentPlansComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DeploymentPlansComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
