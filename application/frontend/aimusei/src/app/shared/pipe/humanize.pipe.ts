import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'humanize'
})
export class HumanizePipe implements PipeTransform {
  transform(value: any): string {
    if (!value) return value;

    // Inserisce uno spazio prima di ogni lettera maiuscola (tranne la prima)
    const spaced = value.replace(/([A-Z])/g, ' $1');

    // Capitalizza la prima lettera di ogni parola
    return spaced.replace(/\b\w/g, (l: string) => l.toUpperCase());
  }
}
