import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MeshFeedbackComponent } from './mesh-feedback.component';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';

describe('MeshFeedbackComponent', () => {
  let component: MeshFeedbackComponent;
  let fixture: ComponentFixture<MeshFeedbackComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MeshFeedbackComponent ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MeshFeedbackComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
