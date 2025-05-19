package com.quiz.com.quiz;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.quiz.com.quiz.dto.ClueData;
import com.quiz.com.quiz.entitys.Question;
import com.quiz.com.quiz.entitys.QuestionType;
import com.quiz.com.quiz.entitys.Team;
import com.quiz.com.quiz.repositorys.QuestionRepository;
import com.quiz.com.quiz.repositorys.TeamRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SocketIOService {

    private final SocketIOServer server;

    private final QuestionRepository questionRepository;

    private final TeamRepository teamRepository;

    private final AtomicInteger currentQuestionIndex = new AtomicInteger(0); // Track the current question index
    private List<Question> questions; // Cache the list of questions

    @PostConstruct
    public void startServer() {
        server.start();
        System.out.println("SocketIO server started on port " + server.getConfiguration().getPort());

        // Load all questions from the database
        questions = questionRepository.findAll();

        server.addConnectListener(client -> {
            HandshakeData handshakeData = client.getHandshakeData();
            String sessionId = handshakeData.getSingleUrlParam("sessionId"); // Extract from query parameters

            if (sessionId == null || sessionId.isEmpty()) {
                System.out.println("Session ID is missing for client: " + client.getSessionId());
                sessionId = client.getSessionId().toString(); // Fallback to the Socket.IO session ID
            }

            System.out.println("Client connected: " + client.getSessionId() + ", Session ID: " + sessionId);

            // Check if a team exists for the sessionId
            Team existingTeam = teamRepository.findBySessionId(sessionId);
            if (existingTeam != null) {
                System.out.println("Team found for sessionId: " + sessionId + ", Team Name: " + existingTeam.getName());
                client.sendEvent("teamExists", existingTeam);
            } else {
                System.out.println("No team found for sessionId: " + sessionId);
                client.sendEvent("teamNotFound"); // Emit teamNotFound event when no team is found
            }

            // Optionally, associate the session ID with the client or perform other logic
            client.set("sessionId", sessionId);

            client.sendEvent("message", "Welcome to the SocketIO server!");
        });

        server.addDisconnectListener(client -> {
            System.out.println("Client disconnected: " + client.getSessionId());
        });

        server.addEventListener("clientToServerEvent", String.class, (client, data, ackSender) -> {
            System.out.println("Received message from " + client.getSessionId() + ": " + data);
            server.getBroadcastOperations().sendEvent("serverToClientEvent", "Server received: " + data);
            ackSender.sendAckData("Message received by server");
        });

        server.addEventListener("createTeam", String.class, (client, teamName, ackSender) -> {
            System.out.println("Received message from " + client.getSessionId() + ": " + teamName);
            HandshakeData handshakeData = client.getHandshakeData();
            String sessionId = handshakeData.getSingleUrlParam("sessionId");
            Team newTeam = new Team();
            newTeam.setName(teamName);
            newTeam.setSessionId(sessionId);
            teamRepository.save(newTeam);
            // Broadcast the updated team list to all clients
            server.getBroadcastOperations().sendEvent("updateTeams", teamRepository.findAll());
            // Send the newly created team as the acknowledgment response
            ackSender.sendAckData(newTeam);
        });

        server.addEventListener("triggerConfetti", String.class, (client, data, ackSender) -> {
            System.out.println("triggerConfetti");
            server.getBroadcastOperations().sendEvent("triggerConfetti");
            ackSender.sendAckData("Message received by server");
            questionRepository.findAll().forEach(question -> {System.out.println(question.getName());});
        });

        server.addEventListener("getTeams", Void.class, (client, data, ackSender) -> {
            System.out.println("Client requested team list");
            ackSender.sendAckData(teamRepository.findAll());
        });

        server.addEventListener("updateTeamScore", Team.class, (client, updatedTeam, ackSender) -> {
            System.out.println("Updating score for team: " + updatedTeam.getName());
            Team team = teamRepository.findById(updatedTeam.getId()).orElse(null);
            if (team != null) {
                team.setScore(updatedTeam.getScore());
                teamRepository.save(team);
                System.out.println("Score updated for team: " + team.getName() + " to " + team.getScore());
                // Broadcast the updated team list to all clients
                server.getBroadcastOperations().sendEvent("updateTeams", teamRepository.findAll());
                ackSender.sendAckData("Score updated successfully");
            } else {
                System.out.println("Team not found with ID: " + updatedTeam.getId());
                ackSender.sendAckData("Team not found");
            }
        });

        server.addEventListener("getCurrentQuestion", Void.class, (client, data, ackRequest) -> {
            String currentQuestion = getCurrentQuestion();
            client.sendEvent("currentQuestion", currentQuestion);
        });

        server.addEventListener("nextQuestion", Void.class, (client, data, ackRequest) -> {
            if (currentQuestionIndex.incrementAndGet() >= questions.size()) {
                currentQuestionIndex.set(questions.size() - 1); // Prevent going out of bounds
            }
            broadcastQuestionUpdate();
        });

        server.addEventListener("lastQuestion", Void.class, (client, data, ackRequest) -> {
            if (currentQuestionIndex.decrementAndGet() < 0) {
                currentQuestionIndex.set(0); // Prevent going below 0
            }
            broadcastQuestionUpdate();
        });

        server.addEventListener("showSolution", Void.class, (client, data, ackRequest) -> {
            System.out.println("Show Solution event triggered");
            server.getBroadcastOperations().sendEvent("showSolution", questions.get(currentQuestionIndex.get()).getSolution());
        });

        server.addEventListener("showClue", Integer.class, (client, data, ackRequest) -> {
            int clueNumber = data;
            Question currentQuestion = questions.get(currentQuestionIndex.get());
            String clue = null;

            switch (clueNumber) {
                case 1:
                    clue = currentQuestion.getClue1();
                    break;
                case 2:
                    clue = currentQuestion.getClue2();
                    break;
                case 3:
                    clue = currentQuestion.getClue3();
                    break;
                case 4:
                    clue = currentQuestion.getClue4();
                    break;
            }

            if (clue != null) {
                server.getBroadcastOperations().sendEvent("showClue", new ClueData(clueNumber, clue));
            }
        });
    }

    private void broadcastQuestionUpdate() {
        Question currentQuestion = questions.get(currentQuestionIndex.get());
        server.getBroadcastOperations().sendEvent("updateQuestions", currentQuestion);
    }

    private String getCurrentQuestion() {
        if (questions.isEmpty()) {
            return "No questions available";
        }
        return questions.get(currentQuestionIndex.get()).getName(); // Return the name of the current question
    }

}