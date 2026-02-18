import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'formatValue'
})
export class FormatValuePipe implements PipeTransform {
  transform(value: any): string {
    if (typeof value === 'boolean') {
      return value ? 'SI' : 'NO';
    }
    // Per altri tipi, restituisci come stringa
    return value?.toString() || '';
  }
}