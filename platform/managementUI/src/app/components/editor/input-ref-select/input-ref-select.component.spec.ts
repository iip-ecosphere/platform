import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InputRefSelectComponent } from './input-ref-select.component';

describe('InputRefSelectComponent', () => {
  let component: InputRefSelectComponent;
  let fixture: ComponentFixture<InputRefSelectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ InputRefSelectComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(InputRefSelectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
