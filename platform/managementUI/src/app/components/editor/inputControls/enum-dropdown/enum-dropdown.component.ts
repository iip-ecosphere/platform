import { Component, Input, OnInit } from '@angular/core';
import { Resource, editorInput } from 'src/interfaces';
import { ivmlEnumeration } from 'src/app/services/env-config.service';

@Component({
  selector: 'app-enum-dropdown',
  templateUrl: './enum-dropdown.component.html',
  styleUrls: ['./enum-dropdown.component.scss']
})
export class EnumDropdownComponent implements OnInit {

  @Input() input: editorInput | undefined
  @Input() meta: Resource | undefined

  enum: string[] = []

  metaTypes = ['metaState', 'metaProject',
  'metaSize', 'metaType', 'metaRefines', 'metaAbstract', 'metaTypeKind'];

  constructor() { }

  ngOnInit(): void {
    if(this.meta && this.input) {
      if(this.meta.value) {
        let enumMeta = this.meta.value.find(a => a.idShort === this.input?.type);
        if(enumMeta) {
          for(let element of enumMeta.value) {
            if(!this.metaTypes.includes(element.idShort)) {
              let value = element.value.find((a: { idShort: string; }) => a.idShort === 'varValue');
              if(value) {
                this.enum.push(ivmlEnumeration + this.input.type + '.' + value.value);
              }
            }
          }
        }

      }

    }


  }

}