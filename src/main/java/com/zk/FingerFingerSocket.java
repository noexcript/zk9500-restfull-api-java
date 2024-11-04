package com.zk;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FingerFingerSocket {
    private static FingerFingerSocket instance;
    private SocketIOServer server;

    private Set<String> connectedClients = new HashSet<>();

    private FingerFingerSocket() {
        Configuration config = new Configuration();
        config.setHostname("127.0.0.1");
        config.setPort(8082);

        config.setOrigin("*");

        server = new SocketIOServer(config);

        server.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(SocketIOClient client) {
                connectedClients.add(client.getSessionId().toString());
                System.out.println("Cliente conectado: " + client.getSessionId());
                System.out.println("Total de clientes conectados: " + connectedClients.size());

                client.sendEvent("response", "Hello World");
                ;
            }
        });

        server.addDisconnectListener(new DisconnectListener() {
            @Override
            public void onDisconnect(SocketIOClient client) {
                connectedClients.remove(client.getSessionId().toString());
                System.out.println("Cliente desconectado: " + client.getSessionId());
                System.out.println("Total de clientes conectados: " + connectedClients.size());
            }
        });

        server.addEventListener("message", String.class, new DataListener<String>() {
            @Override
            public void onData(SocketIOClient client, String data, AckRequest ackSender) {
                System.out.println("Recebido do cliente: " + data);
                client.sendEvent("response", "Olá, cliente!");
            }
        });

        server.start();
        System.out.println("Servidor Socket.IO iniciado. Pressione Ctrl+C para parar.");
    }

    public static synchronized FingerFingerSocket getInstance() {
        if (instance == null) {
            instance = new FingerFingerSocket();
        }
        return instance;
    }

    public void sendDataToClient(SocketIOClient client, String eventName, Map<String, Object> data) {
        if (client != null) {
            client.sendEvent(eventName, data);
        } else {
            System.err.println("Cliente não encontrado.");
        }
    }

    public void broadcastData(String eventName, String data) {
        if (server != null) {
            server.getBroadcastOperations().sendEvent(eventName, data);
        } else {
            System.err.println("O servidor não está inicializado.");
        }
    }
}
