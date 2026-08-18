import { Routes } from '@angular/router';
import { HomeComponent } from './pages/home/home';
import { authGuardLearning } from 'auth-feature';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },

  // Angular Routes Example
  // Eager Loading 
  {
    path: 'dashboard',
    component: HomeComponent
  },

  // Lazy Loading

  {
    path: 'dashboard',
    loadComponent: () => import('./pages/home/home').then((m) => m.HomeComponent)
  },


  // loadChildren Example to load entire home routes
  {
    path: 'home',
    loadChildren: () =>
      import('./pages/home/home.routes').then((m) => m.homeRoutes),
    canActivate: [authGuardLearning]
  },


  {
    path: 'login',
    loadComponent: () =>
      import('./pages/login-with-template-form/login-with-template-form').then((m) => m.LoginWithTemplateForm),
  },
  {
    path: 'register',
    loadComponent: () => 
      import('./pages/register-screen/register-screen').then((m) => m.RegisterScreen)
  },

  // Wild card Example
  {
    path: '**', redirectTo: 'login'
  },

  // redirectTo example with patchMatch prefix
  {
    path: 'old-home', redirectTo: 'home', pathMatch: 'prefix'
  },

    // redirectTo example with patchMatch full
  // {
  //   path: '', redirectTo: 'home', pathMatch: 'full'
  // }
];
