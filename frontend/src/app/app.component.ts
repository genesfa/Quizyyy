import {Component, OnInit, OnDestroy, ChangeDetectionStrategy} from '@angular/core';

import { Subscription } from 'rxjs';
import {FormsModule} from '@angular/forms';
import {SocketioService} from './service/socketio.service';
import {AsyncPipe, CommonModule, NgForOf} from '@angular/common';
import {RouterLink, RouterOutlet} from '@angular/router';
import {BackgroundComponent} from './background/background.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, CommonModule, BackgroundComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent  {

}
