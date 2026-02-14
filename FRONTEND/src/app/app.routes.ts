import { Routes } from '@angular/router';
import { authGuard } from './core/auth/auth.guard';
import { AccessDeniedPageComponent } from './features/errors/access-denied-page.component';

export const routes: Routes = [
  {
    path: 'login',
    loadChildren: () =>
      import('./features/authentication/auth.routes').then((m) => m.AUTHENTICATION_ROUTES)
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadChildren: () =>
      import('./features/dashboard/dashboard.routes').then((m) => m.DASHBOARD_ROUTES)
  },
  {
    path: 'access-denied',
    component: AccessDeniedPageComponent
  },
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'dashboard'
  },
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];
