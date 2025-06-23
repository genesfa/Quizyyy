package com.quiz.com.quiz;


import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.DataListener;
import com.quiz.com.quiz.dto.ClueData;
import com.quiz.com.quiz.entitys.AnswerVO;
import com.quiz.com.quiz.entitys.Question;
import com.quiz.com.quiz.entitys.QuestionType;
import com.quiz.com.quiz.entitys.Team;
import com.quiz.com.quiz.repositorys.QuestionRepository;
import com.quiz.com.quiz.repositorys.TeamRepository;
import com.quiz.com.quiz.repositorys.AnswerRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.Map;

import com.quiz.com.quiz.entitys.Answer;
import com.quiz.com.quiz.config.DataLoader;
import com.corundumstudio.socketio.Configuration;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SocketIOService {

    @Value("${socketio.port:9090}") // Use the socketio.port property
    private int socketIoPort;

    private final SocketIOServer server;

    private final DataLoader dataLoader; // Inject DataLoader

    private final TeamRepository teamRepository;

    private final AnswerRepository answerRepository;

    private final QuestionRepository questionRepository;

    private final AtomicInteger currentQuestionIndex = new AtomicInteger(0); // Track the current question index
    private List<Question> questions; // Cache the list of questions
    private Integer currentClueNumber = 0;
    private boolean isSolutionShown = false; // Add a flag to track if the solution is being shown

    @PostConstruct
    public void startServer() {
        dataLoader.initializeData(); // Ensure questions are loaded before starting the server

        server.start();
        log.info("Socket.IO server started on port {}", socketIoPort);



        // Ensure the root namespace is used


        // Load all questions from the database
        questions = questionRepository.findAll();
        System.out.println("WTTTTFFF");
        System.out.println("questions size: " + questions.size());
        System.out.println(questions);

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

            // Associate the session ID with the client
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
            // Broadcast the updated team list to the management room
            server.getRoomOperations("management").sendEvent("updateTeams", teamRepository.findAll());
            // Send the newly created team as the acknowledgment response
            ackSender.sendAckData(newTeam);
        });

        server.addEventListener("triggerConfetti", String.class, (client, data, ackSender) -> {
            System.out.println("triggerConfetti");
            server.getRoomOperations("management").sendEvent("triggerConfetti");
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
                // Broadcast the updated team list to the management room
                server.getRoomOperations("management").sendEvent("updateTeams", teamRepository.findAll());
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
            System.out.println("nextQuestion");
            System.out.println(currentQuestionIndex.get());
            if (currentQuestionIndex.incrementAndGet() >= questions.size()) {
                currentQuestionIndex.set(questions.size() - 1); // Prevent going out of bounds
            }
            isSolutionShown = false; // Reset the flag when moving to the next question
            broadcastQuestionUpdate();
        });

        server.addEventListener("lastQuestion", Void.class, (client, data, ackRequest) -> {
            System.out.println("lastQuestion");
            System.out.println(currentQuestionIndex.get());
            if (currentQuestionIndex.decrementAndGet() < 0) {
                currentQuestionIndex.set(0); // Prevent going below 0
            }
            isSolutionShown = false; // Reset the flag when moving to the previous question
            broadcastQuestionUpdate();
        });

        server.addEventListener("showSolution", Void.class, (client, data, ackRequest) -> {
            System.out.println("Show Solution event triggered");
            Question currentQuestion = questions.get(currentQuestionIndex.get());
            isSolutionShown = true; // Set the flag to true when the solution is shown
            server.getRoomOperations("management").sendEvent("showSolution", currentQuestion.getSolution());
        });

        server.addEventListener("showClue", Integer.class, (client, data, ackRequest) -> {
            int clueNumber = data;
            Question currentQuestion = questions.get(currentQuestionIndex.get());
            String clue = null;

            switch (clueNumber) {
                case 1:
                    clue = currentQuestion.getClue1();
                    currentClueNumber = 1;
                    break;
                case 2:
                    clue = currentQuestion.getClue2();
                    currentClueNumber = 2;
                    break;
                case 3:
                    clue = currentQuestion.getClue3();
                    currentClueNumber = 3;
                    break;
                case 4:
                    clue = currentQuestion.getClue4();
                    currentClueNumber = 4;
                    break;
            }

            if (clue != null) {
                server.getRoomOperations("management").sendEvent("showClue", new ClueData(clueNumber, clue));
            }
        });

        server.addEventListener("submitAnswer", AnswerVO.class, (client, answerData, ackSender) -> {
            System.out.println("Received answer submission: " + answerData.getAnswerText());
            if (isSolutionShown) { // Reject answers if the solution is being shown
                System.out.println("Answer submission rejected: Solution is currently being shown");
                ackSender.sendAckData("Error: Answers cannot be submitted while the solution is being shown. But nice try :)");
                return;
            }
            Team team = teamRepository.findById(answerData.getTeamId()).orElse(null);
            Question currentQuestion = questions.get(currentQuestionIndex.get()); // Fetch the current question directly

            if (team != null && currentQuestion != null) {
                // Check if an answer already exists for the current team and question
                Answer existingAnswer = answerRepository.findByTeamAndQuestion(team, currentQuestion);
                if (currentClueNumber == 0) {
                    System.out.println("Bruddi warte doch" + team.getName());
                    ackSender.sendAckData("Error: Chill and wait for Clues or you have no chance to win trust me");
                    return; // Do not save a new answer
                }
                if (existingAnswer != null) {
                    System.out.println("Team has already submitted an answer for this question: " + team.getName());
                    ackSender.sendAckData("Error: You can only answer a question once. I told you.");
                    return; // Do not save a new answer
                }

                Answer newAnswer = new Answer();
                newAnswer.setTeam(team);
                newAnswer.setQuestion(currentQuestion);
                newAnswer.setClueNumber(currentClueNumber);
                newAnswer.setAnswerText(answerData.getAnswerText());
                answerRepository.save(newAnswer); // Save the new answer
                System.out.println("Answer saved successfully for team: " + team.getName());

                ackSender.sendAckData("Answer processed successfully");

                // Broadcast updated answers for the current question to the management room
                List<Answer> answers = answerRepository.findByQuestion(currentQuestion);
                Map<Long, Map<String, Object>> teamAnswers = answers.stream()
                    .collect(Collectors.toMap(
                        a -> a.getTeam().getId(),
                        a -> Map.of("answerText", a.getAnswerText(), "clueNumber", a.getClueNumber())
                    ));
                server.getRoomOperations("management").sendEvent("updateAnswersForCurrentQuestion", teamAnswers);
            } else {
                System.out.println("Invalid team or question ID");
                ackSender.sendAckData("Error: Invalid team or question ID.");
            }
        });

        server.addEventListener("getCurrentQuestionId", Void.class, (client, data, ackSender) -> {
            Question currentQuestion = questions.get(currentQuestionIndex.get());
            if (currentQuestion != null) {
                ackSender.sendAckData(currentQuestion.getId()); // Send the current question ID
            } else {
                ackSender.sendAckData((Object) null); // No question available
            }
        });

        server.addEventListener("getAnswersForCurrentQuestion", Void.class, (client, data, ackSender) -> {
            Question currentQuestion = questions.get(currentQuestionIndex.get());
            if (currentQuestion != null) {
                List<Answer> answers = answerRepository.findByQuestion(currentQuestion);
                Map<Long, Map<String, Object>> teamAnswers = answers.stream()
                    .collect(Collectors.toMap(
                        a -> a.getTeam().getId(),
                        a -> Map.of("answerText", a.getAnswerText(), "clueNumber", a.getClueNumber())
                    ));
                ackSender.sendAckData(teamAnswers); // Send the answers mapped by team ID with clueNumber
            } else {
                ackSender.sendAckData(Collections.emptyMap()); // No question available
            }
        });

        server.addEventListener("toggleQRCode", Void.class, (client, data, ackRequest) -> {
            System.out.println("Toggle QR-Code event triggered");
            server.getBroadcastOperations().sendEvent("toggleQRCode");
        });

        server.addEventListener("getAllAnswers", Void.class, (client, data, ackSender) -> {
            List<Team> teams = teamRepository.findAll();
            Map<Long, Map<String, Object>> allAnswers = teams.stream().collect(Collectors.toMap(
                Team::getId,
                team -> {
                    List<Answer> answers = answerRepository.findByTeam(team);
                    List<Map<String, Object>> answerDetails = answers.stream()
                        .map(answer -> {
                            Map<String, Object> answerMap = Map.of(
                                "question", answer.getQuestion().getName(),
                                "answerText", answer.getAnswerText(),
                                "clueNumber", answer.getClueNumber()
                            );
                            return answerMap;
                        })
                        .collect(Collectors.toList());
                    return Map.of("answers", answerDetails);
                }
            ));
            ackSender.sendAckData(allAnswers);
        });

        server.addEventListener("joinRoom", String.class, (client, roomName, ackSender) -> {
            client.joinRoom(roomName);
            System.out.println("Client " + client.getSessionId() + " joined room: " + roomName);

            // Log all clients in the room
            server.getRoomOperations(roomName).getClients().forEach(c -> 
                System.out.println("Client in room " + roomName + ": " + c.getSessionId())
            );

            if ("management".equals(roomName)) {
                client.sendEvent("managementWelcome", "Welcome to the management room!");
            }
        });
    }

    @PreDestroy
    public void stopServer() {
        server.stop();
        log.info("Socket.IO server stopped");
    }

    private void broadcastQuestionUpdate() {
        System.out.println("Broadcasting question update");
        System.out.println("Current question: " + questions.get(currentQuestionIndex.get()));
        Question currentQuestion = questions.get(currentQuestionIndex.get());
        currentClueNumber = 0;
        // Send updates only to the management room
        server.getRoomOperations("management").sendEvent("updateQuestions", currentQuestion);
        server.getRoomOperations("management").sendEvent("currentQuestionId", currentQuestion.getId()); // Emit the current question ID
    }

    private String getCurrentQuestion() {
        if (questions.isEmpty()) {
            return "No questions available";
        }
        return questions.get(currentQuestionIndex.get()).getName(); // Return the name of the current question
    }

}