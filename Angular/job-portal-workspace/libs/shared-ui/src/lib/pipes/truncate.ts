import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'uiTruncate',
})
export class Truncate implements PipeTransform {

  constructor() {}
  /**
   * Transforms a long string to short version
   * */
  transform(value: string, limit: number = 20, trail: string = '...') {
    if(!value) return '';
    if(value.length <=limit) {
      return value;
    }
    return value.substring(0, limit) + trail;
  }
}
