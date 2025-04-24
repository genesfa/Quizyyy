import {AfterViewInit, Component, ElementRef, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {Subscription} from 'rxjs';
import {SocketioService} from '../service/socketio.service';

import {CommonModule} from '@angular/common';
import {Container, Engine, MoveDirection, OutMode} from '@tsparticles/engine';
import { loadSlim } from "@tsparticles/slim";
import {NgParticlesService, NgxParticlesModule} from '@tsparticles/angular';
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

  constructor(private socketService: SocketioService) {}

  ngOnInit(): void {
    this.socketService.connect();

    // Subscribe to the 'triggerConffeti' event
    this.confettiSubscription = this.socketService.onMessage('triggerConfetti').subscribe(() => {
      console.log('triggerConfetti');
      this.shotConfetti(true)
    });
    this.shotConfetti(false);

    // Subscribe to the 'updateTeams' event to update top teams
    this.topTeamsSubscription = this.socketService.onMessage('updateTeams').subscribe((teams: any[]) => {
      this.topTeams = teams
        .sort((a, b) => b.score - a.score)
        .slice(0, 5);
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
