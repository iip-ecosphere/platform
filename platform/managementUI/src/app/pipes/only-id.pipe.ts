import { Injectable, Pipe, PipeTransform } from '@angular/core';

@Pipe({
    name: 'onlyId',
    standalone: false
})
@Injectable({providedIn: 'root'})

export class OnlyIdPipe implements PipeTransform {

  transform(value: any): string {
    if (typeof(value) === 'string') {

      let i = value.indexOf(':') + 2;
      let j = value.indexOf('}') -1;

      if(i > -1 && j > -1) {
        value = value.substring(i, j);
      }

    }
    return value;

  }
}
