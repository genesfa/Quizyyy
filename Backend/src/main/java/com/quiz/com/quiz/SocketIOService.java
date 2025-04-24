package com.quiz.com.quiz;

import com.corundumstudio.socketio.SocketIOServer;
import com.quiz.com.quiz.entitys.Question;
import com.quiz.com.quiz.entitys.QuestionType;
import com.quiz.com.quiz.repositorys.QuestionRepository;
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


    @PostConstruct
    public void startServer() {
        server.start();
        System.out.println("SocketIO server started on port " + server.getConfiguration().getPort());

        server.addConnectListener(client -> {
            System.out.println("Client connected: " + client.getSessionId());
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

        server.addEventListener("triggerConfetti", String.class, (client, data, ackSender) -> {
            System.out.println("triggerConfetti");
            server.getBroadcastOperations().sendEvent("triggerConfetti");
            ackSender.sendAckData("Message received by server");
            questionRepository.findAll().forEach(question -> {System.out.println(question.getName());});
           });
    }

    @PreDestroy
    public void stopServer() {
        server.stop();
        System.out.println("SocketIO server stopped.");
    }
}