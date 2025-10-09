import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { editorInput } from 'src/interfaces';

@Component({
    selector: 'app-boolean-dropdown',
    templateUrl: './boolean-dropdown.component.html',
    styleUrls: ['./boolean-dropdown.component.scss'],
    standalone: false
})
export class BooleanDropdownComponent implements OnInit {

  @Input() input: editorInput | undefined;
  @Output() valueChange = new EventEmitter<boolean>(); // the output

  onChange(value: boolean) {
    this.selected = value;
    if (this.input) {
      this.input.value = value; // reflect back to parent data
    }
  }

  selected: boolean | undefined;
  
  constructor() { }

  ngOnInit(): void {
    if (this.input) {
      // input.value is of type any, not matched by mat-select
      let val;
      if (Array.isArray(this.input.value)) {
        val = this.input.value[0]; 
      } else {
        val = this.input.value;
      }
      this.selected = String(val) == "true";
    }
  }

}
