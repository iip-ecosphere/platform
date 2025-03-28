import { Component, Input, OnInit } from '@angular/core';
import { Resource, editorInput, metaTypes, MT_varValue } from 'src/interfaces';

@Component({
    selector: 'app-enum-dropdown',
    templateUrl: './enum-dropdown.component.html',
    styleUrls: ['./enum-dropdown.component.scss'],
    standalone: false
})
export class EnumDropdownComponent implements OnInit {

  @Input() input: editorInput | undefined
  @Input() meta: Resource | undefined

  enum: string[] = [];
  selected: string = "";

  constructor() { }

  ngOnInit(): void {
    if (this.meta && this.input) {
      if (this.meta.value) {
        this.selected = String(this.input.value); // input.value is of type any, not matched by mat-select
        let enumMeta = this.meta.value.find(a => a.idShort === this.input?.type);
        if (enumMeta) {
          for (let element of enumMeta.value) {
            if (!metaTypes.includes(element.idShort)) {
              let value = element.value.find((a: { idShort: string; }) => a.idShort === MT_varValue);
              if (value) {
                //this.enum.push(ivmlEnumeration + this.input.type + '.' + value.value);
                this.enum.push(value.value);
              }
            }
          }
        }
      }
    }
  }

}
