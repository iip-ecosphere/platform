import { Injectable, Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'onlyId'
})
@Injectable({providedIn: 'root'})

export class OnlyIdPipe implements PipeTransform {

  transform(value: any): string {
    if (typeof(value) === 'string') {

      let i = value.indexOf(':') + 1;
      let j = value.indexOf('}') -1;

      value = value.substring(i, j);
    }
    return value;

  }
}
