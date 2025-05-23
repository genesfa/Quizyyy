import { Injectable } from '@angular/core';
import { io, Socket } from 'socket.io-client';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class SocketioService {
  private socket: Socket;

  constructor() {
    let sessionId = localStorage.getItem('socketio_session_id');
    if (!sessionId) {
      sessionId = this.generateSessionId();
      localStorage.setItem('socketio_session_id', sessionId);
    }

    this.socket = io('http://localhost:9092', {
      query: { sessionId } // Send sessionId as a query parameter
    });
  }

  private generateSessionId(): string {
    return crypto.randomUUID(); // Generate a UUID
  }

  connect(): void {
    this.socket.on('connect', () => {
      console.log('Connected to SocketIO server');
    });

    this.socket.on('disconnect', () => {
      console.log('Disconnected from SocketIO server');
    });
  }

  sendMessage(event: string, data: any, ackCallback?: (response: any) => void): void {
    if (ackCallback) {
      this.socket.emit(event, data, ackCallback); // Emit with acknowledgment callback
    } else {
      this.socket.emit(event, data); // Emit without acknowledgment
    }
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
