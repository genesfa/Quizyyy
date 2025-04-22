import {AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {Subscription} from 'rxjs';
import {SocketioService} from '../service/socketio.service';
import {FormsModule} from '@angular/forms';
import {NgForOf} from '@angular/common';

@Component({
  selector: 'app-chat',
  imports: [
    FormsModule,
    NgForOf
  ],
  templateUrl: './chat.component.html',
  styleUrl: './chat.component.css'
})
export class ChatComponent implements OnInit, OnDestroy {
  newMessage: string = '';
  messages: string[] = [];
  serverMessageSubscription: Subscription | undefined;
  confettiSubscription: Subscription | undefined;
  constructor(private socketService: SocketioService) {}



  ngOnInit(): void {
    this.socketService.connect();
    this.serverMessageSubscription = this.socketService.onMessage('serverToClientEvent').subscribe((data: string) => {
      console.log('RECIVED FORM SERVER')
      console.log(data)
      this.messages.push(data);
      console.log('Updated messages array:', this.messages);
    });

    // Subscribe to the 'triggerConffeti' event
    this.confettiSubscription = this.socketService.onMessage('triggerConfetti').subscribe(() => {
      console.log('triggerConfetti');

    });
  }



  sendMessage(): void {
    if (this.newMessage) {
      this.socketService.sendMessage('clientToServerEvent', this.newMessage);
      this.newMessage = '';
    }
  }

  ngOnDestroy(): void {
    if (this.serverMessageSubscription) {
      this.serverMessageSubscription.unsubscribe();
    }
    if (this.confettiSubscription) {
      this.confettiSubscription.unsubscribe();
    }
  }

  randomInRange(min: number, max: number) {
    return Math.random() * (max - min) + min;
  }



}
