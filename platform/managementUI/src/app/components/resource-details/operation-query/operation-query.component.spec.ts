import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OperationQueryComponent } from './operation-query.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('OperationQueryComponent', () => {
  let component: OperationQueryComponent;
  let fixture: ComponentFixture<OperationQueryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule ],
      declarations: [ OperationQueryComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OperationQueryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
