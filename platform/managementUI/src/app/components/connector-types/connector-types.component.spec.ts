import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConnectorTypesComponent } from './connector-types.component';

describe('ConnectorTypesComponent', () => {
  let component: ConnectorTypesComponent;
  let fixture: ComponentFixture<ConnectorTypesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ConnectorTypesComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ConnectorTypesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
