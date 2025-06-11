import { Component } from '@angular/core';
import { TeamService } from '../service/team.service';
import {FormsModule} from '@angular/forms';
import {NgIf} from '@angular/common';
import { SocketioService } from '../service/socketio.service';
import { Team } from '../models/team.model';
import {MatButton} from '@angular/material/button';

@Component({
  selector: 'app-input',
  imports: [
    FormsModule,
    NgIf,
    MatButton
  ],
  templateUrl: './input.component.html',
  styleUrl: './input.component.css'
})
export class InputComponent {
  teamName: string = '';
  team: Team| null = null;
  teamExists: boolean = false; // Add a flag to track if the teamExists message was received
  isSubmitting: boolean = false; // Add a flag to track submission state
  isTeamCheckComplete: boolean = false; // Add a flag to track if the team check is complete
  answer: string = ''; // Add a property to hold the answer
  isSubmittingAnswer: boolean = false; // Add a flag to track answer submission state
  errorMessage: string = ''; // Add a property to hold error messages

  constructor(private teamService: TeamService, private readonly socketService: SocketioService) {
    this.socketService.onMessage('teamExists').subscribe((team: Team) => {
      console.log(team); // Log the message for debugging
      this.team = team;
      this.teamExists = true; // Set the flag to true when the event is received
      this.isTeamCheckComplete = true; // Mark the team check as complete
    });

    this.socketService.onMessage('teamNotFound').subscribe(() => {
      this.teamExists = false; // Ensure the teamExists flag is false

      this.isTeamCheckComplete = true; // Mark the team check as complete even when no team is found
    });

    this.socketService.onMessage('updateTeams').subscribe((teams: Team[]) => {
      if (this.team) {
        const updatedTeam = teams.find(t => t.id === this.team?.id);
        if (updatedTeam) {
          this.team.score = updatedTeam.score; // Update the score of the current team
        }
      }
    });
  }

  submitTeamName() {
    if (this.isSubmitting) return; // Prevent multiple submissions
    this.isSubmitting = true; // Set the flag to true when submitting
    this.isTeamCheckComplete = false; // Reset the flag when submitting
    this.socketService.sendMessage('createTeam', this.teamName, (newTeam: Team) => {
      console.log("WTF");
      console.log(newTeam); // Log the acknowledgment response for debugging
      this.team = newTeam; // Set the newly created team
      this.teamExists = true; // Set the flag to true
      this.isSubmitting = false; // Reset the flag after the flow completes
      this.isTeamCheckComplete = true; // Mark the team check as complete after creation
    });
  }

  submitAnswer() {
    if (this.isSubmittingAnswer) return; // Prevent multiple submissions
    this.isSubmittingAnswer = true; // Set the flag to true when submitting

    const teamId = this.team?.id;
    const answerText = this.answer;

    if (!teamId || !answerText) {
        console.error('Missing required data for answer submission:', { teamId, answerText });
        this.isSubmittingAnswer = false; // Reset the flag
        this.errorMessage = "You should type something"
        return;
    }

    this.socketService.sendMessage('submitAnswer', {
        teamId: teamId,
        answerText: answerText
    }, (response: string) => {
        if (response.startsWith("Error:")) {
            this.errorMessage = response.replace("Error:", ""); // Display the error message
            console.error(response); // Log the error for debugging
        } else {
            console.log('Answer submitted successfully'); // Log for debugging
            this.answer = ''; // Clear the answer field after submission
            this.errorMessage = ''; // Clear any previous error messages
        }
        this.isSubmittingAnswer = false; // Reset the flag after submission
    });
  }
}
