import { ApplicationConfig, importProvidersFrom, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideApi } from '../../../../libs/job-portal-api';
import { environment } from '../environments/environment';
import { authInterceptor } from 'auth-feature';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(), 
    provideRouter(routes),
    provideHttpClient(
      withInterceptors([authInterceptor]) 
    ),
    provideApi(environment.apiUrl)
  ],
};
