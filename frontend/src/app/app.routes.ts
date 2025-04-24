import { Routes } from '@angular/router';

import { ManagementComponent } from './management/management.component';
import {QuizComponent} from './quiz/quiz.component'; // Import the new component
import { InputComponent } from './input/input.component';
import {PasswordGuard} from './password.guard';


export const routes: Routes = [
  { path: '', redirectTo: 'input', pathMatch: 'full' }, // Redirect root to /chat
  {
    path: 'quiz',
    component: QuizComponent,
    canActivate: [PasswordGuard]
  },
  {
    path: 'management',
    component: ManagementComponent,
    canActivate: [PasswordGuard]
  },
  { path: 'input', component: InputComponent },
  { path: '**', redirectTo: 'input' }, // Redirect root to /chat
];
