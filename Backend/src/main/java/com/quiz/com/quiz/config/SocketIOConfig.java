package com.quiz.com.quiz.config;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.Transport; // Import the correct Transport class
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class SocketIOConfig {

    @Value("${socketio.host}")
    private String host;

    @Value("${socketio.port:9090}") // Use the socketio.port property
    private Integer port;

    @Bean
    public SocketIOServer socketIOServer() {
        Configuration config = new Configuration();
        config.setHostname(host != null ? host : "localhost");
        config.setPort(port); // Use the socketio.port property
        config.setTransports(Transport.WEBSOCKET, Transport.POLLING); // Use the correct Transport types
        return new SocketIOServer(config);
    }
}