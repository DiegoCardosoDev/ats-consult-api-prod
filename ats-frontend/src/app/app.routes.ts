import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { LoginComponent } from './pages/login/login.component';
import { JobPositionListComponent } from './features/job-position/components/job-position-list/job-position-list.component';
import { JobPositionFormComponent } from './features/job-position/components/job-position-form/job-position-form.component';
import { HomeComponent } from './features/home/home.component';
import { JobPositionDetailComponent } from './features/job-position/job-detail/job-position-detail.component';
import { CandidateListComponent } from './features/candidate/components/candidate-list/candidate-list.component';
import { ClientListComponent } from './features/client/components/client-list/client-list.component';
import { ProcessListComponent } from './features/selection-process/components/process-list/process-list.component';
import { ProcessDetailComponent } from './features/selection-process/components/process-detail/process-detail.component';
import { PrivacyPolicyComponent } from './features/privacy/privacy-policy.component';
import { RegisterComponent } from './pages/register/register.component';
import { UserManagementComponent } from './features/user/components/user-management/user-management.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'privacidade', component: PrivacyPolicyComponent },
  
  // Rotas protegidas pelo Guard
  { path: 'home', component: HomeComponent, canActivate: [authGuard] },
  { path: 'jobs', component: JobPositionListComponent, canActivate: [authGuard] },
  {
    path: 'jobs/new',
    component: JobPositionFormComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN', 'TECH_RECRUITER'] }
  },
  { path: 'jobs/:id', component: JobPositionDetailComponent, canActivate: [authGuard] }, // 👈 Adicione esta linha para os detalhes
  {
    path: 'jobs/edit/:id',
    component: JobPositionFormComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN', 'TECH_RECRUITER'] }
  },
  {
    path: 'candidates',
    component: CandidateListComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN', 'TECH_RECRUITER'] }
  },
  {
    path: 'clients',
    component: ClientListComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN', 'TECH_RECRUITER'] }
  },
  {
    path: 'processes',
    component: ProcessListComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN', 'TECH_RECRUITER'] }
  },
  {
    path: 'processes/:id',
    component: ProcessDetailComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN', 'TECH_RECRUITER'] }
  },
  {
    path: 'users',
    component: UserManagementComponent,
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN', 'TECH_RECRUITER'] }
  },

  // Redirecionamentos padrão
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' }
];