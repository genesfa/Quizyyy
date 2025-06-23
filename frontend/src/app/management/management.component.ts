import { Component, OnInit } from '@angular/core';
import { SocketioService } from "../service/socketio.service";
import { Team } from '../models/team.model';
import {MatList, MatListItem} from '@angular/material/list';
import {MatButton} from '@angular/material/button';
import {NgForOf, NgIf} from '@angular/common';
import {Subscription} from 'rxjs';
import jsPDF from 'jspdf';

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
  answers: { [teamId: string]: { answerText: string, clueNumber: number } } = {}; // Store answers with clueNumber for the current question

  constructor(private socketService: SocketioService) {}

  ngOnInit() {
    this.socketService.connect();

    // Join the management room
    this.socketService.joinRoom('management'); // Join a dedicated room for management

    // Listen for updates to the team list
    this.socketService.onMessage('updateTeams').subscribe((teams: Team[]) => {
      this.teams = teams;
    });

    // Request the initial list of teams and handle the response
    this.socketService.sendMessage('getTeams', null, (response: Team[]) => {
      this.teams = response; // Write the response into this.teams
      console.log("Initial team list:", this.teams);
    });

    this.socketService.onMessage('updateQuestions').subscribe((data: any) => {
      console.log(data)
      this.currentQuestionTitle = data.name; // Update the current question title
      this.fetchAnswersForCurrentQuestion(); // Fetch answers for the current question
    });

    // Listen for updates to answers for the current question
    this.socketService.onMessage('updateAnswersForCurrentQuestion').subscribe((updatedAnswers: { [teamId: string]: { answerText: string, clueNumber: number } }) => {
      this.answers = updatedAnswers; // Update the answers map
      console.log("Updated answers for current question:", this.answers);
    });


  }

  fetchAnswersForCurrentQuestion() {
    this.socketService.sendMessage('getAnswersForCurrentQuestion', null, (response: { [teamId: string]: { answerText: string, clueNumber: number } }) => {
      this.answers = response; // Update the answers map
      console.log("Answers for current question:", this.answers);
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

  toggleQRCode() {
    console.log('Toggle QR-Code button clicked');
    this.socketService.sendMessage('toggleQRCode', {});
  }

  generatePDF() {
    this.socketService.sendMessage('getAllAnswers', null, (response: { [teamId: string]: { answers: { question: string, answerText: string, clueNumber: number }[] } }) => {
      const doc = new jsPDF();
      doc.setFontSize(16);
      doc.text('Teams and All Answers Report', 10, 10);

      let y = 20;
      this.teams.forEach((team) => {
        doc.setFontSize(12);
        doc.text(`Team: ${team.name}`, 10, y);
        doc.text(`Score: ${team.score}`, 10, y + 5);

        const teamAnswers = response[team.id]?.answers || [];
        if (teamAnswers.length > 0) {
          teamAnswers.forEach((answer, index) => {
            doc.text(`Q${index + 1}: ${answer.question}`, 10, y + 10);
            doc.text(`Answer: ${answer.answerText} (Clue: ${answer.clueNumber})`, 10, y + 15);
            y += 10;
          });
        } else {
          doc.text('No answers submitted yet.', 10, y + 10);
          y += 10;
        }
        y += 10;
      });

      doc.save('teams_all_answers_report.pdf');
    });
  }
}
