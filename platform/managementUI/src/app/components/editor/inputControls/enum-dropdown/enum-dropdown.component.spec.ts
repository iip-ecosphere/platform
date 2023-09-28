import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EnumDropdownComponent } from './enum-dropdown.component';

describe('EnumDropdownComponent', () => {
  let component: EnumDropdownComponent;
  let fixture: ComponentFixture<EnumDropdownComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EnumDropdownComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EnumDropdownComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
