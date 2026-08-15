import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root', // 'root' : singleton ijection accross application
  // 'platform' :  shared across multiple angular application with same page(micro frontend).
  // 'any': unique instance for every lazy loaded module, single instance for eager loaded module 
})
export class Theme {
  color: string = "Light mode";
}
