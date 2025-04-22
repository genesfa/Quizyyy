package com.quiz.com.quiz;

import com.corundumstudio.socketio.SocketIOServer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

@Service
public class SocketIOService {

    private final SocketIOServer server;

    public SocketIOService(SocketIOServer server) {
        this.server = server;
    }

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
        });
    }

    @PreDestroy
    public void stopServer() {
        server.stop();
        System.out.println("SocketIO server stopped.");
    }
}