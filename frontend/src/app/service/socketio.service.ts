import { Injectable } from '@angular/core';
import { io, Socket } from 'socket.io-client';
import { Observable } from 'rxjs';


@Injectable({
  providedIn: 'root'
})
export class SocketioService {
  private socket: Socket;
  private currentQuestionId: string | null = null; // Store the current question ID

  constructor() {
    let sessionId = localStorage.getItem('socketio_session_id');
    if (!sessionId) {
      sessionId = this.generateSessionId();
      localStorage.setItem('socketio_session_id', sessionId);
    }

    let backendUrl =  window.location.origin; // Use environment or current host
    console.log("Socker URL")
    console.log(backendUrl)
    console.log(window.location.origin)
   // backendUrl = "localhost:9090"
    this.socket = io(`${backendUrl}`, { // Connect to the correct backend URL
      query: { sessionId }, // Send sessionId as a query parameter
      transports: ['websocket', 'polling'], // Ensure both WebSocket and polling are supported
      reconnection: true, // Enable automatic reconnection
      reconnectionAttempts: 5, // Limit the number of reconnection attempts
      reconnectionDelay: 1000 // Delay between reconnection attempts
    });

    this.onMessage('currentQuestionId').subscribe((questionId: string) => {
      this.currentQuestionId = questionId; // Update the current question ID when received
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

  joinRoom(roomName: string): void {
    console.log(`Joining room: ${roomName}`); // Debug log
    this.socket.emit('joinRoom', roomName); // Emit the joinRoom event
  }

  onMessage(event: string): Observable<any> {
    return new Observable(observer => {
      this.socket.on(event, (data: any) => {
        observer.next(data);
      });
    });
  }

  getCurrentQuestionId(): string | null {
    return this.currentQuestionId; // Return the stored question ID
  }

  fetchCurrentQuestionId(): void {
    this.sendMessage('getCurrentQuestionId', null, (questionId: string) => {
      this.currentQuestionId = questionId; // Update the current question ID
    });
  }
}
