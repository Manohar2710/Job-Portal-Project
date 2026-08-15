import { Component, inject, Optional, Self } from '@angular/core';
import { LoginCard } from 'auth-feature';
import { APP_CONFIG, PLUGINS } from '../../app.config';
import { HttpClient } from '@angular/common/http';
@Component({
  selector: 'app-login-page',
  templateUrl: './login-page.html',
  styleUrl: './login-page.scss',
  imports: [LoginCard]
})
export class LoginPage {

  //  Consuming the Injected Token interface
  private apiConfig = inject(APP_CONFIG)

  // consuming the muplti : true plugin
  private plugins = inject(PLUGINS);

  // inject equavalent for @Optional
  private http = inject(HttpClient, {optional: true});

  // @Optional Example which will set to null when there is no provider available
  // Legacy Syntax
  constructor(@Optional() http: HttpClient


    // @Self which will inject token from its own component , ignores parent 
    // Legacy syntax
    // @Self() localState: StateService
  ) {


    this.plugins.forEach(plugin => plugin.init());
  }

  // @Self with inject function

  // private localState = inject(StateService);
}
