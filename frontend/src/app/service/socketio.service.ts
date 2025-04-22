import { Injectable } from '@angular/core';
import { io, Socket } from 'socket.io-client';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SocketioService {
  private socket: Socket;

  constructor() {
    this.socket = io('http://localhost:9092'); // Replace with your backend SocketIO server URL
  }

  connect(): void {
    this.socket.on('connect', () => {
      console.log('Connected to SocketIO server');
    });

    this.socket.on('disconnect', () => {
      console.log('Disconnected from SocketIO server');
    });
  }

  sendMessage(event: string, data: any): void {
    this.socket.emit(event, data);
  }

  triggerConfetti(event: string): void {
    this.socket.emit(event);
  }

  onMessage(event: string): Observable<any> {
    return new Observable(observer => {
      this.socket.on(event, (data: any) => {
        observer.next(data);
      });
    });
  }
}
