import { Component, OnInit } from '@angular/core';
import { SocketioService } from "../service/socketio.service";
import { Team } from '../models/team.model';
import {MatList, MatListItem} from '@angular/material/list';
import {MatButton} from '@angular/material/button';
import {NgForOf} from '@angular/common';

@Component({
  selector: 'app-management',
  templateUrl: './management.component.html',
  imports: [
    MatListItem,
    MatList,
    MatButton,
    NgForOf
  ],
  styleUrl: './management.component.css'
})
export class ManagementComponent implements OnInit {
  teams: Team[] = []; // List of teams
  currentQuestionTitle: string = ''; // Store the current question title

  constructor(private socketService: SocketioService) {}

  ngOnInit() {
    this.socketService.connect();

    // Listen for updates to the team list
    this.socketService.onMessage('updateTeams').subscribe((teams: Team[]) => {
      this.teams = teams;
      console.log("WTFFF")
      console.log(this.teams)
    });

    // Request the initial list of teams and handle the response
    this.socketService.sendMessage('getTeams', null, (response: Team[]) => {
      this.teams = response; // Write the response into this.teams
      console.log("Initial team list:", this.teams);
    });

    this.socketService.onMessage('updateQuestions').subscribe((data: any) => {
      console.log(data)
      this.currentQuestionTitle = data.name; // Update the current question title
    });
  }

  addPoints(team: Team, points: number) {
    const newScore = team.score + points;
    if (newScore >= 0) { // Ensure the score doesn't go below 0
      team.score = newScore;
      this.socketService.sendMessage('updateTeamScore', { id: team.id, score: team.score });
    }
  }

  triggerConfetti() {
    this.socketService.triggerConfetti('triggerConfetti');
  }

  lastQuestion() {
    console.log('Last Question button clicked');
    this.socketService.sendMessage('lastQuestion',{});
  }

  nextClue() {
    console.log('Next Clue button clicked');
    this.socketService.sendMessage('nextClue',{});
  }

  nextQuestion() {
    console.log('Next Question button clicked');
    this.socketService.sendMessage('nextQuestion', {});
  }

  closeQuestion() {
    console.log('Close Question button clicked');
    this.socketService.sendMessage('closeQuestion', {});
  }

  showSolution() {
    console.log('Show Solution button clicked');
    this.socketService.sendMessage('showSolution', {});
  }

  showClue(clueNumber: number) {
    console.log(`Show Clue ${clueNumber} button clicked`);
    this.socketService.sendMessage('showClue', clueNumber ); // Send an object with clueNumber
  }
}
