import { Component, Input, OnInit } from '@angular/core';
import { editorInput } from 'src/interfaces';

@Component({
  selector: 'app-boolean-dropdown',
  templateUrl: './boolean-dropdown.component.html',
  styleUrls: ['./boolean-dropdown.component.scss']
})
export class BooleanDropdownComponent implements OnInit {

  @Input() input: editorInput | undefined

  constructor() { }

  ngOnInit(): void {
  }

}
