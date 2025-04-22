import { Routes } from '@angular/router';

import { ManagementComponent } from './management/management.component';
import {ChatComponent} from './chat/chat.component';
import {QuizComponent} from './quiz/quiz.component'; // Import the new component

export const routes: Routes = [
  { path: '', redirectTo: 'quiz', pathMatch: 'full' }, // Redirect root to /chat

  { path: 'chat', component: ChatComponent }, // The new /chat route
  { path: 'quiz', component: QuizComponent }, // The new /chat route
  { path: 'management', component: ManagementComponent },
  { path: '**', redirectTo: 'quiz' }, // Redirect root to /chat
];
