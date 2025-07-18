import {Component, OnDestroy, OnInit} from '@angular/core';
import {Subscription} from 'rxjs';
import {SocketioService} from '../service/socketio.service';

import {CommonModule} from '@angular/common';
import {NgxParticlesModule} from '@tsparticles/angular';
import {confetti} from '@tsparticles/confetti';


@Component({
  selector: 'app-quiz',
  imports: [CommonModule, NgxParticlesModule],
  templateUrl: './quiz.component.html',
  styleUrl: './quiz.component.css'
})
export class QuizComponent implements OnInit, OnDestroy {

  confettiSubscription: Subscription | undefined;
  topTeams: { name: string; score: number }[] = [];
  topTeamsSubscription: Subscription | undefined;
  questions: { id: number; clues: string[]; solution: string }[] = [];
  currentQuestion: { id: number; clues: string[]; solution: string } | null = null;
  showCluesAndSolution: boolean = false;
  showSolution: boolean = false; // New flag for solution visibility
  showQRCode: boolean = false; // Flag to control QR code visibility
  window = window; // Expose the global window object

  constructor(private socketService: SocketioService) {}

  ngOnInit(): void {
    this.socketService.connect();
    console.log('Socket connected'); // Debug log

    this.socketService.joinRoom('management');
    console.log('Joined management room'); // Debug log

    this.socketService.onMessage('managementWelcome').subscribe((message: string) => {
      console.log('Received managementWelcome event:', message); // Debug log
    });

    this.confettiSubscription = this.socketService.onMessage('triggerConfetti').subscribe(() => {
      console.log('Received triggerConfetti event'); // Debug log
      this.shotConfetti(true);
    });

    this.topTeamsSubscription = this.socketService.onMessage('updateTeams').subscribe((teams: any[]) => {
      console.log('Received updateTeams event', teams); // Debug log
      this.topTeams = teams
        .sort((a, b) => b.score - a.score)
        .slice(0, 5);
    });

    this.socketService.onMessage('updateQuestions').subscribe((data: any) => {
      console.log('Received updateQuestions event', data); // Debug log
      this.currentQuestion = { id: data.id, clues: [], solution: "" }; // Reset clues and solution
      this.showCluesAndSolution = false; // Reset clues visibility
      this.showSolution = false; // Reset solution visibility
    });

    // Subscribe to the 'showSolution' event to reveal the solution
    this.socketService.onMessage('showSolution').subscribe((data:any) => {
      if (this.currentQuestion) {
        this.currentQuestion.solution = data;
        this.showSolution = true; // Reveal the solution
      }
    });

    // Subscribe to the 'showClue' event to reveal a specific clue
    this.socketService.onMessage('showClue').subscribe((data: any) => {
      if (this.currentQuestion && data.clueNumber >= 1 && data.clueNumber <= 4) {
        this.currentQuestion.clues[data.clueNumber - 1] = data.clue; // Update the specific clue
        this.showCluesAndSolution = true; // Ensure clues are visible
      }
    });

    // Subscribe to the 'toggleQRCode' event to toggle QR code visibility
    this.socketService.onMessage('toggleQRCode').subscribe(() => {
      this.showQRCode = !this.showQRCode; // Toggle QR code visibility
    });
  }

  generateRandomColors(x: number): string[] {
    const colors: string[] = [];

    for (let i = 0; i < x; i++) {
      const color = '#' + Math.floor(Math.random() * 0xffffff)
        .toString(16)
        .padStart(6, '0');
      colors.push(color);
    }

    return colors;
  }

  shotConfetti(visible: boolean): void {
    var count = 0;
    if (visible) {
      count = 50;
    }
    (async () => {
      await confetti("tsparticles", {
        angle: 90,
        count: count,
        position: {
          x: 50,
          y: 50,
        },
        spread: 45,
        startVelocity: 45,
        decay: 0.9,
        gravity: 1,
        drift: 0,
        ticks: 200,
        colors: this.generateRandomColors(5),
        shapes: ["square", "circle"],
        scalar: 1,
        zIndex: 100,
        disableForReducedMotion: true,
      });
    })();
  }

  ngOnDestroy(): void {
    if (this.confettiSubscription) {
      this.confettiSubscription.unsubscribe();
    }
    if (this.topTeamsSubscription) {
      this.topTeamsSubscription.unsubscribe();
    }
  }
}
