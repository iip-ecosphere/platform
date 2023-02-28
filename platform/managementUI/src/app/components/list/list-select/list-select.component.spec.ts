import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListSelectComponent } from './list-select.component';

describe('ListSelectComponent', () => {
  let component: ListSelectComponent;
  let fixture: ComponentFixture<ListSelectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ListSelectComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ListSelectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
