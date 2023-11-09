import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FlowchartComponent } from './flowchart.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { RouterTestingModule } from "@angular/router/testing";
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';

describe('FlowchartComponent', () => {
  let component: FlowchartComponent;
  let fixture: ComponentFixture<FlowchartComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ HttpClientTestingModule, 
          RouterTestingModule, 
          MatDialogModule, 
          FormsModule, 
          MatIconModule, 
          MatCardModule, 
          MatInputModule ],
      declarations: [ FlowchartComponent ],
      providers: [
          {provide: MatDialogRef, useValue: {}},
          {provide: MAT_DIALOG_DATA, useValue: []},
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FlowchartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
