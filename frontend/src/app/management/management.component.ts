import {Component, OnInit} from '@angular/core';
import {SocketioService} from "../service/socketio.service";

@Component({
  selector: 'app-management',
  imports: [],
  templateUrl: './management.component.html',
  styleUrl: './management.component.css'
})
export class ManagementComponent implements OnInit{
  constructor(private socketService: SocketioService) {}
  ngOnInit() {
    this.socketService.connect();
  }


  triggerConfetti() {
    this.socketService.triggerConfetti('triggerConfetti');
  }


}
