import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StatusBoxComponent } from './status-box.component';

describe('StatusBoxComponent', () => {
  let component: StatusBoxComponent;
  let fixture: ComponentFixture<StatusBoxComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ StatusBoxComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(StatusBoxComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
