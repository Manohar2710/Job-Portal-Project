import { ApplicationConfig, importProvidersFrom, inject, InjectionToken, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { provideApi } from '../../../../libs/job-portal-api';
import { environment } from '../environments/environment';
import { authInterceptor } from 'auth-feature';
import { Theme } from 'shared-ui';


export interface AppConfig {
  apiUrl: string;
  maxRetries: number;
}

// interface for multi : true plugin example
export interface Plugin {
  name: string,
  init(): void
}

export const PLUGINS = new InjectionToken<Plugin[]>('PLUGINS')


// Non class data injection using InjectionToken
export const APP_CONFIG = new InjectionToken<AppConfig>('APP_CONFIG', {
  providedIn: 'root', // tree shakable
  factory: () => ({
    apiUrl: '',
    maxRetries: 1,
  }),
});


export const THEME_CONFIG = new InjectionToken<string>('THEME_CONFIG');

// Injecting using the useValue paramenter in the provider example
export const API_URL = new InjectionToken<string>('API_URL')

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideAnimationsAsync(),

    // configuring http interceptor
    provideHttpClient(
      withInterceptors([authInterceptor])
    ),
    
    provideApi(environment.apiUrl),
    // Injecting using the useValue paramenter in the provider example
    {
      provide: API_URL,
      useValue: 'htt://api.example.com'
    },

    // useFactory example with dependencies 
    // Legacy Example
    // {
    //   provide: DataService,
    //   useFactory: (http: HttpClient, config: AppConfig) => {
    //     config.useMock ? new MockDataservice : new RealDataService
    //   },
    //   deps: [HttpClient, APP_CONFIG]
    // } ,

    // Angular 14+ example with dependencies injected with inject 
    // {
    //   provide: DataService,
    //   useFactory: new RealDataService(inject(HttpClient))
    // },


    // useExisting example
    // used when we want to create a new instance of a existing provided instance
    // {
    //   provide : NewLogger, useExisting: OldLogger
    // },

    // Multi : true example which will inject a list of plugin

    {
      provide: PLUGINS, useValue: {name: 'Analytics', init: () =>{} }, multi: true

    },
    {
      provide: PLUGINS, useValue: { name: 'logger', init: () => {}}, multi: true
    },


    {
        provide: THEME_CONFIG,
        useFactory: () => inject(Theme)

    },
    // {
    //   provide: LoggerService,
    //   useClass: ConsoleLogger
    // }

  ],

};
