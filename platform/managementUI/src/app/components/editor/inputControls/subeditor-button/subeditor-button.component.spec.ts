import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SubeditorButtonComponent } from './subeditor-button.component';

describe('SubeditorButtonComponent', () => {
  let component: SubeditorButtonComponent;
  let fixture: ComponentFixture<SubeditorButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SubeditorButtonComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SubeditorButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
