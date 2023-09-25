import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MeshFeedbackComponent } from './mesh-feedback.component';

describe('MeshFeedbackComponent', () => {
  let component: MeshFeedbackComponent;
  let fixture: ComponentFixture<MeshFeedbackComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MeshFeedbackComponent ]
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
