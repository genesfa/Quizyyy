package com.quiz.com.quiz;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.HandshakeData;
import com.quiz.com.quiz.entitys.Question;
import com.quiz.com.quiz.entitys.QuestionType;
import com.quiz.com.quiz.entitys.Team;
import com.quiz.com.quiz.repositorys.QuestionRepository;
import com.quiz.com.quiz.repositorys.TeamRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SocketIOService {

    private final SocketIOServer server;

    private final QuestionRepository questionRepository;

    private final TeamRepository teamRepository;

    @PostConstruct
    public void startServer() {
        server.start();
        System.out.println("SocketIO server started on port " + server.getConfiguration().getPort());

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
    }
}