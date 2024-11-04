package com.zk;

import java.net.URI;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class App {
    public static final String BASE_URI = "http://localhost:8080/";

    public static HttpServer startServer() {
        final ResourceConfig rc = new ResourceConfig().packages("com.zk");
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    public static void main(String[] args) {
        final HttpServer server = startServer();
        FingerFingerSocket.getInstance();
        System.out.println("Servidor HTTP iniciado em: " + BASE_URI);
        System.out.println("Servidor Socket.IO iniciado em: http://localhost:8081");
        try {
            Thread.currentThread().join(); 
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            server.shutdownNow(); 
        }

    }
}
