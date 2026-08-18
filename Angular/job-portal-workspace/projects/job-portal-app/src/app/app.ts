import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { RxJSCommonOperartors, RxJSSubjectTypes } from 'shared-ui';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  // private rxjs= inject(RxJSCommonOperartors);
    private rxjsSubject= inject(RxJSSubjectTypes);

}
